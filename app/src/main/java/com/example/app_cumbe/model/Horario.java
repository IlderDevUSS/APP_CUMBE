package com.example.app_cumbe.model;

import com.google.gson.annotations.SerializedName;

public class Horario {
    @SerializedName("horario_id")
    private int id;

    @SerializedName("hora_salida")
    private String horaSalida; // "08:00:00"

    @SerializedName("precio")
    private double precio;

    @SerializedName("asientos_libres")
    private int asientosLibres;

    @SerializedName("placa_bus")
    private String placaBus;

    @SerializedName("clase_bus") // "Cama", "Semi-Cama"
    private String claseBus;

    @SerializedName("total_asientos")
    private int totalAsientos;

    @SerializedName("num_pisos") // 1 o 2
    private int numPisos;

    // Getters
    public int getId() {
        return id;
    }

    public String getHoraSalida() {
        return horaSalida;
    }

    public double getPrecio() {
        return precio;
    }

    public int getAsientosLibres() {
        return asientosLibres;
    }

    public String getPlacaBus() {
        return placaBus;
    }

    public String getClaseBus() {
        return claseBus;
    }

    public int getNumPisos() {
        return numPisos;
    }

    public int getTotalAsientos() {
        return totalAsientos;
    }
}