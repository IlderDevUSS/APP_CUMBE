package com.example.app_cumbe;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class TrackingActivity extends AppCompatActivity {

    Toolbar toolbar;
    TextView tvWelcomeUser, tvUserDni;
    TabLayout tabLayout;
    RecyclerView rvEnviadas, rvPorRecibir;

    EncomiendaAdapter adapterEnviadas;
    EncomiendaAdapter adapterPorRecibir;
    List<Encomienda> listaCompleta = new ArrayList<>();
    List<Encomienda> listaEnviadas = new ArrayList<>();
    List<Encomienda> listaPorRecibir = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);


        toolbar = findViewById(R.id.toolbarTracking);
        tvWelcomeUser = findViewById(R.id.tvWelcomeUser);
        tvUserDni = findViewById(R.id.tvUserDni);
        tabLayout = findViewById(R.id.tabLayout);
        rvEnviadas = findViewById(R.id.rvEnviadas);
        rvPorRecibir = findViewById(R.id.rvPorRecibir);


        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish()); // Flecha "atrás" cierra la pantalla


        cargarDatosDeUsuario(); // TODO: Reemplazar con datos reales del Login
        cargarDatosDePrueba(); // TODO: Reemplazar con datos de la BD


        setupRecyclerViews();


        setupTabs();
    }

    private void cargarDatosDeUsuario() {

        tvWelcomeUser.setText("Bienvenid@, Usuario de Prueba");
        tvUserDni.setText("Tus encomiendas registradas con el DNI 12345678");
    }

    private void cargarDatosDePrueba() {
        // Creamos datos falsos para probar la interfaz
        listaCompleta.add(new Encomienda("TRX70251", "Chiclayo", "Jaén", "Juan Pérez", true, 4, "ENTREGADO", "01/10 08:30", "01/10 10:00", "02/10 09:00", "02/10 10:30"));
        listaCompleta.add(new Encomienda("TRX70252", "Chiclayo", "Cajamarca", "Ana Rodríguez", true, 2, "EN CAMINO", "06/10 10:30", "06/10 11:30", null, null));
        listaCompleta.add(new Encomienda("TRX70253", "Cajamarca", "Chiclayo", "Usuario de Prueba", false, 3, "EN DESTINO", "05/10 15:00", "05/10 21:00", "06/10 07:00", null));
        listaCompleta.add(new Encomienda("TRX70254", "Jaén", "Chiclayo", "Usuario de Prueba", false, 1, "RECIBIDO", "07/10 08:30", null, null, null));

        // Separamos las listas para cada pestaña
        for (Encomienda e : listaCompleta) {
            if (e.isEsEnviada()) {
                listaEnviadas.add(e);
            } else {
                listaPorRecibir.add(e);
            }
        }
    }

    private void setupRecyclerViews() {

        adapterEnviadas = new EncomiendaAdapter(this, listaEnviadas);
        rvEnviadas.setLayoutManager(new LinearLayoutManager(this));
        rvEnviadas.setAdapter(adapterEnviadas);


        adapterPorRecibir = new EncomiendaAdapter(this, listaPorRecibir);
        rvPorRecibir.setLayoutManager(new LinearLayoutManager(this));
        rvPorRecibir.setAdapter(adapterPorRecibir);
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

        // Asegurarse de que la primera pestaña (Enviadas) se muestre al inicio
        rvEnviadas.setVisibility(View.VISIBLE);
        rvPorRecibir.setVisibility(View.GONE);
    }
}