package com.example.gmaps;

import static androidx.constraintlayout.widget.ConstraintLayoutStates.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.gmaps.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.model.PointOfInterest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor sensorTemp,sensorHumid;
    private long mLastClickTime = 0;
    private boolean keyboardVisibility = false;
    public static boolean isDayMode = false;
    public boolean clicked = false;
    View view;
    public static FloatingActionButton changeModeButton;
    public FloatingActionButton changeLangButton;
    public FloatingActionButton optionsButton;
    public FloatingActionButton changePageButton, searchOpen;
//    public static FloatingActionButton detailsButton;
    private Animation rotateOpen;
    private Animation rotateClose;
    private Animation fromBtm;
    private Animation toBtm;
    private Animation fromRight;
    private Animation toRight, openSearch,closeSearch, rotateSearch;
    public static String MODE_KEY = "Mode";
    public static SharedPreferences sharedPreferences;
    private final String sharedPrefFile = "com.example.gmaps";

    private PlacesClient placesClient;
    public static AlertDialog dialog;
    private  boolean isScreenMaps = true;
    public boolean clickedSearch = false;


    private FragmentManager fragmentManager = getSupportFragmentManager();
    private FragmentTransaction fragmentTransaction;
    Fragment mapFragment;
    Fragment qiblaFragment;
    public static float currentTemp = 0f;
    public static float currentHumidity = 0f;
    public SearchView searchView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        searchView =  findViewById(R.id.locationSearch);
        searchView.bringToFront();
//        searchView.setIconified(false);
        searchView.requestFocusFromTouch();
        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchView.setIconified(false);
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                List<Address> addressList = null;
                if (!s.equals("")){
                    Geocoder geocoder = new Geocoder(MapsActivity.this);
                    try {
                        addressList = geocoder.getFromLocationName(s,5);
                    } catch (IOException e) {
                        Toast.makeText(MapsActivity.this,getString(R.string.Err_Message),Toast.LENGTH_LONG).show();
                        throw new RuntimeException(e);
                    }
                    for (int i = 0; i<addressList.size();i++ )
                    {Address address = addressList.get(i);
                        LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                        if (Maps.poiMarker!= null){Maps.poiMarker.remove();}
                        Maps.poiMarker =  Maps.mMap.addMarker(new MarkerOptions()
                                .position(latLng)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE)));
                        Maps.mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));}
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
        searchOpen = findViewById(R.id.searchOpen);
        sharedPreferences = getSharedPreferences(sharedPrefFile, MODE_PRIVATE);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorHumid = sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
        sensorTemp = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        mapFragment = new Maps();
        qiblaFragment = new QiblaFrag();
        view=findViewById(R.id.loading);
        sharedPreferences = getSharedPreferences(sharedPrefFile, MODE_PRIVATE);
        isDayMode = !sharedPreferences.getBoolean(MODE_KEY, true);
        makeLoading();
        dialog.show();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                appearButton(optionsButton);
            }
        });
        changeModeButton = findViewById(R.id.changeModeButton);
        changeLangButton = findViewById(R.id.changeLangButton);
        optionsButton = findViewById(R.id.optionsButton);
