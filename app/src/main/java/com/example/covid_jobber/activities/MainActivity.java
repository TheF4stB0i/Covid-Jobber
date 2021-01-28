package com.example.covid_jobber.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;

import com.example.covid_jobber.R;
import com.example.covid_jobber.classes.Job;
import com.example.covid_jobber.classes.services.ApiCall;
import com.example.covid_jobber.classes.services.ApiHandler;
import com.example.covid_jobber.classes.services.Filter;
import com.example.covid_jobber.databinding.ActivityMainBinding;
import com.example.covid_jobber.fragments.FavoritesFragment;
import com.example.covid_jobber.fragments.FiltersFragment;
import com.example.covid_jobber.fragments.NavbarFragment;
import com.example.covid_jobber.fragments.SettingsFragment;
import com.example.covid_jobber.fragments.SwipeFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.Request;


public class MainActivity extends AppCompatActivity {
    public ActivityMainBinding mainBinding;
    private final ApiHandler handler = new ApiHandler();
    private final  SwipeFragment swipeFragment = new SwipeFragment();
    private final FavoritesFragment favoritesFragment = new FavoritesFragment();
    private final  FiltersFragment filtersFragment = new FiltersFragment(); // vllt autofilter
    private final SettingsFragment settingsFragment = new SettingsFragment();
    private final NavbarFragment navbarFragment = new NavbarFragment();

    private SharedPreferences prefs;
    public String language;
    public String darkMode;

    private final MainActivity instance = this;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        filtersFragment.fillCategorySpinner();
        setContentView(R.layout.activity_main);

        prefs = getApplicationContext().getSharedPreferences("FilterPref", MODE_PRIVATE);
//        Get Language and Darkmode from prefs
        getSettingsPreferences();
        setLanguage();
        setMode();

//        At first Swipe Fragment in Content Frame
        replaceFrame(R.id.content_frame, swipeFragment);

//        Put Navbar Fragment in Navbar Frame
        replaceFrame(R.id.navbar_frame, navbarFragment);

    }

    public void changeToSwipe(){
        replaceFrame(R.id.content_frame, swipeFragment);
    }
    public void changeToFavorites(){
        replaceFrame(R.id.content_frame, favoritesFragment);
    }
    public void changeToFilters(){
        replaceFrame(R.id.content_frame, filtersFragment);
    }
    public void changeToSettings(){
        replaceFrame(R.id.content_frame, settingsFragment);
    }

//    Replaces Frame with frameID with Fragment
    public void replaceFrame(int frameID, Fragment fragment){
        // check if content frame exists
        if(findViewById(frameID) != null){
//            Check if this Fragment is already in Frame
            if(this.getSupportFragmentManager().findFragmentById(frameID) != fragment){
                // replace container with fragment object
                getSupportFragmentManager().beginTransaction().replace(frameID,fragment).commitAllowingStateLoss();
                System.out.println("Changed Fragment");
            }
        }

    }

    public void resultsToJobs(JSONArray results) throws JSONException {
        List<Job> jobs = new ArrayList<>();
        for(int i = 0; i< results.length(); i++){
            jobs.add(new Job((JSONObject) results.get(i)));
        }
        System.out.println(jobs.size());

        //  make Jobs into cards
//        List<String> jobtitles = new ArrayList<>();
//        for (Job j:jobs) {
//            jobtitles.add(j.getTitle());
//        }
        swipeFragment.addJob(jobs);
    }
    public FiltersFragment getFilterFragment(){
        return filtersFragment;
    }
    public FavoritesFragment getFavoritesFragment(){
        return favoritesFragment;
    }

    public ApiHandler getHandler(){
        return handler;
    }

//    public FilterOptions getFilterOptions(){
//        return filterOptions;
//    }

    public void addFavoriteJob(Job job){
        favoritesFragment.addFavorite(job);
    }

    public SharedPreferences getPrefs(){
        return prefs;
    }

    // Assign variables from SharedPreferences
    private void getSettingsPreferences(){
        language = prefs.getString("language","Deutsch");
        darkMode = prefs.getString("darkMode","System");

        Log.d("TAG","SP DATA"+"\n"+
                "language: "+language+"\n"+
                "darkMode: "+darkMode+"\n"
        );

        setLanguage();
        setMode();
    }

    public void setLanguage(){
        String localeCode = "de";
        if (language.equals("English")){
            localeCode = "en";
        }
        Resources resources = getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        Configuration config = resources.getConfiguration();
        Locale locale = new Locale(localeCode.toLowerCase());
        config.setLocale(locale);
        Locale.setDefault(locale);
        resources.updateConfiguration(config, dm);

        SharedPreferences.Editor editor = prefs.edit();
        System.out.println("editor should write");
        editor.putString("language",language);
        editor.apply();
    }

    public void setMode(){
        switch (darkMode){
            case "On": AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES); break;
            case "Off": AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); break;
            case "System": AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM); break;
        }

        SharedPreferences.Editor editor = prefs.edit();
        System.out.println("editor should write");
        editor.putString("darkMode",darkMode);
        editor.apply();
    }


}