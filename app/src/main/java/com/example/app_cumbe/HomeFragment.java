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
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.app_cumbe.api.ApiClient;
import com.example.app_cumbe.api.ApiService;
import com.example.app_cumbe.databinding.FragmentHomeBinding;
import com.example.app_cumbe.databinding.ItemPromoBannerBinding;
import com.example.app_cumbe.model.ResponseProximoViaje;
import com.example.app_cumbe.model.RutaConductor;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.Arrays;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static com.example.app_cumbe.LoginActivity.SP_NAME;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private SharedPreferences sharedPreferences;
    private RutaConductor rutaActualConductor;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sharedPreferences = requireContext().getSharedPreferences(SP_NAME, MODE_PRIVATE);

        setupHeader();
        setupDriverSwitch();
        setupPromoCarousel();

        // Lógica de tarjetas inferiores (Cliente)
        setNavCard(binding.cardComprarPasaje.getRoot(), R.drawable.ic_local_activity, "Comprar Pasaje", "Encuentra tu ruta y reserva tu asiento.", R.color.color_principal_cumbe);
        setNavCard(binding.cardSeguimiento.getRoot(), R.drawable.ic_track_changes, "Seguimiento de Encomienda", "Consulta el estado de tu envío.", R.color.color_estado_entregado);

        // Carga inicial (Cliente por defecto o estado anterior)
        cargarProximoViaje();
        setupListeners();
    }

    private void setupHeader() {
        String userName = sharedPreferences.getString("USER_NAME", "Viajero");
        String primerNombre = userName.split(" ")[0];
        binding.tvWelcomeName.setText("Hola, " + primerNombre);

        binding.ivLogout.setOnClickListener(v -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();

            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
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
            // Referencia al BottomNav para cambiar el icono
            BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottom_navigation);

            if (isChecked) {
                // --- MODO CONDUCTOR ACTIVO ---
                binding.tvModeLabel.setText("Modo Conductor");
                binding.tvNextTripTitle.setText("Tus Rutas Asignadas");
                binding.tvWelcomeName.setTextColor(ContextCompat.getColor(requireContext(), R.color.Textos_destcados_conductor));
                binding.switchDriverMode.setTrackTintList(ContextCompat.getColorStateList(requireContext(), R.color.Textos_destcados_conductor));


                // 1. Ocultar interfaz Cliente
                binding.cardNextTrip.setVisibility(View.GONE);
                binding.cardNoNextTrip.setVisibility(View.GONE);

                // 2. Cambiar icono del BottomNav a BUS (Conductor)
                if (bottomNav != null) {
                    bottomNav.getMenu().findItem(R.id.navigation_tickets).setIcon(R.drawable.ic_directions_bus);
                    bottomNav.getMenu().findItem(R.id.navigation_tickets).setTitle("Rutas");
                }

                // Guardar estado en preferencias para HomeActivity
                sharedPreferences.edit().putBoolean("MODO_CONDUCTOR_ACTIVO", true).apply();

                // 3. Cargar datos Conductor
                cargarRutaConductor(conductorDni);

            } else {
                // --- MODO CLIENTE ACTIVO ---
                binding.tvModeLabel.setText("Modo Cliente");
                binding.tvNextTripTitle.setText("Tu Próximo Viaje");
                //volvemos el tvWelcomeName a su color original
                binding.tvWelcomeName.setTextColor(ContextCompat.getColor(requireContext(), R.color.Textos_destacado));
                binding.switchDriverMode.setTrackTintList(ContextCompat.getColorStateList(requireContext(), R.color.Textos_destacado));

                // 1. Ocultar interfaz Conductor (AMBAS TARJETAS)
                binding.cardDriverRoute.setVisibility(View.GONE);
                binding.cardDriverNoRoute.setVisibility(View.GONE); // <--- ESTA ES LA CORRECCIÓN CLAVE

                // 2. Restaurar icono BottomNav a TICKET (Cliente)
                if (bottomNav != null) {
                    bottomNav.getMenu().findItem(R.id.navigation_tickets).setIcon(R.drawable.ic_history);
                    bottomNav.getMenu().findItem(R.id.navigation_tickets).setTitle("Mis Tickets");
                }

                // Guardar estado en preferencias
                sharedPreferences.edit().putBoolean("MODO_CONDUCTOR_ACTIVO", false).apply();

                // 3. Cargar datos Cliente
                cargarProximoViaje();
            }
        });
    }

    private void cargarRutaConductor(String dni) {
        if (dni == null || dni.trim().isEmpty()) return;

        ApiService apiService = ApiClient.getApiService();
        Call<RutaConductor> call = apiService.getRutaActualConductor(dni);

        call.enqueue(new Callback<RutaConductor>() {
            @Override
            public void onResponse(Call<RutaConductor> call, Response<RutaConductor> response) {
                if (!isAdded() || binding == null) return;

                if (response.isSuccessful() && response.body() != null) {
                    rutaActualConductor = response.body();
                    mostrarTarjetaConductor(true);

                    binding.tvDriverOrigin.setText(rutaActualConductor.getOrigen());
                    binding.tvDriverDest.setText(rutaActualConductor.getDestino());

                    // CORRECCIÓN 1: Formateo de Estado (Quitar guión bajo y mayúsculas bonitas)
                    String estadoRaw = rutaActualConductor.getEstado();
                    String estadoLimpio = estadoRaw.replace("_", " ");
                    binding.tvDriverStatus.setText("ESTADO: " + estadoLimpio);
                    //asignarle color de acuerdo al estado
                    switch (estadoLimpio) {
                        case "PROGRAMADO":
                            binding.tvDriverStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.color_programado));
                            break;
                            case "EN RUTA":
                                binding.tvDriverStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.color_Ruta));
                                break;
                                case "FINALIZADO":
                                    binding.tvDriverStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.color_finalizado));
                                    break;
                                    case "CANCELADO":
                                        binding.tvDriverStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.color_cancelado));
                                        break;
                    }

                    // CORRECCIÓN 2: Mostrar Pasajeros
                    String info = rutaActualConductor.getFechaSalida() + " - " + rutaActualConductor.getHoraSalida();
                    String infoPasajeros = "\nPasajeros: " + rutaActualConductor.getCantidadPasajeros();
                    binding.tvDriverDateInfo.setText(info + infoPasajeros);
                    binding.cardDriverRoute.setOnClickListener(v -> {
                        Intent intent = new Intent(requireContext(), DetalleRutaActivity.class);
                        intent.putExtra("RUTA_DATA", rutaActualConductor);
                        startActivity(intent);
                    });

                } else {
                    mostrarTarjetaConductor(false);
                }
            }

            @Override
            public void onFailure(Call<RutaConductor> call, Throwable t) {
                mostrarTarjetaConductor(false);
            }
        });
    }

    private void mostrarTarjetaConductor(boolean tieneRuta) {
        // Solo mostramos si el switch sigue activo
        if (!binding.switchDriverMode.isChecked()) return;

        if (tieneRuta) {
            binding.cardDriverRoute.setVisibility(View.VISIBLE);
            binding.cardDriverNoRoute.setVisibility(View.GONE);

        } else {
            binding.cardDriverRoute.setVisibility(View.GONE);
            binding.cardDriverNoRoute.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setupHeader();
        // Si volvemos y está activo el modo conductor, recargamos su ruta
        if (binding.switchDriverMode.isChecked()) {
            String conductorDni = sharedPreferences.getString("USER_DNI", "");
            cargarRutaConductor(conductorDni);
        } else {
            cargarProximoViaje();
        }
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
        // Solo actuar si NO estamos en modo conductor
        if (binding.switchDriverMode.isChecked()) return;

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

    private void setupPromoCarousel() {
        List<Integer> promoImages = Arrays.asList(
                R.drawable.prom_1,
                R.drawable.bus_2,
                R.drawable.bus_3
        );
        PromoAdapter adapter = new PromoAdapter(promoImages);
        binding.vpPromos.setAdapter(adapter);
    }

    class PromoAdapter extends RecyclerView.Adapter<PromoAdapter.PromoViewHolder> {
        private final List<Integer> images;
        public PromoAdapter(List<Integer> images) { this.images = images; }
        @NonNull @Override public PromoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemPromoBannerBinding itemBinding = ItemPromoBannerBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new PromoViewHolder(itemBinding);
        }
        @Override public void onBindViewHolder(@NonNull PromoViewHolder holder, int position) {
            holder.binding.ivPromoImage.setImageResource(images.get(position));
        }
        @Override public int getItemCount() { return images.size(); }
        class PromoViewHolder extends RecyclerView.ViewHolder {
            ItemPromoBannerBinding binding;
            public PromoViewHolder(ItemPromoBannerBinding binding) { super(binding.getRoot()); this.binding = binding; }
        }
    }
}