//        detailsButton = findViewById(R.id.details);
        changePageButton = findViewById(R.id.changePageButton);
        rotateOpen = AnimationUtils.loadAnimation(this, R.anim.rotate_open);
        rotateClose = AnimationUtils.loadAnimation(this, R.anim.rotate_close);
        fromBtm = AnimationUtils.loadAnimation(this, R.anim.from_bottom);
        toBtm = AnimationUtils.loadAnimation(this, R.anim.to_bottom);
        fromRight = AnimationUtils.loadAnimation(this, R.anim.from_right);
        toRight = AnimationUtils.loadAnimation(this, R.anim.to_right);
        openSearch = AnimationUtils.loadAnimation(this, R.anim.search_open);
        closeSearch = AnimationUtils.loadAnimation(this, R.anim.seach_close);
        rotateSearch = AnimationUtils.loadAnimation(this, R.anim.rotate_search);
        changeMode(changeModeButton);
        display(changePageButton);


    }

    public void display(View v) {
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.page_up, R.anim.page_down);
        if(isScreenMaps) {fragmentTransaction.replace(R.id.screenFrag, mapFragment);
        }
        else {fragmentTransaction.replace(R.id.screenFrag, qiblaFragment);
        }
        isScreenMaps = !isScreenMaps;
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    public void changeMode(View v) {
        try {
            if (Maps.mMap!=null){
            if (isDayMode) {
                Maps.mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(
                        this, R.raw.night_mode));}
            else {Maps.mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(
                    this, R.raw.day_mode));}
        }
            isDayMode = !isDayMode;
            changeColorBtn(v.getContext());
            if (QiblaFrag.coba!= null){
                QiblaFrag.changeBG(isDayMode, this);
            }
        }
        catch(Resources.NotFoundException e){
            return;
        }
    }
    public void changeColorBtn(Context ctx){
        int id = searchView.getContext()
                .getResources()
                .getIdentifier("android:id/search_src_text", null, null);
        int searchPlateId = searchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
        EditText editText = (EditText) searchView.findViewById(id);
        EditText searchPlate = (EditText) searchView.findViewById(searchPlateId);
        searchPlate.setHint("Search");
        if (isDayMode) {
            changeModeButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
            changeModeButton.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.baseline_dark_mode_24));
            optionsButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
            optionsButton.setColorFilter(getResources().getColor(R.color.black));
//            detailsButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
//            detailsButton.setColorFilter(getResources().getColor(R.color.black));
            changePageButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
            changePageButton.setColorFilter(getResources().getColor(R.color.black));
            changeLangButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
            changeLangButton.setColorFilter(getResources().getColor(R.color.black));
            searchOpen.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
            searchOpen.setColorFilter(getResources().getColor(R.color.black));
            searchView.setBackground(ContextCompat.getDrawable(ctx, R.drawable.shape));
            editText.setTextColor(getColor(R.color.black));
        }
        else {
            changeModeButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.black)));
            changeModeButton.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.baseline_wb_sunny_24));
            optionsButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.black)));
            optionsButton.setColorFilter(getResources().getColor(R.color.white));
//            detailsButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.black)));
//            detailsButton.setColorFilter(getResources().getColor(R.color.white));
            changePageButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.black)));
            changePageButton.setColorFilter(getResources().getColor(R.color.white));
            changeLangButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.black)));
            changeLangButton.setColorFilter(getResources().getColor(R.color.white));
            searchOpen.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.black)));
            searchOpen.setColorFilter(getResources().getColor(R.color.white));
            searchView.setBackground(ContextCompat.getDrawable(ctx, R.drawable.shape_dark));
            editText.setTextColor(getColor(R.color.white));
        }
    }
    public void appearButton(View v){
        setVisibility(clicked);
        setAnimation(clicked);
        clicked = !clicked;
        if (clicked){
        view.setVisibility(View.VISIBLE);
        view.bringToFront();
        }
        else{view.setVisibility(View.GONE);}
    }
    public void appearSearch(View v){
        clickedSearch = !clickedSearch;
        if (clickedSearch){
            searchView.setVisibility(View.VISIBLE);
            searchView.startAnimation(openSearch);
            searchOpen.startAnimation(rotateSearch);
            searchOpen.setImageDrawable(ContextCompat.getDrawable(v.getContext(), R.drawable.baseline_search_off_24));
        }
        else {
            searchView.setVisibility(View.GONE);
            searchView.startAnimation(closeSearch);
            searchOpen.startAnimation(rotateSearch);
            searchOpen.setImageDrawable(ContextCompat.getDrawable(v.getContext(), R.drawable.baseline_search_24));
        }
    }

    private void setAnimation(boolean clicked) {
    if (clicked){
        changeModeButton.startAnimation(toBtm);
        changePageButton.startAnimation(toRight);
        changeLangButton.startAnimation(toBtm);
        optionsButton.startAnimation(rotateOpen);
    }
    else {
        changeModeButton.startAnimation(fromBtm);
        changeLangButton.startAnimation(fromBtm);
        changePageButton.startAnimation(fromRight);
        optionsButton.startAnimation(rotateClose);}
    }

    private void setVisibility(boolean clicked) {
        if (clicked){
            ((FrameLayout)findViewById(R.id.screenFrag)).setAlpha(1f);
//            detailsButton.setAlpha(1f);
//            detailsButton.setClickable(true);
            searchOpen.setAlpha(1f);
            searchOpen.setClickable(true);
            searchView.setClickable(true);
            searchView.setFocusable(true);
            searchView.setAlpha(1f);
            changeModeButton.setVisibility(View.GONE);
            changePageButton.setVisibility(View.GONE);
            changeLangButton.setVisibility(View.GONE);
        }
        else {
            ((FrameLayout)findViewById(R.id.screenFrag)).setAlpha(0.5f);
//            detailsButton.setAlpha(0.5f);
//            detailsButton.setClickable(false);
            searchOpen.setAlpha(0.5f);
            searchOpen.setClickable(false);
            searchView.setClickable(false);
            searchView.setFocusable(false);
            searchView.setAlpha(0.5f);
            changeModeButton.setVisibility(View.VISIBLE);
            changePageButton.setVisibility(View.VISIBLE);
            changeLangButton.setVisibility(View.VISIBLE);
        }
    }

    public void makeLoading(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
        View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.loading_bar,null);
        builder.setView(view);
