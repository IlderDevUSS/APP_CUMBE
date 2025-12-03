package com.example.app_cumbe;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.app_cumbe.api.ApiClient;
import com.example.app_cumbe.api.ApiService;
import com.example.app_cumbe.model.Ticket;
import com.example.app_cumbe.model.db.AppDatabase;
import com.example.app_cumbe.model.db.NotificacionEntity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Response;

import static com.example.app_cumbe.LoginActivity.SP_NAME;

public class NotificationsWorker extends Worker {

    private static final String TAG = "NotifWorker";

    public NotificationsWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Iniciando chequeo de viajes próximos...");
        checkUpcomingTrips();
        return Result.success();
    }

    private void checkUpcomingTrips() {
        Context context = getApplicationContext();
        SharedPreferences prefs = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        String token = prefs.getString("USER_TOKEN", null);

        if (token == null) return; // No hay sesión

        ApiService api = ApiClient.getApiService();
        try {
            // Llamada SÍNCRONA porque estamos en un hilo de fondo (Worker)
            Call<List<Ticket>> call = api.getHistorialPasajes(token);
            Response<List<Ticket>> response = call.execute();

            if (response.isSuccessful() && response.body() != null) {
                List<Ticket> tickets = response.body();
                AppDatabase db = AppDatabase.getDatabase(context);

                for (Ticket t : tickets) {
                    long horasRestantes = calcularHorasRestantes(t.getFechaSalida(), t.getHoraSalida());

                    // CASO 1: Faltan 24 horas o menos (pero más de 1)
                    if (horasRestantes > 1 && horasRestantes <= 24) {
                        if (!yaFueNotificado(db, t.getPasajeId(), "%mañana%")) { // Buscamos si ya dijimos "mañana"
                            crearNotificacion(db, t, "Tu viaje es mañana",
                                    "Recuerda que tu viaje a " + t.getDestino() + " sale mañana a las " + t.getHoraSalida());
                        }
                    }

                    // CASO 2: Falta 1 hora o menos (Urgente)
                    else if (horasRestantes >= 0 && horasRestantes <= 1) {
                        if (!yaFueNotificado(db, t.getPasajeId(), "%pronto%")) { // Buscamos si ya dijimos "pronto"
                            crearNotificacion(db, t, "¡Tu viaje es pronto!",
                                    "Falta menos de 1 hora para tu salida a " + t.getDestino() + ". ¡No llegues tarde!");
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error verificando viajes", e);
        }
    }

    private long calcularHorasRestantes(String fechaStr, String horaStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        try {
            Date fechaViaje = sdf.parse(fechaStr + " " + horaStr);
            long diff = fechaViaje.getTime() - new Date().getTime();
            return TimeUnit.MILLISECONDS.toHours(diff);
        } catch (Exception e) { return -1; }
    }

    private boolean yaFueNotificado(AppDatabase db, int pasajeId, String palabraClave) {
        // Usamos el nuevo método del DAO para buscar por título (así distinguimos el de 24h del de 1h)
        return db.notificacionDao().existeNotificacionEspecifica(pasajeId, "HORARIO", palabraClave) > 0;
    }

    private void crearNotificacion(AppDatabase db, Ticket t, String titulo, String mensaje) {
        NotificacionEntity notif = new NotificacionEntity(titulo, mensaje, "VIAJE", getCurrentDateStr());
        notif.referenciaId = t.getPasajeId();
        notif.origenReferencia = "HORARIO";
        db.notificacionDao().insertar(notif);

        mostrarNotificacionSistema(titulo, mensaje); // Tu método de notificación push
    }

    private String getCurrentDateStr() {
        return new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
    }

    private void mostrarNotificacionSistema(String titulo, String contenido) {
        Context context = getApplicationContext();
        String channelId = "canal_viajes";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Viajes Próximos", NotificationManager.IMPORTANCE_HIGH);
            context.getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_directions_bus) // Asegúrate de tener este icono
                .setContentTitle(titulo)
                .setContentText(contenido)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat.from(context).notify((int) System.currentTimeMillis(), builder.build());
    }
}