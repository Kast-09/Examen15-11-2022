package com.example.examen15_11_2022;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.examen15_11_2022.entities.Imagen;
import com.example.examen15_11_2022.entities.ImagenBase64;
import com.example.examen15_11_2022.entities.Pokemon;
import com.example.examen15_11_2022.services.ImagenServices;
import com.example.examen15_11_2022.services.PokemonesServices;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DetallePokemonActivity extends AppCompatActivity {

    public EditText etEditNombre;
    public ImageView ivEditPhoto;
    public TextView tvEditLatitud, tvEditLongitud;

    public String encoded, link, tipo;
    public double latitud, longitud;

    Pokemon pokemon;

    String[] tipos = {"Electrico", "Fuego", "Tierra", "Aire"};
    AutoCompleteTextView acEditTipos;
    ArrayAdapter<String> adapterTipos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_pokemon);

        etEditNombre = findViewById(R.id.etEditNombre);
        ivEditPhoto = findViewById(R.id.ivEditPhoto);
        tvEditLatitud = findViewById(R.id.tvEditLatitud);
        tvEditLongitud = findViewById(R.id.tvEditLongitud);

        acEditTipos = findViewById(R.id.acEditTipos);
        adapterTipos = new ArrayAdapter<String>(this, R.layout.list_item, tipos);
        acEditTipos.setAdapter(adapterTipos);

        acEditTipos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                tipo = parent.getItemAtPosition(position).toString();
                Toast.makeText(getApplicationContext(), "Tipo" + tipo, Toast.LENGTH_SHORT).show();
            }
        });

        Intent intent = getIntent();
        String pokemonJson = intent.getStringExtra("POKEMON_DATA");

        if(pokemonJson!= null){
            pokemon = new Gson().fromJson(pokemonJson, Pokemon.class);
            etEditNombre.setText(pokemon.nombre);
            Picasso.get().load(pokemon.imageURL).into(ivEditPhoto);
            tvEditLatitud.setText("Latitud: " + pokemon.latitud);
            tvEditLongitud.setText("Longitud" + pokemon.longitud);
        }
        if (pokemon == null) return;
    }

    public void actualizarFoto(View view){
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            OpenGallery();
        } else {//pido el permiso
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 101);//un número cualquiera
        }
    }

    public void OpenGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 1001);//los requestCode deben ser únicos
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1001) {
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(data.getData(), filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            Bitmap imageBitmap = BitmapFactory.decodeFile(picturePath);
            ivEditPhoto.setImageBitmap(imageBitmap);

            //esto sirve para convertir bitmap a base64
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();

            encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);

            Log.i("MAIN_APP", encoded);

            ObtenerBase64();
        }
    }

    public void ObtenerBase64() {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.imgur.com/")// -> Aquí va la URL sin el Path
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ImagenBase64 imagen = new ImagenBase64();
        imagen.image = encoded;

        ImagenServices services = retrofit.create(ImagenServices.class);
        services.create(imagen).enqueue(new Callback<Imagen>() {
            @Override
            public void onResponse(Call<Imagen> call, Response<Imagen> response) {
                Log.i("MAIN_APP", String.valueOf(response.code()));
                Imagen data = response.body();
                link = data.data.link;
                Log.i("MAIN_APP", new Gson().toJson(data));
            }

            @Override
            public void onFailure(Call<Imagen> call, Throwable t) {
                Log.i("MAIN_APP", "Fallo a obtener datos");
            }
        });
    }

    public void actualizarUbicacion(View view){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,}, 1000);
        } else {
            locationStart();
        }
    }

    private void locationStart() {
        LocationManager mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        DetallePokemonActivity.Localizacion Local = new DetallePokemonActivity.Localizacion();
        Local.setMainActivity(this);
        final boolean gpsEnabled = mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!gpsEnabled) {
            Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(settingsIntent);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,}, 1000);
            return;
        }
        mlocManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, (LocationListener) Local);
        mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, (LocationListener) Local);
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1000) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationStart();
                return;
            }
        }
    }

    /* Aqui empieza la Clase Localizacion */
    public class Localizacion implements LocationListener {

        DetallePokemonActivity newPokemonActivity;
        public DetallePokemonActivity getMainActivity() {
            return newPokemonActivity;
        }

        public void setMainActivity(DetallePokemonActivity mainActivity) {
            this.newPokemonActivity = mainActivity;
        }

        @Override
        public void onLocationChanged(Location loc) {
            // Este metodo se ejecuta cada vez que el GPS recibe nuevas coordenadas
            // debido a la deteccion de un cambio de ubicacion
            loc.getLatitude();
            loc.getLongitude();
            latitud = loc.getLatitude();
            longitud = loc.getLongitude();
            tvEditLatitud.setText("Latitud: " + latitud);
            tvEditLongitud.setText("Longitud: " + longitud);
        }

        @Override
        public void onProviderDisabled(String provider) {
            // Este metodo se ejecuta cuando el GPS es desactivado
            Toast.makeText(getApplicationContext(), "GPS Desactivado", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProviderEnabled(String provider) {
            // Este metodo se ejecuta cuando el GPS es activado
            Toast.makeText(getApplicationContext(), "GPS Activado", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            switch (status) {
                case LocationProvider.AVAILABLE:
                    Log.d("debug", "LocationProvider.AVAILABLE");
                    break;
                case LocationProvider.OUT_OF_SERVICE:
                    Log.d("debug", "LocationProvider.OUT_OF_SERVICE");
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    Log.d("debug", "LocationProvider.TEMPORARILY_UNAVAILABLE");
                    break;
            }
        }
    }

    public void actualizarPokemon(View view){
        if(etEditNombre.getText().toString() != null) pokemon.nombre = etEditNombre.getText().toString();
        if(tipo != null)  pokemon.tipo = tipo;
        if(link != null)  pokemon.imageURL = link;
        if(latitud != 0)  pokemon.latitud = latitud;
        if(longitud != 0)  pokemon.longitud = longitud;

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://6352ca44a9f3f34c3749009a.mockapi.io/")// -> Aquí va la URL sin el Path
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        PokemonesServices services = retrofit.create(PokemonesServices.class);
        services.update(pokemon.id, pokemon).enqueue(new Callback<Pokemon>() {
            @Override
            public void onResponse(Call<Pokemon> call, Response<Pokemon> response) {
                Log.i("MAIN_APP", String.valueOf(response.code()));
                Toast.makeText(getApplicationContext(), "Se actualizó correctamente", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<Pokemon> call, Throwable t) {
                Log.i("MAIN_APP", "No se actualizó");
                Toast.makeText(getApplicationContext(), "No se actualizó", Toast.LENGTH_SHORT).show();
            }
        });

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }

    public void eliminarPokemon(View view){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://6352ca44a9f3f34c3749009a.mockapi.io/")// -> Aquí va la URL sin el Path
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        PokemonesServices services = retrofit.create(PokemonesServices.class);
        services.delete(pokemon.id).enqueue(new Callback<Pokemon>() {
            @Override
            public void onResponse(Call<Pokemon> call, Response<Pokemon> response) {
                Log.i("MAIN_APP", String.valueOf(response.code()));
                Toast.makeText(getApplicationContext(), "Se elimino correctamente", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<Pokemon> call, Throwable t) {
                Log.i("MAIN_APP", "No se elimino");
                Toast.makeText(getApplicationContext(), "No se elimino", Toast.LENGTH_SHORT).show();
            }
        });

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }
}