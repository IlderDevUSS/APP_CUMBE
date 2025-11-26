package com.example.app_cumbe.model;

import com.google.gson.annotations.SerializedName;

public class ResponseProximoViaje {

    @SerializedName("origen")
    private String origen;

    @SerializedName("destino")
    private String destino;

    @SerializedName("fecha_salida")
    private String fecha_salida;

    @SerializedName("hora_salida")
    private String hora_salida;

    @SerializedName("cantidad_pasajeros")
    private int cantidadPasajeros;

    // --- Getters ---
    public String getOrigen() { return origen; }
    public String getDestino() { return destino; }
    public String getFecha_salida() { return fecha_salida; }
    public String getHora_salida() { return hora_salida; }
    public int getCantidadPasajeros() { return cantidadPasajeros; }
}