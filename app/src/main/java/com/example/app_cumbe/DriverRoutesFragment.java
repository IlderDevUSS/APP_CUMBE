package com.example.app_cumbe;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.app_cumbe.api.ApiClient;
import com.example.app_cumbe.api.ApiService;
import com.example.app_cumbe.databinding.FragmentDriverRoutesBinding;
import com.example.app_cumbe.model.RutaConductor;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DriverRoutesFragment extends Fragment {

    private FragmentDriverRoutesBinding binding;
    private SharedPreferences sharedPreferences;
    private DriverRoutesAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDriverRoutesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sharedPreferences = requireActivity().getSharedPreferences(LoginActivity.SP_NAME, Context.MODE_PRIVATE);

        setupRecyclerView();
        loadDriverRoutes();
    }

    private void setupRecyclerView() {
        binding.rvDriverRoutes.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new DriverRoutesAdapter();
        binding.rvDriverRoutes.setAdapter(adapter);
    }

    private void loadDriverRoutes() {
        String dni = sharedPreferences.getString("USER_DNI", "");
        if (dni.isEmpty()) {
            Toast.makeText(getContext(), "Error: DNI no encontrado.", Toast.LENGTH_LONG).show();
            return;
        }

        ApiService apiService = ApiClient.getApiService();

        // Usamos la misma API getRutaActualConductor para probar.
        // Si tienes una API de historial que devuelva lista, úsala aquí.
        Call<List<RutaConductor>> call = apiService.getHistorialRutasConductor(dni);

        call.enqueue(new Callback<List<RutaConductor>>() {
            @Override
            public void onResponse(Call<List<RutaConductor>> call, Response<List<RutaConductor>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Pasamos la lista completa directamente al adaptador
                    adapter.setData(response.body());

                    if (response.body().isEmpty()) {
                        Toast.makeText(getContext(), "No tienes historial de rutas.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Error al cargar rutas", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<RutaConductor>> call, Throwable t) {
                Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- ADAPTER INTERNO (Mismo que antes) ---
    class DriverRoutesAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<DriverRoutesAdapter.RouteViewHolder> {
        private List<RutaConductor> rutas = new ArrayList<>();

        public void setData(List<RutaConductor> nuevasRutas) {
            this.rutas = nuevasRutas;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public RouteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ruta_conductor, parent, false);
            return new RouteViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RouteViewHolder holder, int position) {
            RutaConductor ruta = rutas.get(position);
            holder.tvTitulo.setText(ruta.getOrigen() + " - " + ruta.getDestino());
            String estado = ruta.getEstado() != null ? ruta.getEstado().replace("_", " ") : "DESCONOCIDO";
            holder.tvEstado.setText(estado);
            holder.tvFecha.setText(ruta.getFechaSalida() + " " + ruta.getHoraSalida());
            holder.tvPlaca.setText("Bus: " + (ruta.getPlacaBus() != null ? ruta.getPlacaBus() : "N/A"));

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), DetalleRutaActivity.class);
                intent.putExtra("RUTA_DATA", ruta);
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return rutas.size();
        }

        class RouteViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
            android.widget.TextView tvTitulo, tvEstado, tvFecha, tvPlaca;

            public RouteViewHolder(@NonNull View itemView) {
                super(itemView);
                tvTitulo = itemView.findViewById(R.id.tvRutaTitulo);
                tvEstado = itemView.findViewById(R.id.tvEstadoRuta);
                tvFecha = itemView.findViewById(R.id.tvFechaHora);
                tvPlaca = itemView.findViewById(R.id.tvPlacaBus);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}