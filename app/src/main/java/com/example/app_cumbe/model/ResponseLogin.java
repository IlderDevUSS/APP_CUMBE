package com.example.app_cumbe.model;

public class ResponseLogin {
    private String access_token;
    private String nombre;
    private String dni;
    private String email;

    // --- AÑADIR ESTOS DOS CAMPOS ---
    private String telefono;
    private String fecha_nacimiento;

    public String getAccess_token() { return access_token; }
    public String getNombre() { return nombre; }
    public String getDni() { return dni; }
    public String getEmail() { return email; }

    public String getTelefono() { return telefono; }
    public String getFecha_nacimiento() { return fecha_nacimiento; }
}
