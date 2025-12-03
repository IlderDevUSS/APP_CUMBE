package com.example.app_cumbe.api;

// 1. Importamos los nuevos modelos
import com.example.app_cumbe.model.AsientoOcupado;
import com.example.app_cumbe.model.Horario;
import com.example.app_cumbe.model.RequestCompra;
import com.example.app_cumbe.model.RequestRegister;
import com.example.app_cumbe.model.ResponseCompra;
import com.example.app_cumbe.model.ResponseRefresh;
import com.example.app_cumbe.model.ResponseRegister;
import com.example.app_cumbe.model.RequestLogin;
import com.example.app_cumbe.model.ResponseLogin;
import com.example.app_cumbe.model.ResponseProximoViaje;
import com.example.app_cumbe.model.Encomienda;
import com.example.app_cumbe.model.RequestUpdateProfile;
import com.example.app_cumbe.model.RutaConductor;
import com.example.app_cumbe.model.Ticket;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    @POST("auth")
    Call<ResponseLogin> login(@Body RequestLogin requestLogin);


    @POST("usuarios/registro")
    Call<ResponseRegister> registrarUsuario(@Body RequestRegister requestRegister);


    @GET("api/encomiendas")
    Call<List<Encomienda>> getEncomiendas(@Header("Authorization") String token);

    @GET("api/proximo-viaje")
    Call<ResponseProximoViaje> getProximoViaje(@Header("Authorization") String token);

    @PUT("usuarios/actualizar")
    retrofit2.Call<ResponseRegister> actualizarUsuario(
            @retrofit2.http.Header("Authorization") String token,
            @retrofit2.http.Body RequestUpdateProfile request
    );

    @POST("auth/refresh")
    Call<ResponseRefresh> refreshToken(@Header("Authorization") String refreshToken);

    @GET("api/horarios")
    Call<List<Horario>> buscarHorarios(
            @Header("Authorization") String token,
            @Query("origen") String origen,
            @Query("destino") String destino,
            @Query("fecha") String fecha
    );

    @GET("api/viajes/{horario_id}/asientos")
    Call<List<AsientoOcupado>> obtenerAsientosOcupados(
            @Header("Authorization") String token,
            @Path("horario_id") int horarioId
    );

    @POST("api/pasajes/comprar")
    Call<ResponseCompra> comprarPasaje(
            @Header("Authorization") String token,
            @Body RequestCompra request
    );

    @GET("api/pasajes/historial")
    Call<List<Ticket>> getHistorialPasajes(@Header("Authorization") String token);

    @GET("conductor/historial_rutas")
    Call<List<RutaConductor>> getHistorialRutasConductor(@Query("dni") String dni);
    // Dentro de ApiService interface
// Obtener la ruta asignada actual (o próxima) del conductor
    @GET("conductor/ruta_actual")
    Call<RutaConductor> getRutaActualConductor(@Query("dni") String dni);

    // Actualizar estado del horario (ej: de PROGRAMADO a EN_RUTA)
    @FormUrlEncoded
    @POST("conductor/actualizar_estado")
    Call<Void> actualizarEstadoRuta(
            @Field("horario_id") int horarioId,
            @Field("nuevo_estado") String nuevoEstado
    );


    @FormUrlEncoded
    @POST("conductor/crear_reporte")
    Call<Void> crearReporteBus(
            @Field("bus_id") int busId,
            @Field("tipo_reporte") String tipo,
            @Field("descripcion") String descripcion,
            @Field("conductor_id") int conductorId,
            @Field("costo_estimado") double costoEstimado // <--- AÑADIDO
    );

    @GET("api/pasajes/{id}")
    Call<Ticket> getPasajePorId(@Header("Authorization") String token, @Path("id") int pasajeId);
}