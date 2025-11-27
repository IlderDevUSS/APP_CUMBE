package com.example.app_cumbe.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class RutaConductor implements Serializable {
    @SerializedName("horario_id")
    private int horarioId;

    @SerializedName("ruta_origen")
    private String origen;

    @SerializedName("ruta_destino")
    private String destino;

    @SerializedName("fecha_salida")
    private String fechaSalida;

    @SerializedName("hora_salida")
    private String horaSalida;

    @SerializedName("estado")
    private String estado;

    @SerializedName("asientos_ocupados")
    private int cantidadPasajeros;

    @SerializedName("placa_bus")
    private String placaBus;

    @SerializedName("bus_id")
    private int busId;

    @SerializedName("conductor_id")
    private int conductorId;

    // NUEVO CAMPO
    @SerializedName("tiene_reporte")
    private boolean tieneReporte;

    // Getters
    public int getHorarioId() { return horarioId; }
    public String getOrigen() { return origen; }
    public String getDestino() { return destino; }
    public String getFechaSalida() { return fechaSalida; }
    public String getHoraSalida() { return horaSalida; }
    public String getEstado() { return estado; }
    public int getCantidadPasajeros() { return cantidadPasajeros; }
    public String getPlacaBus() { return placaBus; }
    public int getBusId() { return busId; }
    public int getConductorId() { return conductorId; }
    public boolean tieneReporte() { return tieneReporte; }

    public void setEstado(String estado) { this.estado = estado; }
    public void setTieneReporte(boolean tieneReporte) { this.tieneReporte = tieneReporte; }
}