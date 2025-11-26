package com.example.app_cumbe.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

// Implementamos Serializable para pasarlo fácil entre actividades
public class Ticket implements Serializable {

    @SerializedName("pasaje_id")
    private int pasajeId;

    @SerializedName("transaccion_id")
    private int transaccionId;

    @SerializedName("origen")
    private String origen;

    @SerializedName("destino")
    private String destino;

    @SerializedName("fecha_salida")
    private String fechaSalida;

    @SerializedName("hora_salida")
    private String horaSalida;

    @SerializedName("numero_asiento")
    private int asiento;

    @SerializedName("precio")
    private double precio;

    @SerializedName("estado")
    private String estado;

    @SerializedName("servicio")
    private String servicio;

    @SerializedName("pasajero_nombres")
    private String nombres;

    @SerializedName("pasajero_apellidos")
    private String apellidos;

    @SerializedName("pasajero_dni")
    private String dni;

    // Getters
    public int getPasajeId() { return pasajeId; }
    public int getTransaccionId() { return transaccionId; }
    public String getOrigen() { return origen; }
    public String getDestino() { return destino; }
    public String getFechaSalida() { return fechaSalida; }
    public String getHoraSalida() { return horaSalida; }
    public int getAsiento() { return asiento; }
    public double getPrecio() { return precio; }
    public String getEstado() { return estado; }
    public String getServicio() { return servicio; }
    public String getNombres() { return nombres; }
    public String getApellidos() { return apellidos; }
    public String getDni() { return dni; }
}