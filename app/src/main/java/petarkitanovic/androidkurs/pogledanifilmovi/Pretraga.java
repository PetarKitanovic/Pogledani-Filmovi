package petarkitanovic.androidkurs.pogledanifilmovi;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.j256.ormlite.android.apptools.OpenHelperManager;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import petarkitanovic.androidkurs.pogledanifilmovi.adapteri.AdapterPregledFilmova;
import petarkitanovic.androidkurs.pogledanifilmovi.db.DatabaseHelper;
import petarkitanovic.androidkurs.pogledanifilmovi.db.Filmovi;
import petarkitanovic.androidkurs.pogledanifilmovi.net.MyService;
import petarkitanovic.androidkurs.pogledanifilmovi.net.model2.Example;
import petarkitanovic.androidkurs.pogledanifilmovi.net.model2.Search;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Pretraga extends AppCompatActivity implements AdapterPregledFilmova.OnItemClickListener{


    SharedPreferences prefs;
    Toolbar toolbar;

    private DatabaseHelper databaseHelper;

    private RecyclerView recyclerView;
    private AdapterPregledFilmova adapter;
    private RecyclerView.LayoutManager layoutManager;

    Button btnSearch;
    EditText movieName;

    public static String KEY = "KEY";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pretraga);

        btnSearch = findViewById(R.id.btn_search);
        movieName = findViewById(R.id.ime_filma);
        recyclerView = findViewById(R.id.rvLista);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(20);




        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getMovieByName(movieName.getText().toString());
            }
        });

    }

    private void getMovieByName(String name) {
        Map<String, String> query = new HashMap<>();
        //TODO upisi api key
        query.put("apikey", "fd705d15");
        query.put("s", name.trim());

        Call<Example> call = MyService.apiInterface().getMovieByName(query);
        call.enqueue(new Callback<Example>() {
            @Override
            public void onResponse(Call<Example> call, Response<Example> response) {

                if (response.code() == 200) {
                    try {
                        Example searches = response.body();

                        ArrayList<Search> search = new ArrayList<>();

                        for (Search e : searches.getSearch()) {

                            if (e.getType().equals("movie")) {
                                search.add(e);
                            }
                        }

                        layoutManager = new LinearLayoutManager(Pretraga.this);
                        recyclerView.setLayoutManager(layoutManager);

                        adapter = new AdapterPregledFilmova(Pretraga.this, search, Pretraga.this);
                        recyclerView.setAdapter(adapter);

                        Toast.makeText(Pretraga.this, "Prikaz filmova.", Toast.LENGTH_SHORT).show();

                    } catch (NullPointerException e) {
                        Toast.makeText(Pretraga.this, "Ne postoji film sa tim nazivom", Toast.LENGTH_SHORT).show();
                    }

                } else {

                    Toast.makeText(Pretraga.this, "Greska sa serverom", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Example> call, Throwable t) {
                Toast.makeText(Pretraga.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        setTitle("Pretraga");
    }

    @Override
    public void onItemClick(int position) {

        Search movie = adapter.get(position);

        Intent i = new Intent(Pretraga.this, DetaljiActivity.class);
        i.putExtra(KEY, movie.getImdbID());
        startActivity(i);

    }

    public DatabaseHelper getDatabaseHelper() {
        if (databaseHelper == null) {
            databaseHelper = OpenHelperManager.getHelper(this, DatabaseHelper.class);
        }
        return databaseHelper;
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        if (databaseHelper != null) {
            OpenHelperManager.releaseHelper();
            databaseHelper = null;
        }

    }

}
