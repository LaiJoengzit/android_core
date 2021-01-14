package com.bot.android_etabotproj;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.TextView;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class SensorInfo extends Activity implements SensorEventListener, LocationListener, GpsStatus.Listener {
    private Sensor Sensor_Acc, Sensor_Mag, Sensor_RV, Sensor_GameRV, Sensor_GeoRV, Sensor_G, Sensor_LAcc;
    private SensorManager sensorManager;
    private LocationManager locationManager;
    private final float[] accelerometerReading = new float[3];
    private final float[] magnetometerReading = new float[3];
    private final float[] rotationMatrix = new float[9];
    private final float[] orientationAngles = new float[3];
    private final float[] RVReading = new float[3];
    private final float[] GameRVReading = new float[3];
    private final float[] GeoRVReading = new float[3];
    private final float[] RVRotationMatrix = new float[16];
    private final float[] GReading = new float[3];
    private final float[] LAccReading = new float[3];
    TextView vector_AM;
    TextView vector_RV;
    TextView vector_RV_q;
    TextView vector_GameRV;
    TextView vector_GeoRV;
    TextView vector_G;
    TextView vector_LAcc;

    @SuppressLint("InlinedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get an instance of the SensorManager
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        // Get sensors
        Sensor_Acc = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor_Mag = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        Sensor_RV = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        Sensor_GameRV = sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);
        Sensor_GeoRV = sensorManager.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR);
        Sensor_G = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        Sensor_LAcc = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        updateOrientationAngles();
        // Create our Preview view and set it as the content of our
        // Activity
        setContentView(R.layout.main);
        vector_AM = (TextView) findViewById(R.id.vc0);
        vector_RV = (TextView) findViewById(R.id.vc1);
        vector_RV_q = (TextView) findViewById(R.id.Quan);
        vector_GameRV = (TextView) findViewById(R.id.vc2);
        vector_GeoRV = (TextView) findViewById(R.id.vc3);
        vector_G = (TextView) findViewById(R.id.vc4);
        vector_LAcc = (TextView) findViewById(R.id.vc5);
        RVRotationMatrix[ 0] = 1;
        RVRotationMatrix[ 4] = 1;
        RVRotationMatrix[ 8] = 1;
        RVRotationMatrix[12] = 1;
    }

    @Override
    protected void onResume() {
        // Ideally a game should implement onResume() and onPause()
        // to take appropriate action when the activity looses focus
        super.onResume();
        int delay;
        Log.i("OnResume","OnResume");
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(SensorInfo.this);
        switch (Integer.parseInt(pref.getString("Option", "2")))
        {
            case 1:
                delay=SensorManager.SENSOR_DELAY_FASTEST;
                Log.i("OnResume", "Option 1: SENSOR_DELAY_FASTEST");
                break;
            case 2:
                delay=SensorManager.SENSOR_DELAY_GAME;
                Log.i("OnResume", "Option 2: SENSOR_DELAY_GAME");
                break;
            case 3:
                delay=SensorManager.SENSOR_DELAY_NORMAL;
                Log.i("OnResume", "Option 3: SENSOR_DELAY_NORMAL");
                break;
            case 4:
                delay=SensorManager.SENSOR_DELAY_UI;
                Log.i("OnResume", "Option 4: SENSOR_DELAY_UI");
                break;
            default:
                delay=SensorManager.SENSOR_DELAY_GAME;
        }
        if ( pref.getBoolean("Option 5", false) )
        {
            try
            {
                double freq=Integer.parseInt(pref.getString("Option 2", "100"));
                delay=(int) (1/freq*1000000);
            } catch (Exception x)
            { Log.i("Preference","Update rate in Hz not parsed");}
        }
        Log.i("Delay Rate","Delay rate set");
        if (Sensor_Acc != null) {
            sensorManager.registerListener( this, Sensor_Acc, delay);
            Log.i("Acc","Acc registered");
        }
        if (Sensor_Mag != null) {
            sensorManager.registerListener( this, Sensor_Mag, delay);
            Log.i("Mag","Acc registered");
        }
        if (Sensor_RV != null) {
            sensorManager.registerListener( this, Sensor_RV, delay);
            Log.i("RV","Acc registered");
        }
        if (Sensor_GameRV != null) {
            sensorManager.registerListener( this, Sensor_GameRV, delay);
            Log.i("RV","Acc registered");
        }
        if (Sensor_GeoRV != null) {
            sensorManager.registerListener( this, Sensor_GeoRV, delay);
            Log.i("RV","Acc registered");
        }
        if (Sensor_G != null) {
            sensorManager.registerListener( this, Sensor_G, delay);
            Log.i("RV","Acc registered");
        }
        if (Sensor_LAcc != null) {
            sensorManager.registerListener( this, Sensor_LAcc, delay);
            Log.i("RV","Acc registered");
        }
        Log.i("OnResume","SensorManager registered again");
        if (locationManager!=null)
        {
            if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions( this, new String[] {  ACCESS_COARSE_LOCATION  },11);
                ActivityCompat.requestPermissions( this, new String[] {  ACCESS_FINE_LOCATION  },12 );
                finish();
            }
            /*locationManager.addGpsStatusListener((GpsStatus.Listener) this);
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 200,0, (LocationListener) this);
                Log.i("OnResume","GPS provider requested");
            }
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
            {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 200,0, (LocationListener) this);
                Log.i("OnResume","NETWORK provider requested");
            }
            if (locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER))
            {
                locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 200,0, (LocationListener) this);
                Log.i("OnResume","PASSIVE provider requested");
            }*/
        }
        Log.i("OnResume","locationManager registered again");
        updateOrientationAngles();
    }

    // Get readings from accelerometer and magnetometer. To simplify calculations,
    // consider storing these readings as unit vectors.
    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()){
            case Sensor.TYPE_ACCELEROMETER:
                System.arraycopy(event.values, 0, accelerometerReading,
                        0, accelerometerReading.length);
                Log.i("Acc","Acc updated");
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                System.arraycopy(event.values, 0, magnetometerReading,
                        0, magnetometerReading.length);
                Log.i("Mag","Mag updated");
            case Sensor.TYPE_ROTATION_VECTOR:
                System.arraycopy(event.values, 0, RVReading,
                        0, RVReading.length);
                SensorManager.getRotationMatrixFromVector(
                        RVRotationMatrix , event.values);
                Log.i("RV","RV updated");
            case Sensor.TYPE_GAME_ROTATION_VECTOR:
                System.arraycopy(event.values, 0, GameRVReading,
                        0, GameRVReading.length);
                Log.i("GameRV","GameRV updated");
            case Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR:
                System.arraycopy(event.values, 0, GeoRVReading,
                        0, GeoRVReading.length);
                Log.i("GeoRV","GeoRV updated");
            case Sensor.TYPE_GRAVITY:
                System.arraycopy(event.values, 0, GReading,
                        0, GReading.length);
                Log.i("G","G updated");
            case Sensor.TYPE_LINEAR_ACCELERATION:
                System.arraycopy(event.values, 0, LAccReading,
                        0, LAccReading.length);
                Log.i("LAcc","LAcc updated");
        }
        updateOrientationAngles();
        vector_AM.setText("X:"+ java.lang.String.valueOf(orientationAngles[0])+"\nY:"+ java.lang.String.valueOf(orientationAngles[1])+"/nZ:"+ java.lang.String.valueOf(orientationAngles[2]));
        vector_RV.setText("X:"+ java.lang.String.valueOf(RVReading[0])+"\nY:"+ java.lang.String.valueOf(RVReading[1])+"\nZ:"+ java.lang.String.valueOf(RVReading[2]));
        vector_RV_q.setText("Qx:"+ java.lang.String.valueOf(RVRotationMatrix[0])+"Qy:"+ java.lang.String.valueOf(RVRotationMatrix[1])+"Qz:"+ java.lang.String.valueOf(RVRotationMatrix[2])+"Qw:"+ java.lang.String.valueOf(RVRotationMatrix[3]));
        vector_GameRV.setText("X:"+ java.lang.String.valueOf(GameRVReading[0])+"\nY:"+ java.lang.String.valueOf(GameRVReading[1])+"\nZ:"+ java.lang.String.valueOf(GameRVReading[2]));
        vector_GeoRV.setText("X:"+ java.lang.String.valueOf(GeoRVReading[0])+"\nY:"+ java.lang.String.valueOf(GeoRVReading[1])+"\nZ:"+ java.lang.String.valueOf(GeoRVReading[2]));
        vector_G.setText("X:"+ java.lang.String.valueOf(GReading[0])+"\nY:"+ java.lang.String.valueOf(GReading[1])+"\nZ:"+ java.lang.String.valueOf(GReading[2]));
        vector_LAcc.setText("X:"+ java.lang.String.valueOf(LAccReading[0])+"\nY:"+ java.lang.String.valueOf(LAccReading[1])+"\nZ:"+ java.lang.String.valueOf(LAccReading[2]));
        //update the vector
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        // Do something here if sensor accuracy changes.
        // You must implement this callback in your code.
    }

    public void updateOrientationAngles() {
        // Update rotation matrix, which is needed to update orientation angles.
        SensorManager.getRotationMatrix(rotationMatrix, null,
                accelerometerReading, magnetometerReading);
        // "mRotationMatrix" now has up-to-date information.
        SensorManager.getOrientation(rotationMatrix, orientationAngles);
        Log.i("OrientationAngles","OrientationAngles updated");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i("OnStart","OnStart");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i("OnRestart","OnRestart");
    }


    @Override
    protected void onPause() {
        super.onPause();
        Log.i("OnPause","OnPause");
        // Ideally a game should implement onResume() and onPause()
        // to take appropriate action when the activity looses focus
        sensorManager.unregisterListener(this);
        locationManager.removeUpdates((LocationListener) this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i("OnStop","OnStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("OnDestroy", "OnDestroy");
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    public void onGpsStatusChanged(int i) {

    }
}
