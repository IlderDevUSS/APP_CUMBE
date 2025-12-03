package com.example.app_cumbe;

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

import com.example.app_cumbe.model.db.AppDatabase;
import com.example.app_cumbe.model.db.NotificacionEntity;

import java.util.ArrayList;
import java.util.List;

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
        cargarNotificaciones();
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new NotificationsAdapter();
        recyclerView.setAdapter(adapter);

        // MANEJO DEL CLIC EN LA NOTIFICACIÓN
        adapter.setOnItemClickListener(notificacion -> {
            // 1. Marcar como leída en BD
            AppDatabase db = AppDatabase.getDatabase(requireContext());
            db.notificacionDao().marcarComoLeida(notificacion.id);

            // 2. Actualizar el badge del menú principal (el puntito rojo)
            if (getActivity() instanceof HomeActivity) {
                ((HomeActivity) getActivity()).actualizarBadgeNotificaciones();
            }

            // 3. Recargar la lista para que se le quite el fondo azulito
            cargarNotificaciones();

            // 4. LÓGICA DE REDIRECCIÓN (Lo que hablamos del horario_id)
            if ("HORARIO".equals(notificacion.origenReferencia)) {
                // Aquí podrías abrir DetalleRutaActivity o TicketActivity pasando el ID
                Toast.makeText(getContext(), "Abriendo viaje ID: " + notificacion.referenciaId, Toast.LENGTH_SHORT).show();
                // Intent intent = ...
                // startActivity(intent);
            }
        });
    }

    private void cargarNotificaciones() {
        AppDatabase db = AppDatabase.getDatabase(requireContext());
        // Obtenemos la lista ordenada por fecha
        List<NotificacionEntity> lista = db.notificacionDao().obtenerTodas();

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