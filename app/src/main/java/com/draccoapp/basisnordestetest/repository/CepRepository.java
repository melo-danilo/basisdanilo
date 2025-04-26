package com.draccoapp.basisnordestetest.repository;

import com.draccoapp.basisnordestetest.api.CepService;
import com.draccoapp.basisnordestetest.model.response.CepResponse;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;
import retrofit2.Callback;

@Singleton
public class CepRepository {

    private final CepService cepService;

    @Inject
    public CepRepository(CepService cepService) {
        this.cepService = cepService;
    }

    public void getCepInfo(String cep, Callback<CepResponse> callback) {
        // Remove caracteres não numéricos
        cep = cep.replaceAll("[^0-9]", "");

        Call<CepResponse> call = cepService.getCepInfo(cep);
        call.enqueue(callback);
    }
}
