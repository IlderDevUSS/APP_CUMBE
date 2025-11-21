package com.example.app_cumbe;

import androidx.appcompat.app.AppCompatActivity;
import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import com.example.app_cumbe.api.ApiClient;
import com.example.app_cumbe.api.ApiService;
import com.example.app_cumbe.databinding.ActivityEditProfileBinding;
import com.example.app_cumbe.model.RequestUpdateProfile;
import com.example.app_cumbe.model.ResponseRegister;

import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.app_cumbe.LoginActivity.SP_NAME;

public class EditProfileActivity extends AppCompatActivity {

    private ActivityEditProfileBinding binding;
    private String fechaParaApi = ""; // Formato AAAA-MM-DD

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolbar();
        cargarDatosActuales();

        binding.etEditFechaNac.setOnClickListener(v -> mostrarDatePicker());
        binding.btnGuardarCambios.setOnClickListener(v -> validarYGuardar());
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbarEditProfile);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        binding.toolbarEditProfile.setNavigationOnClickListener(v -> finish());
    }

    private void cargarDatosActuales() {
        SharedPreferences prefs = getSharedPreferences(SP_NAME, MODE_PRIVATE);
        String fechaActual = prefs.getString("USER_BIRTH_DATE", "");

        // Mostramos la fecha actual si existe
        if (!fechaActual.isEmpty()) {
            binding.etEditFechaNac.setText(fechaActual); // Asumimos que ya viene formateada o la mostramos tal cual
            fechaParaApi = fechaActual; // Guardamos valor inicial
        }
    }

    private void mostrarDatePicker() {
        Calendar c = Calendar.getInstance();
        int anio = c.get(Calendar.YEAR); // Valores por defecto si no hay fecha previa
        int mes = c.get(Calendar.MONTH);
        int dia = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePicker = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            // Mostrar al usuario DD/MM/AAAA
            String fechaSeleccionada = String.format("%02d/%02d/%d", dayOfMonth, (month + 1), year);
            binding.etEditFechaNac.setText(fechaSeleccionada);

            // Guardar para la API AAAA-MM-DD
            fechaParaApi = String.format("%d-%02d-%02d", year, (month + 1), dayOfMonth);

        }, anio, mes, dia);
        datePicker.show();
    }

    private void validarYGuardar() {
        String pass = binding.etEditPassword.getText().toString().trim();
        String confirmPass = binding.etEditPasswordConfirm.getText().toString().trim();

        // Si el usuario quiere cambiar la contraseña, validamos
        if (!pass.isEmpty()) {
            if (pass.length() < 8) {
                binding.etEditPassword.setError("Mínimo 8 caracteres");
                return;
            }
            if (!pass.equals(confirmPass)) {
                binding.etEditPasswordConfirm.setError("Las contraseñas no coinciden");
                return;
            }
        } else {
            pass = null; // Si está vacío, enviamos null para que el backend sepa que no debe cambiarla
        }

        // Llamada a la API
        guardarEnBackend(fechaParaApi, pass);
    }

    private void guardarEnBackend(String fecha, String password) {
        binding.btnGuardarCambios.setEnabled(false);
        binding.btnGuardarCambios.setText("Guardando...");

        SharedPreferences prefs = getSharedPreferences(SP_NAME, MODE_PRIVATE);
        String token = prefs.getString("USER_TOKEN", "");

        ApiService apiService = ApiClient.getApiService();
        RequestUpdateProfile request = new RequestUpdateProfile(fecha, password);

        apiService.actualizarUsuario(token, request).enqueue(new Callback<ResponseRegister>() {
            @Override
            public void onResponse(Call<ResponseRegister> call, Response<ResponseRegister> response) {
                binding.btnGuardarCambios.setEnabled(true);
                binding.btnGuardarCambios.setText("Guardar Cambios");

                if (response.isSuccessful()) {
                    Toast.makeText(EditProfileActivity.this, "Datos actualizados correctamente", Toast.LENGTH_SHORT).show();


                    SharedPreferences.Editor editor = prefs.edit();
                    if (!fecha.isEmpty()) {
                        String fechaVisual = binding.etEditFechaNac.getText().toString();
                        editor.putString("USER_BIRTH_DATE", fechaVisual);
                    }
                    editor.apply();

                    finish();
                } else {
                    Toast.makeText(EditProfileActivity.this, "Error al actualizar", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseRegister> call, Throwable t) {
                binding.btnGuardarCambios.setEnabled(true);
                binding.btnGuardarCambios.setText("Guardar Cambios");
                Toast.makeText(EditProfileActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }
}