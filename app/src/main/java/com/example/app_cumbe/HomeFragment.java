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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView; // Necesario para el adaptador interno
import androidx.viewpager2.widget.ViewPager2;   // Necesario para el carrusel

import com.example.app_cumbe.api.ApiClient;
import com.example.app_cumbe.api.ApiService;
import com.example.app_cumbe.databinding.FragmentHomeBinding;
import com.example.app_cumbe.databinding.ItemPromoBannerBinding; // Asegurate que creaste este XML
import com.example.app_cumbe.model.ResponseProximoViaje;
import com.example.app_cumbe.model.RutaConductor;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.Arrays;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static com.example.app_cumbe.LoginActivity.SP_NAME;

public class HomeFragment extends Fragment {
    private RutaConductor rutaActualConductor;
    private FragmentHomeBinding binding;
    private SharedPreferences sharedPreferences;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializar Preferencias
        sharedPreferences = requireContext().getSharedPreferences(SP_NAME, MODE_PRIVATE);

        // 1. Cargar Datos del Usuario (Header nuevo)
        setupHeader();

        // 2. Configurar Switch de Conductor
        setupDriverSwitch();

        // 3. Configurar Carrusel
        setupPromoCarousel();

        // 4. Lógica original de Tarjetas (Manteniendo tu código)
        setNavCard(binding.cardComprarPasaje.getRoot(), R.drawable.ic_local_activity, "Comprar Pasaje", "Encuentra tu ruta y reserva tu asiento.", R.color.color_principal_cumbe);
        setNavCard(binding.cardSeguimiento.getRoot(), R.drawable.ic_track_changes, "Seguimiento de Encomienda", "Consulta el estado de tu envío.", R.color.color_estado_entregado);

        // 5. Cargar API
        cargarProximoViaje();
        setupListeners();
    }

    private void setupDriverSwitch() {
        String tipoUsuario = sharedPreferences.getString("tipo_usuario", "CLIENTE");
        String conductorDni = sharedPreferences.getString("USER_DNI", "");

        if ("CONDUCTOR".equalsIgnoreCase(tipoUsuario)) {
            binding.layoutDriverMode.setVisibility(View.VISIBLE);
        } else {
            binding.layoutDriverMode.setVisibility(View.GONE);
        }

        binding.switchDriverMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // MODO CONDUCTOR ACTIVO
                binding.tvModeLabel.setText("Modo Conductor");

                // 1. Ocultar interfaz Cliente
                binding.cardNextTrip.setVisibility(View.GONE);
                binding.cardNoNextTrip.setVisibility(View.GONE);
                binding.tvNextTripTitle.setText("Tus Rutas Asignadas");

                // 2. Mostrar interfaz Conductor
                cargarRutaConductor(conductorDni);

            } else {
                // MODO CLIENTE ACTIVO
                binding.tvModeLabel.setText("Modo Cliente");
                binding.tvNextTripTitle.setText("Tu Próximo Viaje");
                binding.cardDriverRoute.setVisibility(View.GONE);

                // Cargar datos normales
                cargarProximoViaje();
            }
        });
    }

    private void cargarRutaConductor(String dni) {
        ApiService apiService = ApiClient.getApiService();
        // Asumiendo que existe este endpoint en tu backend
        Call<RutaConductor> call = apiService.getRutaActualConductor(dni);

        call.enqueue(new Callback<RutaConductor>() {
            @Override
            public void onResponse(Call<RutaConductor> call, Response<RutaConductor> response) {
                if (!isAdded() || binding == null) return;

                if (response.isSuccessful() && response.body() != null) {
                    rutaActualConductor = response.body();

                    binding.cardDriverRoute.setVisibility(View.VISIBLE);
                    binding.tvDriverOrigin.setText(rutaActualConductor.getOrigen());
                    binding.tvDriverDest.setText(rutaActualConductor.getDestino());
                    binding.tvDriverStatus.setText("ESTADO: " + rutaActualConductor.getEstado());

                    // Click para ir al detalle
                    binding.cardDriverRoute.setOnClickListener(v -> {
                        Intent intent = new Intent(requireContext(), DetalleRutaActivity.class);
                        intent.putExtra("RUTA_DATA", rutaActualConductor);
                        startActivity(intent);
                    });

                } else {
                    // Si no hay ruta asignada, mostrar mensaje o tarjeta vacía
                    Toast.makeText(getContext(), "No tienes rutas asignadas hoy", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RutaConductor> call, Throwable t) {
                Toast.makeText(getContext(), "Error cargando rutas", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupHeader() {
        // Obtenemos nombre guardado
        String userName = sharedPreferences.getString("USER_NAME", "Viajero");
        // Extraemos solo el primer nombre para el saludo "Hola, Juan"
        String primerNombre = userName.split(" ")[0];
        binding.tvWelcomeName.setText("Hola, " + primerNombre);

        // Listener para cerrar sesión
        binding.ivLogout.setOnClickListener(v -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear(); // Borra token y datos
            editor.apply();

            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }


    private void setupPromoCarousel() {
        // Lista de imágenes para el carrusel (asegurate que existan en drawable)
        List<Integer> promoImages = Arrays.asList(
                R.drawable.bus_1,
                R.drawable.bus_2,
                R.drawable.bus_3
        );
        PromoAdapter adapter = new PromoAdapter(promoImages);
        binding.vpPromos.setAdapter(adapter);
    }

    // --- MÉTODOS ORIGINALES TUYOS (Intactos) ---

    @Override
    public void onResume() {
        super.onResume();
        // Recargar datos al volver
        setupHeader(); // Reutilizamos setupHeader en lugar de loadData parcial
    }

    private void cargarProximoViaje() {
        if (getContext() == null) return;

        String userToken = sharedPreferences.getString("USER_TOKEN", null);

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

    // --- ADAPTER INTERNO PARA CARRUSEL ---
    class PromoAdapter extends RecyclerView.Adapter<PromoAdapter.PromoViewHolder> {
        private final List<Integer> images;

        public PromoAdapter(List<Integer> images) {
            this.images = images;
        }

        @NonNull
        @Override
        public PromoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Asegurate de haber creado item_promo_banner.xml como te indiqué antes
            ItemPromoBannerBinding itemBinding = ItemPromoBannerBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new PromoViewHolder(itemBinding);
        }

        @Override
        public void onBindViewHolder(@NonNull PromoViewHolder holder, int position) {
            holder.binding.ivPromoImage.setImageResource(images.get(position));
        }

        @Override
        public int getItemCount() {
            return images.size();
        }

        class PromoViewHolder extends RecyclerView.ViewHolder {
            ItemPromoBannerBinding binding;
            public PromoViewHolder(ItemPromoBannerBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }
}