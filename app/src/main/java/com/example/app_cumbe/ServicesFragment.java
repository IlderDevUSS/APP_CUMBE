package com.example.app_cumbe;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.app_cumbe.databinding.FragmentServicesBinding; // Asegúrate de que el layout se llame fragment_services.xml

public class ServicesFragment extends Fragment {

    private FragmentServicesBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentServicesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        cargarDatosSucursales();
    }

    private void cargarDatosSucursales() {
        // Reutilizamos tu lógica de ServiciosActivity
        setSucursalInfo(binding.sucursalCajamarca.getRoot(), "Cajamarca", "Av. San Martín de Porres 140", R.drawable.cajamarca);
        setSucursalInfo(binding.sucursalChiclayo.getRoot(), "Chiclayo", "Av. José Quiñones 497", R.drawable.chiclayo);
        setSucursalInfo(binding.sucursalJaen.getRoot(), "Jaén", "Av. Manuel Antonio Mesones Muro 800", R.drawable.jaen);
    }

    private void setSucursalInfo(View cardView, String ciudad, String direccion, int imgResId) {
        TextView tvCiudad = cardView.findViewById(R.id.tvCiudadSucursal);
        TextView tvDireccion = cardView.findViewById(R.id.tvDireccionSucursal);
        ImageView imgSucursal = cardView.findViewById(R.id.ivSucursalImage);

        if (tvCiudad != null) tvCiudad.setText(ciudad);
        if (tvDireccion != null) tvDireccion.setText(direccion);
        if (imgSucursal != null) imgSucursal.setImageResource(imgResId);
    }
}