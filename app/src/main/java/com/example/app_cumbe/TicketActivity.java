package com.example.app_cumbe;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class TicketActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket);

        // Recibir datos
        int pasajeId = getIntent().getIntExtra("PASAJE_ID", 0);
        int transaccionId = getIntent().getIntExtra("TRANSACCION_ID", 0);
        int asiento = getIntent().getIntExtra("ASIENTO", 0);
        String ruta = getIntent().getStringExtra("RUTA");
        String fecha = getIntent().getStringExtra("FECHA");

        // Vincular vistas
        TextView tvRuta = findViewById(R.id.tvTicketRuta);
        TextView tvFecha = findViewById(R.id.tvTicketFecha);
        TextView tvAsiento = findViewById(R.id.tvTicketAsiento);
        TextView tvId = findViewById(R.id.tvTicketId);
        TextView tvTransaccion = findViewById(R.id.tvTransaccionInfo);
        Button btnVolver = findViewById(R.id.btnVolverInicio);

        // Llenar datos
        tvRuta.setText(ruta != null ? ruta : "Viaje Confirmado");
        tvFecha.setText(fecha != null ? fecha : "");
        tvAsiento.setText(String.valueOf(asiento));
        tvId.setText(String.valueOf(pasajeId));
        tvTransaccion.setText("Ref. Transacción: " + transaccionId);

        // Botón Volver
        btnVolver.setOnClickListener(v -> {
            Intent intent = new Intent(TicketActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }
}