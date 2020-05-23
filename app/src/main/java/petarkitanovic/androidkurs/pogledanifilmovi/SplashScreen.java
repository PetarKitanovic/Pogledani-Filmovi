package petarkitanovic.androidkurs.pogledanifilmovi;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

public class SplashScreen extends AppCompatActivity {

    private SharedPreferences prefs;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        String splashTime = prefs.getString(getString(R.string.splashtime_key), "3000");

        boolean splash = prefs.getBoolean(getString(R.string.splash_key), false);

        if (splash) {
            setContentView(R.layout.splash_screen);


            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    startActivity(new Intent(SplashScreen.this, PregledSvihPogledanihFilmova.class));
                    finish();
                }
            }, Integer.parseInt(splashTime));
        } else {
            startActivity(new Intent(SplashScreen.this, PregledSvihPogledanihFilmova.class));
            finish();
        }
    }
}
