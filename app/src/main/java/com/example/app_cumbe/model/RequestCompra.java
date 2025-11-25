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

    // Constructor actualizado
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
    }
}