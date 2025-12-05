package com.example.app_cumbe;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Toast;

// Importamos el ViewBinding
import com.example.app_cumbe.databinding.ActivityRegisterBinding;

// Importamos todo lo de la API
import com.example.app_cumbe.api.ApiClient;
import com.example.app_cumbe.api.ApiService;
import com.example.app_cumbe.model.RequestRegister;
import com.example.app_cumbe.model.ResponseRegister;
import com.example.app_cumbe.model.db.AppDatabase;
import com.example.app_cumbe.model.db.NotificacionEntity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Configurar Listeners
        binding.btnVolverLogin.setOnClickListener(v -> finish());
        binding.etFechaNac.setOnClickListener(v -> mostrarDatePicker());
        binding.btnRegistrarse.setOnClickListener(v -> validarYRegistrar());
    }

    private void mostrarDatePicker() {
        Calendar c = Calendar.getInstance();
        int anio = c.get(Calendar.YEAR);
        int mes = c.get(Calendar.MONTH);
        int dia = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePicker = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            // Formato DD/MM/AAAA (como espera el validador)
            String fechaSeleccionada = String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, (month + 1), year);

            // PERO la BD y el backend esperan AAAA-MM-DD
            // Así que guardamos el formato correcto en el 'tag' del EditText
            String fechaParaApi = String.format(Locale.getDefault(), "%d-%02d-%02d", year, (month + 1), dayOfMonth);
            binding.etFechaNac.setTag(fechaParaApi); // Guardamos "AAAA-MM-DD"
            binding.etFechaNac.setText(fechaSeleccionada); // Mostramos "DD/MM/AAAA"

        }, anio, mes, dia);

        datePicker.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePicker.show();
    }

    /**
     * 1. Valida campos locales
     * 2. Si son válidos, llama a la API de registro
     */
    private void validarYRegistrar() {

        // Obtenemos todos los textos
        String nombres = binding.etNombres.getText().toString().trim();
        String apellidos = binding.etApellidos.getText().toString().trim();
        String dni = binding.etDni.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String telefono = binding.etTelefono.getText().toString().trim();
        String fechaNacApi = (binding.etFechaNac.getTag() != null) ? binding.etFechaNac.getTag().toString() : "";
        String password = binding.etPasswordReg.getText().toString().trim();
        String confirmPassword = binding.etPasswordConfirm.getText().toString().trim();

        // --- VALIDACIONES LOCALES (simplificado para brevedad) ---
        if (nombres.isEmpty() || apellidos.isEmpty() || dni.isEmpty() || email.isEmpty() || telefono.isEmpty() || fechaNacApi.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Todos los campos son requeridos", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            return;
        }


        binding.btnRegistrarse.setEnabled(false);
        binding.btnRegistrarse.setText("Registrando...");

        RequestRegister request = new RequestRegister(dni, nombres, apellidos, email, telefono, fechaNacApi, password);
        ApiService apiService = ApiClient.getApiService();
        Call<ResponseRegister> call = apiService.registrarUsuario(request);

        call.enqueue(new Callback<ResponseRegister>() {
            @Override
            public void onResponse(Call<ResponseRegister> call, Response<ResponseRegister> response) {
                binding.btnRegistrarse.setEnabled(true);
                binding.btnRegistrarse.setText("Registrarme");

                if (response.isSuccessful() && response.body() != null) {
                    String mensaje = response.body().getMensaje();
                    Toast.makeText(RegisterActivity.this, mensaje, Toast.LENGTH_LONG).show();

                    // --- ¡AQUÍ VA LA NUEVA LÓGICA! ---
                    crearNotificacionBienvenida(nombres.split(" ")[0]);

                    finish();

                } else {
                    String errorMsg = "Error al registrar. Inténtelo de nuevo.";
                    if (response.code() == 409) {
                        errorMsg = "El DNI o Email ya están registrados";
                    }
                    Toast.makeText(RegisterActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseRegister> call, Throwable t) {
                binding.btnRegistrarse.setEnabled(true);
                binding.btnRegistrarse.setText("Registrarme");
                Toast.makeText(RegisterActivity.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void crearNotificacionBienvenida(String nombreUsuario) {
        String titulo = "¡Bienvenido/a a El Cumbe!";
        String contenido = "Hola " + nombreUsuario + ", gracias por unirte. ¡Explora nuestros destinos y servicios!";
        String fecha = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());

        // 1. Guardar en la base de datos local
        String userId = binding.etDni.getText().toString().trim();
        AppDatabase db = AppDatabase.getDatabase(this);
        NotificacionEntity notificacionLocal = new NotificacionEntity(titulo, contenido, "SISTEMA", fecha, userId);
        // No tiene ID de referencia, así que no se establece
        db.notificacionDao().insertar(notificacionLocal);

        // 2. Mostrar notificación PUSH en la barra de estado
        String channelId = "canal_sistema";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Sistema", NotificationManager.IMPORTANCE_DEFAULT);
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }

        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_badge) // Asegúrate que este icono exista
                .setContentTitle(titulo)
                .setContentText(contenido)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            NotificationManagerCompat.from(this).notify(1, builder.build()); // Usamos un ID fijo (1) para esta notificación
        }
    }
}