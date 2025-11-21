package com.example.app_cumbe;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app_cumbe.model.Encomienda;

import java.util.List;

public class EncomiendaAdapter extends RecyclerView.Adapter<EncomiendaAdapter.ViewHolder> {

    private List<Encomienda> listaEncomiendas;
    private Context context;
    private LayoutInflater inflater;
    private String miDniUsuario;
    public EncomiendaAdapter(Context context, List<Encomienda> listaEncomiendas, String miDniUsuario) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.listaEncomiendas = listaEncomiendas;
        this.miDniUsuario = miDniUsuario;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_encomienda, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Encomienda encomienda = listaEncomiendas.get(position);

        String estadoTexto = encomienda.getEstado(); // "RECIBIDO", "EN_CAMINO", etc.
        String titulo = "Encomienda #" + encomienda.getTrackingCode() + " - " + estadoTexto;
        String etiqueta;
        String nombre;
        String dni;

        if (miDniUsuario != null && miDniUsuario.equals(encomienda.getRemitenteDni())) {
            etiqueta = "Para: ";
            nombre = encomienda.getDestinatarioNombres();
            dni = encomienda.getDestinatarioDni();
        } else {

            etiqueta = "De: ";
            nombre = encomienda.getRemitenteNombres();
            dni = encomienda.getRemitenteDni();
        }

        // Validación para evitar textos "null"
        if (nombre == null) nombre = "Sin nombre";
        if (dni == null) dni = "S/D";

        // Seteamos el texto en el NUEVO TextView
        holder.tvEncomiendaTitle.setText(titulo);
        holder.tvDetailOrigen.setText("Origen: " + encomienda.getNombreOrigen());
        holder.tvDetailDestino.setText("Destino: " + encomienda.getNombreDestino());
        holder.tvDetailPersona.setText(etiqueta + nombre + " (" + dni + ")");
        holder.tvDetailPrecio.setText("Precio: S/ " + encomienda.getPrecio());


        int colorResId = getColorPorEstado(estadoTexto);
        holder.viewStatusBorder.setBackgroundColor(ContextCompat.getColor(context, colorResId));
        holder.tvEncomiendaTitle.setTextColor(ContextCompat.getColor(context, colorResId));


        holder.headerLayout.setOnClickListener(v -> {
            boolean isVisible = holder.llCollapsibleContent.getVisibility() == View.VISIBLE;
            if (isVisible) {
                holder.llCollapsibleContent.setVisibility(View.GONE);
                holder.ivAccordionIcon.setRotation(0);
            } else {
                holder.llCollapsibleContent.setVisibility(View.VISIBLE);
                holder.ivAccordionIcon.setRotation(180);
            }
        });

        // 4. Construir Timeline
        holder.llTimelineContainer.removeAllViews();
        int nivelEstado = getNivelEstado(estadoTexto);

        buildTimelineStep(holder.llTimelineContainer, "Paquete Recibido", nivelEstado >= 1);
        buildTimelineStep(holder.llTimelineContainer, "En Camino", nivelEstado >= 2);
        buildTimelineStep(holder.llTimelineContainer, "En Destino", nivelEstado >= 3);
        buildTimelineStep(holder.llTimelineContainer, "Entregado", nivelEstado >= 4);
    }

    @Override
    public int getItemCount() {
        return listaEncomiendas.size();
    }

    // Helper para convertir texto de estado a color
    private int getColorPorEstado(String estado) {
        switch (estado) {
            case "RECIBIDO": return R.color.color_estado_recibido;
            case "EN_CAMINO": return R.color.color_estado_encamino;
            case "EN_DESTINO": return R.color.color_estado_en_destino;
            case "ENTREGADO": return R.color.color_estado_entregado;
            default: return R.color.color_timeline_pending;
        }
    }

    // Helper para convertir texto de estado a nivel (1-4) para el timeline
    private int getNivelEstado(String estado) {
        switch (estado) {
            case "RECIBIDO": return 1;
            case "EN_CAMINO": return 2;
            case "EN_DESTINO": return 3;
            case "ENTREGADO": return 4;
            default: return 0;
        }
    }

    private void buildTimelineStep(LinearLayout container, String titulo, boolean isCompleted) {
        View stepView = inflater.inflate(R.layout.item_timeline_step, container, false);

        TextView tvTitle = stepView.findViewById(R.id.tvTimelineStepTitle);
        TextView tvDate = stepView.findViewById(R.id.tvTimelineStepDate);
        ImageView ivIcon = stepView.findViewById(R.id.ivTimelineIcon);
        View viewLine = stepView.findViewById(R.id.viewTimelineLine);

        tvTitle.setText(titulo);

        if (isCompleted) {
            tvDate.setText("Completado");
            ivIcon.setBackgroundResource(R.drawable.bg_timeline_icon_completed);
            viewLine.setBackgroundColor(ContextCompat.getColor(context, R.color.color_estado_entregado));
        } else {
            tvDate.setText("Pendiente");
            ivIcon.setBackgroundResource(R.drawable.bg_timeline_icon_pending);
            viewLine.setBackgroundColor(ContextCompat.getColor(context, R.color.color_timeline_pending));
        }

        container.addView(stepView);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        View viewStatusBorder;
        ConstraintLayout headerLayout;
        LinearLayout llCollapsibleContent, llTimelineContainer;
        TextView tvEncomiendaTitle, tvDetailOrigen, tvDetailDestino, tvDetailDestinatario;
        ImageView ivAccordionIcon;
        TextView tvDetailPersona;
        TextView tvDetailPrecio;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            viewStatusBorder = itemView.findViewById(R.id.viewStatusBorder);
            headerLayout = itemView.findViewById(R.id.headerLayout);
            llCollapsibleContent = itemView.findViewById(R.id.llCollapsibleContent);
            llTimelineContainer = itemView.findViewById(R.id.llTimelineContainer);
            tvEncomiendaTitle = itemView.findViewById(R.id.tvEncomiendaTitle);
            tvDetailOrigen = itemView.findViewById(R.id.tvDetailOrigen);
            tvDetailDestino = itemView.findViewById(R.id.tvDetailDestino);
            tvDetailPersona = itemView.findViewById(R.id.tvDetailPersona);
            tvDetailPrecio = itemView.findViewById(R.id.tvDetailPrecio);
            ivAccordionIcon = itemView.findViewById(R.id.ivAccordionIcon);
        }
    }
}