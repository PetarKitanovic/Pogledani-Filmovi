package petarkitanovic.androidkurs.pogledanifilmovi;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.squareup.picasso.Picasso;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import petarkitanovic.androidkurs.pogledanifilmovi.db.DatabaseHelper;
import petarkitanovic.androidkurs.pogledanifilmovi.db.Filmovi;
import petarkitanovic.androidkurs.pogledanifilmovi.net.MyService;
import petarkitanovic.androidkurs.pogledanifilmovi.net.model.Detalji;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static petarkitanovic.androidkurs.pogledanifilmovi.Pretraga.KEY;

public class DetaljiActivity extends AppCompatActivity {

    private Detalji detalji;
    private DatabaseHelper databaseHelper;
    private SharedPreferences prefs;
    public static final String NOTIF_CHANNEL_ID = "notif_1234";

    private TimePicker vremePicker;
    private EditText cenaEdit;

    public static String KEY = "KEY";

    Filmovi film;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detalji_activity);

        createNotificationChannel();
        setupToolbar();

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

                        String vreme = vremePicker.getCurrentHour() + ":" + vremePicker.getCurrentMinute() + "h";

                        film.setmVreme(vreme);

                        try {
                            getDataBaseHelper().getFilmoviDao().create(film);

                            String tekstNotifikacije = film.getmNaziv() + " je uspesno dodat na repertoar!";

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


        String imdbKey = getIntent().getStringExtra(KEY);
        if (imdbKey != null){
            getDetail(imdbKey);
        }else {
            int id = getIntent().getIntExtra("id",-1);
            try {
                Filmovi film = getDataBaseHelper().getFilmoviDao().queryForId(id);

                TextView title = DetaljiActivity.this.findViewById(R.id.detalji_naziv);
                title.setText(film.getmNaziv());

                TextView godina = DetaljiActivity.this.findViewById(R.id.detalji_godina);
                godina.setText(film.getmGodina());
                TextView trajanje = DetaljiActivity.this.findViewById(R.id.detalji_runtime);
                trajanje.setText(film.getmVreme());
                TextView zanr = DetaljiActivity.this.findViewById(R.id.detalji_zanr);
                zanr.setText(film.getmZanr());
                TextView jezik = DetaljiActivity.this.findViewById(R.id.detalji_jezik);
                jezik.setText(film.getmJezik());
                TextView awards = DetaljiActivity.this.findViewById(R.id.detalji_awards);
                awards.setText(film.getmAwards());
                TextView plot = DetaljiActivity.this.findViewById(R.id.detalji_plot);
                plot.setText(film.getmPlot());

                ImageView image = DetaljiActivity.this.findViewById(R.id.detalji_slika);

                Picasso.with(DetaljiActivity.this).load(film.getmImage()).into(image);

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

    public void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar_detalji);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        final ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete_film:
                deleteFilmove();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void deleteFilmove() {


            int filmBrisanje = getIntent().getExtras().getInt("id", 0);
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

