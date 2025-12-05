package com.example.app_cumbe.model.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface NotificacionDao {

    // Insertar una nueva notificación
    @Insert
    void insertar(NotificacionEntity notificacion);

    // Obtener todas para la lista (las más nuevas primero)
    @Query("SELECT * FROM notificaciones ORDER BY id DESC")
    List<NotificacionEntity> obtenerTodas();

    // ¡CLAVE PARA EL PUNTITO ROJO! Cuenta cuántas no se han leído
    @Query("SELECT COUNT(*) FROM notificaciones WHERE leido = 0")
    int contarNoLeidas();

    // Marcar una como leída
    @Query("UPDATE notificaciones SET leido = 1 WHERE id = :id")
    void marcarComoLeida(int id);

    // Borrar todo (opcional, para limpiar caché)
    @Query("DELETE FROM notificaciones")
    void borrarTodo();

    @Query("SELECT COUNT(*) FROM notificaciones WHERE referenciaId = :refId AND origenReferencia = :tipoRef")
    int existeNotificacion(int refId, String tipoRef);

    // y una palabra clave en el contenido o título para distinguir (ej: "mañana", "pronto", "camino", "destino")
    @Query("SELECT COUNT(*) FROM notificaciones WHERE referenciaId = :refId AND origenReferencia = :origenRef AND (contenido LIKE :palabraClave OR titulo LIKE :palabraClave)")
    int existeNotificacionEspecifica(int refId, String origenRef, String palabraClave);

    // --- NUEVO MÉTODO ---
    // Borrar todas las notificaciones de la tabla
    @Query("DELETE FROM notificaciones")
    void borrarTodas();
}