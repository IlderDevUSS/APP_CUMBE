package com.example.app_cumbe;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app_cumbe.api.ApiClient;
import com.example.app_cumbe.api.ApiService;
import com.example.app_cumbe.model.Ticket;
import com.example.app_cumbe.model.db.AppDatabase;
import com.example.app_cumbe.model.db.NotificacionEntity;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationsFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView tvEmpty;
    // Aquí necesitaremos un Adapter (lo crearemos en el siguiente paso),
    // por ahora dejamos la lista lista para recibir datos.
    private NotificationsAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notifications, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        cargarNotificaciones();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.rvNotifications);
        tvEmpty = view.findViewById(R.id.tvEmptyNotifications);

        setupRecyclerView();
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new NotificationsAdapter();
        recyclerView.setAdapter(adapter);

        // MANEJO DEL CLIC EN LA NOTIFICACIÓN
        adapter.setOnItemClickListener(notificacion -> {
            // 1. Marcar como leída y actualizar UI (si no lo estaba ya)
            marcarNotificacionComoLeida(notificacion);

            // 2. LÓGICA DE REDIRECCIÓN
            // Consideramos que si origenReferencia es "HORARIO", el referenciaId es un pasajeId
            if ("HORARIO".equals(notificacion.origenReferencia)) {
                abrirTicketPorId(notificacion.referenciaId);
            } else if ("ENCOMIENDA".equals(notificacion.origenReferencia)) {
                Intent intent = new Intent(requireContext(), TrackingActivity.class);
                intent.putExtra("ENCOMIENDA_ID", notificacion.referenciaId);
                startActivity(intent);
            } else {
                Toast.makeText(getContext(), "Notificación de tipo desconocido.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void marcarNotificacionComoLeida(NotificacionEntity notificacion) {
        if (notificacion.leido || getContext() == null) return;

        AppDatabase db = AppDatabase.getDatabase(requireContext());
        db.notificacionDao().marcarComoLeida(notificacion.id);

        if (getActivity() instanceof HomeActivity) {
            ((HomeActivity) getActivity()).actualizarBadgeNotificaciones();
        }
        // Recargar la lista para que se le quite el fondo azulito
        cargarNotificaciones();
    }

    private void abrirTicketPorId(int pasajeId) {
        if (getContext() == null) return;

        Toast.makeText(getContext(), "Buscando detalles del ticket...", Toast.LENGTH_SHORT).show();

        SharedPreferences prefs = requireContext().getSharedPreferences(LoginActivity.SP_NAME, Context.MODE_PRIVATE);
        String token = prefs.getString("USER_TOKEN", null);

        if (token == null) {
            Toast.makeText(getContext(), "Sesión no válida. Por favor, inicie sesión de nuevo.", Toast.LENGTH_LONG).show();
            return;
        }

        ApiService api = ApiClient.getApiService();
        // Usar el nuevo endpoint para obtener un solo ticket por ID
        Call<Ticket> call = api.getPasajePorId("JWT " + token, pasajeId);

        call.enqueue(new Callback<Ticket>() {
            @Override
            public void onResponse(Call<Ticket> call, Response<Ticket> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    Ticket ticketEncontrado = response.body();
                    Intent intent = new Intent(requireContext(), TicketActivity.class);
                    intent.putExtra("OBJETO_TICKET", ticketEncontrado);
                    startActivity(intent);
                } else {
                    Toast.makeText(getContext(), "No se pudo encontrar el ticket.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Ticket> call, Throwable t) {
                if (isAdded()) {
                    Toast.makeText(getContext(), "Error de conexión. Intente de nuevo.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void cargarNotificaciones() {
        if (getContext() == null) return;

        SharedPreferences prefs = requireContext().getSharedPreferences(LoginActivity.SP_NAME, Context.MODE_PRIVATE);
        String userId = prefs.getString("USER_DNI", null);

        if (userId == null) {
            tvEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            adapter.setData(new ArrayList<>()); // Limpiar lista
            return;
        }

        AppDatabase db = AppDatabase.getDatabase(requireContext());
        List<NotificacionEntity> lista = db.notificacionDao().obtenerPorUsuario(userId);

        if (lista.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            adapter.setData(lista);
        }
    }
}