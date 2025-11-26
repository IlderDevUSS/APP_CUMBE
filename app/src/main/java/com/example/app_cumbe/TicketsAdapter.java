package com.example.app_cumbe;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.app_cumbe.model.Ticket;
import java.util.List;

public class TicketsAdapter extends RecyclerView.Adapter<TicketsAdapter.ViewHolder> {

    private List<Ticket> listaTickets;
    private OnTicketClickListener listener;

    // Interface para el clic
    public interface OnTicketClickListener {
        void onTicketClick(Ticket ticket);
    }

    public TicketsAdapter(List<Ticket> listaTickets, OnTicketClickListener listener) {
        this.listaTickets = listaTickets;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ticket_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Ticket ticket = listaTickets.get(position);

        holder.tvRuta.setText(ticket.getOrigen() + " - " + ticket.getDestino());
        holder.tvFecha.setText(ticket.getFechaSalida());
        holder.tvAsiento.setText("Asiento " + ticket.getAsiento());

        String hora = ticket.getHoraSalida();
        if(hora != null && hora.length() > 5) hora = hora.substring(0, 5);
        holder.tvHora.setText(hora);

        holder.tvServicio.setText(ticket.getServicio() != null ? ticket.getServicio() : "Servicio Estándar");
        holder.tvPrecio.setText("S/ " + String.format("%.2f", ticket.getPrecio()));

        // Datos del pasajero
        String nombreCompleto = (ticket.getNombres() != null ? ticket.getNombres() : "") + " " +
                (ticket.getApellidos() != null ? ticket.getApellidos() : "");
        holder.tvPasajeroNombre.setText(nombreCompleto.trim().isEmpty() ? "Pasajero" : nombreCompleto);
        holder.tvPasajeroDni.setText("DNI: " + (ticket.getDni() != null ? ticket.getDni() : "---"));

        // Click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onTicketClick(ticket);
        });
    }

    @Override
    public int getItemCount() {
        return listaTickets.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRuta, tvFecha, tvAsiento, tvHora, tvServicio, tvPrecio, tvPasajeroNombre, tvPasajeroDni;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRuta = itemView.findViewById(R.id.tvRuta);
            tvFecha = itemView.findViewById(R.id.tvFecha);
            tvAsiento = itemView.findViewById(R.id.tvAsiento);
            tvHora = itemView.findViewById(R.id.tvHora);
            tvServicio = itemView.findViewById(R.id.tvServicio);
            tvPrecio = itemView.findViewById(R.id.tvPrecio);
            tvPasajeroNombre = itemView.findViewById(R.id.tvPasajeroNombre);
            tvPasajeroDni = itemView.findViewById(R.id.tvPasajeroDni);
        }
    }
}