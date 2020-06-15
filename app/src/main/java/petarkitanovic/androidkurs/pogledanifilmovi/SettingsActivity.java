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

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.preference.PreferenceFragmentCompat;

import com.j256.ormlite.android.apptools.OpenHelperManager;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import petarkitanovic.androidkurs.pogledanifilmovi.db.DatabaseHelper;
import petarkitanovic.androidkurs.pogledanifilmovi.db.Filmovi;

import static petarkitanovic.androidkurs.pogledanifilmovi.DetaljiActivity.NOTIF_CHANNEL_ID;


public class SettingsActivity extends AppCompatActivity {


    List<String> drawerItems;
    DrawerLayout drawerLayout;
    ListView drawerList;
    RelativeLayout drawerPane;
    ActionBarDrawerToggle drawerToggle;
    SharedPreferences prefs;
    Toolbar toolbar;

    DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.settings_activity);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        fillData();
        setupToolbar();
        setupDrawer();
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
                        startActivity(new Intent(SettingsActivity.this, PregledSvihPogledanihFilmova.class));
                        break;
                    case 1:
                        title = "Pretraga";
                        startActivity(new Intent(SettingsActivity.this, Pretraga.class));

                        break;
                    case 2:
                        title = "Podesavanja";
                        break;
                    case 3:
                        title = "Podesavanja";
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
                                        Toast.makeText(SettingsActivity.this, tekstNotifikacije, Toast.LENGTH_LONG).show();

                                    }

                                    if (notif) {
                                        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                        NotificationCompat.Builder builder = new NotificationCompat.Builder(SettingsActivity.this, NOTIF_CHANNEL_ID);
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

    @Override
    protected void onResume() {
        super.onResume();
        setTitle("Podesavanja");
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences, rootKey);
        }
    }


}
