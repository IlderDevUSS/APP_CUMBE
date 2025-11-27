package com.example.app_cumbe;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.app_cumbe.api.ApiClient;
import com.example.app_cumbe.api.ApiService;
import com.example.app_cumbe.model.RutaConductor;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetalleRutaActivity extends AppCompatActivity {

    private TextView tvTitulo, tvPasajeros;
    private Button btnIniciar, btnFinalizar, btnReporte;
    private RutaConductor ruta;
    private ApiService apiService;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_ruta);

        apiService = ApiClient.getApiService();
        prefs = getSharedPreferences(LoginActivity.SP_NAME, Context.MODE_PRIVATE);

        setupToolbar();
        initViews();

        if (getIntent().hasExtra("RUTA_DATA")) {
            ruta = (RutaConductor) getIntent().getSerializableExtra("RUTA_DATA");
            setupUI();
        } else {
            Toast.makeText(this, "Error: Datos no recibidos", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Gestión de Ruta");
            // Establecer explícitamente el icono de navegación
            toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void initViews() {
        tvTitulo = findViewById(R.id.tvDetalleRutaTitulo);
        tvPasajeros = findViewById(R.id.tvDetallePasajeros);
        btnIniciar = findViewById(R.id.btnIniciarViaje);
        btnFinalizar = findViewById(R.id.btnFinalizarViaje);
        btnReporte = findViewById(R.id.btnCrearReporte);
    }

    private void setupUI() {
        tvTitulo.setText(ruta.getOrigen() + " ➝ " + ruta.getDestino());
        tvPasajeros.setText("Pasajeros registrados: " + ruta.getCantidadPasajeros());

        // Configurar botones de estado
        actualizarBotonesEstado(ruta.getEstado());

        // Configurar botón de reporte (Independiente del estado de viaje)
        actualizarBotonReporte();

        btnIniciar.setOnClickListener(v -> cambiarEstadoViaje("EN_RUTA"));
        btnFinalizar.setOnClickListener(v -> cambiarEstadoViaje("FINALIZADO"));
        // Aquí usamos el nuevo método para mostrar el BottomSheet
        btnReporte.setOnClickListener(v -> mostrarFormularioReporte());
    }

    private void actualizarBotonesEstado(String estado) {
        if ("PROGRAMADO".equalsIgnoreCase(estado)) {
            btnIniciar.setEnabled(true);
            btnIniciar.setAlpha(1.0f);
            btnFinalizar.setEnabled(false);
            btnFinalizar.setAlpha(0.5f);
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
            btnFinalizar.setAlpha(0.5f);
        }
    }

    private void actualizarBotonReporte() {
        // El reporte solo se deshabilita si YA se hizo uno
        if (ruta.tieneReporte()) {
            btnReporte.setEnabled(false);
            btnReporte.setText("Reporte Enviado");
            btnReporte.setAlpha(0.5f);
        } else {
            btnReporte.setEnabled(true);
            btnReporte.setText("Crear Reporte");
            btnReporte.setAlpha(1.0f);
        }
    }

    private void cambiarEstadoViaje(String nuevoEstado) {
        Call<Void> call = apiService.actualizarEstadoRuta(ruta.getHorarioId(), nuevoEstado);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(DetalleRutaActivity.this, "Estado actualizado", Toast.LENGTH_SHORT).show();
                    ruta.setEstado(nuevoEstado);
                    actualizarBotonesEstado(nuevoEstado);
                    // No cerramos la actividad automáticamente si finaliza, para permitir ver info
                } else {
                    Toast.makeText(DetalleRutaActivity.this, "Error al actualizar", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(DetalleRutaActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Método actualizado para usar ReporteBottomSheet
    private void mostrarFormularioReporte() {
        ReporteBottomSheet bottomSheet = new ReporteBottomSheet();
        bottomSheet.setListener((tipo, descripcion, costo) -> {
            // Al recibir los datos del BottomSheet, enviamos a la API
            enviarReporteApi(tipo, descripcion, costo);
        });
        bottomSheet.show(getSupportFragmentManager(), "ReporteSheet");
    }

    private void enviarReporteApi(String tipo, String descripcion, double costo) {
        // 1. Obtenemos el ID EXCLUSIVAMENTE de la ruta cargada
        int conductorId = ruta.getConductorId();

        if (conductorId <= 0) {
            Toast.makeText(this, "Error: No se ha cargado la información del conductor en esta ruta.", Toast.LENGTH_LONG).show();
            return;
        }

        if (ruta.getBusId() <= 0) {
            Toast.makeText(this, "Error: ID de Bus inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        // 3. Llamamos al servicio.
        // Asegúrate de que el ApiService use el nombre de campo correcto ("conductor_id")
        Call<Void> call = apiService.crearReporteBus(
                ruta.getBusId(),
                tipo,
                descripcion,
                conductorId, // Enviamos el ID correcto (ej. 2)
                costo
        );

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(DetalleRutaActivity.this, "Reporte registrado correctamente", Toast.LENGTH_LONG).show();
                    ruta.setTieneReporte(true);
                    actualizarBotonReporte();
                } else {
                    Toast.makeText(DetalleRutaActivity.this, "Error servidor: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(DetalleRutaActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }
}