//        ((DotProgressBar)view.findViewById(R.id.dotProgressBar)).startAnimation();
        builder.setCancelable(false);
        dialog = builder.create();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(MODE_KEY,isDayMode);
        editor.apply();
    }

    @Override
    protected void onStop() {
        super.onStop();
        sensorManager.unregisterListener(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(MODE_KEY,isDayMode);
        editor.apply();
    }
    @Override
    public void onBackPressed() {
        //Melakukan cek apakah keyboard turun dan recyvler view hilang saat menekan tombol back smartphone
        if (!keyboardVisibility){
            // jika tombol bek dipencet 2 kali dalam kurun waktu 3000 milisec (3 detik), maka akan reset semua dan keluar aplikasi
            if (SystemClock.elapsedRealtime() - mLastClickTime < 3000){
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(MODE_KEY,isDayMode);
                editor.apply();
                finishAffinity();
                finish();
            }
            Toast.makeText(this,"Ketuk Sekali Lagi untuk Keluar Dari Aplikasi",Toast.LENGTH_SHORT).show();
            mLastClickTime = SystemClock.elapsedRealtime();
        }
        else {super.onBackPressed();}
    }
    public void language(View v){
        Intent intent = new Intent(Settings.ACTION_LOCALE_SETTINGS);
        startActivity(intent);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        int sensorType = sensorEvent.sensor.getType();
        float currentValue = sensorEvent.values[0];
        switch (sensorType){
            case Sensor.TYPE_AMBIENT_TEMPERATURE:
                currentTemp = currentValue;
                break;
            case Sensor.TYPE_RELATIVE_HUMIDITY:
                currentHumidity = currentValue;
                break;
            default:
        }
        Intent intent = new Intent(this, WeatherWidget.class);
        intent.setAction("android.appwidget.action.APPWIDGET_UPDATE");
        int ids[] = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(new ComponentName(getApplication(), WeatherWidget.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,ids);
        sendBroadcast(intent);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (sensorTemp!=null){sensorManager.registerListener(this,sensorTemp, SensorManager.SENSOR_DELAY_NORMAL);}
        if (sensorHumid!=null){sensorManager.registerListener(this,sensorHumid, SensorManager.SENSOR_DELAY_NORMAL);}
    }

    @Override
    protected void onResume() {
        super.onResume();

    }
}