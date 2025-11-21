package com.example.app_cumbe;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.app_cumbe.databinding.ActivityServiciosBinding;

public class ServiciosActivity extends AppCompatActivity {

    private ActivityServiciosBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityServiciosBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolbar();
        cargarDatosSucursales();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbarServicios);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        binding.toolbarServicios.setNavigationOnClickListener(v -> finish());
    }

    private void cargarDatosSucursales() {
        // Llenamos los datos manualmente (o podrías traerlos de la API /sucursales)

        // Sucursal 1: Cajamarca
        setSucursalInfo(binding.sucursalCajamarca.getRoot(),
                "Cajamarca", "Av. San Martín de Porres 140", R.drawable.cajamarca);

        // Sucursal 2: Chiclayo
        setSucursalInfo(binding.sucursalChiclayo.getRoot(),
                "Chiclayo", "Av. José Quiñones 497", R.drawable.chiclayo);

        // Sucursal 3: Jaén
        setSucursalInfo(binding.sucursalJaen.getRoot(),
                "Jaén", "Av. Manuel Antonio Mesones Muro 800", R.drawable.jaen);
    }

    private void setSucursalInfo(View cardView, String ciudad, String direccion, int imgResId) {
        TextView tvCiudad = cardView.findViewById(R.id.tvCiudadSucursal);
        TextView tvDireccion = cardView.findViewById(R.id.tvDireccionSucursal);
        ImageView imgSucursal = cardView.findViewById(R.id.ivSucursalImage);

        if (tvCiudad != null) tvCiudad.setText(ciudad);
        if (tvDireccion != null) tvDireccion.setText(direccion);

        if (imgSucursal != null) {
            try {
                imgSucursal.setImageResource(imgResId);
            } catch (Exception e) {
                imgSucursal.setImageResource(R.drawable.bus_3);
            }
        }
    }
}