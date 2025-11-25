package com.example.app_cumbe.model;

import com.google.gson.annotations.SerializedName;

public class AsientoOcupado {
    @SerializedName("numero_asiento")
    private int numero;

    @SerializedName("estado")
    private String estado; // "VENDIDO", "BLOQUEADO"

    @SerializedName("piso")
    private int piso;

    public int getNumero() { return numero; }
    public String getEstado() { return estado; }
    public int getPiso() { return piso; }
}