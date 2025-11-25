package com.example.app_cumbe;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import com.example.app_cumbe.api.ApiClient;
import com.example.app_cumbe.api.ApiService;
import com.example.app_cumbe.databinding.ActivityConfirmarCompraBinding;
import com.example.app_cumbe.model.RequestCompra;
import com.example.app_cumbe.model.ResponseCompra;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.app_cumbe.LoginActivity.SP_NAME;

public class ConfirmarCompraActivity extends AppCompatActivity {

    private ActivityConfirmarCompraBinding binding;

    // Datos recibidos del flujo de compra
    private int horarioId, asiento, piso;
    private double precio = 40.00;
    private String rutaStr = "";
    private String fechaStr = "";

    // Datos del usuario logueado (quien compra)
    private String nombreUser, dniUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityConfirmarCompraBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 1. Recibir datos del Intent
        Intent i = getIntent();
        horarioId = i.getIntExtra("HORARIO_ID", 0);
        asiento = i.getIntExtra("ASIENTO", 0);
        piso = i.getIntExtra("PISO", 1);

        // Recibir datos opcionales si los pasaste
        if (i.hasExtra("PRECIO")) precio = i.getDoubleExtra("PRECIO", 40.0);
        if (i.hasExtra("RUTA")) rutaStr = i.getStringExtra("RUTA");
        if (i.hasExtra("FECHA")) fechaStr = i.getStringExtra("FECHA");

        // 2. Cargar datos del usuario
        SharedPreferences prefs = getSharedPreferences(SP_NAME, MODE_PRIVATE);
        nombreUser = prefs.getString("USER_NAME", "Usuario");
        dniUser = prefs.getString("USER_DNI", "");

        // 3. Llenar la UI con el resumen
        binding.tvResumenAsiento.setText("#" + asiento);
        binding.tvResumenPiso.setText(String.valueOf(piso));
        binding.tvResumenPasajero.setText("Pasajero: " + nombreUser + "\nDNI: " + dniUser);
        binding.tvTotalPagar.setText("S/ " + String.format("%.2f", precio));

        binding.tvResumenRuta.setText(rutaStr.isEmpty() ? "Ruta Seleccionada" : rutaStr);
        binding.tvResumenFecha.setText(fechaStr.isEmpty() ? "Fecha del Viaje" : fechaStr);

        // 4. Listener del Botón Pagar
        binding.btnPagarFinal.setOnClickListener(v -> {
            validarYPagar();
        });
    }

    private void validarYPagar() {
        // 1. Validar método de pago seleccionado
        int selectedId = binding.rgMetodoPago.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Toast.makeText(this, "Por favor selecciona un método de pago", Toast.LENGTH_SHORT).show();
            return;
        }

        String metodoPago = "";
        if (selectedId == R.id.rbYape) metodoPago = "YAPE";
        else if (selectedId == R.id.rbTarjeta) metodoPago = "TARJETA";
        else if (selectedId == R.id.rbEfectivo) metodoPago = "EFECTIVO";

        // 2. Preparar datos del pasajero
        String[] partes = nombreUser.split(" ");
        String nom = partes.length > 0 ? partes[0] : nombreUser;
        String ape = partes.length > 1 ? partes[1] : "";

        // 3. Llamar a la API
        realizarCompra(nom, ape, dniUser, metodoPago);
    }

    private void realizarCompra(String nom, String ape, String dni, String metodo) {
        binding.btnPagarFinal.setEnabled(false);
        binding.btnPagarFinal.setText("Procesando pago...");

        ApiService api = ApiClient.getApiService();
        SharedPreferences prefs = getSharedPreferences(SP_NAME, MODE_PRIVATE);
        String token = prefs.getString("USER_TOKEN", "");

        RequestCompra request = new RequestCompra(horarioId, asiento, piso, precio, nom, ape, dni, metodo);

        api.comprarPasaje(token, request).enqueue(new Callback<ResponseCompra>() {
            @Override
            public void onResponse(Call<ResponseCompra> call, Response<ResponseCompra> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // ¡ÉXITO! Pasamos a la pantalla del TICKET
                    Intent intent = new Intent(ConfirmarCompraActivity.this, TicketActivity.class);

                    // Pasamos datos clave para mostrar el ticket
                    intent.putExtra("PASAJE_ID", response.body().getPasajeId());
                    intent.putExtra("TRANSACCION_ID", response.body().getTransaccionId());
                    intent.putExtra("RUTA", rutaStr);
                    intent.putExtra("FECHA", fechaStr);
                    intent.putExtra("ASIENTO", asiento);

                    startActivity(intent);
                    finish(); // Cerramos confirmación para no volver atrás
                } else {
                    binding.btnPagarFinal.setEnabled(true);
                    binding.btnPagarFinal.setText("Confirmar y Pagar");
                    Toast.makeText(ConfirmarCompraActivity.this, "Error: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseCompra> call, Throwable t) {
                binding.btnPagarFinal.setEnabled(true);
                binding.btnPagarFinal.setText("Confirmar y Pagar");
                Toast.makeText(ConfirmarCompraActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }
}