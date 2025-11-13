package com.example.app_cumbe;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class EncomiendaAdapter extends RecyclerView.Adapter<EncomiendaAdapter.ViewHolder> {

    private List<Encomienda> listaEncomiendas;
    private Context context;
    private LayoutInflater inflater;


    public EncomiendaAdapter(Context context, List<Encomienda> listaEncomiendas) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.listaEncomiendas = listaEncomiendas;
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


        String titulo = "Encomienda #" + encomienda.getTrackingCode() + " - " + encomienda.getEstadoTexto();
        holder.tvEncomiendaTitle.setText(titulo);


        holder.tvDetailOrigen.setText("Origen: " + encomienda.getOrigen());
        holder.tvDetailDestino.setText("Destino: " + encomienda.getDestino());
        holder.tvDetailDestinatario.setText("Destinatario: " + encomienda.getDestinatario());


        int colorResId;
        switch (encomienda.getEstadoNum()) {
            case 1: // RECIBIDO
                colorResId = R.color.color_estado_recibido;
                break;
            case 2: // EN CAMINO
                colorResId = R.color.color_estado_encamino;
                break;
            case 3: // EN DESTINO
                colorResId = R.color.color_estado_encamino;
                break;
            case 4: // ENTREGADO
                colorResId = R.color.color_estado_entregado;
                break;
            default:
                colorResId = R.color.color_timeline_pending;
        }

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


        holder.llTimelineContainer.removeAllViews();


        buildTimelineStep(holder.llTimelineContainer, "Paquete Recibido", encomienda.getFechaRecibido(), encomienda.getEstadoNum() >= 1);
        buildTimelineStep(holder.llTimelineContainer, "En Camino", encomienda.getFechaEnCamino(), encomienda.getEstadoNum() >= 2);
        buildTimelineStep(holder.llTimelineContainer, "En Destino", encomienda.getFechaEnDestino(), encomienda.getEstadoNum() >= 3);
        buildTimelineStep(holder.llTimelineContainer, "Entregado", encomienda.getFechaEntregado(), encomienda.getEstadoNum() >= 4);
    }


    @Override
    public int getItemCount() {
        return listaEncomiendas.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        View viewStatusBorder;
        androidx.constraintlayout.widget.ConstraintLayout headerLayout;
        LinearLayout llCollapsibleContent, llTimelineContainer;
        TextView tvEncomiendaTitle, tvDetailOrigen, tvDetailDestino, tvDetailDestinatario;
        ImageView ivAccordionIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            viewStatusBorder = itemView.findViewById(R.id.viewStatusBorder);
            headerLayout = itemView.findViewById(R.id.headerLayout);
            llCollapsibleContent = itemView.findViewById(R.id.llCollapsibleContent);
            llTimelineContainer = itemView.findViewById(R.id.llTimelineContainer);
            tvEncomiendaTitle = itemView.findViewById(R.id.tvEncomiendaTitle);
            tvDetailOrigen = itemView.findViewById(R.id.tvDetailOrigen);
            tvDetailDestino = itemView.findViewById(R.id.tvDetailDestino);
            tvDetailDestinatario = itemView.findViewById(R.id.tvDetailDestinatario);
            ivAccordionIcon = itemView.findViewById(R.id.ivAccordionIcon);
        }
    }


    private void buildTimelineStep(LinearLayout container, String titulo, String fecha, boolean isCompleted) {

        View stepView = inflater.inflate(R.layout.item_timeline_step, container, false);


        TextView tvTitle = stepView.findViewById(R.id.tvTimelineStepTitle);
        TextView tvDate = stepView.findViewById(R.id.tvTimelineStepDate);
        ImageView ivIcon = stepView.findViewById(R.id.ivTimelineIcon);
        View viewLine = stepView.findViewById(R.id.viewTimelineLine);


        tvTitle.setText(titulo);
        tvDate.setText(fecha != null ? fecha : "Pendiente");


        if (isCompleted) {
            ivIcon.setBackgroundResource(R.drawable.bg_timeline_icon_completed);
            viewLine.setBackgroundColor(ContextCompat.getColor(context, R.color.color_estado_entregado));
        } else {
            ivIcon.setBackgroundResource(R.drawable.bg_timeline_icon_pending);
            viewLine.setBackgroundColor(ContextCompat.getColor(context, R.color.color_timeline_pending));
        }


        container.addView(stepView);
    }
}