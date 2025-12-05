package com.example.app_cumbe;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.app_cumbe.databinding.FragmentProfileBinding;
import com.example.app_cumbe.model.db.AppDatabase;

import static android.content.Context.MODE_PRIVATE;
import static com.example.app_cumbe.LoginActivity.SP_NAME;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserData();
    }

    private void loadUserData() {
        SharedPreferences prefs = requireContext().getSharedPreferences(SP_NAME, MODE_PRIVATE);

        String name = prefs.getString("USER_NAME", "Usuario");
        String email = prefs.getString("USER_EMAIL", "No disponible");
        String dni = prefs.getString("USER_DNI", "No disponible");
        String phone = prefs.getString("USER_PHONE", "No disponible");
        String birthDate = prefs.getString("USER_BIRTH_DATE", "No disponible");
        String tipo_usuario = prefs.getString("tipo_usuario", "No disponible");



        binding.tvProfileNameHeader.setText(name);
        if (tipo_usuario.equals("CONDUCTOR")) {
            binding.tvProfileTipo.setText("Conductor");
        } else if (tipo_usuario.equals("CLIENTE")) {
            binding.tvProfileTipo.setText("Cliente");
        }
        setProfileInfo(binding.cardEmail.getRoot(), "Email", email, R.drawable.ic_profile_mail);
        setProfileInfo(binding.cardDni.getRoot(), "DNI", dni, R.drawable.ic_badge);
        setProfileInfo(binding.cardPhone.getRoot(), "Teléfono", phone, R.drawable.ic_profile_phone);
        setProfileInfo(binding.cardBirthDate.getRoot(), "Fecha de Nacimiento", birthDate, R.drawable.ic_profile_calendar);

        // Listeners
        binding.btnLogout.setOnClickListener(v -> cerrarSesion());
        binding.btnEditarPerfil.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), EditProfileActivity.class);
            startActivity(intent);
        });
    }

    private void setProfileInfo(View cardView, String label, String value, int iconResId) {
        TextView tvLabel = cardView.findViewById(R.id.tvInfoLabel);
        TextView tvValue = cardView.findViewById(R.id.tvInfoValue);
        ImageView ivIcon = cardView.findViewById(R.id.ivInfoIcon);

        if (tvLabel != null) tvLabel.setText(label);
        if (tvValue != null) tvValue.setText(value);
        if (ivIcon != null) ivIcon.setImageResource(iconResId);
    }

    private void cerrarSesion() {
        // Para evitar memory leaks, usamos el contexto de la aplicación que vive más tiempo
        final Context appContext = requireContext().getApplicationContext();

        // Limpiar las preferencias de usuario
        SharedPreferences sharedPreferences = appContext.getSharedPreferences(SP_NAME, MODE_PRIVATE);
        sharedPreferences.edit().clear().apply();

        // Redirigir al Login
        Intent intent = new Intent(appContext, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        // Finalizar la actividad actual
        if (getActivity() != null) {
            getActivity().finish();
        }
    }
}