package com.example.app_cumbe;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import com.example.app_cumbe.databinding.ActivityTicketBinding;
import com.example.app_cumbe.model.Ticket;

public class TicketActivity extends AppCompatActivity {

    private ActivityTicketBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            binding = ActivityTicketBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());

            Intent intent = getIntent();

            Ticket ticketObj = (Ticket) intent.getSerializableExtra("OBJETO_TICKET");

            String ruta, fecha, pasajero, dni, servicio;
            int asiento, transaccionId;
            double precio;

            if (ticketObj != null) {
                // ESCENARIO 1: Viene del historial (Objeto completo)
                ruta = ticketObj.getOrigen() + " - " + ticketObj.getDestino();
                fecha = ticketObj.getFechaSalida() + " " + (ticketObj.getHoraSalida() != null ? ticketObj.getHoraSalida() : "");
                asiento = ticketObj.getAsiento();
                transaccionId = ticketObj.getTransaccionId();
                precio = ticketObj.getPrecio();
                servicio = ticketObj.getServicio();
                pasajero = (ticketObj.getNombres() != null ? ticketObj.getNombres() : "") + " " +
                        (ticketObj.getApellidos() != null ? ticketObj.getApellidos() : "");
                dni = ticketObj.getDni();

                binding.tvTituloExito.setText("Comprobante de Viaje");
                binding.btnVolverInicio.setText("Cerrar");
            } else {
                // ESCENARIO 2: Viene de una Compra Reciente (Datos sueltos)
                ruta = intent.getStringExtra("RUTA");
                fecha = intent.getStringExtra("FECHA");
                asiento = intent.getIntExtra("ASIENTO", 0);
                transaccionId = intent.getIntExtra("TRANSACCION_ID", 0);
                precio = intent.getDoubleExtra("PRECIO", 0.0);

                // Estos extras deben ser enviados desde ConfirmarCompraActivity
                servicio = intent.getStringExtra("SERVICIO");
                pasajero = intent.getStringExtra("PASAJERO_NOMBRE");
                dni = intent.getStringExtra("PASAJERO_DNI");

                if (ruta == null) ruta = "Ruta Confirmada";
                if (fecha == null) fecha = "Fecha pendiente";

                binding.tvTituloExito.setText("¡Compra Exitosa!");
                binding.btnVolverInicio.setText("Volver al Inicio");
            }

            // Renderizar datos en la UI con protecciones contra nulos
            if (binding.tvTicketRuta != null) binding.tvTicketRuta.setText(ruta);
            if (binding.tvTicketFecha != null) binding.tvTicketFecha.setText(fecha);
            if (binding.tvTicketAsiento != null) binding.tvTicketAsiento.setText(String.valueOf(asiento));
            if (binding.tvTransaccionInfo != null) binding.tvTransaccionInfo.setText("Ref. Transacción: #" + transaccionId);

            if (binding.tvTicketPrecio != null) binding.tvTicketPrecio.setText("S/ " + String.format("%.2f", precio));

            // Datos del Pasajero
            if (binding.tvTicketPasajero != null) {
                binding.tvTicketPasajero.setText(pasajero != null && !pasajero.trim().isEmpty() ? pasajero.trim().toUpperCase() : "CLIENTE GENERAL");
            }
            if (binding.tvTicketDni != null) {
                binding.tvTicketDni.setText(dni != null && !dni.isEmpty() ? dni : "---");
            }
            if (binding.tvTicketServicio != null) {
                binding.tvTicketServicio.setText(servicio != null && !servicio.isEmpty() ? servicio.toUpperCase() : "ESTÁNDAR");
            }

            // Botón Volver
            if (binding.btnVolverInicio != null) {
                binding.btnVolverInicio.setOnClickListener(v -> {
                    if (ticketObj != null) {
                        // Si venimos del historial, solo cerramos la actividad actual
                        finish();
                    } else {
                        // Si venimos de una compra, volvemos al Home y limpiamos la pila de actividades
                        Intent i = new Intent(TicketActivity.this, HomeActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(i);
                        finish();
                    }
                });
            }

        } catch (Exception e) {
            Log.e("TicketActivity", "Error UI: " + e.getMessage());
            Toast.makeText(this, "Error mostrando ticket", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}