package com.example.app_cumbe;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app_cumbe.model.db.NotificacionEntity;

import java.util.ArrayList;
import java.util.List;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.NotifViewHolder> {

    private List<NotificacionEntity> lista = new ArrayList<>();
    private Context context;
    private OnItemClickListener listener;

    // Interfaz para manejar el click desde el Fragmento
    public interface OnItemClickListener {
        void onItemClick(NotificacionEntity notificacion);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setData(List<NotificacionEntity> nuevaLista) {
        this.lista = nuevaLista;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NotifViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new NotifViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotifViewHolder holder, int position) {
        NotificacionEntity item = lista.get(position);
        View barraColor = holder.itemView.findViewById(R.id.indicatorBar);

        int colorRes;
        holder.tvTitulo.setText(item.titulo);
        holder.tvContenido.setText(item.contenido);
        holder.tvFecha.setText(item.fecha);

        // CONFIGURACIÓN DE ESTILO SEGÚN SI ESTÁ LEÍDO O NO
        if (item.leido) {
            // Ya leído: Fondo blanco/normal, sin punto rojo, texto normal
            holder.dotUnread.setVisibility(View.GONE);
            holder.tvTitulo.setTextColor(ContextCompat.getColor(context, R.color.Textos_normal_second));
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
        } else {
            // No leído: Fondo ligeramente destacado, punto visible, texto destacado
            holder.dotUnread.setVisibility(View.VISIBLE);
            holder.tvTitulo.setTextColor(ContextCompat.getColor(context, R.color.black)); // O Textos_normal
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.fondo_destacados)); // Azulito muy claro
        }

        switch (item.tipo) {
            case "COMPRA":
                colorRes = R.color.color_finalizado; // Verde (Éxito)
                holder.ivIcon.setImageResource(R.drawable.ic_check_circle); // Icono check
                break;
            case "VIAJE":
                colorRes = R.color.color_programado; // Azul (Info)
                holder.ivIcon.setImageResource(R.drawable.ic_directions_bus);
                break;
            case "ENCOMIENDA":
                colorRes = R.color.color_Ruta; // Amarillo
                holder.ivIcon.setImageResource(R.drawable.ic_local_activity);
                break;
            default:
                colorRes = R.color.color_principal_cumbe; // Naranja
                holder.ivIcon.setImageResource(R.drawable.ic_info);
        }

        // Aplicar color a la barra lateral
        barraColor.setBackgroundColor(ContextCompat.getColor(context, colorRes));

        // El icono lo pintamos del mismo color para combinar
        holder.ivIcon.setColorFilter(ContextCompat.getColor(context, colorRes));
        // LÓGICA DE ICONOS SEGÚN TIPO
        if ("ENCOMIENDA".equals(item.tipo)) {
            holder.ivIcon.setImageResource(R.drawable.ic_local_activity); // O un icono de caja
        } else if ("VIAJE".equals(item.tipo)) {
            holder.ivIcon.setImageResource(R.drawable.ic_directions_bus);
        } else {
            holder.ivIcon.setImageResource(R.drawable.ic_info);
        }

        // Click Listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    static class NotifViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo, tvContenido, tvFecha;
        ImageView ivIcon;
        View dotUnread;

        public NotifViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvTitulo);
            tvContenido = itemView.findViewById(R.id.tvContenido);
            tvFecha = itemView.findViewById(R.id.tvFecha);
            ivIcon = itemView.findViewById(R.id.ivIcono);
            dotUnread = itemView.findViewById(R.id.viewUnreadDot);
        }
    }
}