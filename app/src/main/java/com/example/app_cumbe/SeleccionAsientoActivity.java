package com.example.app_cumbe;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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

    // Variables de datos con valores por defecto, pero que se sobrescribirán
    private int horarioId = 0;
    private int totalAsientos = 40;
    private int numPisos = 2;
    private AsientoBus asientoSeleccionado = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySeleccionAsientoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 1. RECIBIR DATOS DEL INTENT (CRUCIAL HACERLO PRIMERO)
        if (getIntent() != null) {
            horarioId = getIntent().getIntExtra("HORARIO_ID", 0);
            // Leemos los valores enviados. Si no vienen, usamos los default (40 y 2)
            totalAsientos = getIntent().getIntExtra("TOTAL_ASIENTOS", 40);
            numPisos = getIntent().getIntExtra("NUM_PISOS", 2);
        }

        // Configuración inicial de UI
        setupToolbar();
        setupTabs();

        // 2. Generar los asientos "en blanco" con los datos CORRECTOS
        generarAsientosLogica();

        // 3. Configurar el RecyclerView
        setupRecyclerView();

        // 4. Llamar a la API para ver cuáles están ocupados realmente
        cargarAsientosOcupados();
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
                // Al cambiar de pestaña, filtramos la lista visual
                // tab.getPosition() devuelve 0 para el primer tab (Piso 1)
                filtrarPorPiso(tab.getPosition() + 1);
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void generarAsientosLogica() {
        listaCompleta.clear();
        // Generamos los asientos dinámicamente basado en el total recibido
        for (int i = 1; i <= totalAsientos; i++) {
            // Regla de negocio: Asientos 1-12 son Piso 1, el resto Piso 2 (si hay 2 pisos)
            int piso = (numPisos == 2 && i <= 12) ? 1 : 2;

            // Si solo hay 1 piso, todos son piso 1
            if (numPisos == 1) piso = 1;

            listaCompleta.add(new AsientoBus(i, piso, "LIBRE"));
        }
    }

    private void cargarAsientosOcupados() {
        if (horarioId == 0) {
            filtrarPorPiso(1);
            return;
        }

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
                // Refrescamos la vista (Piso 1 por defecto)
                filtrarPorPiso(1);
            }

            @Override
            public void onFailure(Call<List<AsientoOcupado>> call, Throwable t) {
                Toast.makeText(SeleccionAsientoActivity.this, "Error al cargar ocupados", Toast.LENGTH_SHORT).show();
                filtrarPorPiso(1);
            }
        });
    }

    private void marcarOcupados(List<AsientoOcupado> ocupados) {
        for (AsientoOcupado ocupado : ocupados) {
            // Buscamos el asiento en la lista completa por su número
            // (Asumimos que están ordenados, index = numero - 1)
            int numeroAsiento = ocupado.getNumero();
            int index = numeroAsiento - 1;

            if (index >= 0 && index < listaCompleta.size()) {
                listaCompleta.get(index).setEstado("OCUPADO");
            }
        }
    }

    private void filtrarPorPiso(int piso) {
        listaVisual.clear();

        List<AsientoBus> asientosPiso = new ArrayList<>();
        for (AsientoBus a : listaCompleta) {
            if (a.getPiso() == piso) {
                asientosPiso.add(a);
            }
        }

        // Lógica visual para grilla (2 izquierda - pasillo - 2 derecha)
        int contadorColumna = 0;
        for (AsientoBus asiento : asientosPiso) {
            // Si estamos en la columna 3 (índice 2), agregamos un hueco (pasillo)
            if (contadorColumna == 2) {
                listaVisual.add(new AsientoBus(true)); // Es pasillo
                contadorColumna++;
            }

            listaVisual.add(asiento);
            contadorColumna++;

            // Reiniciar contador al completar la fila de 5 columnas
            if (contadorColumna >= 5) contadorColumna = 0;
        }

        if (adapter != null) {
            // IMPORTANTE: Usamos notifyDataSetChanged porque la estructura de la lista cambió
            adapter.notifyDataSetChanged();
        }
    }

    private void setupRecyclerView() {
        // Grid de 5 columnas (2 asientos + 1 pasillo + 2 asientos)
        binding.rvAsientos.setLayoutManager(new GridLayoutManager(this, 5));

        adapter = new AsientoAdapter(this, listaVisual, asiento -> {
            seleccionarAsiento(asiento);
        });
        binding.rvAsientos.setAdapter(adapter);
    }

    private void seleccionarAsiento(AsientoBus nuevoAsiento) {
        // 1. Desmarcar el anterior si existía
        if (asientoSeleccionado != null) {
            // Solo lo ponemos LIBRE si no estaba ocupado (aunque la UI no deja clicar ocupados)
            if (!asientoSeleccionado.getEstado().equals("OCUPADO")) {
                asientoSeleccionado.setEstado("LIBRE");
            }
        }

        // 2. Marcar el nuevo
        // Buscamos la referencia real en la lista completa para que el cambio persista al cambiar de piso
        // (Aunque aquí trabajamos con referencias, es más seguro buscar por ID si hay dudas)
        asientoSeleccionado = nuevoAsiento;
        asientoSeleccionado.setEstado("SELECCIONADO");

        // 3. Refrescar UI
        adapter.notifyDataSetChanged();

        // 4. Actualizar botón
        binding.btnConfirmarAsiento.setEnabled(true);
        binding.btnConfirmarAsiento.setText("Comprar Asiento #" + nuevoAsiento.getNumero());

        binding.btnConfirmarAsiento.setOnClickListener(v -> {
            Intent intent = new Intent(SeleccionAsientoActivity.this, ConfirmarCompraActivity.class);
            intent.putExtra("HORARIO_ID", horarioId);
            intent.putExtra("ASIENTO", nuevoAsiento.getNumero());
            intent.putExtra("PISO", nuevoAsiento.getPiso());
            // Opcional: Pasa el precio si lo tienes disponible en esta actividad
            // intent.putExtra("PRECIO", precioDelViaje);

            startActivity(intent);
        });
    }
}