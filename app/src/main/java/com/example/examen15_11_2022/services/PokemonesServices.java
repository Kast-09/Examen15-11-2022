package com.example.examen15_11_2022.services;

import com.example.examen15_11_2022.entities.Pokemon;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface PokemonesServices {
    @GET("pokemones")
    Call<List<Pokemon>> get();

    @POST("pokemones")
    Call<Pokemon> create(@Body Pokemon pokemon);

    @PUT("pokemones/{id}")
    Call<Pokemon> update(@Path("id") int id, @Body Pokemon pokemon);

    @DELETE("pokemones/{id}")
    Call<Pokemon> delete(@Path("id") int id);
}
