package com.example.app_cumbe;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app_cumbe.model.Horario;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class HorarioAdapter extends RecyclerView.Adapter<HorarioAdapter.ViewHolder> {

    private List<Horario> listaHorarios;
    private Context context;
    private OnHorarioClickListener listener;

    // Variable para rastrear cuál elemento está seleccionado (-1 = ninguno)
    private int selectedPosition = -1;

    // Interfaz para comunicar el click a la Actividad
    public interface OnHorarioClickListener {
        void onHorarioClick(Horario horario);
    }

    public HorarioAdapter(Context context, List<Horario> listaHorarios, OnHorarioClickListener listener) {
        this.context = context;
        this.listaHorarios = listaHorarios;
        this.listener = listener;
    }

    // Método para limpiar la selección (útil cuando se cambian los filtros)
    public void resetSelection() {
        selectedPosition = -1;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_horario_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Horario horario = listaHorarios.get(position);

        // --- Asignación de Datos ---

        // Formatear hora (Quitar segundos si vienen: 08:00:00 -> 08:00)
        String horaRaw = horario.getHoraSalida();
        String horaFormateada = horaRaw != null && horaRaw.length() >= 5 ? horaRaw.substring(0, 5) : horaRaw;

        holder.tvHoraSalida.setText(horaFormateada);
        holder.tvPrecio.setText(String.format("S/ %.2f", horario.getPrecio()));

        String infoBus = String.format("%s (%d Piso%s)", horario.getClaseBus(), horario.getNumPisos(), horario.getNumPisos() > 1 ? "s" : "");
        holder.tvClaseBus.setText(infoBus);

        holder.tvPlaca.setText("Placa: " + horario.getPlacaBus());

        holder.tvAsientosLibres.setText(horario.getAsientosLibres() + " asientos disponibles");

        // --- LÓGICA DE SELECCIÓN VISUAL ---

        if (selectedPosition == position) {
            // Estilo SELECCIONADO (Borde Naranja, Fondo suave)
            // Usamos el color principal de tu app
            holder.cardView.setStrokeColor(ContextCompat.getColor(context, R.color.color_principal_cumbe));
            holder.cardView.setStrokeWidth(4); // Borde más grueso para resaltar
            holder.cardView.setCardBackgroundColor(Color.parseColor("#FFF3E0")); // Fondo naranja muy suave
        } else {
            // Estilo NORMAL (Borde Gris, Fondo Blanco)
            holder.cardView.setStrokeColor(Color.parseColor("#E0E0E0")); // Gris suave
            holder.cardView.setStrokeWidth(2); // Borde normal
            holder.cardView.setCardBackgroundColor(Color.WHITE);
        }

        // --- CLICK LISTENER CON ACTUALIZACIÓN DE SELECCIÓN ---
        holder.itemView.setOnClickListener(v -> {
            // Guardamos la posición anterior para actualizar solo esa fila (optimización)
            int previousPosition = selectedPosition;
            selectedPosition = holder.getAdapterPosition();

            // Notificamos cambios para que el RecyclerView repinte los items afectados
            notifyItemChanged(previousPosition);
            notifyItemChanged(selectedPosition);

            // Avisamos a la actividad que se eligió un horario
            if (listener != null) {
                listener.onHorarioClick(horario);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaHorarios.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView; // Necesitamos el card para cambiar el borde dinámicamente
        TextView tvHoraSalida, tvPrecio, tvClaseBus, tvPlaca, tvAsientosLibres;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Casteamos a MaterialCardView para acceder a métodos como setStrokeColor
            if (itemView instanceof MaterialCardView) {
                cardView = (MaterialCardView) itemView;
            }

            tvHoraSalida = itemView.findViewById(R.id.tvHoraSalida);
            tvPrecio = itemView.findViewById(R.id.tvPrecio);
            tvClaseBus = itemView.findViewById(R.id.tvClaseBus);
            tvPlaca = itemView.findViewById(R.id.tvPlaca);
            tvAsientosLibres = itemView.findViewById(R.id.tvAsientosLibres);
        }
    }
}