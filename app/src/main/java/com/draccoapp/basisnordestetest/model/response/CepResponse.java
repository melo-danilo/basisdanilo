package com.draccoapp.basisnordestetest.model.response;

import com.google.gson.annotations.SerializedName;

public class CepResponse {
    @SerializedName("cep")
    private String cep;

    @SerializedName("logradouro")
    private String street;

    @SerializedName("complemento")
    private String complement;

    @SerializedName("bairro")
    private String neighborhood;

    @SerializedName("localidade")
    private String city;

    @SerializedName("uf")
    private String state;

    @SerializedName("erro")
    private boolean error;

    public String getCep() {
        return cep;
    }

    public String getStreet() {
        return street;
    }

    public String getComplement() {
        return complement;
    }

    public String getNeighborhood() {
        return neighborhood;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public boolean isError() {
        return error;
    }
}
