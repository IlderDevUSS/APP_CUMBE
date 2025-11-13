package com.example.app_cumbe.model;

// 1. IMPORTAMOS LA ANOTACIÓN
import com.google.gson.annotations.SerializedName;

public class RequestLogin {

    // 2. AÑADIMOS LA ANOTACIÓN
    // En Java se llama 'email', pero en JSON se llamará 'username'
    @SerializedName("username")
    private String email;

    private String password;

    public RequestLogin(String email, String password) {
        this.email = email;
        this.password = password;
    }

    // Los getters no cambian
    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}