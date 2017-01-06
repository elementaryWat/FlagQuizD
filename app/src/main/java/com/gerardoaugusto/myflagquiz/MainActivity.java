package com.gerardoaugusto.myflagquiz;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.Set;

public class MainActivity extends AppCompatActivity {
    public static final String CHOICES="pref_numberOfChoices";
    public static final String REGIONS="pref_regionsToInclude";

    private boolean phoneDevice=true;
    private boolean preferencesChanged=true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        PreferenceManager.setDefaultValues(this,R.xml.preferences,false);
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
        int screenSize=getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
        //LARGE Y XLARGE son tamaños de pantalla de tablet
        if (screenSize==Configuration.SCREENLAYOUT_SIZE_LARGE || screenSize==Configuration.SCREENLAYOUT_SIZE_XLARGE)
        {
            phoneDevice=false;
        }
        if (phoneDevice)
        {
            //Fuerza a mostrar la aplicacion solo en portrait orientation
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onStart() {
        super.onStart();
        if (preferencesChanged)
        {
            //Llamar a los metodos para actualizar el cuestionario de acuerdo a las preferencias
            MainActivityFragment quizFragment= (MainActivityFragment) getSupportFragmentManager().findFragmentById(R.id.quizFragment);
            quizFragment.updateGuessRows(PreferenceManager.getDefaultSharedPreferences(this));
            quizFragment.updateRegions(PreferenceManager.getDefaultSharedPreferences(this));
            quizFragment.resetQuiz();
            preferencesChanged=false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        int orientation=getResources().getConfiguration().orientation;
        //Mostrara el boton de configuracion en el menu si esta en orientacion vertical
        if (orientation==Configuration.ORIENTATION_PORTRAIT)
        {
            getMenuInflater().inflate(R.menu.menu_main, menu);
            return true;
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Se inicia el activity de las configuraciones
        Intent preferencesIntent=new Intent(this, SettingsActivity.class);
        startActivity(preferencesIntent);
        return super.onOptionsItemSelected(item);
    }
    private SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener= new SharedPreferences.OnSharedPreferenceChangeListener() {
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            preferencesChanged=true;

            MainActivityFragment quizFragment= (MainActivityFragment) getSupportFragmentManager().findFragmentById(R.id.quizFragment);
            if(key.equals(CHOICES)){
                quizFragment.updateGuessRows(sharedPreferences);
                quizFragment.resetQuiz();
            }else if(key.equals(REGIONS)){
                Set<String> regiones=sharedPreferences.getStringSet(REGIONS,null);

                if (regiones!=null && regiones.size()>0){
                    quizFragment.updateRegions(sharedPreferences);
                    quizFragment.resetQuiz();
                }else{
                    //Establece la region por defecto
                    SharedPreferences.Editor editor= sharedPreferences.edit();
                    regiones.add(getString(R.string.default_region));
                    editor.putStringSet(REGIONS,regiones);
                    editor.apply();
                    Toast.makeText(MainActivity.this, R.string.default_region_message, Toast.LENGTH_SHORT).show();
                }
            }
            Toast.makeText(MainActivity.this, R.string.restarting_quiz, Toast.LENGTH_SHORT).show();
        }
    };
}
