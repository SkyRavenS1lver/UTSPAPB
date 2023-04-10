package com.example.gmaps;

import static androidx.constraintlayout.widget.ConstraintLayoutStates.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

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
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends AppCompatActivity implements GoogleMap.OnMarkerClickListener, OnMapReadyCallback {
    FusedLocationProviderClient fusedLocationProviderClient;
    protected LocationManager locationManager;
    private final static int REQUEST_CODE = 1000;
    public boolean isDayMode = true;
    public boolean clicked = false;
    View view;
    public Marker poiMarker = null;
    public Marker currentLoc = null;
    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    public FloatingActionButton changeModeButton;
    public FloatingActionButton optionsButton;
    private Animation rotateOpen;
    private Animation rotateClose;
    private Animation fromBtm;
    private Animation toBtm;
    private boolean locationPermissionGranted = false;
    private Location lastKnownLocation;
    private PlacesClient placesClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        view=findViewById(R.id.loading);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                appearButton(changeModeButton);
            }
        });
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        changeModeButton = findViewById(R.id.changeModeButton);
        optionsButton = findViewById(R.id.optionsButton);
        rotateOpen = AnimationUtils.loadAnimation(this, R.anim.rotate_open);
        rotateClose = AnimationUtils.loadAnimation(this, R.anim.rotate_close);
        fromBtm = AnimationUtils.loadAnimation(this, R.anim.from_bottom);
        toBtm = AnimationUtils.loadAnimation(this, R.anim.to_bottom);
        changeColorBtn(this);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);


        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.day_mode));
        } catch (Resources.NotFoundException e) {
        }
        enableMyCurrentLocation();
        getDeviceLocation();
        setMapOnClick(mMap, this);
        setPoiClicked(mMap, this);
    }

    private void setMapOnClick(final  GoogleMap map, Context context){
        map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                                          @Override
                                          public void onMapLongClick(@NonNull LatLng latLng) {

                                              String text = String.format(Locale.getDefault(), "Lat : %1$.5f, Long: %2$.5f",
                                                      latLng.latitude, latLng.longitude);
                                              if (poiMarker!=null){poiMarker.remove();}
                                              poiMarker = map.addMarker(new MarkerOptions()
                                                      .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE))
                                                      .position(latLng)
                                                      .snippet(text).title("Your Location"));
                                          }
                                      }
        );

    }
    private void setPoiClicked(final GoogleMap googleMap, Context context){
        googleMap.setOnPoiClickListener(new GoogleMap.OnPoiClickListener() {
            @Override
            public void onPoiClick(@NonNull PointOfInterest pointOfInterest) {

                if (poiMarker!= null){poiMarker.remove();}
                poiMarker = googleMap.addMarker(new MarkerOptions()
                        .position(pointOfInterest.latLng)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE))
                        .title(pointOfInterest.name));
                poiMarker.showInfoWindow();
            }
        });
    }
private void enableMyCurrentLocation(){
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
        == PackageManager.PERMISSION_GRANTED){locationPermissionGranted = true;
        }
        else {ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        ;locationPermissionGranted = false;}
    mMap.setMyLocationEnabled(locationPermissionGranted);
    mMap.getUiSettings().setMyLocationButtonEnabled(locationPermissionGranted);
    mMap.getUiSettings().setCompassEnabled(locationPermissionGranted);
}

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 1:
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    enableMyCurrentLocation();break;
                }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
    public void changeMode(View v) {
        try {
            if (isDayMode) {
                mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(
                        this, R.raw.night_mode));}
            else {mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(
                    this, R.raw.day_mode));}
            isDayMode = !isDayMode;
            changeColorBtn(v.getContext());
        }
        catch(Resources.NotFoundException e){
            return;
        }
    }
    public void changeColorBtn(Context ctx){
        if (isDayMode) {
            changeModeButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
            changeModeButton.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.baseline_dark_mode_24));
            optionsButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
            optionsButton.setColorFilter(getResources().getColor(R.color.black));
        }
        else {
            changeModeButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.black)));
            changeModeButton.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.baseline_wb_sunny_24));
            optionsButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.black)));
            optionsButton.setColorFilter(getResources().getColor(R.color.white));
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

    private void setAnimation(boolean clicked) {
    if (clicked){
        changeModeButton.startAnimation(toBtm);
        optionsButton.startAnimation(rotateOpen);
    }
    else {changeModeButton.startAnimation(fromBtm);
        optionsButton.startAnimation(rotateClose);}
    }

    private void setVisibility(boolean clicked) {
        if (clicked){
            getSupportFragmentManager().findFragmentById(R.id.map).getView().setAlpha(1f);
            changeModeButton.setVisibility(View.GONE);
        }
        else {
            getSupportFragmentManager().findFragmentById(R.id.map).getView().setAlpha(0.5f);
            changeModeButton.setVisibility(View.VISIBLE);
        }
    }
    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (locationPermissionGranted) {
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        int defaulZoom = 15;
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            lastKnownLocation = task.getResult();
                            if (lastKnownLocation != null) {
                                if (currentLoc!=null){currentLoc.remove();}
                                LatLng current = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                                currentLoc = mMap.addMarker(new MarkerOptions().position(current).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current, defaulZoom));
                            }
                        } else {
                            mMap.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(new LatLng(0,0),defaulZoom));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }
    @Override
    public boolean onMarkerClick(final Marker marker) {

        return false;
    }
    public void searchFromId(String Id){
        // Define a Place ID.
        final String placeId = Id;
// Specify the fields to return.
        final List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME,
                Place.Field.ADDRESS,Place.Field.WEBSITE_URI, Place.Field.TYPES, Place.Field.PHONE_NUMBER);
// Construct a request object, passing the place ID and fields array.
        final FetchPlaceRequest request = FetchPlaceRequest.newInstance(placeId, placeFields);
        placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
            Place place = response.getPlace();
            Log.i(TAG, "Place found: " + place.getName());
        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                final ApiException apiException = (ApiException) exception;
                Log.e(TAG, "Place not found: " + exception.getMessage());
                final int statusCode = apiException.getStatusCode();
                // TODO: Handle error with given status code.
            }
        });
    }
}