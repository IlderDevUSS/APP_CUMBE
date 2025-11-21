package com.example.app_cumbe.model;

import com.google.gson.annotations.SerializedName;

public class RequestUpdateProfile {

    @SerializedName("fecha_nacimiento")
    private String fechaNacimiento;

    private String password;

    public RequestUpdateProfile(String fechaNacimiento, String password) {
        this.fechaNacimiento = fechaNacimiento;
        this.password = password;
    }

}