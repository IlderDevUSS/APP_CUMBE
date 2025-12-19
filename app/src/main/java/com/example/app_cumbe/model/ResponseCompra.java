package com.example.app_cumbe.model;

import com.google.gson.annotations.SerializedName;

public class ResponseCompra {
    @SerializedName("mensaje")
    private String mensaje;

    @SerializedName("pasaje_id")
    private int pasajeId;

    @SerializedName("transaccion_id")
    private int transaccionId;

    @SerializedName("error")
    private String error;


    @SerializedName("id_ticket_visual")
    private String idTicketVisual;
    public String getMensaje() { return mensaje; }
    public int getPasajeId() { return pasajeId; }
    public int getTransaccionId() { return transaccionId; }
    public String getError() { return error; }
    public String getIdTicketVisual() { return idTicketVisual; }
}