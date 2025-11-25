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

import com.example.app_cumbe.model.AsientoBus;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class AsientoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<AsientoBus> listaAsientos;
    private final Context context;
    private final OnAsientoClickListener listener;

    private static final int VIEW_TYPE_SEAT = 0;
    private static final int VIEW_TYPE_AISLE = 1;

    public interface OnAsientoClickListener {
        void onAsientoClick(AsientoBus asiento);
    }

    public AsientoAdapter(Context context, List<AsientoBus> listaAsientos, OnAsientoClickListener listener) {
        this.context = context;
        this.listaAsientos = listaAsientos;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        if (listaAsientos.get(position).esPasillo()) {
            return VIEW_TYPE_AISLE;
        } else {
            return VIEW_TYPE_SEAT;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SEAT) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_asiento_bus, parent, false);
            return new SeatViewHolder(view);
        } else {
            // Para el pasillo, inflamos una vista vacía que no mostrará nada
            View view = new View(parent.getContext());
            // La vista del pasillo ocupará el espacio que le asigne el GridLayoutManager, pero estará en blanco.
            return new AisleViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == VIEW_TYPE_SEAT) {
            SeatViewHolder seatHolder = (SeatViewHolder) holder;
            AsientoBus asiento = listaAsientos.get(position);

            seatHolder.tvNumero.setText(String.valueOf(asiento.getNumero()));

            // Colores según estado
            switch (asiento.getEstado()) {
                case "OCUPADO":
                    seatHolder.cardView.setCardBackgroundColor(Color.parseColor("#E0E0E0")); // Gris
                    seatHolder.cardView.setStrokeColor(Color.TRANSPARENT);
                    seatHolder.tvNumero.setTextColor(Color.parseColor("#9E9E9E"));
                    seatHolder.cardView.setEnabled(false); // No clickeable
                    break;
                case "SELECCIONADO":
                    seatHolder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.color_principal_cumbe)); // Naranja
                    seatHolder.cardView.setStrokeColor(ContextCompat.getColor(context, R.color.color_principal_cumbe));
                    seatHolder.tvNumero.setTextColor(Color.WHITE);
                    seatHolder.cardView.setEnabled(true);
                    break;
                default: // "LIBRE"
                    seatHolder.cardView.setCardBackgroundColor(Color.WHITE);
                    seatHolder.cardView.setStrokeColor(Color.parseColor("#DDDDDD"));
                    seatHolder.tvNumero.setTextColor(ContextCompat.getColor(context, R.color.gris1));
                    seatHolder.cardView.setEnabled(true);
                    break;
            }

            seatHolder.itemView.setOnClickListener(v -> {
                if (listener != null && !asiento.getEstado().equals("OCUPADO")) {
                    listener.onAsientoClick(asiento);
                }
            });
        }
        // No hay que hacer nada para el AisleViewHolder, ya que es una vista vacía
    }

    @Override
    public int getItemCount() {
        return listaAsientos.size();
    }

    // ViewHolder para los Asientos
    public static class SeatViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        TextView tvNumero;

        public SeatViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardAsiento);
            tvNumero = itemView.findViewById(R.id.tvNumeroAsiento);
        }
    }

    // ViewHolder para el Pasillo (no necesita contenido)
    public static class AisleViewHolder extends RecyclerView.ViewHolder {
        public AisleViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}