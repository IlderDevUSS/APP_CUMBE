package com.example.app_cumbe.api;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.app_cumbe.LoginActivity;
import com.example.app_cumbe.model.ResponseRefresh;

import java.io.IOException;

import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import retrofit2.Call;

import static android.content.Context.MODE_PRIVATE;
import static com.example.app_cumbe.LoginActivity.SP_NAME;

public class TokenAuthenticator implements Authenticator {

    private Context context;

    public TokenAuthenticator(Context context) {
        this.context = context;
    }

    @Override
    public Request authenticate(Route route, Response response) throws IOException {
        // Verificar si ya intentamos refrescar y falló para evitar bucles extremos
        if (responseCount(response) >= 2) {
            return null; // Si ya falló 2 veces, nos rendimos
        }

        SharedPreferences prefs = context.getSharedPreferences(SP_NAME, MODE_PRIVATE);
        String refreshToken = prefs.getString("REFRESH_TOKEN", null);

        if (refreshToken == null) {
            return null;
        }

        // --- CAMBIO CRÍTICO ---
        // Usamos el cliente LIMPIO. Si esta llamada da 401, NO volverá a entrar a este método authenticate()
        ApiService apiService = ApiClient.getRefreshApiService();
        Call<ResponseRefresh> call = apiService.refreshToken("JWT " + refreshToken);

        try {
            Log.d("TokenAuthenticator", "Intentando refrescar token...");
            retrofit2.Response<ResponseRefresh> refreshResponse = call.execute();

            if (refreshResponse.isSuccessful() && refreshResponse.body() != null) {
                String newAccessToken = refreshResponse.body().getAccess_token();
                Log.d("TokenAuthenticator", "Token refrescado con éxito!");

                // Guardar el nuevo token
                prefs.edit().putString("USER_TOKEN", "JWT " + newAccessToken).apply();

                // Reintentar la petición original con el nuevo token
                return response.request().newBuilder()
                        .header("Authorization", "JWT " + newAccessToken)
                        .build();
            } else {
                Log.e("TokenAuthenticator", "Falló el refresh. Código: " + refreshResponse.code());
                // Si el refresh token también venció (401), cerramos sesión.
                forzarLogout();
                return null;
            }
        } catch (Exception e) {
            Log.e("TokenAuthenticator", "Excepción al refrescar: " + e.getMessage());
            return null;
        }
    }

    private int responseCount(Response response) {
        int result = 1;
        while ((response = response.priorResponse()) != null) {
            result++;
        }
        return result;
    }

    private void forzarLogout() {
        // Borramos datos y mandamos al login
        SharedPreferences prefs = context.getSharedPreferences(SP_NAME, MODE_PRIVATE);
        prefs.edit().clear().apply();

        Intent intent = new Intent(context, LoginActivity.class);
        // Flags para limpiar la pila de actividades y que no pueda volver atrás
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }
}