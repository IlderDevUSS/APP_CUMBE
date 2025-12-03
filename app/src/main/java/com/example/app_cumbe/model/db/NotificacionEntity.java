package com.example.app_cumbe.model.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "notificaciones")
public class NotificacionEntity {

    @PrimaryKey(autoGenerate = true)
    public int id; // ID único interno

    public String titulo;
    public String contenido;
    public String fecha; // Guardaremos fecha como String o Long
    public String tipo;  // "SISTEMA", "ENCOMIENDA", "VIAJE"

    public boolean leido; // true = ya se vio, false = activa el puntito rojo

    // Para la redirección inteligente (lo que hablamos del horario_id)
    public int referenciaId;
    public String origenReferencia; // "HORARIO", "ENCOMIENDA"

    // Constructor vacío requerido por Room
    public NotificacionEntity() {}

    // Constructor útil para crear nuevas
    public NotificacionEntity(String titulo, String contenido, String tipo, String fecha) {
        this.titulo = titulo;
        this.contenido = contenido;
        this.tipo = tipo;
        this.fecha = fecha;
        this.leido = false; // Por defecto nace no leída
    }
}