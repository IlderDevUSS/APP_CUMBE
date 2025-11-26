package com.example.app_cumbe;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.app_cumbe.api.ApiClient;
import com.example.app_cumbe.api.ApiService;
import com.example.app_cumbe.databinding.ActivitySeleccionAsientoBinding;
import com.example.app_cumbe.model.AsientoBus;
import com.example.app_cumbe.model.AsientoOcupado;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.app_cumbe.LoginActivity.SP_NAME;

public class SeleccionAsientoActivity extends AppCompatActivity {

    private ActivitySeleccionAsientoBinding binding;
    private AsientoAdapter adapter;

    // Listas para manejo lógico y visual
    private List<AsientoBus> listaCompleta = new ArrayList<>();
    private List<AsientoBus> listaVisual = new ArrayList<>();

    // Variables de datos del bus
    private int horarioId = 0;
    private int totalAsientos = 40; // Valor por defecto
    private int numPisos = 2;       // Valor por defecto
    private String servicioBus = "";
    private double precioViaje = 0.0;
    private String rutaViaje = "";
    private String fechaViaje = "";

    // Guardamos el NÚMERO del asiento seleccionado
    private AsientoBus asientoSeleccionado = null;
    private int numeroAsientoSeleccionado = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySeleccionAsientoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 1. RECIBIR DATOS DEL INTENT
        if (getIntent() != null) {
            horarioId = getIntent().getIntExtra("HORARIO_ID", 0);

            int totalIntent = getIntent().getIntExtra("TOTAL_ASIENTOS", 0);
            int pisosIntent = getIntent().getIntExtra("NUM_PISOS", 0);
            servicioBus = getIntent().getStringExtra("SERVICIO");
            if (servicioBus == null) servicioBus = "Estándar";
            if (totalIntent > 0) totalAsientos = totalIntent;
            if (pisosIntent > 0) numPisos = pisosIntent;

            // Recibir los datos extra para pasar a confirmación
            precioViaje = getIntent().getDoubleExtra("PRECIO", 0.0);
            rutaViaje = getIntent().getStringExtra("RUTA");
            fechaViaje = getIntent().getStringExtra("FECHA");

            Log.d("SeleccionAsiento", "Datos recibidos -> ID: " + horarioId + ", Asientos: " + totalAsientos + ", Pisos: " + numPisos);
        }

        setupToolbar();
        setupTabs();

        // 2. Generar los asientos "en blanco"
        generarAsientosLogica();

        // 3. Configurar el RecyclerView
        setupRecyclerView();

        // 4. Llamar a la API
        cargarAsientosOcupados();

