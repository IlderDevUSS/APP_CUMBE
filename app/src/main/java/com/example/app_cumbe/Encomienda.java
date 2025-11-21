package com.example.app_cumbe.model;

import com.google.gson.annotations.SerializedName;

public class Encomienda {

    @SerializedName("encomienda_id")
    private int id;

    @SerializedName("tracking_code")
    private String trackingCode;

    @SerializedName("origen_sucursal_id")
    private int origenId;

    @SerializedName("destino_sucursal_id")
    private int destinoId;

    @SerializedName("estado")
    private String estado;

    @SerializedName("remitente_dni")
    private String remitenteDni;

    @SerializedName("remitente_nombres")
    private String remitenteNombres;

    @SerializedName("destinatario_dni")
    private String destinatarioDni;

    @SerializedName("destinatario_nombres")
    private String destinatarioNombres;

    @SerializedName("precio_pagado")
    private double precio;



    private String fechaRecibido;
    private String fechaEnCamino;
    private String fechaEnDestino;
    private String fechaEntregado;



    public Encomienda(int id, String trackingCode, int origenId, int destinoId, String estado,
                      String remitenteDni, String destinatarioDni,String remitenteNombres,String destinatarioNombres, double precio) {
        this.id = id;
        this.trackingCode = trackingCode;
        this.origenId = origenId;
        this.destinoId = destinoId;
        this.estado = estado;
        this.remitenteDni = remitenteDni;
        this.destinatarioDni = destinatarioDni;
        this.remitenteNombres = remitenteNombres;
        this.destinatarioNombres = destinatarioNombres;
        this.precio = precio;
    }


    public Encomienda() {}



    public int getId() { return id; }
    public String getTrackingCode() { return trackingCode; }
    public int getOrigenId() { return origenId; }
    public int getDestinoId() { return destinoId; }
    public String getEstado() { return estado; }
    public String getRemitenteDni() { return remitenteDni; }
    public String getDestinatarioDni() { return destinatarioDni; }
    public double getPrecio() { return precio; }

    public String getRemitenteNombres() { return remitenteNombres; }

    public String getDestinatarioNombres() { return destinatarioNombres; }
    public String getFechaRecibido() { return fechaRecibido; }
    public String getFechaEnCamino() { return fechaEnCamino; }
    public String getFechaEnDestino() { return fechaEnDestino; }
    public String getFechaEntregado() { return fechaEntregado; }


    public String getNombreOrigen() {
        return traducirIdASucursal(origenId);
    }

    public String getNombreDestino() {
        return traducirIdASucursal(destinoId);
    }

    private String traducirIdASucursal(int id) {
        switch (id) {
            case 1: return "Cajamarca";
            case 2: return "Chiclayo";
            case 3: return "Jaén";
            default: return "Sucursal #" + id;
        }
    }
}