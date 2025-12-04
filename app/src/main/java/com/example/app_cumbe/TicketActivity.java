package com.example.app_cumbe;

import static com.example.app_cumbe.LoginActivity.SP_NAME;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.app_cumbe.HomeActivity;
import com.example.app_cumbe.api.ApiClient;
import com.example.app_cumbe.databinding.ActivityTicketBinding;
import com.example.app_cumbe.model.Ticket;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TicketActivity extends AppCompatActivity {

    private ActivityTicketBinding binding;
    private Ticket ticketActual;
    private boolean esCompraReciente = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Inflar la vista y establecerla como el contenido de la actividad
        binding = ActivityTicketBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 3. Configurar listeners y procesar la lógica de la actividad
        binding.btnVolverInicio.setOnClickListener(v -> manejarSalida());
        procesarIntent();
    }

    private void procesarIntent() {
        Intent i = getIntent();

        // CASO 1: Viene del Historial (Objeto Completo)
        if (i.hasExtra("OBJETO_TICKET")) {
            ticketActual = (Ticket) i.getSerializableExtra("OBJETO_TICKET");
            configurarUI("Comprobante de Viaje", "Cerrar");
            mostrarDatos(ticketActual);
        }

        // CASO 2: Viene de Notificación (Solo trae ID)
        else if (i.getIntExtra("PASAJE_ID", 0) > 0) {
            int pasajeId = i.getIntExtra("PASAJE_ID", 0);
            configurarUI("Cargando Ticket...", "Cerrar");

            // Mostrar ProgressBar mientras carga
            if (binding.progressBar != null) binding.progressBar.setVisibility(View.VISIBLE);
            binding.scrollViewContent.setVisibility(View.GONE); // Ocultar contenido vacío

            cargarTicketDesdeApi(pasajeId);
        }

        // CASO 3: Viene de una Compra Reciente (Datos sueltos)
        else if (i.hasExtra("RUTA")) {
            esCompraReciente = true;
            configurarUI("¡Compra Exitosa!", "Volver al Inicio");
            mostrarDatosDeIntent(i); // Usamos datos directos del intent
        }

        else {
            Toast.makeText(this, "Error: Datos de ticket no encontrados", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void cargarTicketDesdeApi(int pasajeId) {
        SharedPreferences prefs = getSharedPreferences(SP_NAME, MODE_PRIVATE);
        String token = prefs.getString("USER_TOKEN", "");

        ApiClient.getApiService().getPasajePorId("JWT " + token, pasajeId).enqueue(new Callback<Ticket>() {
            @Override
            public void onResponse(Call<Ticket> call, Response<Ticket> response) {
                if (binding.progressBar != null) binding.progressBar.setVisibility(View.GONE);
                binding.scrollViewContent.setVisibility(View.VISIBLE);

                if (response.isSuccessful() && response.body() != null) {
                    ticketActual = response.body();
                    configurarUI("Detalle del Viaje", "Cerrar");
                    mostrarDatos(ticketActual);
                } else {
                    Toast.makeText(TicketActivity.this, "No se pudo cargar el ticket", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<Ticket> call, Throwable t) {
                Toast.makeText(TicketActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void configurarUI(String titulo, String textoBoton) {
        if (binding.tvTituloExito != null) binding.tvTituloExito.setText(titulo);
        if (binding.btnVolverInicio != null) binding.btnVolverInicio.setText(textoBoton);
    }

    private void mostrarDatos(Ticket t) {
        if (t == null) return;

        binding.tvTicketRuta.setText(t.getOrigen() + " - " + t.getDestino());
        binding.tvTicketFecha.setText(t.getFechaSalida() + " " + (t.getHoraSalida() != null ? t.getHoraSalida() : ""));
        binding.tvTicketAsiento.setText(String.valueOf(t.getAsiento()));
        binding.tvTicketPrecio.setText("S/ " + String.format("%.2f", t.getPrecio()));
        binding.tvTransaccionInfo.setText("Ref: #" + t.getTransaccionId());

        String nombre = (t.getNombres() != null ? t.getNombres() : "") + " " + (t.getApellidos() != null ? t.getApellidos() : "");
        binding.tvTicketPasajero.setText(nombre.trim().isEmpty() ? "CLIENTE" : nombre.toUpperCase());
        binding.tvTicketDni.setText(t.getDni() != null ? t.getDni() : "---");
        binding.tvTicketServicio.setText(t.getServicio() != null ? t.getServicio() : "ESTÁNDAR");

        if (binding.tvEstadoViaje != null) binding.tvEstadoViaje.setText(t.getEstado());
    }

    // Método auxiliar para el caso de Compra (Datos sueltos)
    private void mostrarDatosDeIntent(Intent i) {
        binding.tvTicketRuta.setText(i.getStringExtra("RUTA"));
        binding.tvTicketFecha.setText(i.getStringExtra("FECHA"));
        binding.tvTicketAsiento.setText(String.valueOf(i.getIntExtra("ASIENTO", 0)));
        binding.tvTicketPrecio.setText("S/ " + String.format("%.2f", i.getDoubleExtra("PRECIO", 0.0)));
        binding.tvTransaccionInfo.setText("Ref: #" + i.getIntExtra("TRANSACCION_ID", 0));

        String pasajero = i.getStringExtra("PASAJERO_NOMBRE");
        binding.tvTicketPasajero.setText(pasajero != null ? pasajero.toUpperCase() : "---");
        binding.tvTicketDni.setText(i.getStringExtra("PASAJERO_DNI"));
        binding.tvTicketServicio.setText(i.getStringExtra("SERVICIO"));

        if (binding.tvEstadoViaje != null) binding.tvEstadoViaje.setText("VENDIDO");
    }

    private void manejarSalida() {
        if (esCompraReciente) {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
        finish();
    }
}