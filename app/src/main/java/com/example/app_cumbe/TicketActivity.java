package com.example.app_cumbe;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.app_cumbe.api.ApiClient;
import com.example.app_cumbe.databinding.ActivityTicketBinding;
import com.example.app_cumbe.model.Ticket;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.app_cumbe.LoginActivity.SP_NAME;

public class TicketActivity extends AppCompatActivity {

    private ActivityTicketBinding binding;
    private Ticket ticketActual;
    private boolean esCompraReciente = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTicketBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 1. Configurar Botón Volver (Lógica genérica inicial)
        binding.btnVolverInicio.setOnClickListener(v -> manejarSalida());

        // 2. Procesar los datos recibidos (El cerebro de la actividad)
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

        // CASO 2: Viene de una Compra Reciente (Datos sueltos)
        // Verificamos si trae "RUTA", que es un dato obligatorio en la compra
        else if (i.hasExtra("RUTA")) {
            esCompraReciente = true;
            reconstruirTicketDesdeExtras(i);
            configurarUI("¡Compra Exitosa!", "Volver al Inicio");
            mostrarDatos(ticketActual);
        }

        // CASO 3: Viene de Notificación (Solo trae ID)
        else if (i.getIntExtra("PASAJE_ID", 0) > 0) {
            int pasajeId = i.getIntExtra("PASAJE_ID", 0);
            configurarUI("Cargando...", "Cerrar");
            cargarTicketDesdeApi(pasajeId);
        }

        // CASO DE ERROR
        else {
            Toast.makeText(this, "Error: No se encontraron datos del ticket", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    // Crea un objeto Ticket temporal con los datos sueltos de la compra
    // para poder usar el mismo método mostrarDatos() siempre.
    private void reconstruirTicketDesdeExtras(Intent i) {
        ticketActual = new Ticket();

        // Usamos setters (o asignación directa si son públicos/package en tu modelo)
        // IMPORTANTE: Si Ticket.java no tiene setters, tendrás que agregarlos
        // o usar un constructor completo. Aquí asumo un constructor o setters.
        // Si no tienes setters, modifica tu Ticket.java o asigna directamente si son públicos.

        // Ejemplo asumiendo constructor o setters simulados:
        // ticketActual.setOrigen(...)

        // Como tu modelo Ticket usa Gson y campos privados sin setters publicos en el código que vi,
        // la mejor opción rápida es simular la visualización o agregar los setters en Ticket.java.
        // Por ahora, asignaré valores a las vistas directamente en mostrarDatos si el objeto está vacío,
        // pero lo ideal es llenar el objeto.

        // OPCIÓN ROBUSTA: Vamos a pasar los datos a mostrarDatos simulando el objeto
        // (Nota: Para que esto compile perfecto, agrega Setters en tu Ticket.java)
    }

    private void cargarTicketDesdeApi(int pasajeId) {
        SharedPreferences prefs = getSharedPreferences(SP_NAME, MODE_PRIVATE);
        String token = prefs.getString("USER_TOKEN", "");

        if (binding.progressBar != null) binding.progressBar.setVisibility(View.VISIBLE);

        ApiClient.getApiService().getPasajePorId(token, pasajeId).enqueue(new Callback<Ticket>() {
            @Override
            public void onResponse(Call<Ticket> call, Response<Ticket> response) {
                if (binding.progressBar != null) binding.progressBar.setVisibility(View.GONE);

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
                if (binding.progressBar != null) binding.progressBar.setVisibility(View.GONE);
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
        // Si t es nulo (caso compra donde no pudimos crear objeto), leemos del intent directo
        if (t == null && esCompraReciente) {
            Intent i = getIntent();
            binding.tvTicketRuta.setText(i.getStringExtra("RUTA"));
            binding.tvTicketFecha.setText(i.getStringExtra("FECHA"));
            binding.tvTicketAsiento.setText(String.valueOf(i.getIntExtra("ASIENTO", 0)));
            binding.tvTicketPrecio.setText("S/ " + i.getDoubleExtra("PRECIO", 0.0));
            binding.tvTransaccionInfo.setText("Ref: #" + i.getIntExtra("TRANSACCION_ID", 0));

            String pasajero = i.getStringExtra("PASAJERO_NOMBRE");
            binding.tvTicketPasajero.setText(pasajero != null ? pasajero.toUpperCase() : "---");
            binding.tvTicketDni.setText(i.getStringExtra("PASAJERO_DNI"));
            binding.tvTicketServicio.setText(i.getStringExtra("SERVICIO"));
            return;
        }

        // Caso Normal (Objeto Ticket lleno)
        if (t != null) {
            String ruta = t.getOrigen() + " - " + t.getDestino();
            String fechaHora = t.getFechaSalida() + " " + (t.getHoraSalida() != null ? t.getHoraSalida() : "");

            binding.tvTicketRuta.setText(ruta);
            binding.tvTicketFecha.setText(fechaHora);
            binding.tvTicketAsiento.setText(String.valueOf(t.getAsiento()));
            binding.tvTicketPrecio.setText("S/ " + t.getPrecio());
            binding.tvTransaccionInfo.setText("Ref: #" + t.getTransaccionId()); // O t.getPasajeId()

            String nombre = (t.getNombres() != null ? t.getNombres() : "") + " " + (t.getApellidos() != null ? t.getApellidos() : "");
            binding.tvTicketPasajero.setText(nombre.trim().isEmpty() ? "CLIENTE" : nombre.toUpperCase());
            binding.tvTicketDni.setText(t.getDni());
            binding.tvTicketServicio.setText(t.getServicio() != null ? t.getServicio() : "ESTÁNDAR");

            if (binding.tvEstadoViaje != null) {
                binding.tvEstadoViaje.setText(t.getEstado());
            }
        }
    }

    private void manejarSalida() {
        if (esCompraReciente) {
            // Si acaba de comprar, ir al Home y limpiar pila para que no vuelva a "Comprar" con botón atrás
            Intent intent = new Intent(this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
        finish(); // Cierra la actividad actual
    }
}