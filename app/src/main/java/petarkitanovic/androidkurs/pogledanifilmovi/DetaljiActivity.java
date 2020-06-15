package petarkitanovic.androidkurs.pogledanifilmovi;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.squareup.picasso.Picasso;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import petarkitanovic.androidkurs.pogledanifilmovi.db.DatabaseHelper;
import petarkitanovic.androidkurs.pogledanifilmovi.db.Filmovi;
import petarkitanovic.androidkurs.pogledanifilmovi.net.MyService;
import petarkitanovic.androidkurs.pogledanifilmovi.net.model.Detalji;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetaljiActivity extends AppCompatActivity {


    List<String> drawerItems;
    DrawerLayout drawerLayout;
    ListView drawerList;
    RelativeLayout drawerPane;
    ActionBarDrawerToggle drawerToggle;
    SharedPreferences prefs;
    Toolbar toolbar;
    private Detalji detalji;
    private DatabaseHelper databaseHelper;
    public static final String NOTIF_CHANNEL_ID = "notif_1234";

    private TimePicker vremePicker;
    public static String KEY = "KEY";

    Filmovi film,films;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detalji_activity);

        fillData();
        setupToolbar();
        setupDrawer();

        createNotificationChannel();

        prefs = PreferenceManager.getDefaultSharedPreferences(this);


    }

    private void getDetail(String imdbKey) {
        HashMap<String, String> queryParams = new HashMap<>();
        //TODO unesi api key
        queryParams.put("apikey", "fd705d15");
        queryParams.put("i", imdbKey);


        Call<Detalji> call = MyService.apiInterface().getMovieData(queryParams);
        call.enqueue(new Callback<Detalji>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(Call<Detalji> call, Response<Detalji> response) {
                if (response.code() == 200) {
                    Log.d("REZ", "200");

                    detalji = response.body();
                    if (detalji != null) {


                        ImageView image = DetaljiActivity.this.findViewById(R.id.detalji_slika);

                        Picasso.with(DetaljiActivity.this).load(detalji.getPoster()).into(image);


                        TextView title = DetaljiActivity.this.findViewById(R.id.detalji_naziv);
                        title.setText(detalji.getTitle());

                        TextView year = DetaljiActivity.this.findViewById(R.id.detalji_godina);
                        year.setText("(" + detalji.getYear() + ")");

                        TextView runtime = DetaljiActivity.this.findViewById(R.id.detalji_runtime);
                        runtime.setText(detalji.getRuntime());

                        TextView genre = DetaljiActivity.this.findViewById(R.id.detalji_zanr);
                        genre.setText(detalji.getGenre());

                        TextView language = DetaljiActivity.this.findViewById(R.id.detalji_jezik);
                        language.setText(detalji.getLanguage());

                        TextView plot = DetaljiActivity.this.findViewById(R.id.detalji_plot);
                        plot.setText(detalji.getPlot());
                        TextView awards = DetaljiActivity.this.findViewById(R.id.detalji_awards);
                        awards.setText(detalji.getAwards());

                        RatingBar ratingBar = findViewById(R.id.rating_bar);
                        ratingBar.setRating(1);

                        vremePicker = findViewById(R.id.details_picker);


                        film = new Filmovi();
                        film.setmNaziv(detalji.getTitle());
                        film.setmGodina(detalji.getYear());
                        film.setmImage(detalji.getPoster());
                        film.setmImdbId(detalji.getImdbID());
                        film.setmAwards(detalji.getAwards());
                        film.setmJezik(detalji.getLanguage());
                        film.setmVreme(detalji.getRuntime());
                        film.setmZanr(detalji.getGenre());
                        film.setmPlot(detalji.getPlot());
                        film.setmRating(ratingBar.getRating());


                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            film.setmHour(vremePicker.getHour());
                            film.setmMin(vremePicker.getMinute());
                        }


                        try {
                            getDataBaseHelper().getFilmoviDao().create(film);

                            String tekstNotifikacije = film.getmNaziv() + " je uspesno dodat!";

                            boolean toast = prefs.getBoolean(getString(R.string.toast_key), false);
                            boolean notif = prefs.getBoolean(getString(R.string.notif_key), false);


                            if (toast) {
                                Toast.makeText(DetaljiActivity.this, tekstNotifikacije, Toast.LENGTH_LONG).show();

                            }

                            if (notif) {
                                showNotification(tekstNotifikacije);

                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }

                    }
                }
            }

            @Override
            public void onFailure(Call<Detalji> call, Throwable t) {
                Toast.makeText(DetaljiActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        setTitle("Detalji Filma");


        String imdbKey = getIntent().getStringExtra(KEY);
        if (imdbKey != null) {
            getDetail(imdbKey);
        } else {
            int id = getIntent().getIntExtra("id", -1);
            try {
                films = getDataBaseHelper().getFilmoviDao().queryForId(id);

                TextView title = DetaljiActivity.this.findViewById(R.id.detalji_naziv);
                title.setText(films.getmNaziv());

                TextView godina = DetaljiActivity.this.findViewById(R.id.detalji_godina);
                godina.setText(films.getmGodina());
                TextView trajanje = DetaljiActivity.this.findViewById(R.id.detalji_runtime);
                trajanje.setText(films.getmVreme());
                TextView zanr = DetaljiActivity.this.findViewById(R.id.detalji_zanr);
                zanr.setText(films.getmZanr());
                TextView jezik = DetaljiActivity.this.findViewById(R.id.detalji_jezik);
                jezik.setText(films.getmJezik());
                TextView awards = DetaljiActivity.this.findViewById(R.id.detalji_awards);
                awards.setText(films.getmAwards());
                TextView plot = DetaljiActivity.this.findViewById(R.id.detalji_plot);
                plot.setText(films.getmPlot());

                RatingBar ratingBar = findViewById(R.id.rating_bar);
                ratingBar.setRating(films.getmRating());


                vremePicker = findViewById(R.id.details_picker);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    vremePicker.setHour(films.getmHour());
                    vremePicker.setMinute(films.getmMin());
                }
                ImageView image = DetaljiActivity.this.findViewById(R.id.detalji_slika);

                Picasso.with(DetaljiActivity.this).load(films.getmImage()).into(image);

            } catch (SQLException e) {
                e.printStackTrace();
            }

        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detalji_menu, menu);
        return super.onCreateOptionsMenu(menu);
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
                        startActivity(new Intent(DetaljiActivity.this, PregledSvihPogledanihFilmova.class));

                        break;
                    case 1:
                        title = "Pretraga";
                        startActivity(new Intent(DetaljiActivity.this, Pretraga.class));

                        break;
                    case 2:
                        title = "Podesavanja";
                        startActivity(new Intent(DetaljiActivity.this, SettingsActivity.class));
                        break;
                    case 3:
                        title = "Detalji Filma";
                        deleteSveFilmove();
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

    public void deleteSveFilmove() {

        try {
            List<Filmovi> filmovi = getDataBaseHelper().getFilmoviDao().queryForAll();

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
                                    ArrayList<Filmovi> filmoviZaBrisanje = (ArrayList<Filmovi>) getDataBaseHelper().getFilmoviDao().queryForAll();
                                    getDataBaseHelper().getFilmoviDao().delete(filmoviZaBrisanje);

                                    String tekstNotifikacije = "Svi filmovi obrisani";
                                    boolean toast = prefs.getBoolean(getString(R.string.toast_key), false);
                                    boolean notif = prefs.getBoolean(getString(R.string.notif_key), false);

                                    if (toast) {
                                        Toast.makeText(DetaljiActivity.this, tekstNotifikacije, Toast.LENGTH_LONG).show();

                                    }

                                    if (notif) {
                                        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                        NotificationCompat.Builder builder = new NotificationCompat.Builder(DetaljiActivity.this, NOTIF_CHANNEL_ID);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.delete_film) {
            deleteFilmove();
        }
        return super.onOptionsItemSelected(item);
    }

    public void deleteFilmove() {


        int filmBrisanje = getIntent().getExtras().getInt("id", 0);
        if (filmBrisanje == 0) {
            try {
                getDataBaseHelper().getFilmoviDao().delete(film);

                String tekstNotifikacije = "Film je uspesno obrisan!";

                boolean toast = prefs.getBoolean(getString(R.string.toast_key), false);
                boolean notif = prefs.getBoolean(getString(R.string.notif_key), false);


                if (toast) {
                    Toast.makeText(DetaljiActivity.this, tekstNotifikacije, Toast.LENGTH_LONG).show();

                }

                if (notif) {
                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(DetaljiActivity.this, NOTIF_CHANNEL_ID);
                    builder.setSmallIcon(android.R.drawable.ic_menu_delete);
                    builder.setContentTitle("Notifikacija");
                    builder.setContentText(tekstNotifikacije);

                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);


                    builder.setLargeIcon(bitmap);
                    notificationManager.notify(1, builder.build());

                }
                finish();
            } catch (SQLException e) {
                e.printStackTrace();
            }

        } else {
            try {
                getDataBaseHelper().getFilmoviDao().deleteById(filmBrisanje);

                String tekstNotifikacije = "Film je uspesno obrisan!";

                boolean toast = prefs.getBoolean(getString(R.string.toast_key), false);
                boolean notif = prefs.getBoolean(getString(R.string.notif_key), false);


                if (toast) {
                    Toast.makeText(DetaljiActivity.this, tekstNotifikacije, Toast.LENGTH_LONG).show();

                }

                if (notif) {
                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(DetaljiActivity.this, NOTIF_CHANNEL_ID);
                    builder.setSmallIcon(android.R.drawable.ic_menu_delete);
                    builder.setContentTitle("Notifikacija");
                    builder.setContentText(tekstNotifikacije);

                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);


                    builder.setLargeIcon(bitmap);
                    notificationManager.notify(1, builder.build());

                }
                finish();
            } catch (SQLException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        RatingBar ratingBar = findViewById(R.id.rating_bar);

        int id = getIntent().getExtras().getInt("id", 0);
        if (id == 0) {


            film.setmRating(ratingBar.getRating());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                film.setmHour(vremePicker.getHour());
                film.setmMin(vremePicker.getMinute());
            }
            try {
                getDataBaseHelper().getFilmoviDao().update(film);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {

            films.setmRating(ratingBar.getRating());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                films.setmHour(vremePicker.getHour());
                films.setmMin(vremePicker.getMinute());
            }
            try {
                getDataBaseHelper().getFilmoviDao().update(films);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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

    public void showNotification(String poruka) {

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(DetaljiActivity.this, NOTIF_CHANNEL_ID);
        builder.setSmallIcon(android.R.drawable.ic_menu_add);
        builder.setContentTitle("Notifikacija");
        builder.setContentText(poruka);

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);


        builder.setLargeIcon(bitmap);
        notificationManager.notify(1, builder.build());
    }

    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "My Channel";
            String description = "Description of My Channel";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(NOTIF_CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}

