package com.example.app_cumbe;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.app_cumbe.api.ApiClient;
import com.example.app_cumbe.api.ApiService;
import com.example.app_cumbe.databinding.ActivityLoginBinding;
import com.example.app_cumbe.model.RequestLogin;
import com.example.app_cumbe.model.ResponseLogin;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    public static final String SP_NAME = "SP_CUMBE_APP";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ApiClient.init(this);

        // Antes de mostrar la vista revisamos si ya hay un token guardado.
        if (verificarSesion()) {

            navegarAlHome(null);
            finish();
            return;
        }


        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnIngresar.setOnClickListener(v -> {
            validarEIniciarLogin();
        });

        binding.tvIrARegistro.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        if (binding.btnVerServicios != null) {
            binding.btnVerServicios.setOnClickListener(v -> {
                Intent intent = new Intent(LoginActivity.this, ServiciosActivity.class);
                startActivity(intent);
            });
        }
    }

    /**
     * --- NUEVA FUNCIÓN ---
     * Revisa SharedPreferences buscando un token.
     * @return true si existe un token, false si no.
     */
    private boolean verificarSesion() {
        SharedPreferences sharedPreferences = getSharedPreferences(SP_NAME, MODE_PRIVATE);
        String token = sharedPreferences.getString("USER_TOKEN", null);
        return token != null && !token.isEmpty();
    }

    private void validarEIniciarLogin() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        if (email.isEmpty()) {
            binding.etEmail.setError("Este campo es requerido");
            binding.etEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.setError("Ingresa un email válido");
            binding.etEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            binding.etPassword.setError("Este campo es requerido");
            binding.etPassword.requestFocus();
            return;
        }

        binding.btnIngresar.setEnabled(false);
        binding.btnIngresar.setText("Validando...");

        llamarApiLogin(email, password);
    }

    private void llamarApiLogin(String email, String password) {
        ApiService apiService = ApiClient.getApiService();
        RequestLogin requestLogin = new RequestLogin(email, password);

        Call<ResponseLogin> call = apiService.login(requestLogin);

        call.enqueue(new Callback<ResponseLogin>() {
            @Override
            public void onResponse(Call<ResponseLogin> call, Response<ResponseLogin> response) {
                binding.btnIngresar.setEnabled(true);
                binding.btnIngresar.setText("Ingresar");

                if (response.isSuccessful()) {
                    ResponseLogin responseBody = response.body();

                    if (responseBody != null && responseBody.getAccess_token() != null) {

                        guardarDatosDeSesion(responseBody);

                        navegarAlHome(responseBody.getNombre());
                        finish();

                    } else {
                        Toast.makeText(LoginActivity.this, "Error: Respuesta vacía del servidor", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (response.code() == 401) {
                        Toast.makeText(LoginActivity.this, "Error: Email o contraseña incorrectos.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(LoginActivity.this, "Error: " + response.code(), Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseLogin> call, Throwable t) {
                binding.btnIngresar.setEnabled(true);
                binding.btnIngresar.setText("Ingresar");
                Toast.makeText(LoginActivity.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void guardarDatosDeSesion(ResponseLogin response) {
        SharedPreferences sharedPreferences = getSharedPreferences(SP_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("USER_TOKEN", "JWT " + response.getAccess_token());
        editor.putString("USER_NAME", response.getNombre());
        editor.putString("USER_DNI", response.getDni());
        editor.putString("USER_EMAIL", response.getEmail());
        editor.putString("USER_PHONE", response.getTelefono());
        editor.putString("USER_BIRTH_DATE", response.getFecha_nacimiento());

        // Guardamos el Refresh Token
        editor.putString("REFRESH_TOKEN", response.getRefresh_token());
        editor.apply();
    }

    /**
     * --- NUEVA FUNCIÓN ---
     * Navega al HomeActivity, pasando el nombre de usuario si está disponible.
     * @param userName El nombre del usuario (puede ser null si ya se salta desde verificarSesion)
     */
    private void navegarAlHome(String userName) {
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        if (userName != null) {
            intent.putExtra("USER_NAME", userName);
        }
        startActivity(intent);
    }
}