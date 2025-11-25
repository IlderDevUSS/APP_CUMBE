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

    // NUEVO CAMPO IMPORTANTE
    @SerializedName("metodo_pago")
    private String metodoPago;

    public RequestCompra(int horarioId, int numeroAsiento, int piso, double precio, String nombres, String apellidos, String dni, String metodoPago) {
        this.horarioId = horarioId;
        this.numeroAsiento = numeroAsiento;
        this.piso = piso;
        this.precio = precio;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.dni = dni;
        this.metodoPago = metodoPago;
    }
}