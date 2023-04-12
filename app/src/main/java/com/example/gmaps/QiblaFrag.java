package com.example.gmaps;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class QiblaFrag extends Fragment implements SensorEventListener {
    private static SensorManager sensorManager;
    private static Sensor acceleroSensor, magnetoSensor;
    private float[] lastAccelerometer = new float[3];
    private float[] lastMagneto = new float[3];
    private float[] orientation = new float[3];
    private float[] rotationMatrix = new float[9];
    private boolean isLastAccelerometerCopied = false;
    private boolean isLastMagnetoCopied = false;
    private AlertDialog dialog;
    long lastUpdated= 0;
    float currentDegree = 0f;
    public static float azimuthInRadians = 0f;
    public static float azimuthInDegree = 0f;

    private ImageView ic_compass;
    public static ConstraintLayout coba;
    public static TextView coba2;
    private boolean isDone = false;



    public QiblaFrag() {
        // Required empty public constructor

    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sensorManager =  (SensorManager)getActivity().getSystemService(Context.SENSOR_SERVICE);
        acceleroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetoSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (acceleroSensor != null && magnetoSensor!=null){
            sensorManager.registerListener(this, acceleroSensor, SensorManager.SENSOR_DELAY_NORMAL);
            sensorManager.registerListener(this, magnetoSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_qibla, container, false);
        ic_compass = v.findViewById(R.id.imageViewCompass);
        if (acceleroSensor != null && magnetoSensor!=null){
            sensorManager.registerListener(this, acceleroSensor, SensorManager.SENSOR_DELAY_NORMAL);
            sensorManager.registerListener(this, magnetoSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        coba = v.findViewById(R.id.background);
        coba2 = v.findViewById(R.id.txt);
        changeBG(MapsActivity.isDayMode, this.getContext());
        makeLoading();
        dialog.show();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        return v;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor==acceleroSensor){
            System.arraycopy(sensorEvent.values, 0 , lastAccelerometer, 0 , sensorEvent.values.length);
            isLastAccelerometerCopied = true;
        }
        else if (sensorEvent.sensor == magnetoSensor){
            System.arraycopy(sensorEvent.values, 0 , lastMagneto, 0 , sensorEvent.values.length);
            isLastMagnetoCopied = true;
        }
        if (isLastAccelerometerCopied && isLastMagnetoCopied && System.currentTimeMillis()-lastUpdated>150){
            SensorManager.getRotationMatrix(rotationMatrix, null, lastAccelerometer, lastMagneto);
            SensorManager.getOrientation(rotationMatrix, orientation);

             azimuthInRadians = orientation[0];
             azimuthInDegree = (float) Math.toDegrees(azimuthInRadians)+66;
            RotateAnimation animation = new RotateAnimation(currentDegree,-azimuthInDegree, Animation.RELATIVE_TO_SELF,
                    0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            animation.setDuration(250);
            animation.setFillAfter(true);
            dialog.dismiss();
            ic_compass.setAnimation(animation);
            currentDegree=-azimuthInDegree;
            lastUpdated = System.currentTimeMillis();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    @Override
    public void onStart() {
        super.onStart();
        if (acceleroSensor != null){
            sensorManager.registerListener(this, acceleroSensor, SensorManager.SENSOR_DELAY_NORMAL);
            sensorManager.registerListener(this, magnetoSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        else {
            Toast.makeText(getContext(), getString(R.string.notSupported), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        sensorManager.unregisterListener(this, acceleroSensor);
        sensorManager.unregisterListener(this, magnetoSensor);
    }
        @Override
    public void onResume() {
        super.onResume();
        if (acceleroSensor != null){
            sensorManager.registerListener(this, acceleroSensor, SensorManager.SENSOR_DELAY_NORMAL);
            sensorManager.registerListener(this, magnetoSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        else {
            Toast.makeText(getContext(), getString(R.string.notSupported), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this, acceleroSensor);
        sensorManager.unregisterListener(this, magnetoSensor);
    }
    public static void changeBG(boolean mode, Context ctx){
        if (mode){coba.setBackground(ContextCompat.getDrawable(ctx, R.drawable.shape));
        coba2.setTextColor(ctx.getResources().getColor(R.color.black));}
        else {coba.setBackground(ContextCompat.getDrawable(ctx, R.drawable.shape_dark));
            coba2.setTextColor(ctx.getResources().getColor(R.color.white));}
    }
    public void makeLoading(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
        View view = LayoutInflater.from(getActivity().getApplicationContext()).inflate(R.layout.loading_bar,null);
        builder.setView(view);
        builder.setCancelable(false);
        dialog = builder.create();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }
}