        // Cargar vista inicial (Piso 1)
        filtrarPorPiso(1);
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbarAsientos);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        binding.toolbarAsientos.setNavigationOnClickListener(v -> finish());
    }

    private void setupTabs() {
        if (numPisos == 1) {
            binding.tabPisos.setVisibility(View.GONE);
        }

        binding.tabPisos.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // Tab 0 = Piso 1, Tab 1 = Piso 2
                filtrarPorPiso(tab.getPosition() + 1);
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void generarAsientosLogica() {
        listaCompleta.clear();
        for (int i = 1; i <= totalAsientos; i++) {
            // Regla: Asientos 1-12 son Piso 1, el resto Piso 2 (si hay 2 pisos)
            int piso = (numPisos == 2 && i <= 12) ? 1 : 2;
            if (numPisos == 1) piso = 1; // Si es de 1 piso, todos son piso 1

            listaCompleta.add(new AsientoBus(i, piso, "LIBRE"));
        }
    }

    private void cargarAsientosOcupados() {
        if (horarioId == 0) return;

        SharedPreferences prefs = getSharedPreferences(SP_NAME, MODE_PRIVATE);
        String token = prefs.getString("USER_TOKEN", "");

        ApiService apiService = ApiClient.getApiService();
        Call<List<AsientoOcupado>> call = apiService.obtenerAsientosOcupados(token, horarioId);

        call.enqueue(new Callback<List<AsientoOcupado>>() {
            @Override
            public void onResponse(Call<List<AsientoOcupado>> call, Response<List<AsientoOcupado>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    marcarOcupados(response.body());
                }
                // Refrescamos la vista actual (sea cual sea el piso activo)
                int pisoActual = binding.tabPisos.getSelectedTabPosition() + 1;
                filtrarPorPiso(pisoActual);
            }

            @Override
            public void onFailure(Call<List<AsientoOcupado>> call, Throwable t) {
                Toast.makeText(SeleccionAsientoActivity.this, "Error al cargar ocupados", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void marcarOcupados(List<AsientoOcupado> ocupados) {
        for (AsientoOcupado ocupado : ocupados) {
            int index = ocupado.getNumero() - 1;
            if (index >= 0 && index < listaCompleta.size()) {
                listaCompleta.get(index).setEstado("OCUPADO");
            }
        }
    }

    private void filtrarPorPiso(int piso) {
        listaVisual.clear();

        // 1. Obtener asientos del piso
        List<AsientoBus> asientosPiso = new ArrayList<>();
        for (AsientoBus a : listaCompleta) {
            if (a.getPiso() == piso) {
                asientosPiso.add(a);
            }
        }

        // 2. Insertar Pasillos (Layout 2-1-2)
        int contadorColumna = 0;
        for (AsientoBus asiento : asientosPiso) {
            // Si toca columna 3 (índice 2), metemos pasillo
            if (contadorColumna == 2) {
                listaVisual.add(new AsientoBus(true));
                contadorColumna++;
            }

            listaVisual.add(asiento);
            contadorColumna++;

            if (contadorColumna >= 5) contadorColumna = 0;
        }

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private void setupRecyclerView() {
        binding.rvAsientos.setLayoutManager(new GridLayoutManager(this, 5));

        adapter = new AsientoAdapter(this, listaVisual, asiento -> {
            seleccionarAsiento(asiento);
        });
        binding.rvAsientos.setAdapter(adapter);
    }

    private void seleccionarAsiento(AsientoBus nuevoAsiento) {
        // Si ya estaba ocupado, no hacemos nada
        if (nuevoAsiento.getEstado().equals("OCUPADO")) return;

        // 1. Desmarcar el anterior en la LISTA COMPLETA
        if (asientoSeleccionado != null) {
            // Buscar por referencia o número en la lista completa
            int indexAnterior = asientoSeleccionado.getNumero() - 1;
            if (indexAnterior >= 0 && indexAnterior < listaCompleta.size()) {
                if (!listaCompleta.get(indexAnterior).getEstado().equals("OCUPADO")) {
                    listaCompleta.get(indexAnterior).setEstado("LIBRE");
                }
            }
        }

        // 2. Marcar el nuevo en la LISTA COMPLETA
        int indexNuevo = nuevoAsiento.getNumero() - 1;
        if (indexNuevo >= 0 && indexNuevo < listaCompleta.size()) {
            listaCompleta.get(indexNuevo).setEstado("SELECCIONADO");

            // Guardamos la referencia del objeto de la lista completa
            asientoSeleccionado = listaCompleta.get(indexNuevo);
            numeroAsientoSeleccionado = nuevoAsiento.getNumero();
        }

        // 3. Refrescar la vista actual
        int pisoActual = binding.tabPisos.getSelectedTabPosition() + 1;
        filtrarPorPiso(pisoActual);

        // 4. Actualizar Botón con precio
        binding.btnConfirmarAsiento.setEnabled(true);
        binding.btnConfirmarAsiento.setText("Comprar Asiento #" + numeroAsientoSeleccionado + " - S/ " + String.format("%.2f", precioViaje));

        binding.btnConfirmarAsiento.setOnClickListener(v -> {
            Intent intent = new Intent(SeleccionAsientoActivity.this, ConfirmarCompraActivity.class);

            intent.putExtra("HORARIO_ID", horarioId);
            intent.putExtra("ASIENTO", numeroAsientoSeleccionado);
            intent.putExtra("PISO", asientoSeleccionado.getPiso());
            intent.putExtra("SERVICIO", servicioBus);
            intent.putExtra("PRECIO", precioViaje);
            intent.putExtra("RUTA", rutaViaje);
            intent.putExtra("FECHA", fechaViaje);

            startActivity(intent);
        });
    }
}