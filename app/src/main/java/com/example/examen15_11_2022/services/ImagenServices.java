package com.example.examen15_11_2022.services;

import com.example.examen15_11_2022.entities.Imagen;
import com.example.examen15_11_2022.entities.ImagenBase64;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ImagenServices {
    @Headers("Authorization: Client-ID 8bcc638875f89d9")
    @POST("3/image")
    Call<Imagen> create(@Body ImagenBase64 image);
}
