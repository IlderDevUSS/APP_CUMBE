package com.example.app_cumbe;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.app_cumbe.api.ApiClient;
import com.example.app_cumbe.api.ApiService;
import com.example.app_cumbe.model.Encomienda;
import com.example.app_cumbe.model.Ticket;
import com.example.app_cumbe.model.db.AppDatabase;
import com.example.app_cumbe.model.db.NotificacionEntity;

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
        Log.d(TAG, "Iniciando chequeo de notificaciones...");
        checkUpcomingTrips(); // Revisar Pasajes
        checkEncomiendas();   // Revisar Encomiendas
        return Result.success();
    }

    // --- LÓGICA DE PASAJES ---
    private void checkUpcomingTrips() {
        Context context = getApplicationContext();
        SharedPreferences prefs = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        String token = prefs.getString("USER_TOKEN", null);
        String userId = prefs.getString("USER_DNI", null);

        if (token == null || userId == null) return;

        ApiService api = ApiClient.getApiService();
        try {
            Call<List<Ticket>> call = api.getHistorialPasajes(token);
            Response<List<Ticket>> response = call.execute();

            if (response.isSuccessful() && response.body() != null) {
                List<Ticket> tickets = response.body();
                AppDatabase db = AppDatabase.getDatabase(context);

                for (Ticket t : tickets) {
                    long horasRestantes = calcularHorasRestantes(t.getFechaSalida(), t.getHoraSalida());

                    // CASO 1: Faltan 24 horas o menos
                    if (horasRestantes > 1 && horasRestantes <= 24) {
                        if (!yaFueNotificado(db, t.getPasajeId(), "HORARIO", "%mañana%", userId)) {
                            // CORRECCIÓN: Usamos el ID del pasaje, no el objeto Ticket
                            crearNotificacion(db, t.getPasajeId(), "VIAJE", "Tu viaje es mañana",
                                    "Recuerda que tu viaje a " + t.getDestino() + " sale mañana a las " + t.getHoraSalida(), userId);
                        }
                    }

                    // CASO 2: Falta 1 hora o menos
                    else if (horasRestantes >= 0 && horasRestantes <= 1) {
                        if (!yaFueNotificado(db, t.getPasajeId(), "HORARIO", "%pronto%", userId)) {
                            // CORRECCIÓN: Usamos el ID del pasaje, no el objeto Ticket
                            crearNotificacion(db, t.getPasajeId(), "VIAJE", "¡Tu viaje es pronto!",
                                    "Falta menos de 1 hora para tu salida a " + t.getDestino() + ". ¡No llegues tarde!", userId);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error verificando viajes", e);
        }
    }

    // --- LÓGICA DE ENCOMIENDAS ---
    private void checkEncomiendas() {
        Context context = getApplicationContext();
        SharedPreferences prefs = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        String token = prefs.getString("USER_TOKEN", null);
        String miDni = prefs.getString("USER_DNI", "");

        if (token == null) return;

        try {
            // Llamada síncrona al API de encomiendas
            Response<List<Encomienda>> response = ApiClient.getApiService().getEncomiendas(token).execute();

            if (response.isSuccessful() && response.body() != null) {
                AppDatabase db = AppDatabase.getDatabase(context);

                for (Encomienda e : response.body()) {
                    String estado = e.getEstado();

                    // CASO A: Encomienda EN CAMINO
                    if ("EN_CAMINO".equalsIgnoreCase(estado)) {
                        if (!yaFueNotificado(db, e.getId(), "ENCOMIENDA", "%camino%", miDni)) {
                            String msg = "Tu encomienda con código " + e.getTrackingCode() + " está en ruta hacia " + e.getNombreDestino();
                            crearNotificacion(db, e.getId(), "ENCOMIENDA", "📦 Encomienda en Camino", msg, miDni);
                        }
                    }

                    // CASO B: Encomienda EN DESTINO
                    else if ("EN_DESTINO".equalsIgnoreCase(estado)) {
                        if (!yaFueNotificado(db, e.getId(), "ENCOMIENDA", "%destino%", miDni)) {
                            String msg;
                            if (e.getDestinatarioDni() != null && e.getDestinatarioDni().equals(miDni)) {
                                msg = "¡Llegó! Tu encomienda " + e.getTrackingCode() + " está lista para recoger en " + e.getNombreDestino();
                            } else {
                                msg = "La encomienda que enviaste (" + e.getTrackingCode() + ") ya llegó a su destino.";
                            }
                            crearNotificacion(db, e.getId(), "ENCOMIENDA", "✅ Encomienda en Destino", msg, miDni);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            Log.e(TAG, "Error encomiendas", ex);
        }
    }

    // --- MÉTODO UNIFICADO PARA CREAR Y MOSTRAR ---
    // Este es el método que causaba conflicto. Ahora está estandarizado.
    private void crearNotificacion(AppDatabase db, int refId, String tipoRef, String titulo, String mensaje, String userId) {
        // 1. Insertar en Room
        NotificacionEntity notif = new NotificacionEntity(titulo, mensaje, tipoRef, getCurrentDateStr(), userId);
        notif.referenciaId = refId;
        notif.origenReferencia = tipoRef.equals("VIAJE") ? "HORARIO" : "ENCOMIENDA";
        db.notificacionDao().insertar(notif);

        // 2. Mostrar en Barra de Estado (Push)
        mostrarNotificacionSistema(refId, tipoRef, titulo, mensaje);
    }

    private void mostrarNotificacionSistema(int refId, String tipoRef, String titulo, String mensaje) {
        Context context = getApplicationContext();
        String channelId = "canal_viajes";

        // Crear canal si es necesario (Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Avisos Cumbe", NotificationManager.IMPORTANCE_HIGH);
            context.getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }

        // Preparar el Intent para abrir la pantalla correcta al hacer clic
        Intent intent;
        int uniqueRequestCode;

        if ("VIAJE".equals(tipoRef)) {
            intent = new Intent(context, TicketActivity.class);
            intent.putExtra("PASAJE_ID", refId); // Enviamos el ID para que TicketActivity cargue los datos
            uniqueRequestCode = refId + 10000;   // Evitar colisión de IDs
        } else {
            intent = new Intent(context, TrackingActivity.class);
            // intent.putExtra("ENCOMIENDA_ID", refId); // Si tu TrackingActivity soporta abrir detalle
            uniqueRequestCode = refId + 20000;
        }

        // Banderas importantes para que la actividad se abra bien
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                uniqueRequestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_notifications) // Asegúrate de tener este icono
                .setContentTitle(titulo)
                .setContentText(mensaje)
                .setContentIntent(pendingIntent) // Conectamos el click
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        // Validación de Permisos (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return; // Si no hay permiso, no mostramos nada
            }
        }

        NotificationManagerCompat.from(context).notify(uniqueRequestCode, builder.build());
    }

    // --- UTILITARIOS ---

    private long calcularHorasRestantes(String fechaStr, String horaStr) {
        // Ajusta el formato si tu fecha viene diferente (ej: yyyy-MM-dd)
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        try {
            // Concatenamos fecha y hora para parsear
            // Si horaStr solo trae "10:30", agregamos ":00" si es necesario
            String horaFull = horaStr.length() == 5 ? horaStr + ":00" : horaStr;
            Date fechaViaje = sdf.parse(fechaStr + " " + horaFull);

            if (fechaViaje == null) return -1;

            long diff = fechaViaje.getTime() - new Date().getTime();
            return TimeUnit.MILLISECONDS.toHours(diff);
        } catch (Exception e) {
            return -1;
        }
    }

        private boolean yaFueNotificado(AppDatabase db, int refId, String origenRef, String palabraClave, String userId) {
        // Usamos el DAO actualizado que creamos antes
        return db.notificacionDao().existeNotificacionEspecifica(refId, origenRef, palabraClave, userId) > 0;
    }

    private String getCurrentDateStr() {
        return new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
    }
}