package com.example.app_cumbe;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.example.app_cumbe.api.ApiClient;
import com.example.app_cumbe.api.ApiService;
import com.example.app_cumbe.databinding.ActivityComprarPasajeBinding;
import com.example.app_cumbe.model.Horario;
import com.google.android.material.tabs.TabLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.app_cumbe.LoginActivity.SP_NAME;

public class CompraPasajeActivity extends AppCompatActivity {

    private ActivityComprarPasajeBinding binding;
    private HorarioAdapter adapter;
    private List<Horario> listaHorarios = new ArrayList<>();

    // Datos base (podrían venir de API)
    private final String[] ciudadesBase = {"Jaén", "Chiclayo", "Cajamarca"};

    // Variables de estado
    private String origenSeleccionado = "";
    private String destinoSeleccionado = "";
    private String fechaSeleccionada = ""; // YYYY-MM-DD

    // Control para evitar bucles infinitos al actualizar spinners
    private boolean isUpdatingSpinners = false;

    // Variable para guardar el horario que el usuario tocó (Opcional si navegas directo)
    private Horario horarioSeleccionado = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityComprarPasajeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolbar();
        setupSpinners();
        setupTabs();
        setupRecyclerView();

        // Inicializar con fecha de hoy
        fechaSeleccionada = getFechaActual();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbarCompra);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        binding.toolbarCompra.setNavigationOnClickListener(v -> finish());
    }

    private void setupSpinners() {
        // 1. Configurar Spinner Origen (Siempre tiene todas las ciudades)
        List<String> listaOrigen = new ArrayList<>();
        listaOrigen.add("-- Seleccionar --");
        for (String ciudad : ciudadesBase) listaOrigen.add(ciudad);

        // Usamos layout personalizado para el item cerrado
        ArrayAdapter<String> adapterOrigen = new ArrayAdapter<>(this, R.layout.spinner_item, listaOrigen);
        // Usamos layout personalizado para el dropdown abierto
        adapterOrigen.setDropDownViewResource(R.layout.item_spinner_dropdown);
        binding.spinnerOrigen.setAdapter(adapterOrigen);

        // 2. Configurar Spinner Destino (Inicialmente todas)
        actualizarSpinnerDestino(null);

        // 3. Listeners
        binding.spinnerOrigen.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isUpdatingSpinners) return;

                String seleccion = parent.getItemAtPosition(position).toString();
                if (!seleccion.equals("-- Seleccionar --")) {
                    origenSeleccionado = seleccion;
                    // Al elegir origen, filtramos el destino para que no sea igual
                    actualizarSpinnerDestino(origenSeleccionado);
                } else {
                    origenSeleccionado = "";
                    actualizarSpinnerDestino(null); // Resetear destino
                }
                verificarYCargar();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        binding.spinnerDestino.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isUpdatingSpinners) return;

                String seleccion = parent.getItemAtPosition(position).toString();
                if (!seleccion.equals("-- Seleccionar --")) {
                    destinoSeleccionado = seleccion;
                } else {
                    destinoSeleccionado = "";
                }
                verificarYCargar();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    /**
     * Actualiza la lista del spinner de destino excluyendo la ciudad seleccionada en origen.
     */
    private void actualizarSpinnerDestino(String ciudadExcluir) {
        isUpdatingSpinners = true; // Bloquear listeners para evitar disparos falsos

        List<String> listaDestino = new ArrayList<>();
        listaDestino.add("-- Seleccionar --");

        for (String ciudad : ciudadesBase) {
            if (!ciudad.equals(ciudadExcluir)) {
                listaDestino.add(ciudad);
            }
        }

        ArrayAdapter<String> adapterDestino = new ArrayAdapter<>(this, R.layout.spinner_item, listaDestino);
        adapterDestino.setDropDownViewResource(R.layout.item_spinner_dropdown);
        binding.spinnerDestino.setAdapter(adapterDestino);

        // Intentar mantener la selección previa si aún es válida
        if (!destinoSeleccionado.isEmpty() && listaDestino.contains(destinoSeleccionado)) {
            int pos = adapterDestino.getPosition(destinoSeleccionado);
            binding.spinnerDestino.setSelection(pos);
        } else {
            binding.spinnerDestino.setSelection(0); // Volver a "-- Seleccionar --"
            destinoSeleccionado = "";
        }

        isUpdatingSpinners = false; // Desbloquear
    }

    private void setupTabs() {
        binding.tabFechas.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                if (position == 0) { // HOY
                    fechaSeleccionada = getFechaActual();
                    verificarYCargar();
                } else if (position == 1) { // MAÑANA
                    fechaSeleccionada = getFechaManana();
                    verificarYCargar();
                } else if (position == 2) { // CALENDARIO
                    mostrarDatePicker();
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {
                if (tab.getPosition() == 2) mostrarDatePicker();
            }
        });
    }

    private void mostrarDatePicker() {
        Calendar c = Calendar.getInstance();
        DatePickerDialog dpd = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            fechaSeleccionada = String.format(Locale.US, "%d-%02d-%02d", year, month + 1, dayOfMonth);
            verificarYCargar();
            Toast.makeText(this, "Fecha: " + fechaSeleccionada, Toast.LENGTH_SHORT).show();
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        dpd.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        dpd.show();
    }

    // --- AQUÍ ESTÁ LA LÓGICA QUE PEDÍAS ---
    private void setupRecyclerView() {
        // Configurar el adaptador
        adapter = new HorarioAdapter(this, listaHorarios, horario -> {
            // AL HACER CLICK EN UN HORARIO (Callback del Listener)

            // 1. Guardar referencia (opcional)
            horarioSeleccionado = horario;

            // 2. Navegar directamente a la siguiente pantalla
            Intent intent = new Intent(CompraPasajeActivity.this, SeleccionAsientoActivity.class);

            // Pasamos el ID del horario para cargar los asientos ocupados
            intent.putExtra("HORARIO_ID", horario.getId());

            // Pasamos configuración del bus para dibujar el mapa correctamente
            // Asegúrate de tener estos getters en tu modelo Horario.java
            intent.putExtra("TOTAL_ASIENTOS", horario.getTotalAsientos());
            intent.putExtra("NUM_PISOS", horario.getNumPisos());
            intent.putExtra("PRECIO", horario.getPrecio());


            startActivity(intent);
        });

        binding.rvHorarios.setLayoutManager(new LinearLayoutManager(this));
        binding.rvHorarios.setAdapter(adapter);
    }

    private void verificarYCargar() {
        // Validar que tengamos los 3 datos necesarios
        if (origenSeleccionado.isEmpty() || destinoSeleccionado.isEmpty() || fechaSeleccionada.isEmpty()) {
            // Estado inicial o incompleto
            binding.tvEmptyState.setVisibility(View.VISIBLE);
            binding.tvEmptyState.setText("Selecciona origen y destino para ver horarios.");
            binding.rvHorarios.setVisibility(View.GONE);
            return;
        }

        // Cargar automáticamente
        cargarHorariosAPI();
    }

    private void cargarHorariosAPI() {
        SharedPreferences prefs = getSharedPreferences(SP_NAME, MODE_PRIVATE);
        String token = prefs.getString("USER_TOKEN", "");

        ApiService apiService = ApiClient.getApiService();
        Call<List<Horario>> call = apiService.buscarHorarios(token, origenSeleccionado, destinoSeleccionado, fechaSeleccionada);

        binding.tvEmptyState.setText("Buscando horarios...");
        binding.tvEmptyState.setVisibility(View.VISIBLE);
        binding.rvHorarios.setVisibility(View.GONE);

        call.enqueue(new Callback<List<Horario>>() {
            @Override
            public void onResponse(Call<List<Horario>> call, Response<List<Horario>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaHorarios.clear();
                    listaHorarios.addAll(response.body());

                    // Resetear selección visual en el adaptador al cargar nuevos datos
                    adapter.resetSelection();

                    if (listaHorarios.isEmpty()) {
                        binding.tvEmptyState.setText("No hay viajes programados para esta ruta y fecha.");
                        binding.tvEmptyState.setVisibility(View.VISIBLE);
                        binding.rvHorarios.setVisibility(View.GONE);
                    } else {
                        binding.tvEmptyState.setVisibility(View.GONE);
                        binding.rvHorarios.setVisibility(View.VISIBLE);
                        adapter.notifyDataSetChanged();
                    }
                } else {
                    binding.tvEmptyState.setText("No se encontraron horarios.");
                }
            }

            @Override
            public void onFailure(Call<List<Horario>> call, Throwable t) {
                binding.tvEmptyState.setText("Error de conexión.");
            }
        });
    }

    private String getFechaActual() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
    }

    private String getFechaManana() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_YEAR, 1);
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(c.getTime());
    }
}