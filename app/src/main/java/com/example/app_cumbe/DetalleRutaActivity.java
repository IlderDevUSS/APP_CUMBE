package com.example.app_cumbe;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.app_cumbe.api.ApiClient;
import com.example.app_cumbe.api.ApiService;
import com.example.app_cumbe.model.RutaConductor;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetalleRutaActivity extends AppCompatActivity {

    private TextView tvTitulo, tvPasajeros, tvEstadoActual;
    private Button btnIniciar, btnFinalizar, btnReporte;
    private RutaConductor ruta;
    private ApiService apiService;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_ruta);

        // 1. Inicializar API y Preferencias
        apiService = ApiClient.getApiService();
        prefs = getSharedPreferences(LoginActivity.SP_NAME, Context.MODE_PRIVATE);

        // 2. Vincular Vistas
        tvTitulo = findViewById(R.id.tvDetalleRutaTitulo);
        tvPasajeros = findViewById(R.id.tvDetallePasajeros);
        // Nota: Agregué tvDriverStatus en el layout anterior? Si no, usa un Toast o busca el ID correcto.
        // Asumiremos que reutilizamos botones para mostrar estado visualmente.

        btnIniciar = findViewById(R.id.btnIniciarViaje);
        btnFinalizar = findViewById(R.id.btnFinalizarViaje);
        btnReporte = findViewById(R.id.btnCrearReporte);

        // 3. Obtener datos del Intent
        if (getIntent().hasExtra("RUTA_DATA")) {
            ruta = (RutaConductor) getIntent().getSerializableExtra("RUTA_DATA");
            setupUI();
        } else {
            Toast.makeText(this, "Error: No se recibieron datos de la ruta", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void setupUI() {
        // Llenar información básica
        tvTitulo.setText(ruta.getOrigen() + " ➝ " + ruta.getDestino());
        tvPasajeros.setText("Pasajeros registrados: " + ruta.getCantidadPasajeros());

        // Configurar estado inicial de los botones
        actualizarBotonesSegunEstado(ruta.getEstado());

        // Listeners para acciones
        btnIniciar.setOnClickListener(v -> cambiarEstadoViaje("EN_RUTA"));
        btnFinalizar.setOnClickListener(v -> cambiarEstadoViaje("FINALIZADO"));
        btnReporte.setOnClickListener(v -> mostrarDialogoReporte());
    }

    private void actualizarBotonesSegunEstado(String estado) {
        // Lógica visual para bloquear botones según el estado actual
        if ("PROGRAMADO".equalsIgnoreCase(estado)) {
            btnIniciar.setEnabled(true);
            btnIniciar.setAlpha(1.0f);

            btnFinalizar.setEnabled(false);
            btnFinalizar.setAlpha(0.5f); // Efecto visual de deshabilitado

        } else if ("EN_RUTA".equalsIgnoreCase(estado)) {
            btnIniciar.setEnabled(false);
            btnIniciar.setText("Viaje en Curso");
            btnIniciar.setAlpha(0.5f);

            btnFinalizar.setEnabled(true);
            btnFinalizar.setAlpha(1.0f);

        } else if ("FINALIZADO".equalsIgnoreCase(estado)) {
            btnIniciar.setEnabled(false);
            btnIniciar.setText("Viaje Finalizado");
            btnFinalizar.setEnabled(false);
            btnReporte.setEnabled(false); // Ya no se pueden reportar fallas si acabó
        }
    }

    private void cambiarEstadoViaje(String nuevoEstado) {
        Call<Void> call = apiService.actualizarEstadoRuta(ruta.getHorarioId(), nuevoEstado);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(DetalleRutaActivity.this, "Estado actualizado a: " + nuevoEstado, Toast.LENGTH_SHORT).show();
                    ruta.setEstado(nuevoEstado); // Actualizamos el objeto local
                    actualizarBotonesSegunEstado(nuevoEstado); // Actualizamos la UI

                    if("FINALIZADO".equals(nuevoEstado)){
                        finish(); // Cerramos la pantalla si terminó
                    }
                } else {
                    Toast.makeText(DetalleRutaActivity.this, "Error al actualizar estado", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(DetalleRutaActivity.this, "Fallo de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarDialogoReporte() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Reportar Incidencia");

        // Crear layout del diálogo programáticamente para no crear otro XML
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        // 1. Selector de Tipo
        final Spinner spinnerTipo = new Spinner(this);
        String[] tipos = {"Falla Mecánica", "Retraso por Tráfico", "Accidente", "Incidente con Pasajero", "Otro"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tipos);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipo.setAdapter(adapter);
        layout.addView(spinnerTipo);

        // 2. Campo de Descripción
        final EditText inputDescripcion = new EditText(this);
        inputDescripcion.setHint("Describa el detalle aquí...");
        inputDescripcion.setHeight(300); // Altura mínima para que escriba cómodo
        inputDescripcion.setGravity(android.view.Gravity.TOP);
        layout.addView(inputDescripcion);

        builder.setView(layout);

        // Botones del diálogo
        builder.setPositiveButton("Enviar Reporte", (dialog, which) -> {
            String tipo = spinnerTipo.getSelectedItem().toString();
            String descripcion = inputDescripcion.getText().toString();

            if (!descripcion.trim().isEmpty()) {
                enviarReporteApi(tipo, descripcion);
            } else {
                Toast.makeText(this, "La descripción es obligatoria", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void enviarReporteApi(String tipo, String descripcion) {
        // 1. OBTENER EL ID DIRECTAMENTE DEL OBJETO RUTA
        // Ya no lo sacamos de SharedPreferences, porque LoginActivity no lo guardaba.
        int conductorId = ruta.getConductorId();

        // Validaciones de seguridad
        if (conductorId <= 0) {
            Toast.makeText(this, "Error: No se pudo identificar al conductor", Toast.LENGTH_SHORT).show();
            return;
        }

        if (ruta.getBusId() <= 0) {
            Toast.makeText(this, "Error: No se pudo identificar el bus", Toast.LENGTH_SHORT).show();
            return;
        }

        // Llamada a la API (tu interfaz ApiService ya estaba bien)
        Call<Void> call = apiService.crearReporteBus(ruta.getBusId(), tipo, descripcion, conductorId);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(DetalleRutaActivity.this, "Reporte enviado correctamente", Toast.LENGTH_LONG).show();
                    // Opcional: limpiar el diálogo o cerrar
                } else {
                    Toast.makeText(DetalleRutaActivity.this, "Error al enviar reporte: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(DetalleRutaActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }
}