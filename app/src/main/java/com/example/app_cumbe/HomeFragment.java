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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.app_cumbe.api.ApiClient;
import com.example.app_cumbe.api.ApiService;
import com.example.app_cumbe.databinding.FragmentHomeBinding;
import com.example.app_cumbe.model.ResponseProximoViaje;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;
import static com.example.app_cumbe.LoginActivity.SP_NAME;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadData();
        cargarProximoViaje();
        setupListeners();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Recargar datos al volver (importante si se editó el perfil o se vuelve de otra actividad)
        loadData();
    }

    private void loadData() {
        if (getActivity() == null) return;

        SharedPreferences prefs = requireContext().getSharedPreferences(SP_NAME, MODE_PRIVATE);
        // LEER SIEMPRE DE PREFS, el valor por defecto es "Usuario"
        String userName = prefs.getString("USER_NAME", "Usuario");

        if (binding.tvUserName != null) {
            binding.tvUserName.setText(userName);
        }

        // Configuración de tarjetas (esto no cambia mucho)
        setNavCard(binding.cardComprarPasaje.getRoot(), R.drawable.ic_local_activity, "Comprar Pasaje", "Encuentra tu ruta y reserva tu asiento.", R.color.color_principal_cumbe);
        setNavCard(binding.cardSeguimiento.getRoot(), R.drawable.ic_track_changes, "Seguimiento de Encomienda", "Consulta el estado de tu envío.", R.color.color_estado_entregado);
    }

    private void cargarProximoViaje() {
        if (getContext() == null) return;

        SharedPreferences prefs = requireContext().getSharedPreferences(SP_NAME, MODE_PRIVATE);
        String userToken = prefs.getString("USER_TOKEN", null);

        if (userToken == null) {
            mostrarTarjetaGris(true);
            return;
        }

        ApiService apiService = ApiClient.getApiService();
        Call<ResponseProximoViaje> call = apiService.getProximoViaje(userToken);

        call.enqueue(new Callback<ResponseProximoViaje>() {
            @Override
            public void onResponse(Call<ResponseProximoViaje> call, Response<ResponseProximoViaje> response) {
                if (!isAdded() || binding == null) return;

                if (response.isSuccessful() && response.body() != null) {
                    ResponseProximoViaje viaje = response.body();
                    mostrarTarjetaGris(false);
                    binding.tvTripOrigin.setText(viaje.getOrigen());
                    binding.tvTripDestination.setText(viaje.getDestino());
                    setTripInfoRow(binding.rowFecha.getRoot(), "Fecha:", viaje.getFecha_salida());
                    setTripInfoRow(binding.rowHora.getRoot(), "Hora de Salida:", viaje.getHora_salida());
                    setTripInfoRow(binding.rowPasajeros.getRoot(), "Pasajeros:", String.valueOf(viaje.getCantidadPasajeros()));
                } else {
                    mostrarTarjetaGris(true);
                }
            }

            @Override
            public void onFailure(Call<ResponseProximoViaje> call, Throwable t) {
                if (!isAdded() || binding == null) return;
                mostrarTarjetaGris(true);
            }
        });
    }

    private void mostrarTarjetaGris(boolean mostrarGris) {
        if (binding == null) return;
        if (mostrarGris) {
            binding.tvNextTripTitle.setVisibility(View.GONE);
            binding.cardNextTrip.setVisibility(View.GONE);
            binding.cardNoNextTrip.setVisibility(View.VISIBLE);
        } else {
            binding.tvNextTripTitle.setVisibility(View.VISIBLE);
            binding.cardNextTrip.setVisibility(View.VISIBLE);
            binding.cardNoNextTrip.setVisibility(View.GONE);
        }
    }

    private void setupListeners() {
        binding.cardSeguimiento.getRoot().setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), TrackingActivity.class);
            startActivity(intent);
        });
        binding.cardComprarPasaje.getRoot().setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), CompraPasajeActivity.class);
            startActivity(intent);
        });
    }

    private void setNavCard(View cardView, int iconResId, String title, String description, int iconColorResId) {
        ImageView ivIcon = cardView.findViewById(R.id.ivCardIcon);
        TextView tvTitle = cardView.findViewById(R.id.tvCardTitle);
        TextView tvDescription = cardView.findViewById(R.id.tvCardDescription);

        ivIcon.setImageResource(iconResId);
        tvTitle.setText(title);
        tvDescription.setText(description);
        ivIcon.setColorFilter(ContextCompat.getColor(requireContext(), iconColorResId));
    }

    private void setTripInfoRow(View rowView, String label, String value) {
        TextView tvLabel = rowView.findViewById(R.id.tvInfoLabel);
        TextView tvValue = rowView.findViewById(R.id.tvInfoValue);
        tvLabel.setText(label);
        tvValue.setText(value);
    }
}