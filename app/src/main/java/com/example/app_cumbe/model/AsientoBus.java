package com.example.app_cumbe.model;

public class AsientoBus {
    private int numero;
    private int piso;
    private String estado; // "LIBRE", "OCUPADO", "SELECCIONADO"
    private boolean esPasillo; // Para dibujar espacios vacíos

    public AsientoBus(int numero, int piso, String estado) {
        this.numero = numero;
        this.piso = piso;
        this.estado = estado;
        this.esPasillo = false;
    }

    // Constructor para pasillos (espacios vacíos)
    public AsientoBus(boolean esPasillo) {
        this.esPasillo = true;
        this.estado = "PASILLO";
    }

    public int getNumero() { return numero; }
    public int getPiso() { return piso; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public boolean esPasillo() { return esPasillo; }
}