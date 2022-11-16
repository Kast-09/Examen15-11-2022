package com.example.examen15_11_2022;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.examen15_11_2022.adapters.PokemonAdapter;
import com.example.examen15_11_2022.entities.Pokemon;
import com.example.examen15_11_2022.services.PokemonesServices;
import com.google.gson.Gson;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    RecyclerView rvPokemones;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://6352ca44a9f3f34c3749009a.mockapi.io/")// -> Aquí va la URL sin el Path
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        PokemonesServices services = retrofit.create(PokemonesServices.class);//aquí instanciamos los servicios
        services.get().enqueue(new Callback<List<Pokemon>>() {
            @Override
            public void onResponse(Call<List<Pokemon>> call, Response<List<Pokemon>> response) {
                List<Pokemon> data = response.body();
                rvPokemones = findViewById(R.id.rvPokemones);
                rvPokemones.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                rvPokemones.setAdapter(new PokemonAdapter(data));
                Log.i("MAIN_APP", "Response: "+response.body().size());
                Log.i("MAIN_APP", new Gson().toJson(data));
            }

            @Override
            public void onFailure(Call<List<Pokemon>> call, Throwable t) {

            }
        });
    }

    public void crearPokemon(View view){
        Intent intent = new Intent(getApplicationContext(), NewPokemonActivity.class);
        startActivity(intent);
    }
}