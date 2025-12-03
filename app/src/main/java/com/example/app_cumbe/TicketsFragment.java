package com.example.app_cumbe;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app_cumbe.api.ApiClient;
import com.example.app_cumbe.api.ApiService;
import com.example.app_cumbe.model.Ticket;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.app_cumbe.LoginActivity.SP_NAME;

public class TicketsFragment extends Fragment {

    private RecyclerView rvTickets;
    private TextView tvEmpty;
    private ProgressBar progressBar;
    private TicketsAdapter adapter;
    private List<Ticket> listaTickets = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tickets, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvTickets = view.findViewById(R.id.rvTicketsHistory);
        tvEmpty = view.findViewById(R.id.tvEmptyTickets);
        progressBar = view.findViewById(R.id.progressBarTickets);

        setupRecyclerView();
        cargarHistorial();
    }

    private void setupRecyclerView() {
        // Pasamos el listener para manejar el clic
        adapter = new TicketsAdapter(listaTickets, ticket -> {
            // Abrir TicketActivity con todos los detalles
            Intent intent = new Intent(requireContext(), TicketActivity.class);
            intent.putExtra("OBJETO_TICKET", ticket); // Pasamos el objeto completo
            // También pasamos los extras individuales por compatibilidad si se necesita
            intent.putExtra("PASAJE_ID", ticket.getPasajeId());
            intent.putExtra("TRANSACCION_ID", ticket.getTransaccionId());
            intent.putExtra("RUTA", ticket.getOrigen() + " - " + ticket.getDestino());
            intent.putExtra("FECHA", ticket.getFechaSalida() + " " + ticket.getHoraSalida());
            intent.putExtra("ASIENTO", ticket.getAsiento());
            intent.putExtra("PRECIO", ticket.getPrecio());
            intent.putExtra("SERVICIO", ticket.getServicio());
            intent.putExtra("ESTADO", ticket.getEstado());


            startActivity(intent);
        });
        rvTickets.setLayoutManager(new LinearLayoutManager(getContext()));
        rvTickets.setAdapter(adapter);
    }

    private void cargarHistorial() {
        if (getContext() == null) return;

        SharedPreferences prefs = requireContext().getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        String token = prefs.getString("USER_TOKEN", null);

        if (token == null) {
            progressBar.setVisibility(View.GONE);
            tvEmpty.setText("Inicia sesión para ver tus viajes");
            tvEmpty.setVisibility(View.VISIBLE);
            return;
        }

        ApiService api = ApiClient.getApiService();
        Call<List<Ticket>> call = api.getHistorialPasajes(token);

        call.enqueue(new Callback<List<Ticket>>() {
            @Override
            public void onResponse(Call<List<Ticket>> call, Response<List<Ticket>> response) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    listaTickets.clear();
                    listaTickets.addAll(response.body());
                    adapter.notifyDataSetChanged();

                    if (listaTickets.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                    } else {
                        tvEmpty.setVisibility(View.GONE);
                    }
                } else {
                    // CAMBIO: Mostrar el error real del servidor
                    String errorMsg = "Error desconocido";
                    try {
                        if (response.errorBody() != null) {
                            errorMsg = response.errorBody().string(); // Aquí vendrá el mensaje de Python
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    tvEmpty.setText("Error del servidor (" + response.code() + "):\n" + errorMsg);
                    tvEmpty.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<List<Ticket>> call, Throwable t) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);
                tvEmpty.setText("Fallo de conexión: " + t.getMessage());
                tvEmpty.setVisibility(View.VISIBLE);
            }
        });
    }
}