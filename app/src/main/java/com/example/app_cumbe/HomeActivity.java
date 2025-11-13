package com.example.app_cumbe;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.app_cumbe.databinding.ActivityHomeBinding;

import static com.example.app_cumbe.LoginActivity.SP_NAME;

public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        loadData();
        setupListeners();
    }

    private void loadData() {
        // --- CAMBIO: LÓGICA DE SALUDO MEJORADA ---
        String userName = getIntent().getStringExtra("USER_NAME");

        if (userName == null || userName.isEmpty()) {
            // Si el Intent no trae el nombre (porque venimos de verificarSesion),
            // lo leemos desde SharedPreferences
            SharedPreferences prefs = getSharedPreferences(SP_NAME, MODE_PRIVATE);
            userName = prefs.getString("USER_NAME", "Bienvenido"); // Usamos "Bienvenido" como fallback
        }
        binding.tvUserName.setText(userName);
        // --- FIN DEL CAMBIO ---

        Object proximoViaje = null; // Simulación

        if (proximoViaje == null) {
            binding.tvNextTripTitle.setVisibility(View.GONE);
            binding.cardNextTrip.setVisibility(View.GONE);
            binding.cardNextTrip.setVisibility(View.VISIBLE);
        } else {
            binding.tvNextTripTitle.setVisibility(View.VISIBLE);
            binding.cardNextTrip.setVisibility(View.VISIBLE);
            binding.cardNextTrip.setVisibility(View.GONE);

            binding.tvTripOrigin.setText("Chiclayo");
            binding.tvTripDestination.setText("Jaén");

            setTripInfoRow(binding.rowFecha.getRoot(), "Fecha:", "Lun, 06 Oct 2025");
            setTripInfoRow(binding.rowHora.getRoot(), "Hora de Salida:", "08:30 AM");
            setTripInfoRow(binding.rowPasajeros.getRoot(), "Pasajeros:", "2 Pendientes");
        }

        setNavCard(binding.cardComprarPasaje.getRoot(), R.drawable.ic_local_activity, "Comprar Pasaje", "Encuentra tu ruta y reserva tu asiento.", R.color.color_principal_cumbe);
        setNavCard(binding.cardSeguimiento.getRoot(), R.drawable.ic_track_changes, "Seguimiento de Encomienda", "Consulta el estado de tu envío.", R.color.color_estado_entregado);
        setNavCard(binding.cardMisTickets.getRoot(), R.drawable.ic_history, "Mis Tickets", "Revisa tus viajes futuros.", android.R.color.black);
        setNavCard(binding.cardServicios.getRoot(), R.drawable.ic_info, "Nuestros Servicios", "Explora nuestras flotas y beneficios.", android.R.color.black);
    }

    private void setupListeners() {
        binding.btnMenu.setOnClickListener(v -> showPopupMenu(v));

        binding.cardComprarPasaje.getRoot().setOnClickListener(v -> {
            Toast.makeText(this, "Abriendo Comprar Pasaje...", Toast.LENGTH_SHORT).show();
            // Intent intent = new Intent(HomeActivity.this, ComprarPasajeActivity.class);
            // startActivity(intent);
        });

        binding.cardSeguimiento.getRoot().setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences(SP_NAME, MODE_PRIVATE);
            String userToken = prefs.getString("USER_TOKEN", null);

            if (userToken == null) {
                Toast.makeText(this, "Error: No se encontró token. Inicia sesión de nuevo.", Toast.LENGTH_LONG).show();
                cerrarSesion();
                return;
            }

            Intent intent = new Intent(HomeActivity.this, TrackingActivity.class);
            intent.putExtra("USER_TOKEN", userToken);
            startActivity(intent);
        });

        binding.cardNextTrip.setOnClickListener(v -> {
            Toast.makeText(this, "Abriendo Mis Tickets...", Toast.LENGTH_SHORT).show();
        });

        binding.cardMisTickets.getRoot().setOnClickListener(v -> {
            Toast.makeText(this, "Abriendo Mis Tickets...", Toast.LENGTH_SHORT).show();
        });

        binding.cardServicios.getRoot().setOnClickListener(v -> {
            Toast.makeText(this, "Abriendo Nuestros Servicios...", Toast.LENGTH_SHORT).show();
        });
    }

    private void showPopupMenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenuInflater().inflate(R.menu.home_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_perfil) {
                Toast.makeText(this, "Abriendo Perfil...", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.menu_cerrar_sesion) {
                cerrarSesion();
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void cerrarSesion() {
        SharedPreferences sharedPreferences = getSharedPreferences(SP_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setTripInfoRow(View rowView, String label, String value) {
        TextView tvLabel = rowView.findViewById(R.id.tvInfoLabel);
        TextView tvValue = rowView.findViewById(R.id.tvInfoValue);
        tvLabel.setText(label);
        tvValue.setText(value);
    }

    private void setNavCard(View cardView, int iconResId, String title, String description, int iconColorResId) {
        ImageView ivIcon = cardView.findViewById(R.id.ivCardIcon);
        TextView tvTitle = cardView.findViewById(R.id.tvCardTitle);
        TextView tvDescription = cardView.findViewById(R.id.tvCardDescription);

        ivIcon.setImageResource(iconResId);
        tvTitle.setText(title);
        tvDescription.setText(description);
        ivIcon.setColorFilter(ContextCompat.getColor(this, iconColorResId));
    }
}