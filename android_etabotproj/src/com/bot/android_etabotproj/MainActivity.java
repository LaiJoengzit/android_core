/*
 * Copyright (C) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

/*
 * edited from: android_tutorial_pubsub:@author damonkohler@google.com (Damon Kohler)
 */
package com.bot.android_etabotproj;

import android.annotation.SuppressLint;
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

import org.ros.android.MessageCallable;
import org.ros.android.RosActivity;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;
import org.ros.time.WallTimeProvider;

import geometry_msgs.Vector3Stamped;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends RosActivity implements SensorEventListener, LocationListener, GpsStatus.Listener {
    private DownloaderView<Vector3Stamped> downloaderView;
    private DownloaderView<Vector3Stamped> downloaderView2;
    private Uploader<Vector3Stamped,float[]> uploader;
    private Uploader<Vector3Stamped,float[]> uploader2;

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

    public MainActivity() {
        // The RosActivity constructor configures the notification title and ticker
        // messages.
        super("Etabotproj", "Etabotproj");
    }

    @SuppressLint("InlinedApi")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        //ini for ROS' subscriber
        downloaderView = findViewById(R.id.ROStext1);
        downloaderView.setTopicName("Lineal");
        downloaderView.setMessageType(Vector3Stamped._TYPE);
        downloaderView.setMessageToStringCallable(new MessageCallable<java.lang.String, Vector3Stamped>() {
            @Override
            public java.lang.String call(Vector3Stamped message) {
                return "X:" + message.getVector().getX() + "\nY:" + message.getVector().getY() + "\nZ:" + message.getVector().getZ()
                        + "\nSeq:" + message.getHeader().getSeq() + "\nStamp:" + message.getHeader().getStamp() + "\nId:" + message.getHeader().getFrameId();
            }
        });
        downloaderView2 = findViewById(R.id.ROStext2);
        downloaderView2.setDefaultNodeName("android/Downloader2");//avoid collapsing
        downloaderView2.setTopicName("Rotation");
        downloaderView2.setMessageType(Vector3Stamped._TYPE);
        downloaderView2.setMessageToStringCallable(new MessageCallable<java.lang.String, Vector3Stamped>() {
            @Override
            public java.lang.String call(Vector3Stamped message) {
                return "X:" + message.getVector().getX() + "\nY:" + message.getVector().getY() + "\nZ:" + message.getVector().getZ()
                        + "\nSeq:" + message.getHeader().getSeq() + "\nStamp:" + message.getHeader().getStamp() + "\nId:" + message.getHeader().getFrameId();
            }
        });
        // Get an instance of the SensorManager
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Sensor_Acc = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor_Mag = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        Sensor_RV = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        Sensor_GameRV = sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);
        Sensor_GeoRV = sensorManager.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR);
        updateOrientationAngles();
        // Create our Preview view and set it as the content of our
        // Activity
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
    protected void init(NodeMainExecutor nodeMainExecutor) {
        uploader = new Uploader<Vector3Stamped,float[]>("Lineal", "android/Uploader",Vector3Stamped._TYPE);
        uploader2 = new Uploader<Vector3Stamped,float[]>("Rotation", "android/Uploader2",Vector3Stamped._TYPE);
        // At this point, the user has already been prompted to either enter the URI
        // of a master to use or to start a master locally.

        // The user can easily use the selected ROS Hostname in the master chooser
        // activity.
        NodeConfiguration nodeConfiguration = NodeConfiguration.newPublic(getRosHostname());
        nodeConfiguration.setMasterUri(getMasterUri());
        nodeMainExecutor.execute(uploader, nodeConfiguration);
        nodeMainExecutor.execute(uploader2, nodeConfiguration);
        while( ! uploader.getvecOn() && ! uploader2.getvecOn()){;}
        setUploader(uploader);
        setUploader(uploader2);
        // The RosTextView is also a NodeMain that must be executed in order to
        // start displaying incoming messages.
        nodeMainExecutor.execute(downloaderView, nodeConfiguration);
        nodeMainExecutor.execute(downloaderView2, nodeConfiguration);
        Log.i("ROSinit","ROSinit");
    }

    public void setUploader(Uploader<Vector3Stamped,float[]> uploader){
        float[] injector = new float[]{0,0,0};
        uploader.setLoopPeriod(10);//ms
        uploader.setInjector(injector);
        uploader.setMessageToStringCallable(new MessageCallable<Vector3Stamped, float[]>() {
            @Override
            public Vector3Stamped call(float[] injector) {
                Vector3Stamped vecS = uploader.getVecS();
                vecS.getHeader().setSeq(uploader.addSeq());
                vecS.getHeader().setStamp(new WallTimeProvider().getCurrentTime());
                vecS.getHeader().setFrameId("etabot");
                vecS.getVector().setX(injector[0]);
                vecS.getVector().setY(injector[1]);
                vecS.getVector().setZ(injector[2]);
                return vecS;
            }
        });
        uploader.on();
        Log.i("UploaderSet","Uploader set");
    }

    @Override
    public void onResume() {
        super.onResume();

        int delay;
        Log.i("OnResume","OnResume");
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
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
            Log.i("Mag","Mag registered");
        }
        if (Sensor_RV != null) {
            sensorManager.registerListener( this, Sensor_RV, delay);
            Log.i("RV","RV registered");
        }
        if (Sensor_GameRV != null) {
            sensorManager.registerListener( this, Sensor_GameRV, delay);
            Log.i("GameRV","GameRV registered");
        }
        if (Sensor_GeoRV != null) {
            sensorManager.registerListener( this, Sensor_GeoRV, delay);
            Log.i("GeoRV","GeoRV registered");
        }
        if (Sensor_G != null) {
            sensorManager.registerListener( this, Sensor_G, delay);
            Log.i("G","G registered");
        }
        if (Sensor_LAcc != null) {
            sensorManager.registerListener(this, Sensor_LAcc, delay);
            Log.i("LAcc", "LAcc registered");
        }
        Log.i("OnResume","SensorManager registered again");
        if (locationManager!=null)
        {
            if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions( this, new java.lang.String[] {  ACCESS_COARSE_LOCATION  },11);
                ActivityCompat.requestPermissions( this, new java.lang.String[] {  ACCESS_FINE_LOCATION  },12 );
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


    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()){
            case Sensor.TYPE_ACCELEROMETER:
                System.arraycopy(event.values, 0, accelerometerReading,
                        0, accelerometerReading.length);
                //Log.i("Acc","Acc updated");
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                System.arraycopy(event.values, 0, magnetometerReading,
                        0, magnetometerReading.length);
                //Log.i("Mag","Mag updated");
            case Sensor.TYPE_ROTATION_VECTOR:
                System.arraycopy(event.values, 0, RVReading,
                        0, RVReading.length);
                SensorManager.getRotationMatrixFromVector(
                        RVRotationMatrix , event.values);
                //Log.i("RV","RV updated");
            case Sensor.TYPE_GAME_ROTATION_VECTOR:
                System.arraycopy(event.values, 0, GameRVReading,
                        0, GameRVReading.length);
                //Log.i("GameRV","GameRV updated");
            case Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR:
                System.arraycopy(event.values, 0, GeoRVReading,
                        0, GeoRVReading.length);
                //Log.i("GeoRV","GeoRV updated");
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
        if(uploader != null){
            uploader.setInjector(LAccReading);
        }
        if(uploader2 != null){
            uploader2.setInjector(orientationAngles);
        }
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
        //Log.i("OrientationAngles","OrientationAngles updated");
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
    public void onStatusChanged(java.lang.String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(java.lang.String s) {

    }

    @Override
    public void onProviderDisabled(java.lang.String s) {

    }

    @Override
    public void onGpsStatusChanged(int i) {

    }
}
