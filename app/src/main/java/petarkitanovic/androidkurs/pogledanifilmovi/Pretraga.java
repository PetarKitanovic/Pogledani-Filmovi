package petarkitanovic.androidkurs.pogledanifilmovi;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import androidx.core.app.NotificationCompat;
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

import static petarkitanovic.androidkurs.pogledanifilmovi.DetaljiActivity.NOTIF_CHANNEL_ID;

public class Pretraga extends AppCompatActivity implements AdapterPregledFilmova.OnItemClickListener {


    List<String> drawerItems;
    DrawerLayout drawerLayout;
    ListView drawerList;
    RelativeLayout drawerPane;
    ActionBarDrawerToggle drawerToggle;
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

        fillData();
        setupToolbar();
        setupDrawer();

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

    public void deleteFilmove() {

        try {
            List<Filmovi> filmovi = getDatabaseHelper().getFilmoviDao().queryForAll();

            if (filmovi.isEmpty()) {
                Toast.makeText(this, "Lista filmova je vec prazna", Toast.LENGTH_SHORT).show();
            } else {
                AlertDialog dialogDelete = new AlertDialog.Builder(this)
                        .setTitle("Brisanje svih filmova")
                        .setMessage("Da li zelite da obrisete?")
                        .setPositiveButton("DA", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                try {
                                    ArrayList<Filmovi> filmoviZaBrisanje = (ArrayList<Filmovi>) getDatabaseHelper().getFilmoviDao().queryForAll();
                                    getDatabaseHelper().getFilmoviDao().delete(filmoviZaBrisanje);

                                    String tekstNotifikacije = "Svi filmovi obrisani";
                                    boolean toast = prefs.getBoolean(getString(R.string.toast_key), false);
                                    boolean notif = prefs.getBoolean(getString(R.string.notif_key), false);

                                    if (toast) {
                                        Toast.makeText(Pretraga.this, tekstNotifikacije, Toast.LENGTH_LONG).show();

                                    }

                                    if (notif) {
                                        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                        NotificationCompat.Builder builder = new NotificationCompat.Builder(Pretraga.this, NOTIF_CHANNEL_ID);
                                        builder.setSmallIcon(android.R.drawable.ic_menu_delete);
                                        builder.setContentTitle("Notifikacija");
                                        builder.setContentText(tekstNotifikacije);

                                        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);


                                        builder.setLargeIcon(bitmap);
                                        notificationManager.notify(1, builder.build());

                                    }
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }
                            }
                        })
                        .setNegativeButton("NE", null)
                        .show();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }


    }

    private void fillData() {
        drawerItems = new ArrayList<>();
        drawerItems.add("Moji filmovi");
        drawerItems.add("Pretraga");
        drawerItems.add("Podesavanja");
        drawerItems.add("Obrisi sve");

    }

    public void setupToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        final ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.drawer_menu);
            actionBar.setHomeButtonEnabled(true);
            actionBar.show();
        }
    }

    private void setupDrawer() {
        drawerList = findViewById(R.id.left_drawer);
        drawerLayout = findViewById(R.id.drawer_layout);
        drawerPane = findViewById(R.id.drawerPane);

        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String title = "Unknown";
                switch (i) {
                    case 0:
                        title = "Pregled svih filmova";
                        startActivity(new Intent(Pretraga.this, PregledSvihPogledanihFilmova.class));

                        break;
                    case 1:
                        title = "Pretraga";
                        break;
                    case 2:
                        title = "Podesavanja";
                        startActivity(new Intent(Pretraga.this, SettingsActivity.class));
                        break;
                    case 3:
                        title = "Pretraga";
                        deleteFilmove();
                        break;

                    default:
                        break;
                }
                drawerList.setItemChecked(i, true);
                setTitle(title);
                drawerLayout.closeDrawer(drawerPane);
            }
        });
        drawerList.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, drawerItems));


        drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.app_name,
                R.string.app_name
        ) {
            public void onDrawerClosed(View view) {
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                invalidateOptionsMenu();
            }
        };
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
