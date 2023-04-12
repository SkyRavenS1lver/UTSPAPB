package com.example.gmaps;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PointOfInterest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.Locale;
import java.util.Objects;

public class Maps extends Fragment implements OnMapReadyCallback {
    public Maps() {
        // Required empty public constructor
    }

    FusedLocationProviderClient fusedLocationProviderClient;
    protected LocationManager locationManager;
    public static GoogleMap mMap;
    private Location lastKnownLocation;
    private PlacesClient placesClient;
    private boolean locationPermissionGranted = false;
    public static Marker poiMarker = null;
    public Marker currentLoc = null;
    private String poiType;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this.requireActivity());
        locationManager = (LocationManager) this.requireActivity().getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_maps, container, false);
        // initialize maps
        SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        supportMapFragment.getMapAsync(this);
        return view;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        enableMyCurrentLocation();
        getDeviceLocation();
//        mMap.setOnMarkerClickListener(this);
        setMapOnClick(mMap, this.getContext());
        setPoiClicked(mMap, this.getContext());
        if (MapsActivity.isDayMode) {
            mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(
                    this.getContext(), R.raw.day_mode));}
        else {mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(
                this.getContext(), R.raw.night_mode));}

    }

    private void setPoiClicked(final GoogleMap googleMap, Context context){
        googleMap.setOnPoiClickListener(new GoogleMap.OnPoiClickListener() {
            @Override
            public void onPoiClick(@NonNull PointOfInterest pointOfInterest) {
                poiType = "poi";
                if (poiMarker!= null){poiMarker.remove();}
                poiMarker = googleMap.addMarker(new MarkerOptions()
                        .position(pointOfInterest.latLng)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE))
                        .title(pointOfInterest.name));
                poiMarker.showInfoWindow();
            }
        });
    }

    private void setMapOnClick(final  GoogleMap map, Context context){
//        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
//            @Override
//            public void onMapClick(@NonNull LatLng latLng) {
//                MapsActivity.detailsButton.setVisibility(View.GONE);
//            }
//        });
        map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                                          @Override
                                          public void onMapLongClick(@NonNull LatLng latLng) {
                                              poiType = "direct";
//                                              MapsActivity.detailsButton.setVisibility(View.GONE);
                                              String text = String.format(Locale.getDefault(), "Lat : %1$.5f, Long: %2$.5f",
                                                      latLng.latitude, latLng.longitude);
                                              if (poiMarker!=null){poiMarker.remove();}
                                              poiMarker = map.addMarker(new MarkerOptions()
                                                      .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE))
                                                      .position(latLng)
                                                      .snippet(text).title(getString(R.string.destination)));
                                          }
                                      }
        );

    }

    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (locationPermissionGranted) {
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this.getActivity(), new OnCompleteListener<Location>() {
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
        mMap.clear();
        MapsActivity.dialog.dismiss();
    }

    private void enableMyCurrentLocation(){
        if (ContextCompat.checkSelfPermission(this.getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED){locationPermissionGranted = true;
        }
        else {
            ActivityCompat.requestPermissions(this.getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            ;locationPermissionGranted = false;}
        mMap.setMyLocationEnabled(locationPermissionGranted);
        mMap.getUiSettings().setMyLocationButtonEnabled(locationPermissionGranted);
        mMap.getUiSettings().setCompassEnabled(locationPermissionGranted);
    }

    @Override
    public void onResume() {
        super.onResume();
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
}