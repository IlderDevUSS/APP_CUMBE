package com.example.app_cumbe.model;

import com.google.gson.annotations.SerializedName;

public class RequestCompra {
    @SerializedName("horario_id")
    private int horarioId;

    @SerializedName("numero_asiento")
    private int numeroAsiento;

    @SerializedName("piso")
    private int piso;

    @SerializedName("precio_pagado")
    private double precio;

    @SerializedName("nombres")
    private String nombres;

    @SerializedName("apellidos")
    private String apellidos;

    @SerializedName("dni")
    private String dni;

    // --- NUEVO CAMPO: Celular ---
    // Asegúrate que tu backend espere "celular" o cambia el SerializedName a "pasajero_celular" si es necesario
    @SerializedName("celular")
    private String celular;

    @SerializedName("metodo_pago")
    private String metodoPago;

    @SerializedName("id_transaccion_externa")
    private String idTransaccionExterna;

    @SerializedName("detalle_estado")
    private String detalleEstado;

    // Constructor para cuando se crea el objeto antes de tener los datos de pago online
    public RequestCompra(int horarioId, int numeroAsiento, int piso, double precio,
                         String nombres, String apellidos, String dni, String celular, String metodoPago) {
        this.horarioId = horarioId;
        this.numeroAsiento = numeroAsiento;
        this.piso = piso;
        this.precio = precio;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.dni = dni;
        this.celular = celular;
        this.metodoPago = metodoPago;
        this.idTransaccionExterna = null; // Se setea después
        this.detalleEstado = null;      // Se setea después
    }

    // Constructor actualizado
    public RequestCompra(int horarioId, int numeroAsiento, int piso, double precio,
                         String nombres, String apellidos, String dni, String celular, String metodoPago, String idTransaccionExterna, String detalleEstado) {
        this.horarioId = horarioId;
        this.numeroAsiento = numeroAsiento;
        this.piso = piso;
        this.precio = precio;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.dni = dni;
        this.celular = celular;
        this.metodoPago = metodoPago;
        this.idTransaccionExterna = idTransaccionExterna;
        this.detalleEstado = detalleEstado;
    }

    // Setters para los campos que se actualizan después de la creación
    public void setIdTransaccionExterna(String idTransaccionExterna) {
        this.idTransaccionExterna = idTransaccionExterna;
    }

    public void setDetalleEstado(String detalleEstado) {
        this.detalleEstado = detalleEstado;
    }
}