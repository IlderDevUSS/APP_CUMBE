package com.example.app_cumbe.model;

public class ResponseLogin {
    private String access_token;

    private String nombre;
    private String dni;
    private String email;

    public String getAccess_token() {
        return access_token;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDni() {
        return dni;
    }

    public String getEmail() {
        return email;
    }
}
