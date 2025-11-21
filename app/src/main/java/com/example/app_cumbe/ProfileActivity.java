package com.example.app_cumbe;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.app_cumbe.databinding.ActivityProfileBinding;

import static com.example.app_cumbe.LoginActivity.SP_NAME;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolbar();
        loadUserData();
        setupListeners();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbarProfile);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        binding.toolbarProfile.setNavigationOnClickListener(v -> finish());
    }

    private void loadUserData() {
        SharedPreferences prefs = getSharedPreferences(SP_NAME, MODE_PRIVATE);

        String name = prefs.getString("USER_NAME", "Usuario");
        String email = prefs.getString("USER_EMAIL", "No disponible");
        String dni = prefs.getString("USER_DNI", "No disponible");

        String phone = prefs.getString("USER_PHONE", "No disponible");
        String birthDate = prefs.getString("USER_BIRTH_DATE", "No disponible");

        // 1. Header (Nombre)
        binding.tvProfileNameHeader.setText(name);



        setProfileInfo(binding.cardEmail.getRoot(), "Email", email, R.drawable.ic_profile_mail); // Asegúrate de tener este icono
        setProfileInfo(binding.cardDni.getRoot(), "DNI", dni, R.drawable.ic_badge);     // Asegúrate de tener este icono
        setProfileInfo(binding.cardPhone.getRoot(), "Teléfono", phone, R.drawable.ic_profile_phone);
        setProfileInfo(binding.cardBirthDate.getRoot(), "Fecha de Nacimiento", birthDate, R.drawable.ic_profile_calendar);
    }

    /**
     * Función auxiliar para llenar los datos de una tarjeta de perfil.
     */
    private void setProfileInfo(View cardView, String label, String value, int iconResId) {
        TextView tvLabel = cardView.findViewById(R.id.tvInfoLabel);
        TextView tvValue = cardView.findViewById(R.id.tvInfoValue);
        ImageView ivIcon = cardView.findViewById(R.id.ivInfoIcon);

        if (tvLabel != null) tvLabel.setText(label);
        if (tvValue != null) tvValue.setText(value);
        if (ivIcon != null) ivIcon.setImageResource(iconResId);
    }

    private void setupListeners() {
        binding.btnLogout.setOnClickListener(v -> {
            cerrarSesion();
        });
    }

    private void cerrarSesion() {
        SharedPreferences sharedPreferences = getSharedPreferences(SP_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}