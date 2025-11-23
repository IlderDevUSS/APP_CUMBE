package com.example.app_cumbe.api;

// 1. Importamos los nuevos modelos
import com.example.app_cumbe.model.RequestRegister;
import com.example.app_cumbe.model.ResponseRefresh;
import com.example.app_cumbe.model.ResponseRegister;
import com.example.app_cumbe.model.RequestLogin;
import com.example.app_cumbe.model.ResponseLogin;
import com.example.app_cumbe.model.ResponseProximoViaje;
import com.example.app_cumbe.model.Encomienda;
import com.example.app_cumbe.model.RequestUpdateProfile;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;

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
}