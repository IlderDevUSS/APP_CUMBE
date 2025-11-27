package com.example.app_cumbe;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;

public class ReporteBottomSheet extends BottomSheetDialogFragment {

    // Interfaz para comunicar el resultado a la Activity
    public interface ReporteListener {
        void onReporteEnviado(String tipo, String descripcion, double costo);
    }

    private ReporteListener listener;

    public void setListener(ReporteListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reporte_bottom_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AutoCompleteTextView dropdown = view.findViewById(R.id.dropdownTipoReporte);
        TextInputEditText etCosto = view.findViewById(R.id.etCosto);
        TextInputEditText etDescripcion = view.findViewById(R.id.etDescripcion);
        Button btnEnviar = view.findViewById(R.id.btnEnviarReporteSheet);

        // Configurar lista desplegable
        String[] tipos = {"Falla Mecánica", "Retraso Tráfico", "Accidente", "Incidente Pasajero", "Gasto Extra", "Otro"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, tipos);
        dropdown.setAdapter(adapter);

        btnEnviar.setOnClickListener(v -> {
            String tipo = dropdown.getText().toString();
            String descripcion = etDescripcion.getText() != null ? etDescripcion.getText().toString() : "";
            String costoStr = etCosto.getText() != null ? etCosto.getText().toString() : "";

            if (tipo.isEmpty()) {
                Toast.makeText(getContext(), "Selecciona un tipo", Toast.LENGTH_SHORT).show();
                return;
            }
            if (descripcion.isEmpty()) {
                Toast.makeText(getContext(), "Escribe una descripción", Toast.LENGTH_SHORT).show();
                return;
            }

            double costo = 0.0;
            try {
                if (!costoStr.isEmpty()) costo = Double.parseDouble(costoStr);
            } catch (NumberFormatException e) {
                costo = 0.0;
            }

            if (listener != null) {
                listener.onReporteEnviado(tipo, descripcion, costo);
            }
            dismiss();
        });
    }
}