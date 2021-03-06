package com.example.covid_jobber.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.covid_jobber.activities.MainActivity;
import com.example.covid_jobber.classes.Category;
import com.example.covid_jobber.classes.services.ApiCall;
import com.example.covid_jobber.classes.services.Filter;
import com.example.covid_jobber.classes.services.FilterType;
import com.example.covid_jobber.databinding.FragmentFiltersBinding;
import com.example.covid_jobber.enums.ContractTime;
import com.example.covid_jobber.enums.Language;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FiltersFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FiltersFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private FragmentFiltersBinding binding;
    private MainActivity mainActivity;

    // Persistence
    SharedPreferences prefs;

    // Variables
//    chosen options
    private int expSalary;
    private ContractTime contractTime;
    private Category category;
    private List<String> keywords = new ArrayList<>();

    private int surrounding;
    private double latitude;
    private double longitude;

//    available options
    private final List<ContractTime> contractTimes = new ArrayList<>(Arrays.asList(ContractTime.EITHER, ContractTime.FULL_TIME, ContractTime.PART_TIME));
    private final List<Integer> surroundingList = new ArrayList<>(Arrays.asList(50, 75, 100, 150, 250));
    private List<View> editOptions;

    private final List<Category> categories = new ArrayList<>();

//    debug variables
    private boolean editing = false;
    private boolean filtersActive = false;
    private boolean locationActive = false;


    public FiltersFragment() {
        // Required empty public constructor
    }

    public static FiltersFragment newInstance() {

        return new FiltersFragment();
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentFiltersBinding.inflate(inflater, container, false);




//        // Assign variables from SharedPreferences
          getPreferences();

//        Inputs ------------------------------------------------

        //        Salary Input
        binding.inputFilterSalary.setText(String.valueOf(expSalary));
        //        Keyword Inputs
        for (int i = 0; i < keywords.size(); i++) {
            addKeywordView(keywords.get(i));
            EditText view = (EditText) binding.layoutFilterKeywords.getChildAt(i);
            view.setEnabled(false);
        }

//        Buttons ----------------------------------------------

        //        Edit Button
        binding.btnFilterEdit.setOnClickListener(this);
        //        Save Button
        binding.btnFilterSave.setOnClickListener(this);
        binding.btnFilterSave.setVisibility(View.INVISIBLE);
        //        Add Keyword Button
        binding.btnFilterAddKeyword.setOnClickListener(this);
        //        Delete Keyword Button
        binding.btnFilterDeleteKeyword.setOnClickListener(this);
        //        Location Permission Button
        binding.btnLocationPermission.setOnClickListener(this);

        if(locationActive && ContextCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            String btnText = "Aktiviert";
            if(mainActivity.language == Language.ENGLISH){
                btnText = "Activated";
            }
            binding.btnLocationPermission.setText(btnText);
        }else{
            // reset gps data
            latitude = 0;
            longitude = 0;
            locationActive = false;
            String btnText = "Deaktiviert";
            if(mainActivity.language == Language.ENGLISH){
                btnText = "Deactivated";
            }
            binding.btnLocationPermission.setText(btnText);
        }

//        Toggles --------------------------------------------
        //        Surrounding Option visible/not visible -> depends on locationActive
        if(locationActive){
            binding.txtFilterSurrounding.setVisibility(View.VISIBLE);
            binding.spinnerFilterSurrounding.setVisibility(View.VISIBLE);
        }
        else{
            binding.txtFilterSurrounding.setVisibility(View.GONE);
            binding.spinnerFilterSurrounding.setVisibility(View.GONE);
        }

//        Spinner -----------------------------------------------------

        //        Category Spinner
        binding.spinnerFilterCategory.setOnItemSelectedListener(this);
        List<String> translatedCategories = new ArrayList<>();
        for (Category c:categories) {
            translatedCategories.add(c.getTranslation(mainActivity.language));
        }
//        Collections.sort(translatedCategories);
        binding.spinnerFilterCategory.setAdapter(new ArrayAdapter<>(this.getContext(), android.R.layout.simple_dropdown_item_1line, translatedCategories));
        binding.spinnerFilterCategory.setSelection(translatedCategories.indexOf(category.getTranslation(mainActivity.language)));

        //        Contract Time Spinner
        binding.spinnerFilterContractTime.setOnItemSelectedListener(this);
        List<String> translatedContractTimes = new ArrayList<>();
        for (ContractTime c:contractTimes) {
            translatedContractTimes.add(c.getTranslation(mainActivity.language));
        }
        binding.spinnerFilterContractTime.setAdapter(new ArrayAdapter<>(this.getContext(), android.R.layout.simple_dropdown_item_1line, translatedContractTimes));
        if (contractTime != null) {
            binding.spinnerFilterContractTime.setSelection(translatedContractTimes.indexOf(contractTime.getTranslation(mainActivity.language)));
        }

        //        Surrounding Spinner
        binding.spinnerFilterSurrounding.setOnItemSelectedListener(this);
        binding.spinnerFilterSurrounding.setAdapter(new ArrayAdapter<>(this.getContext(), android.R.layout.simple_dropdown_item_1line, surroundingList));
        binding.spinnerFilterSurrounding.setSelection(surroundingList.indexOf(surrounding));

//        --------------------------------------------------------------
//        All options set to disabled if not in edit mode
        editOptions = new ArrayList<>(Arrays.asList(binding.inputFilterSalary, binding.spinnerFilterCategory, binding.spinnerFilterContractTime,
                binding.btnLocationPermission,  binding.spinnerFilterSurrounding, binding.btnFilterAddKeyword, binding.btnFilterDeleteKeyword));
        for (View option:editOptions) {
            option.setEnabled(false);
        }


        return binding.getRoot();
    }

    @Override
    public void onStart() {
        if(editing){
            endEditing();
        }
        super.onStart();
    }

    @Override
    public void onDestroyView() {

        if(editing){
            endEditing();
        }
        binding = null;

        super.onDestroyView();
    }

    //    Currently only used for toggle button
    @Override
    public void onClick(View v) {
        if (v == binding.btnFilterEdit) {
            startEditing();

        }
//        Button to end edting
        else if (v == binding.btnFilterSave) {
            endEditing();

        }
//        Add Keyword Button
        else if(v == binding.btnFilterAddKeyword){
            addKeywordView("");
            keywords.add("");
        }
//        Delete Keyword Button
        else if(v == binding.btnFilterDeleteKeyword){
            int index = keywords.size()-1;
            if(index >= 0){
                binding.layoutFilterKeywords.removeViewAt(index);
                keywords.remove(index);
            }

        }
//        Location Button
        else if (v == binding.btnLocationPermission) {
            if(locationActive){
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                String message = "";
                switch (mainActivity.language){
                    case GERMAN:
                        message = "Wenn du den Standort deaktivierst, wird deine Suche nicht geografisch eingegrenzt. Du erhältst Vorschläge aus ganz Deutschland. Wenn du zudem den Standort nicht mehr freigeben möchtest, kannst du das in den Einstellungen ändern.\n\nEinstellungen > Standort > App-Berechtigungen > Covid-Jobber > Deny"; break;
                    case ENGLISH:
                        message = "If you deactivate your location, your search cannot be filtered geographically. You will see job offers from all over Germany. \n Settings > Location > App access to location > Covid-Jobber > Deny"; break;
                }
                builder.setMessage(message)
                        .setCancelable(false)
                        .setPositiveButton("Ok", (dialog, id) -> dialog.cancel());
                AlertDialog alert = builder.create();
                alert.show();

                // reset gps data
                latitude = 0;
                longitude = 0;

                locationActive = false;
                String btnText = "Deaktiviert";
                if (mainActivity.language == Language.ENGLISH) {
                    btnText = "Deactivated";
                }
                binding.btnLocationPermission.setText(btnText);
                binding.spinnerFilterSurrounding.setVisibility(View.GONE);
                binding.txtFilterSurrounding.setVisibility(View.GONE);

            } else{
                if (ContextCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    updateLocation();
                    locationActive = true;
                    String btnText = "Aktiviert";
                    if(mainActivity.language == Language.ENGLISH){
                        btnText = "Activated";
                    }
                    binding.btnLocationPermission.setText(btnText);
                    binding.spinnerFilterSurrounding.setVisibility(View.VISIBLE);
                    binding.txtFilterSurrounding.setVisibility(View.VISIBLE);
                } else {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
                }
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if(parent == binding.spinnerFilterCategory){
            category = Category.getByTranslation(binding.spinnerFilterCategory.getSelectedItem().toString(), categories);
            System.out.println("category chosen: "+category);
        }
        else if(parent == binding.spinnerFilterContractTime){
            contractTime = ContractTime.getByTranslation((String) binding.spinnerFilterContractTime.getSelectedItem());
            System.out.println("contract time chosen: "+contractTime.toString());
        }
        else if(parent == binding.spinnerFilterSurrounding){
            surrounding = (int) binding.spinnerFilterSurrounding.getSelectedItem();
            System.out.println("surrounding chosen: "+surrounding);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        if(parent == binding.spinnerFilterCategory){
            category = Category.getByTranslation(binding.spinnerFilterCategory.getSelectedItem().toString(), categories);
            System.out.println("category chosen: "+category);
        }
        else if(parent == binding.spinnerFilterContractTime){
            contractTime = ContractTime.getByTranslation((String) binding.spinnerFilterContractTime.getSelectedItem());
            System.out.println("contract time chosen: "+contractTime.toString());
        }
        else if(parent == binding.spinnerFilterSurrounding){
            surrounding = (int) binding.spinnerFilterSurrounding.getSelectedItem();
            System.out.println("surrounding chosen: "+surrounding);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            updateLocation();
            locationActive = true;
            String btnText = "Aktiviert";
            if(mainActivity.language == Language.ENGLISH){
                btnText = "Activated";
            }
            binding.btnLocationPermission.setText(btnText);
            binding.spinnerFilterSurrounding.setVisibility(View.VISIBLE);
            binding.txtFilterSurrounding.setVisibility(View.VISIBLE);
        }
        else{
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            String message = "";
            switch (mainActivity.language){
                case GERMAN:
                    message = "Wenn du den Standort nicht freigeben möchtest, kann deine Suche nicht geografisch eingegrenzt werden. Du erhältst Vorschläge aus ganz Deutschland."; break;
                case ENGLISH:
                    message = "If you do not want to share your location, your search cannot be filtered geographically. You will receive job offers from all Germany."; break;
            }
            builder.setMessage(message)
                    .setCancelable(false)
                    .setPositiveButton("Ok", (dialog, id) -> dialog.cancel());
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    @SuppressLint("MissingPermission")
    public void updateLocation() {
        FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this.getContext());
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this::onLocationReceived);
    }


    public void onLocationReceived(Location location){
        if(location != null){
            latitude = location.getLatitude();
            longitude = location.getLongitude();
        }
        else{
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            String message = "";
            switch (mainActivity.language){
                case GERMAN:
                    message = "Entschuldige, dein Standort konnte nicht bestimmt werden. Prüfe ob GPS eingeschaltet ist und dein Gerät geortet werden kann."; break;
                case ENGLISH:
                    message = "Sorry, your location could not be determined. Check if your device has GPS enabled."; break;
            }
            builder.setMessage(message)
                    .setCancelable(false)
                    .setPositiveButton("Ok", (dialog, id) -> dialog.cancel());
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    //      Berechnung zur Bestimmung ob ein Ort aus der API innerhalb des ausgewählten Umkreises des Users liegt
    public boolean checkDistance(int surrounding, double latlocation, double lonlocation){
        double dx, dy, lat, distance;

        lat = (latitude + latlocation) / 2 * (Math.PI/180);
        dx = 111.3 * Math.cos(lat) * (longitude - lonlocation);
        dy = 111.3 * (latitude - latlocation);
        distance = Math.sqrt(dx * dx + dy * dy);

        return distance <= surrounding;
    }


    public void fillCategorySpinner(){

        // Hardcoded now cause categories never change
        // Getting categories through APICall is possible, but causes unnecessary problems and is
        // harder to deal with
        categories.add(new Category("-","Beliebig"));
        categories.add(new Category("consultancy-jobs","Beraterstellen"));
        categories.add(new Category("charity-voluntary-jobs","Gemeinnützige & ehrenamtliche Stellen"));
        categories.add(new Category("property-jobs","Immobilienstellen"));
        categories.add(new Category("it-jobs","IT-Stellen"));
        categories.add(new Category("legal-jobs","Juristische Stellen"));
        categories.add(new Category("customer-services-jobs","Kundendienststellen"));
        categories.add(new Category("teaching-jobs","Lehrberufe"));
        categories.add(new Category("other-general-jobs","Sonstige/Allgemeine Stellen"));
        categories.add(new Category("accounting-finance-jobs","Stellen aus Buchhaltung & Finanzwesen"));
        categories.add(new Category("retail-jobs","Stellen aus Einzelhandel"));
        categories.add(new Category("manufacturing-jobs","Stellen aus Fertigung"));
        categories.add(new Category("hospitality-catering-jobs","Stellen aus Gastronomie & Catering"));
        categories.add(new Category("healthcare-nursing-jobs","Stellen aus Gesundheitswesen & Pflege"));
        categories.add(new Category("trade-construction-jobs","Stellen aus Handel & Bau"));
        categories.add(new Category("domestic-help-cleaning-jobs","Stellen aus Haushaltshilfen & Reinigung"));
        categories.add(new Category("creative-design-jobs","Stellen aus Kreation & Design"));
        categories.add(new Category("logistics-warehouse-jobs","Stellen aus Logistik & Lagerhaltung"));
        categories.add(new Category("hr-jobs","Stellen aus Personal & Personalbeschaffung"));
        categories.add(new Category("pr-advertising-marketing-jobs","Stellen aus PR, Werbung & Marketing"));
        categories.add(new Category("social-work-jobs","Stellen aus Sozialarbeit"));
        categories.add(new Category("travel-jobs","Stellen aus Tourismus"));
        categories.add(new Category("energy-oil-gas-jobs","Stellen aus Versorgungsunternehmen"));
        categories.add(new Category("maintenance-jobs","Stellen aus Wartung"));
        categories.add(new Category("scientific-qa-jobs","Stellen aus Wissenschaft & Qualitätssicherung"));
        categories.add(new Category("graduate-jobs","Stellen für Hochschulabsolventen"));
        categories.add(new Category("engineering-jobs","Technikerstellen"));
        categories.add(new Category("part-time-jobs","Teilzeitstellen"));
        categories.add(new Category("unknown","Unknown"));
        categories.add(new Category("sales-jobs","Vertriebsstellen"));
        categories.add(new Category("admin-jobs","Verwaltungsstellen"));

    }


    public Filter getFilter(){
        Filter filter = new Filter();
        filter.addFilter(FilterType.CATEGORY,category.toString());
        filter.addFilter(FilterType.SALARY,String.valueOf((int) Math.floor(expSalary)));
        if(!(contractTime.toString()).equals("-")){
            filter.addFilter(contractTime.toString()+"=1");
        }
        String keywordString="";
        for(int i = 0;i<keywords.size();i++){
            if(i == keywords.size()-1){
                keywordString = keywordString + keywords.get(i);
            }else{
                keywordString = keywordString + keywords.get(i) + ",";
            }
        }
        if(!keywordString.equals("")){
            filter.addFilter(FilterType.KEYWORDS,keywordString);
        }

        return filter;
    }


//    Private Functions -----------------------------------------------

    //    enables edit mode -> activated by edit button
    private void startEditing(){
        editing = true;
        binding.btnFilterSave.setVisibility(View.VISIBLE);
        binding.btnFilterEdit.setEnabled(false);

//        Enable all edit options
        for (View option:editOptions) {
            option.setEnabled(true);
        }

        for (int i = 0; i < keywords.size(); i++) {
            EditText view = (EditText) binding.layoutFilterKeywords.getChildAt(i);
            view.setEnabled(true);
        }
    }

    //    disables edit mode -> activated by save button
    private void endEditing(){

        editing = false;
        binding.btnFilterSave.setVisibility(View.INVISIBLE);
        binding.btnFilterEdit.setEnabled(true);

//        Disble all edit options
        for (View option:editOptions) {
            option.setEnabled(false);
        }

//        Save salary input value
        try{
            expSalary = Integer.parseInt(binding.inputFilterSalary.getText().toString());
        }
        catch (NumberFormatException e){
            System.out.println("Not a number was entered");
        }
        binding.inputFilterSalary.setText(String.valueOf(expSalary));

//        Save keywords inputs
        for (int i = 0; i < keywords.size(); i++) {
            EditText view = (EditText) binding.layoutFilterKeywords.getChildAt(i);
            keywords.set(i, view.getText().toString());
            view.setEnabled(false);
        }

        //get new cards based on new filter
        mainActivity.getSwipeFragment().setPageNumber(1);
        mainActivity.getSwipeFragment().resetJobList();
        mainActivity.getHandler().makeApiCall(new ApiCall(mainActivity.getFilterFragment().getFilter(),mainActivity.getSwipeFragment().getPageNumberAndIncrease()) {
            @Override
            public void callback(JSONArray results) {
                try {
                    mainActivity.resultsToJobs(results);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, mainActivity);

        setPreferences();
    }

    // Assign variables from SharedPreferences
    public void getPreferences(){
        prefs = mainActivity.getPrefs();

        category = Category.getByTag(prefs.getString("category","it-jobs"), categories);
        expSalary = (int) prefs.getFloat("expSalary",0);
        contractTime = ContractTime.getByName(prefs.getString("contractTime",ContractTime.EITHER.toString()));
        Set<String> keywordSet = prefs.getStringSet("keywords", new HashSet<>());
        keywords = new ArrayList<>(keywordSet);
        surrounding = prefs.getInt("surrounding",5);
        latitude = prefs.getFloat("latitude",0);
        longitude = prefs.getFloat("longitude",0);
        filtersActive = prefs.getBoolean("filtersActive",false);
        locationActive = prefs.getBoolean("locationActive",false);

        Log.d("TAG","SP DATA"+"\n"+
                "expSalary: "+expSalary+"\n"+
                "category: "+category+"\n"+
                "surrounding: "+surrounding+"\n"+
                "filtersActive: "+filtersActive+"\n"+
                "locationActive: "+locationActive+"\n"
        );
    }

    // set variables from SharedPreferences
    private void setPreferences(){
        SharedPreferences.Editor editor = prefs.edit();
        System.out.println("editor should write");
        editor.putFloat("expSalary",(float) expSalary);
        editor.putString("contractTime",contractTime.toString());
        editor.putString("category",category.getTag());
        Set<String> keywordSet = new HashSet<>(keywords);
        editor.putStringSet("keywords",keywordSet);
        editor.putInt("surrounding",surrounding);
        editor.putFloat("latitude",(float) latitude);
        editor.putFloat("longitude",(float) longitude);
        editor.putBoolean("filtersActive",filtersActive);
        editor.putBoolean("locationActive",locationActive);
        editor.apply();
    }

    //    Adds a EditText to the Keywords Layout with the given text
    private void addKeywordView(String text){
        EditText newKeyword = new EditText(this.getContext());
        newKeyword.setId(View.generateViewId());
        newKeyword.setText(text);
        newKeyword.setSingleLine();
        binding.layoutFilterKeywords.addView(newKeyword);
    }

    public double getLatitude(){
        return latitude;
    }

    public double getLongitude(){
        return longitude;
    }

    public boolean isLocationActive() {
        return locationActive;
    }

    public int getSurrounding() {
        return surrounding;
    }


    public void setMainActivity(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }
}


