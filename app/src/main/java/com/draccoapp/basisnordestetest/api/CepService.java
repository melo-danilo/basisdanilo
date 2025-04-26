package com.draccoapp.basisnordestetest.api;

import com.draccoapp.basisnordestetest.model.response.CepResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface CepService {
    @GET("{cep}/json/")
    Call<CepResponse> getCepInfo(@Path("cep") String cep);
}
