package com.example.app_cumbe;

public class Encomienda {

    // Datos del ítem
    private String trackingCode;
    private String origen;
    private String destino;
    private String destinatario;
    private boolean esEnviada; // true si es "enviada", false si es "por recibir"

    // Datos del Estado
    private int estadoNum; // 1, 2, 3, o 4
    private String estadoTexto; // "RECIBIDO", "EN CAMINO", etc.

    // Fechas para el Timeline (pueden ser null si aún no ocurren)
    private String fechaRecibido;
    private String fechaEnCamino;
    private String fechaEnDestino;
    private String fechaEntregado;

    // --- Constructor (lo usaremos para crear datos de prueba) ---
    public Encomienda(String trackingCode, String origen, String destino, String destinatario, boolean esEnviada, int estadoNum, String estadoTexto, String fechaRecibido, String fechaEnCamino, String fechaEnDestino, String fechaEntregado) {
        this.trackingCode = trackingCode;
        this.origen = origen;
        this.destino = destino;
        this.destinatario = destinatario;
        this.esEnviada = esEnviada;
        this.estadoNum = estadoNum;
        this.estadoTexto = estadoTexto;
        this.fechaRecibido = fechaRecibido;
        this.fechaEnCamino = fechaEnCamino;
        this.fechaEnDestino = fechaEnDestino;
        this.fechaEntregado = fechaEntregado;
    }

    // --- Getters (para que el Adapter pueda leer los datos) ---
    public String getTrackingCode() { return trackingCode; }
    public String getOrigen() { return origen; }
    public String getDestino() { return destino; }
    public String getDestinatario() { return destinatario; }
    public boolean isEsEnviada() { return esEnviada; }
    public int getEstadoNum() { return estadoNum; }
    public String getEstadoTexto() { return estadoTexto; }
    public String getFechaRecibido() { return fechaRecibido; }
    public String getFechaEnCamino() { return fechaEnCamino; }
    public String getFechaEnDestino() { return fechaEnDestino; }
    public String getFechaEntregado() { return fechaEntregado; }
}