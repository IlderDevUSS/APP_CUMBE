package com.example.app_cumbe;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.app_cumbe.api.ApiClient;
import com.example.app_cumbe.databinding.ActivityConfirmarCompraBinding;
import com.example.app_cumbe.model.RequestCompra;
import com.example.app_cumbe.model.RequestPreferencia;
import com.example.app_cumbe.model.ResponseCompra;
import com.example.app_cumbe.model.ResponsePreferencia;
import com.example.app_cumbe.model.db.AppDatabase;
import com.example.app_cumbe.model.db.NotificacionEntity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.app_cumbe.LoginActivity.SP_NAME;

public class ConfirmarCompraActivity extends AppCompatActivity {

    private ActivityConfirmarCompraBinding binding;

    // Datos del viaje
    private int horarioId, asiento, piso;
    private double precio = 0.0;
    private String rutaStr = "";
    private String fechaStr = "";
    private String servicioBus = "";

    // Datos del pasajero
    private String misNombres, misApellidos, miDni, miCelular;

    // --- PERMISOS NOTIFICACIÓN ---
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (!isGranted) {
                    Toast.makeText(this, "No se podrán mostrar notificaciones.", Toast.LENGTH_LONG).show();
                }
            });

    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityConfirmarCompraBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent i = getIntent();
        servicioBus = i.getStringExtra("SERVICIO");
        if (servicioBus == null) servicioBus = "Estándar";

        recibirDatosIntent();
        cargarDatosUsuarioSesion();
        setupUI();
        setupListeners();
        askNotificationPermission();

        // Verificar si la actividad se abrió por un Deep Link (Regreso de Pago)
        checkPaymentResult(getIntent());
    }

    // --- NUEVO: Capturar el regreso del Navegador ---
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent); // Actualizar el intent actual
        checkPaymentResult(intent);
    }

    // --- NUEVO: Analizar la URL de respuesta ---
    private void checkPaymentResult(Intent intent) {
        if (intent == null || intent.getData() == null) return;

        Uri data = intent.getData();

        // Verificamos si es el esquema que configuramos: appcumbe://checkout
        if ("appcumbe".equals(data.getScheme()) && "checkout".equals(data.getHost())) {

            String path = data.getPath(); // "/success", "/failure" o "/pending"
            String status = data.getQueryParameter("collection_status");
            String paymentId = data.getQueryParameter("collection_id");

            if ("/success".equals(path) && "approved".equals(status)) {
                // ¡PAGO APROBADO! -> Procedemos a guardar en BD
                procesarCompraFinal(paymentId, status, "MERCADO_PAGO_WEB");
            } else if ("/failure".equals(path)) {
                mostrarError("El pago fue rechazado o cancelado.");
            } else if ("/pending".equals(path)) {
                mostrarError("El pago está pendiente de confirmación.");
            }
        }
    }

    private void recibirDatosIntent() {
        Intent i = getIntent();
        horarioId = i.getIntExtra("HORARIO_ID", 0);
        asiento = i.getIntExtra("ASIENTO", 0);
        piso = i.getIntExtra("PISO", 1);
        if (i.hasExtra("PRECIO")) precio = i.getDoubleExtra("PRECIO", 0.0);
        rutaStr = i.getStringExtra("RUTA");
        if (rutaStr == null) rutaStr = "";
        fechaStr = i.getStringExtra("FECHA");
        if (fechaStr == null) fechaStr = "";
    }

    private void cargarDatosUsuarioSesion() {
        SharedPreferences prefs = getSharedPreferences(SP_NAME, MODE_PRIVATE);
        String nombreCompleto = prefs.getString("USER_NAME", "");
        miDni = prefs.getString("USER_DNI", "");
        miCelular = prefs.getString("USER_PHONE", "");

        String[] partes = nombreCompleto.split(" ");
        if (partes.length > 2) {
            misNombres = partes[0] + " " + partes[1];
            misApellidos = "";
            for (int k = 2; k < partes.length; k++) misApellidos += partes[k] + " ";
        } else if (partes.length == 2) {
            misNombres = partes[0];
            misApellidos = partes[1];
        } else {
            misNombres = nombreCompleto;
            misApellidos = "";
        }
        misApellidos = misApellidos.trim();
    }

    private void setupUI() {
        binding.tvResumenAsiento.setText("#" + asiento);
        binding.tvResumenPiso.setText(String.valueOf(piso));
        binding.tvTotalPagar.setText("S/ " + String.format("%.2f", precio));
        binding.tvResumenRuta.setText(rutaStr.isEmpty() ? "Ruta Seleccionada" : rutaStr);
        binding.tvResumenFecha.setText(fechaStr.isEmpty() ? "Fecha del Viaje" : fechaStr);
        llenarFormularioConMisDatos();
    }

    private void setupListeners() {
        binding.cbSoyPasajero.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                llenarFormularioConMisDatos();
                habilitarEdicion(false);
            } else {
                limpiarFormulario();
                habilitarEdicion(true);
            }
        });

        binding.btnPagarFinal.setOnClickListener(v -> validarYPagar());
    }

    private void llenarFormularioConMisDatos() {
        binding.etPasajeroDni.setText(miDni);
        binding.etPasajeroNombres.setText(misNombres);
        binding.etPasajeroApellidos.setText(misApellidos);
        binding.etPasajeroCelular.setText(miCelular);
    }

    private void limpiarFormulario() {
        binding.etPasajeroDni.setText("");
        binding.etPasajeroNombres.setText("");
        binding.etPasajeroApellidos.setText("");
        binding.etPasajeroCelular.setText("");
        binding.etPasajeroDni.requestFocus();
    }

    private void habilitarEdicion(boolean habilitar) {
        binding.etPasajeroDni.setEnabled(habilitar);
        binding.etPasajeroNombres.setEnabled(habilitar);
        binding.etPasajeroApellidos.setEnabled(habilitar);
        binding.etPasajeroCelular.setEnabled(habilitar);
    }

    private void validarYPagar() {
        // 1. Obtener datos del formulario
        String dni = binding.etPasajeroDni.getText().toString().trim();
        String nombres = binding.etPasajeroNombres.getText().toString().trim();
        String apellidos = binding.etPasajeroApellidos.getText().toString().trim();
        String celular = binding.etPasajeroCelular.getText().toString().trim();

        // 2. Validaciones UI
        if (dni.isEmpty() || dni.length() != 8) {
            binding.etPasajeroDni.setError("DNI inválido (8 dígitos)");
            return;
        }
        if (nombres.isEmpty()) {
            binding.etPasajeroNombres.setError("Ingrese nombres");
            return;
        }
        if (apellidos.isEmpty()) {
            binding.etPasajeroApellidos.setError("Ingrese apellidos");
            return;
        }

        // Guardamos los datos temporalmente
        this.misNombres = nombres;
        this.misApellidos = apellidos;
        this.miDni = dni;
        this.miCelular = celular;

        // 3. Decidir flujo según método de pago
        int selectedId = binding.rgMetodoPago.getCheckedRadioButtonId();

        if (selectedId == R.id.rbTarjeta || selectedId == R.id.rbYape) {
            // CASO A: Pago ONLINE (Web Checkout)
            iniciarFlujoMercadoPago();
        } else {
            // CASO B: Pago EFECTIVO (Presencial)
            procesarCompraFinal(null, null, "EFECTIVO");
        }
    }

    // --- MODIFICADO: Iniciar flujo WEB (Chrome) ---
    private void iniciarFlujoMercadoPago() {
        binding.btnPagarFinal.setEnabled(false);
        binding.btnPagarFinal.setText("Cargando Pasarela...");

        RequestPreferencia req = new RequestPreferencia("Pasaje a " + rutaStr, precio);

        SharedPreferences prefs = getSharedPreferences(SP_NAME, MODE_PRIVATE);
        String token = prefs.getString("USER_TOKEN", "");

        ApiClient.getApiService().crearPreferencia(token, req).enqueue(new Callback<ResponsePreferencia>() {
            @Override
            public void onResponse(Call<ResponsePreferencia> call, Response<ResponsePreferencia> response) {
                // Reactivamos el botón por si el usuario cancela/regresa
                binding.btnPagarFinal.setEnabled(true);
                binding.btnPagarFinal.setText("Confirmar y Pagar");

                if (response.isSuccessful() && response.body() != null) {
                    String urlPago = response.body().getUrlPago();

                    if (urlPago != null && !urlPago.isEmpty()) {
                        // ABRIR NAVEGADOR
                        try {
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlPago));
                            startActivity(browserIntent);
                            Toast.makeText(ConfirmarCompraActivity.this, "Redirigiendo a Mercado Pago...", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            mostrarError("Error al abrir navegador: " + e.getMessage());
                        }
                    } else {
                        mostrarError("Error: El servidor no devolvió el link de pago.");
                    }
                } else {
                    mostrarError("Error del servidor: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ResponsePreferencia> call, Throwable t) {
                binding.btnPagarFinal.setEnabled(true);
                binding.btnPagarFinal.setText("Confirmar y Pagar");
                mostrarError("Error de conexión: " + t.getMessage());
            }
        });
    }

    // --- Guardar Compra en BD (Backend) ---
    private void procesarCompraFinal(String idExterno, String detalleEstado, String metodoPago) {
        binding.btnPagarFinal.setEnabled(false);
        binding.btnPagarFinal.setText("Generando Ticket...");

        SharedPreferences prefs = getSharedPreferences(SP_NAME, MODE_PRIVATE);
        String token = prefs.getString("USER_TOKEN", "");

        RequestCompra request = new RequestCompra(
                horarioId, asiento, piso, precio,
                misNombres, misApellidos, miDni, miCelular, metodoPago
        );

        if (idExterno != null) {
            request.setIdTransaccionExterna(idExterno);
            request.setDetalleEstado(detalleEstado);
        }

        ApiClient.getApiService().comprarPasaje(token, request).enqueue(new Callback<ResponseCompra>() {
            @Override
            public void onResponse(Call<ResponseCompra> call, Response<ResponseCompra> response) {
                if (response.isSuccessful() && response.body() != null) {
                    finalizarExito(response.body());
                } else {
                    mostrarError("Error al guardar ticket: " + response.message());
                }
            }
            @Override
            public void onFailure(Call<ResponseCompra> call, Throwable t) {
                mostrarError("Error al conectar para guardar ticket");
            }
        });
    }

    private void finalizarExito(ResponseCompra body) {
        // 1. Notificación Local
        String notifTitle = "Viaje Confirmado";
        String notifMessage = "Asiento " + asiento + " a " + rutaStr;

        AppDatabase db = AppDatabase.getDatabase(this);
        String userId = getSharedPreferences(SP_NAME, MODE_PRIVATE).getString("USER_DNI", null);

        new Thread(() -> {
            NotificacionEntity notif = new NotificacionEntity(
                    notifTitle, notifMessage, "COMPRA",
                    new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date()),
                    userId
            );
            notif.referenciaId = body.getPasajeId();
            notif.origenReferencia = "TICKET";
            db.notificacionDao().insertar(notif);
        }).start();

        // 2. Notificación Push
        mostrarNotificacionSistema(body.getPasajeId(), notifTitle, notifMessage);

        // 3. Abrir Ticket y cerrar esta pantalla
        Intent intent = new Intent(ConfirmarCompraActivity.this, TicketActivity.class);
        intent.putExtra("TICKET_VISUAL_ID", body.getIdTicketVisual());
        intent.putExtra("PASAJE_ID", body.getPasajeId());
        intent.putExtra("RUTA", rutaStr);
        intent.putExtra("FECHA", fechaStr);
        intent.putExtra("ASIENTO", asiento);
        intent.putExtra("PRECIO", precio);
        intent.putExtra("PASAJERO_NOMBRE", misNombres + " " + misApellidos);
        intent.putExtra("PASAJERO_DNI", miDni);
        intent.putExtra("SERVICIO", servicioBus);

        startActivity(intent);
        finish();
    }

    private void mostrarError(String msg) {
        binding.btnPagarFinal.setEnabled(true);
        binding.btnPagarFinal.setText("Confirmar y Pagar");
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    private void mostrarNotificacionSistema(int pasajeId, String titulo, String mensaje) {
        Context context = getApplicationContext();
        String channelId = "canal_compras";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Compras", NotificationManager.IMPORTANCE_DEFAULT);
            context.getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }

        Intent intent = new Intent(context, TicketActivity.class);
        intent.putExtra("PASAJE_ID", pasajeId);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, pasajeId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_local_activity)
                .setContentTitle(titulo)
                .setContentText(mensaje)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            NotificationManagerCompat.from(context).notify(pasajeId, builder.build());
        }
    }
}