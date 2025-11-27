package com.example.app_cumbe.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class RutaConductor implements Serializable {
    @SerializedName("horario_id")
    private int horarioId;

    @SerializedName("ruta_origen") // Asumo que el backend devuelve nombres
    private String origen;

    @SerializedName("ruta_destino")
    private String destino;

    @SerializedName("fecha_salida")
    private String fechaSalida;

    @SerializedName("hora_salida")
    private String horaSalida;

    @SerializedName("estado")
    private String estado; // "PROGRAMADO", "EN_RUTA", "FINALIZADO"

    @SerializedName("asientos_ocupados")
    private int cantidadPasajeros;

    @SerializedName("placa_bus")
    private String placaBus;
    @SerializedName("bus_id")
    private int busId;

    @SerializedName("conductor_id")
    private int conductorId;

    public int getConductorId() { return conductorId; }

    public int getBusId() { return busId; }
    public int getHorarioId() { return horarioId; }
    public String getOrigen() { return origen; }
    public String getDestino() { return destino; }
    public String getFechaSalida() { return fechaSalida; }
    public String getHoraSalida() { return horaSalida; }
    public String getEstado() { return estado; }
    public int getCantidadPasajeros() { return cantidadPasajeros; }
    public String getPlacaBus() { return placaBus; }

    //SET estado
    public void setEstado(String nuevoEstado) {
        this.estado = nuevoEstado;
    }


//
}