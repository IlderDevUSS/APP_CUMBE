package com.example.app_cumbe;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.app_cumbe.api.ApiClient;
import com.example.app_cumbe.api.ApiService;
import com.example.app_cumbe.model.Encomienda;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.app_cumbe.LoginActivity.SP_NAME;

public class TrackingActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView tvWelcomeUser, tvUserDni;
    private TabLayout tabLayout;
    private RecyclerView rvEnviadas, rvPorRecibir;
    private FrameLayout listContainer;

    // Adaptadores y Listas
    private EncomiendaAdapter adapterEnviadas;
    private EncomiendaAdapter adapterPorRecibir;
    private List<Encomienda> listaEnviadas = new ArrayList<>();
    private List<Encomienda> listaPorRecibir = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);

        initViews();
        setupToolbar();
        cargarDatosUsuario();

        // Configuramos RecyclerViews vacíos primero
        setupRecyclerViews();

        // Cargamos datos de la API
        cargarEncomiendas();

        // Configuramos pestañas
        setupTabs();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbarTracking);
        tvWelcomeUser = findViewById(R.id.tvWelcomeUser);
        tvUserDni = findViewById(R.id.tvUserDni);
        tabLayout = findViewById(R.id.tabLayout);
        rvEnviadas = findViewById(R.id.rvEnviadas);
        rvPorRecibir = findViewById(R.id.rvPorRecibir);
        listContainer = findViewById(R.id.listContainer);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void cargarDatosUsuario() {
        SharedPreferences prefs = getSharedPreferences(SP_NAME, MODE_PRIVATE);
        String userName = prefs.getString("USER_NAME", "Usuario");
        String userDni = prefs.getString("USER_DNI", "---");

        tvWelcomeUser.setText("Bienvenid@, " + userName);
        tvUserDni.setText("En este apartado puedes visualizar todas las encomiendas enviadas o por recibir con el DNI " + userDni);
    }

    private void setupRecyclerViews() {

        SharedPreferences prefs = getSharedPreferences(SP_NAME, MODE_PRIVATE);
        String miDni = prefs.getString("USER_DNI", "");

        // Lista Enviadas
        adapterEnviadas = new EncomiendaAdapter(this, listaEnviadas, miDni);
        rvEnviadas.setLayoutManager(new LinearLayoutManager(this));
        rvEnviadas.setAdapter(adapterEnviadas);

        // Lista Por Recibir
        adapterPorRecibir = new EncomiendaAdapter(this, listaPorRecibir, miDni);
        rvPorRecibir.setLayoutManager(new LinearLayoutManager(this));
        rvPorRecibir.setAdapter(adapterPorRecibir);
    }

    private void cargarEncomiendas() {
        SharedPreferences prefs = getSharedPreferences(SP_NAME, MODE_PRIVATE);
        String token = prefs.getString("USER_TOKEN", null);
        // Necesitamos el DNI del usuario actual para filtrar
        // (Aunque la API ya filtra por remitente O destinatario, nosotros debemos separar las listas)
        String miDni = prefs.getString("USER_DNI", "");

        if (token == null) {
            Toast.makeText(this, "Error de sesión", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ApiService apiService = ApiClient.getApiService();
        Call<List<Encomienda>> call = apiService.getEncomiendas(token);

        call.enqueue(new Callback<List<Encomienda>>() {
            @Override
            public void onResponse(Call<List<Encomienda>> call, Response<List<Encomienda>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    filtrarYMostrarDatos(response.body(), miDni);
                } else {
                    Toast.makeText(TrackingActivity.this, "No se encontraron encomiendas", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Encomienda>> call, Throwable t) {
                Toast.makeText(TrackingActivity.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Separa la lista completa en "Enviadas" y "Por Recibir"
     * basándose en el DNI del usuario.
     */
    private void filtrarYMostrarDatos(List<Encomienda> listaCompleta, String miDni) {
        listaEnviadas.clear();
        listaPorRecibir.clear();

        // La clase Encomienda no tiene el campo "remitente_dni" en el modelo Java aún.
        // IMPORTANTE: Para que este filtro funcione perfecto, deberíamos añadir
        // 'remitente_dni' y 'destinatario_dni' al modelo Encomienda.java.
        //
        // POR AHORA (Solución temporal si no quieres cambiar el modelo):
        // Asumiremos que si el origen es MI ciudad (ej. Cajamarca) es Enviada, si no Recibida.
        // O mejor: Mostramos todas en ambas listas hasta que actualicemos el modelo.

        // TODO: Actualizar modelo Encomienda.java con 'remitente_dni' para filtrar correctamente.

        // Simulación de filtro (Añade todas a ambas para que veas que carga)
        for (Encomienda e : listaCompleta) {
            if (e.getRemitenteDni() != null && e.getRemitenteDni().equals(miDni)) {
                listaEnviadas.add(e);
            } else {
                // Si no soy el remitente, asumo que soy el destinatario
                listaPorRecibir.add(e);
            }
        }

        // Notificar cambios a los adaptadores
        adapterEnviadas.notifyDataSetChanged();
        adapterPorRecibir.notifyDataSetChanged();
    }

    private void setupTabs() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    rvEnviadas.setVisibility(View.VISIBLE);
                    rvPorRecibir.setVisibility(View.GONE);
                } else {
                    rvEnviadas.setVisibility(View.GONE);
                    rvPorRecibir.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }
}