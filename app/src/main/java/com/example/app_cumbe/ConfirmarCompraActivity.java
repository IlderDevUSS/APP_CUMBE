package com.example.app_cumbe;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.RadioGroup;
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
    private double precio = 40.00; // Valor por defecto, debería pasarse por Intent
    private String rutaStr = "Ruta Desconocida"; // Debería pasarse por Intent
    private String fechaStr = "Fecha Desconocida"; // Debería pasarse por Intent

    // Datos del usuario logueado (quien compra)
    private String nombreUser, dniUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityConfirmarCompraBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 1. Recibir datos del Intent (Asegúrate de enviar estos extras desde SeleccionAsientoActivity)
        Intent i = getIntent();
        horarioId = i.getIntExtra("HORARIO_ID", 0);
        asiento = i.getIntExtra("ASIENTO", 0);
        piso = i.getIntExtra("PISO", 1);

        // Opcional: Recibir precio y detalles para mostrar en el resumen
        // if (i.hasExtra("PRECIO")) precio = i.getDoubleExtra("PRECIO", 40.0);
        // if (i.hasExtra("RUTA")) rutaStr = i.getStringExtra("RUTA");
        // if (i.hasExtra("FECHA")) fechaStr = i.getStringExtra("FECHA");

        // 2. Cargar datos del usuario de la sesión (Simulamos que compra para sí mismo)
        SharedPreferences prefs = getSharedPreferences(SP_NAME, MODE_PRIVATE);
        nombreUser = prefs.getString("USER_NAME", "Usuario");
        dniUser = prefs.getString("USER_DNI", "");

        // 3. Llenar la UI con el resumen
        binding.tvResumenAsiento.setText("#" + asiento);
        binding.tvResumenPiso.setText(String.valueOf(piso));
        binding.tvResumenPasajero.setText("Pasajero: " + nombreUser + "\nDNI: " + dniUser);
        binding.tvTotalPagar.setText("S/ " + String.format("%.2f", precio));

        // Mostrar datos mock si no se pasaron por intent (para probar UI)
        binding.tvResumenRuta.setText(rutaStr);
        binding.tvResumenFecha.setText(fechaStr);

        // 4. Listener del Botón Pagar
        binding.btnPagarFinal.setOnClickListener(v -> {
            validarYPagar();
        });
    }

    private void validarYPagar() {
        // 1. Validar que se haya seleccionado un método de pago
        int selectedId = binding.rgMetodoPago.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Toast.makeText(this, "Por favor selecciona un método de pago", Toast.LENGTH_SHORT).show();
            return;
        }

        String metodoPago = "";
        if (selectedId == R.id.rbYape) metodoPago = "YAPE";
        else if (selectedId == R.id.rbTarjeta) metodoPago = "TARJETA";
        else if (selectedId == R.id.rbEfectivo) metodoPago = "EFECTIVO";

        // 2. Preparar datos del pasajero (Para este MVP usamos el usuario logueado)
        // Si quieres permitir comprar para otros, necesitarías un formulario previo de datos de pasajero
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

        // Creamos el objeto Request con todos los datos necesarios para la transacción
        RequestCompra request = new RequestCompra(horarioId, asiento, piso, precio, nom, ape, dni, metodo);

        api.comprarPasaje(token, request).enqueue(new Callback<ResponseCompra>() {
            @Override
            public void onResponse(Call<ResponseCompra> call, Response<ResponseCompra> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // ¡ÉXITO!
                    Toast.makeText(ConfirmarCompraActivity.this, "¡Compra realizada con éxito!", Toast.LENGTH_LONG).show();

                    // Navegar al Home limpiando el stack para que no pueda volver atrás a "comprar" lo mismo
                    Intent intent = new Intent(ConfirmarCompraActivity.this, HomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    binding.btnPagarFinal.setEnabled(true);
                    binding.btnPagarFinal.setText("Confirmar y Pagar");
                    Toast.makeText(ConfirmarCompraActivity.this, "Error en la compra: Asiento ocupado o fallo de servidor", Toast.LENGTH_SHORT).show();
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