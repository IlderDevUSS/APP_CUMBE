package com.example.app_cumbe;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.example.app_cumbe.api.ApiClient;
import com.example.app_cumbe.api.ApiService;
import com.example.app_cumbe.databinding.ActivityConfirmarCompraBinding;
import com.example.app_cumbe.model.RequestCompra;
import com.example.app_cumbe.model.ResponseCompra;
import com.example.app_cumbe.model.db.AppDatabase;
import com.example.app_cumbe.model.db.NotificacionEntity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.app_cumbe.LoginActivity.SP_NAME;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ConfirmarCompraActivity extends AppCompatActivity {

    private ActivityConfirmarCompraBinding binding;

    // Datos del viaje
    private int horarioId, asiento, piso;
    private double precio = 40.00;
    private String rutaStr = "";
    private String fechaStr = "";

    private String servicioBus = "";
    private String misNombres, misApellidos, miDni, miCelular;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityConfirmarCompraBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Intent i = getIntent();
        servicioBus = i.getStringExtra("SERVICIO");
        if (servicioBus == null) servicioBus = "Estándar";
        recibirDatosIntent();
        cargarDatosUsuarioSesion();
        setupUI();
        setupListeners();
    }

    private void recibirDatosIntent() {
        Intent i = getIntent();
        horarioId = i.getIntExtra("HORARIO_ID", 0);
        asiento = i.getIntExtra("ASIENTO", 0);
        piso = i.getIntExtra("PISO", 1);

        if (i.hasExtra("PRECIO")) precio = i.getDoubleExtra("PRECIO", 40.0);
        rutaStr = i.getStringExtra("RUTA");
        if (rutaStr == null) rutaStr = "";
        fechaStr = i.getStringExtra("FECHA");
        if (fechaStr == null) fechaStr = "";

    }

    private void cargarDatosUsuarioSesion() {
        SharedPreferences prefs = getSharedPreferences(SP_NAME, MODE_PRIVATE);
        String nombreCompleto = prefs.getString("USER_NAME", "");
        miDni = prefs.getString("USER_DNI", "");
        miCelular = prefs.getString("USER_PHONE", ""); // Asegúrate de guardar esto en LoginActivity

        // Separar nombre completo en Nombres y Apellidos (Lógica básica)
        String[] partes = nombreCompleto.split(" ");
        if (partes.length > 2) {
            // Ejemplo: "Juan Carlos" "Perez Lopez"
            misNombres = partes[0] + " " + partes[1];
            misApellidos = "";
            for(int k=2; k<partes.length; k++) misApellidos += partes[k] + " ";
        } else if (partes.length == 2) {
            misNombres = partes[0];
            misApellidos = partes[1];
        } else {
            misNombres = nombreCompleto;
            misApellidos = "";
        }
        misApellidos = misApellidos.trim();
    }

    private void setupUI() {
        // Llenar resumen visual
        binding.tvResumenAsiento.setText("#" + asiento);
        binding.tvResumenPiso.setText(String.valueOf(piso));
        binding.tvTotalPagar.setText("S/ " + String.format("%.2f", precio));
        binding.tvResumenRuta.setText(rutaStr.isEmpty() ? "Ruta Seleccionada" : rutaStr);
        binding.tvResumenFecha.setText(fechaStr.isEmpty() ? "Fecha del Viaje" : fechaStr);

        // Estado inicial del formulario (Marcado "Soy yo")
        llenarFormularioConMisDatos();
    }

    private void setupListeners() {
        // Lógica del Checkbox
        binding.cbSoyPasajero.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                llenarFormularioConMisDatos();
                habilitarEdicion(false); // Bloquear campos para no editar datos del perfil por error
            } else {
                limpiarFormulario();
                habilitarEdicion(true);
            }
        });

        binding.btnPagarFinal.setOnClickListener(v -> validarYPagar());
    }

    private void llenarFormularioConMisDatos() {
        binding.etPasajeroDni.setText(miDni);
        binding.etPasajeroNombres.setText(misNombres);
        binding.etPasajeroApellidos.setText(misApellidos);
        binding.etPasajeroCelular.setText(miCelular);
    }

    private void limpiarFormulario() {
        binding.etPasajeroDni.setText("");
        binding.etPasajeroNombres.setText("");
        binding.etPasajeroApellidos.setText("");
        binding.etPasajeroCelular.setText("");
        binding.etPasajeroDni.requestFocus();
    }

    private void habilitarEdicion(boolean habilitar) {
        // Si quieres que siempre se pueda editar (incluso siendo "yo"), borra este método
        // y las llamadas a él. Si prefieres bloquearlo:
        binding.etPasajeroDni.setEnabled(habilitar);
        binding.etPasajeroNombres.setEnabled(habilitar);
        binding.etPasajeroApellidos.setEnabled(habilitar);
        binding.etPasajeroCelular.setEnabled(habilitar);
    }

    private void validarYPagar() {
        // 1. Obtener datos del formulario
        String dni = binding.etPasajeroDni.getText().toString().trim();
        String nombres = binding.etPasajeroNombres.getText().toString().trim();
        String apellidos = binding.etPasajeroApellidos.getText().toString().trim();
        String celular = binding.etPasajeroCelular.getText().toString().trim();

        // 2. Validaciones
        if (dni.isEmpty() || dni.length() != 8) {
            binding.etPasajeroDni.setError("DNI inválido (8 dígitos)");
            return;
        }
        if (nombres.isEmpty()) {
            binding.etPasajeroNombres.setError("Ingrese nombres");
            return;
        }
        if (apellidos.isEmpty()) {
            binding.etPasajeroApellidos.setError("Ingrese apellidos");
            return;
        }

        // 3. Método de Pago
        int selectedId = binding.rgMetodoPago.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Toast.makeText(this, "Selecciona un método de pago", Toast.LENGTH_SHORT).show();
            return;
        }
        String metodoPago = "";
        if (selectedId == R.id.rbYape) metodoPago = "YAPE";
        else if (selectedId == R.id.rbTarjeta) metodoPago = "TARJETA";
        else if (selectedId == R.id.rbEfectivo) metodoPago = "EFECTIVO";

        // 4. Proceder a la compra
        realizarCompra(nombres, apellidos, dni, celular, metodoPago);
    }

    private void realizarCompra(String nom, String ape, String dni, String cel, String metodo) {
        binding.btnPagarFinal.setEnabled(false);
        binding.btnPagarFinal.setText("Procesando...");

        ApiService api = ApiClient.getApiService();
        SharedPreferences prefs = getSharedPreferences(SP_NAME, MODE_PRIVATE);
        String token = prefs.getString("USER_TOKEN", "");

        // Usamos el constructor actualizado con 'celular'
        RequestCompra request = new RequestCompra(horarioId, asiento, piso, precio, nom, ape, dni, cel, metodo);

        api.comprarPasaje(token, request).enqueue(new Callback<ResponseCompra>() {
            @Override
            public void onResponse(Call<ResponseCompra> call, Response<ResponseCompra> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Intent intent = new Intent(ConfirmarCompraActivity.this, TicketActivity.class);
                    intent.putExtra("PASAJE_ID", response.body().getPasajeId());
                    intent.putExtra("TRANSACCION_ID", response.body().getTransaccionId());
                    intent.putExtra("RUTA", rutaStr);
                    intent.putExtra("FECHA", fechaStr);
                    intent.putExtra("ASIENTO", asiento);
                    intent.putExtra("PRECIO", precio);
                    intent.putExtra("PASAJERO_NOMBRE", nom+ " " + ape);
                    intent.putExtra("PASAJERO_DNI", dni);
                    intent.putExtra("SERVICIO", servicioBus);
                    intent.putExtra("PASAJERO_CELULAR", cel);

                        // --- NUEVO: Crear Notificación Local ---
                    AppDatabase db = AppDatabase.getDatabase(ConfirmarCompraActivity.this);
                        NotificacionEntity notifCompra = new NotificacionEntity(
                                "Compra Exitosa",
                                "Has comprado tu pasaje a " + rutaStr + " correctamente. Asiento: " + asiento,
                                "COMPRA",
                                new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date())
                        );
                        notifCompra.referenciaId = response.body().getPasajeId();
                        notifCompra.origenReferencia = "HORARIO";
                        db.notificacionDao().insertar(notifCompra);
                        // ---------------------------------------

                    startActivity(intent);
                    finish();
                } else {
                    binding.btnPagarFinal.setEnabled(true);
                    binding.btnPagarFinal.setText("Confirmar y Pagar");
                    Toast.makeText(ConfirmarCompraActivity.this, "Error en la compra: " + response.message(), Toast.LENGTH_SHORT).show();
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