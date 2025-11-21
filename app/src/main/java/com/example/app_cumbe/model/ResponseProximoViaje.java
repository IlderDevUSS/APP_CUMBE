package com.example.app_cumbe.model;

import com.google.gson.annotations.SerializedName;

public class ResponseProximoViaje {

    private String origen;
    private String destino;
    private String fecha_salida;
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