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
import java.util.List;

import petarkitanovic.androidkurs.pogledanifilmovi.adapteri.AdapterLista;
import petarkitanovic.androidkurs.pogledanifilmovi.db.DatabaseHelper;
import petarkitanovic.androidkurs.pogledanifilmovi.db.Filmovi;

import static petarkitanovic.androidkurs.pogledanifilmovi.Pretraga.KEY;

public class PregledSvihPogledanihFilmova extends AppCompatActivity implements AdapterLista.OnItemClickListener {

    List<String> drawerItems;
    DrawerLayout drawerLayout;
    ListView drawerList;
    RelativeLayout drawerPane;
    ActionBarDrawerToggle drawerToggle;
    SharedPreferences prefs;
    Toolbar toolbar;

    private RecyclerView recyclerView;
    private AdapterLista adapter;
    private RecyclerView.LayoutManager layoutManager;
    private DatabaseHelper databaseHelper;

    private List<Filmovi> filmovi;

    public static final String NOTIF_CHANNEL_ID = "notif_1234";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pregled_pregledanih);

        fillData();
        setupToolbar();
        setupDrawer();


        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        inicalizacijaRecycler();

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
        toolbar.setTitleTextColor(Color.BLACK);
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
                        break;
                    case 1:
                        title = "Pretraga";
                        startActivity(new Intent(PregledSvihPogledanihFilmova.this, Pretraga.class));

                        break;
                    case 2:
                        title = "Podesavanja";
                        startActivity(new Intent(PregledSvihPogledanihFilmova.this, SettingsActivity.class));
                        break;
                    case 3:
                        title = "Pregled svih filmova";


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

    public void deleteFilmove() {

        AlertDialog dialogDelete = new AlertDialog.Builder(this)
                .setTitle("Brisanje svih filmova")
                .setMessage("Da li zelite da obrisete?")
                .setPositiveButton("DA", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        try {
                            ArrayList<Filmovi> filmoviZaBrisanje = (ArrayList<Filmovi>) getDataBaseHelper().getFilmoviDao().queryForAll();
                            getDataBaseHelper().getFilmoviDao().delete(filmoviZaBrisanje);


                            adapter.removeAll();
                            adapter.notifyDataSetChanged();

                            String tekstNotifikacije = "Svi fimovi obrisani";
                            boolean toast = prefs.getBoolean(getString(R.string.toast_key), false);
                            boolean notif = prefs.getBoolean(getString(R.string.notif_key), false);

                            if (toast) {
                                Toast.makeText(PregledSvihPogledanihFilmova.this, tekstNotifikacije, Toast.LENGTH_LONG).show();

                            }

                            if (notif) {
                                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                NotificationCompat.Builder builder = new NotificationCompat.Builder(PregledSvihPogledanihFilmova.this, NOTIF_CHANNEL_ID);
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

    private void inicalizacijaRecycler() {

        recyclerView = findViewById(R.id.rvList);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        try {
            filmovi = getDataBaseHelper().getFilmoviDao().queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }


        adapter = new AdapterLista(this, filmovi, this);
        recyclerView.setAdapter(adapter);

    }

    @Override
    protected void onResume() {
        super.onResume();
        inicalizacijaRecycler();

        setTitle("Pregled svih filmova");
    }

    @Override
    public void onItemClick(int position) {

        Filmovi film = adapter.get(position);

        Intent i = new Intent(PregledSvihPogledanihFilmova.this, DetaljiActivity.class);
        i.putExtra("id", film.getmId());
        startActivity(i);
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        if (databaseHelper != null) {
            OpenHelperManager.releaseHelper();
            databaseHelper = null;
        }

    }

    public DatabaseHelper getDataBaseHelper() {
        if (databaseHelper == null) {
            databaseHelper = OpenHelperManager.getHelper(this, DatabaseHelper.class);
        }
        return databaseHelper;
    }
}
