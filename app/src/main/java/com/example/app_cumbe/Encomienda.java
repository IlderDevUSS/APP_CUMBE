package com.example.app_cumbe.model;

import com.google.gson.annotations.SerializedName;

public class Encomienda {

    // --- CAMPOS DE LA BASE DE DATOS ---
    // Usamos @SerializedName para mapear el JSON del backend a variables Java

    @SerializedName("encomienda_id")
    private int id;

    @SerializedName("tracking_code")
    private String trackingCode;

    @SerializedName("origen_sucursal_id")
    private int origenId;

    @SerializedName("destino_sucursal_id")
    private int destinoId;

    @SerializedName("estado")
    private String estado; // "RECIBIDO", "EN_CAMINO", etc.

    @SerializedName("remitente_dni")
    private String remitenteDni;

    @SerializedName("destinatario_dni")
    private String destinatarioDni;

    @SerializedName("precio_pagado")
    private double precio;

    // Campos adicionales que podrían venir en el futuro o calcularse
    private String fechaRecibido;
    private String fechaEnCamino;
    private String fechaEnDestino;
    private String fechaEntregado;


    // --- CONSTRUCTOR ---
    // Actualizado para incluir todos los campos importantes
    // Nota: Gson puede usar este constructor o uno vacío por defecto.
    public Encomienda(int id, String trackingCode, int origenId, int destinoId, String estado,
                      String remitenteDni, String destinatarioDni, double precio) {
        this.id = id;
        this.trackingCode = trackingCode;
        this.origenId = origenId;
        this.destinoId = destinoId;
        this.estado = estado;
        this.remitenteDni = remitenteDni;
        this.destinatarioDni = destinatarioDni;
        this.precio = precio;
    }

    // Constructor vacío (requerido a veces por ciertas versiones de convertidores)
    public Encomienda() {}


    // --- GETTERS ---

    public int getId() { return id; }
    public String getTrackingCode() { return trackingCode; }
    public int getOrigenId() { return origenId; }
    public int getDestinoId() { return destinoId; }
    public String getEstado() { return estado; }
    public String getRemitenteDni() { return remitenteDni; }
    public String getDestinatarioDni() { return destinatarioDni; }
    public double getPrecio() { return precio; }

    // Getters de fechas (pueden ser null)
    public String getFechaRecibido() { return fechaRecibido; }
    public String getFechaEnCamino() { return fechaEnCamino; }
    public String getFechaEnDestino() { return fechaEnDestino; }
    public String getFechaEntregado() { return fechaEntregado; }


    // --- HELPERS (Traducción de IDs a Nombres) ---
    // TODO: Idealmente, el backend debería devolver los nombres de las ciudades.
    // Por ahora, hacemos la traducción manual aquí.

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