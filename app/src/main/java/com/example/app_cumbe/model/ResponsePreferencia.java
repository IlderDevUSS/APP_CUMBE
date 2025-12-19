package com.example.app_cumbe.model;

import com.google.gson.annotations.SerializedName;

public class ResponsePreferencia {
    private String preferenceId;


    @SerializedName("url_pago")
    private String urlPago;

    public String getPreferenceId() {
        return preferenceId;
    }

    public String getUrlPago() {
        return urlPago;
    }
}