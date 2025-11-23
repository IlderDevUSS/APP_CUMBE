package com.example.app_cumbe.api;

import android.content.Context;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static final String BASE_URL = "http://byRedli.pythonanywhere.com/"; // Asegúrate de que sea tu URL correcta
    private static Retrofit retrofit = null;
    private static Context appContext;

    public static void init(Context context) {
        appContext = context.getApplicationContext();
    }

    // Cliente NORMAL (Con portero) - Usado para todo excepto refrescar
    public static ApiService getApiService() {
        if (retrofit == null) {
            OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();

            if (appContext != null) {
                // Aquí añadimos al portero que causa el bucle si no tenemos cuidado
                clientBuilder.authenticator(new TokenAuthenticator(appContext));
            }

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(clientBuilder.build())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(ApiService.class);
    }

    // --- NUEVO MÉTODO ---
    // Cliente LIMPIO (Sin portero) - Usado EXCLUSIVAMENTE para refrescar el token
    public static ApiService getRefreshApiService() {
        // Creamos un cliente nuevo y básico, SIN Authenticator
        OkHttpClient client = new OkHttpClient.Builder().build();

        Retrofit refreshRetrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client) // Cliente limpio
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return refreshRetrofit.create(ApiService.class);
    }
}