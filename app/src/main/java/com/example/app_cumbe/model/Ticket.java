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

    public void setPasajeId(int pasajeId) { this.pasajeId = pasajeId; }
    public void setTransaccionId(int transaccionId) { this.transaccionId = transaccionId; }
    public void setOrigen(String origen) { this.origen = origen; }
    public void setDestino(String destino) { this.destino = destino; }
    public void setFechaSalida(String fechaSalida) { this.fechaSalida = fechaSalida; }
    public void setHoraSalida(String horaSalida) { this.horaSalida = horaSalida; }
    public void setAsiento(int asiento) { this.asiento = asiento; }
    public void setPrecio(double precio) { this.precio = precio; }
    public void setEstado(String estado) { this.estado = estado; }
    public void setServicio(String servicio) { this.servicio = servicio; }
    public void setNombres(String nombres) { this.nombres = nombres; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }
    public void setDni(String dni) { this.dni = dni; }
}