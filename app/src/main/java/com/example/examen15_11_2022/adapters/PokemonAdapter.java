package com.example.examen15_11_2022.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.examen15_11_2022.DetallePokemonActivity;
import com.example.examen15_11_2022.R;
import com.example.examen15_11_2022.entities.Pokemon;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.util.List;

public class PokemonAdapter extends RecyclerView.Adapter {

    List<Pokemon> data;

    public PokemonAdapter(List<Pokemon> data){
        this.data = data;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());//aquí llamamos al contexto

        View itemView = inflater.inflate(R.layout.item_pokemon, parent, false);//aquí hacemos referencia al item creado

        return new PokemonesViewHolder(itemView);//aquí retornamos el itemView creado
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Pokemon pokemon = data.get(position);

        TextView tvNombre = holder.itemView.findViewById(R.id.tvNombre);
        tvNombre.setText(data.get(position).nombre);

        TextView tvTipo = holder.itemView.findViewById(R.id.tvTipo);
        tvTipo.setText(data.get(position).tipo);

        ImageView ivPokemon = holder.itemView.findViewById(R.id.ivPokemon);
        Picasso.get().load(data.get(position).imageURL).into(ivPokemon);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(holder.itemView.getContext(), DetallePokemonActivity.class);
                intent.putExtra("POKEMON_DATA", new Gson().toJson(pokemon));
                holder.itemView.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class PokemonesViewHolder extends RecyclerView.ViewHolder {
        public PokemonesViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
