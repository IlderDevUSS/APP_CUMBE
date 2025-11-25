package com.example.app_cumbe;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import com.example.app_cumbe.databinding.ActivityTicketBinding;

public class TicketActivity extends AppCompatActivity {

    private ActivityTicketBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Usamos ViewBinding para evitar errores de IDs nulos
        binding = ActivityTicketBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 1. Recibir datos de manera segura (con valores por defecto)
        int pasajeId = getIntent().getIntExtra("PASAJE_ID", 0);
        int transaccionId = getIntent().getIntExtra("TRANSACCION_ID", 0);
        int asiento = getIntent().getIntExtra("ASIENTO", 0);

        String ruta = getIntent().getStringExtra("RUTA");
        if (ruta == null) ruta = "Ruta Confirmada"; // Protección contra nulos

        String fecha = getIntent().getStringExtra("FECHA");
        if (fecha == null) fecha = "Fecha pendiente"; // Protección contra nulos

        // 2. Mostrar datos en la interfaz usando binding
        // Esto evita que la app se cierre si un ID cambió en el XML
        binding.tvTicketRuta.setText(ruta);
        binding.tvTicketFecha.setText(fecha);
        binding.tvTicketAsiento.setText(String.valueOf(asiento));
        binding.tvTicketId.setText(String.valueOf(pasajeId));
        binding.tvTransaccionInfo.setText("Ref. Transacción: " + transaccionId);

        // 3. Configurar botón de volver
        binding.btnVolverInicio.setOnClickListener(v -> {
            Intent intent = new Intent(TicketActivity.this, HomeActivity.class);
            // Limpiamos la pila para que no pueda volver atrás con el botón "Atrás" del celular
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}