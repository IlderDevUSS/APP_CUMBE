package com.example.app_cumbe;

import androidx.appcompat.app.AppCompatActivity;
import android.app.DatePickerDialog;
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

import java.util.Calendar;

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
            String fechaSeleccionada = String.format("%02d/%02d/%d", dayOfMonth, (month + 1), year);

            // PERO la BD y el backend esperan AAAA-MM-DD
            // Así que guardamos el formato correcto en el 'tag' del EditText
            String fechaParaApi = String.format("%d-%02d-%02d", year, (month + 1), dayOfMonth);
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
        // El campo 'usuario' ya no existe
        String email = binding.etEmail.getText().toString().trim();
        String telefono = binding.etTelefono.getText().toString().trim();

        // Obtenemos la fecha para mostrar (DD/MM/AAAA)
        String fechaNacDisplay = binding.etFechaNac.getText().toString().trim();
        // Obtenemos la fecha para la API (AAAA-MM-DD) del tag
        String fechaNacApi = (binding.etFechaNac.getTag() != null) ? binding.etFechaNac.getTag().toString() : "";

        String password = binding.etPasswordReg.getText().toString().trim();
        String confirmPassword = binding.etPasswordConfirm.getText().toString().trim();

        // --- VALIDACIONES LOCALES ---
        if (nombres.isEmpty() || apellidos.isEmpty() || dni.isEmpty() || email.isEmpty() || telefono.isEmpty() || fechaNacDisplay.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Todos los campos son requeridos", Toast.LENGTH_SHORT).show();
            return;
        }
        if (dni.length() != 8) {
            binding.etDni.setError("El DNI debe tener 8 dígitos");
            binding.etDni.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.setError("Email no válido");
            binding.etEmail.requestFocus();
            return;
        }
        if (telefono.length() != 9) {
            binding.etTelefono.setError("El teléfono debe tener 9 dígitos");
            binding.etTelefono.requestFocus();
            return;
        }
        if (fechaNacApi.isEmpty()) {
            Toast.makeText(this, "Selecciona tu fecha de nacimiento", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() < 8) {
            binding.etPasswordReg.setError("La contraseña debe tener al menos 8 caracteres");
            binding.etPasswordReg.requestFocus();
            return;
        }
        if (!password.matches(".*[A-Z].*") || !password.matches(".*[0-9].*")) {
            binding.etPasswordReg.setError("Debe contener al menos una mayúscula y un número");
            binding.etPasswordReg.requestFocus();
            return;
        }
        if (!password.equals(confirmPassword)) {
            binding.etPasswordConfirm.setError("Las contraseñas no coinciden");
            binding.etPasswordConfirm.requestFocus();
            return;
        }

        // --- VALIDACIÓN LOCAL EXITOSA ---
        // Deshabilitamos el botón para evitar clics múltiples
        binding.btnRegistrarse.setEnabled(false);
        binding.btnRegistrarse.setText("Registrando...");

        // 1. Crear el objeto Request
        RequestRegister request = new RequestRegister(dni, nombres, apellidos, email, telefono, fechaNacApi, password);

        // 2. Obtener el servicio y llamar a la API
        ApiService apiService = ApiClient.getApiService();
        Call<ResponseRegister> call = apiService.registrarUsuario(request);

        call.enqueue(new Callback<ResponseRegister>() {
            @Override
            public void onResponse(Call<ResponseRegister> call, Response<ResponseRegister> response) {
                // Volvemos a habilitar el botón
                binding.btnRegistrarse.setEnabled(true);
                binding.btnRegistrarse.setText("Registrarme");

                if (response.isSuccessful() && response.body() != null) {
                    // --- ÉXITO (Código 201 Created) ---
                    String mensaje = response.body().getMensaje();
                    Toast.makeText(RegisterActivity.this, mensaje, Toast.LENGTH_LONG).show();

                    // Cerramos la pantalla de Registro y volvemos al Login
                    finish();

                } else {
                    // --- ERROR (Ej: 409 Conflict - DNI/Email ya existe) ---
                    String errorMsg = "Error al registrar. Inténtelo de nuevo.";
                    if (response.code() == 409) {
                        // Este es el error "Duplicate entry" que definimos en el backend
                        errorMsg = "El DNI o Email ya están registrados";
                    }

                    Toast.makeText(RegisterActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    Log.e("API_ERROR", "Code: " + response.code() + " - " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ResponseRegister> call, Throwable t) {
                // --- FALLO DE RED ---
                binding.btnRegistrarse.setEnabled(true);
                binding.btnRegistrarse.setText("Registrarme");
                Toast.makeText(RegisterActivity.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("API_FAILURE", t.getMessage());
            }
        });
    }
}