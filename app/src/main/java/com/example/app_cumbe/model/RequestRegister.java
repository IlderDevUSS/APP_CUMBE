package com.example.app_cumbe.model;

// Importamos esto para asegurarnos que el JSON se genere con snake_case
import com.google.gson.annotations.SerializedName;

public class RequestRegister {

    // Los nombres de las variables coinciden con los campos del formulario
    private String dni;
    private String nombres;
    private String apellidos;
    private String email;
    private String telefono;

    // Usamos @SerializedName para que en el JSON se llame "fecha_nacimiento"
    // como lo espera nuestro backend en Python
    @SerializedName("fecha_nacimiento")
    private String fechaNacimiento;

    private String password;

    // Constructor que usaremos en RegisterActivity
    public RequestRegister(String dni, String nombres, String apellidos, String email, String telefono, String fechaNacimiento, String password) {
        this.dni = dni;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.email = email;
        this.telefono = telefono;
        this.fechaNacimiento = fechaNacimiento;
        this.password = password;
    }

    // Getters (no son estrictamente necesarios para enviar, pero es buena práctica)
}