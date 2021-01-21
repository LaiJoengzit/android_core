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

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;

import org.ros.android.MessageCallable;
import org.ros.android.RosActivity;
import org.ros.android.view.camera.RosCameraPreviewView;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;
import org.ros.time.WallTimeProvider;

import java.io.IOException;
import java.util.List;

import geometry_msgs.PoseStamped;
import geometry_msgs.Vector3Stamped;

public class MainActivity extends RosActivity implements SensorEventListener, LocationListener, GpsStatus.Listener {
    private DownloaderView<Vector3Stamped> downloaderLinear;
    private DownloaderView<Vector3Stamped> downloaderRotation;
    private DownloaderView<PoseStamped> downloaderPose;
    private Uploader<Vector3Stamped,float[]> uploaderLinear;
    private Uploader<Vector3Stamped,float[]> uploaderRotation;
    private Uploader<Vector3Stamped,float[]> uploaderLocation;


    private Sensor Sensor_Acc, Sensor_Mag, Sensor_RV, Sensor_GameRV, Sensor_GeoRV, Sensor_G, Sensor_LAcc;
    private SensorManager sensorManager;
    private LocationManager locationManager;
    private LocationRequest locationRequest;
    FusedLocationProviderClient fusedLocationClient;
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
    private TextView LocationToSendView;
    Switch SwitchSensorsShown;
    Switch SwitchCameraShown;
    Switch SwitchLocation;
    ConstraintLayout SensorsConstraint;
    private int cameraId;
    private Camera cam;
    private RosCameraPreviewView rosCameraPreviewView;

    private LocationCallback locationCallback;
    private Location mLocation;
    private Location LocationNetwork;
    private Location LocationGPS;
    private boolean requestingLocationUpdates = false;

    public MainActivity() {
        // The RosActivity constructor configures the notification title and ticker
        // messages.
        super("Etabotproj", "Etabotproj");
    }

    public void getView(){
        vector_AM = (TextView) findViewById(R.id.vc0);
        vector_RV = (TextView) findViewById(R.id.vc1);
        vector_RV_q = (TextView) findViewById(R.id.Quan);
        vector_GameRV = (TextView) findViewById(R.id.vc2);
        vector_GeoRV = (TextView) findViewById(R.id.vc3);
        vector_G = (TextView) findViewById(R.id.vc4);
        vector_LAcc = (TextView) findViewById(R.id.vc5);
        rosCameraPreviewView = (RosCameraPreviewView) findViewById(R.id.RosCamera);
        LocationToSendView = (TextView) findViewById(R.id.LocationToSend);
        SwitchSensorsShown = findViewById(R.id.switchSensors);
        SwitchCameraShown = findViewById(R.id.switchCamera);
        SwitchLocation = findViewById(R.id.switchLocation);
        SensorsConstraint = findViewById(R.id.SensorsConstraint);
    }

    @SuppressLint("InlinedApi")
    public void getSensors(){
        // Get an instance of the SensorManager
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor_Acc = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor_Mag = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        Sensor_RV = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        Sensor_GameRV = sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);
        Sensor_GeoRV = sensorManager.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR);
    }

    //fusedLocationClient requires Play Service of Version 11 or higher.
    /*public void checkLocationCallback(){
        if (requestingLocationUpdates) {
            updateStartLocation();
            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult == null) {
                        return;
                    }
                    for (Location location : locationResult.getLocations()) {
                        // Update UI with location data
                        // ...
                        LocationToSendView.setText("get");
                    }
                }
            };
        }
    }*/

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        //ini for ROS' subscriber
        downloaderLinear = findViewById(R.id.ROStext1);
        downloaderLinear.setDefaultNodeName("android/DownloaderLinear");//avoid collapsing
        downloaderLinear.setTopicName("Lineal");
        downloaderLinear.setMessageType(Vector3Stamped._TYPE);
        downloaderLinear.setMessageToStringCallable(new MessageCallable<java.lang.String, Vector3Stamped>() {
            @Override
            public java.lang.String call(Vector3Stamped message) {
                return "X:" + message.getVector().getX() + "\nY:" + message.getVector().getY() + "\nZ:" + message.getVector().getZ()
                        + "\nSeq:" + message.getHeader().getSeq() + "\nStamp:" + message.getHeader().getStamp() + "\nId:" + message.getHeader().getFrameId();
            }
        });
        downloaderRotation = findViewById(R.id.ROStext2);
        downloaderRotation.setDefaultNodeName("android/DownloaderRotation");//avoid collapsing
        downloaderRotation.setTopicName("Rotation");
        downloaderRotation.setMessageType(Vector3Stamped._TYPE);
        downloaderRotation.setMessageToStringCallable(new MessageCallable<java.lang.String, Vector3Stamped>() {
            @Override
            public java.lang.String call(Vector3Stamped message) {
                return "X:" + message.getVector().getX() + "\nY:" + message.getVector().getY() + "\nZ:" + message.getVector().getZ()
                        + "\nSeq:" + message.getHeader().getSeq() + "\nStamp:" + message.getHeader().getStamp() + "\nId:" + message.getHeader().getFrameId();
            }
        });
        downloaderPose = findViewById(R.id.PoseOrders);
        downloaderPose.setDefaultNodeName("android/DownloaderOrders");//avoid collapsing
        downloaderPose.setTopicName("PoseOrders");
        downloaderPose.setMessageType(PoseStamped._TYPE);
        downloaderPose.setMessageToStringCallable(new MessageCallable<java.lang.String, PoseStamped>() {
            @Override
            public java.lang.String call(PoseStamped message) {
                if(message !=null) {
                    return "X:" + String.format("%.2f", message.getPose().getPosition().getX()) +
                            " Y:" + String.format("%.2f", message.getPose().getPosition().getY()) +
                            " Z:" + String.format("%.2f", message.getPose().getPosition().getZ())
                            + "\nXo:" + String.format("%.2f", message.getPose().getOrientation().getX()) +
                            " Yo:" + String.format("%.2f", message.getPose().getOrientation().getY()) +
                            " Zo:" + String.format("%.2f", message.getPose().getOrientation().getZ()) +
                            " Wo:" + String.format("%.2f", message.getPose().getOrientation().getW());
                }else return "No commands.";
            }
        });
        updateOrientationAngles();
        // Create our Preview view and set it as the content of our
        // Activity
        getSensors();
        //fusedLocationClient requires Play Service of Version 11 or higher.
        //fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        //checkLocationCallback();

        getView();
        SwitchSensorsShown.setOnCheckedChangeListener((buttonView, isChecked) -> setShown());
        SwitchCameraShown.setOnCheckedChangeListener((buttonView, isChecked) -> setShown());
        SwitchLocation.setOnCheckedChangeListener((buttonView, isChecked) -> setShown());
        RVRotationMatrix[ 0] = 1;
        RVRotationMatrix[ 4] = 1;
        RVRotationMatrix[ 8] = 1;
        RVRotationMatrix[12] = 1;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA},
                    50);
        }
        setShown();
    }

    private void setShown(){
        boolean ShowSensors = SwitchSensorsShown.isChecked();
        boolean ShowCamera = SwitchCameraShown.isChecked();
        boolean ShowLocation = SwitchLocation.isChecked();
        if (ShowSensors){
            SensorsConstraint.setVisibility(View.VISIBLE);
        }
        else{
            SensorsConstraint.setVisibility(View.GONE);
        }
        if (ShowCamera){
            rosCameraPreviewView.setVisibility(View.VISIBLE);
        }
        else{
            rosCameraPreviewView.setVisibility(View.GONE);
        }
        if (ShowLocation){
            Log.i("SwitchLocation","SwitchLocation on");
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
                        50);
                Toast.makeText(this, "No permission for locating", Toast.LENGTH_SHORT).show();
                Log.i("NoPermission","No permission");
            }else{
                requestingLocationUpdates = true;
                locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                registerLocators();
                setUploaderVector3Stamped(uploaderLocation);
                LocationToSendView.setVisibility(View.VISIBLE);
                Log.i("LocationINI","LocationINI succeed");
            }
        }
        else{
            LocationToSendView.setVisibility(View.GONE);
            if(locationManager !=null){
                stopLocating();
            }
            mLocation = null;
            LocationToSendView.setText(null);
        }
    }

    //fusedLocationClient requires Play Service of Version 11 or higher.
    protected void createLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    protected void init(NodeMainExecutor nodeMainExecutor) {

        uploaderLinear = new Uploader<Vector3Stamped,float[]>("Lineal", "android/UploaderLinear",Vector3Stamped._TYPE);
        uploaderRotation = new Uploader<Vector3Stamped,float[]>("Rotation", "android/UploaderRotation",Vector3Stamped._TYPE);
        uploaderLocation = new Uploader<Vector3Stamped,float[]>("Location", "android/UploaderLocation",Vector3Stamped._TYPE);
        // At this point, the user has already been prompted to either enter the URI
        // of a master to use or to start a master locally.

        // The user can easily use the selected ROS Hostname in the master chooser
        // activity.
        NodeConfiguration nodeConfiguration = NodeConfiguration.newPublic(getRosHostname());
        nodeConfiguration.setMasterUri(getMasterUri());
        nodeMainExecutor.execute(uploaderLinear, nodeConfiguration);
        nodeMainExecutor.execute(uploaderRotation, nodeConfiguration);
        nodeMainExecutor.execute(uploaderLocation, nodeConfiguration);
        while( ! uploaderLinear.getmsgOn() && ! uploaderRotation.getmsgOn()){;}
        setUploaderVector3Stamped(uploaderLinear);
        setUploaderVector3Stamped(uploaderRotation);
        // The RosTextView is also a NodeMain that must be executed in order to
        // start displaying incoming messages.
        nodeMainExecutor.execute(downloaderLinear, nodeConfiguration);
        nodeMainExecutor.execute(downloaderRotation, nodeConfiguration);
        nodeMainExecutor.execute(downloaderPose, nodeConfiguration);
        cameraId = 0;
        cam = getCamera();
        rosCameraPreviewView.setCamera(cam);
        try {
            java.net.Socket socket = new java.net.Socket(getMasterUri().getHost(), getMasterUri().getPort());
            java.net.InetAddress local_network_address = socket.getLocalAddress();
            socket.close();
            nodeConfiguration =
                    NodeConfiguration.newPublic(local_network_address.getHostAddress(), getMasterUri());
            nodeMainExecutor.execute(rosCameraPreviewView, nodeConfiguration);
        } catch (IOException e) {
            // Socket problem
            Log.e("Camera", "socket error trying to get networking information from the master uri");
        }
        Log.i("ROSinit","ROSinit");
    }

    public void setUploaderVector3Stamped(Uploader<Vector3Stamped,float[]> uploaderVector3Stamped){
        float[] injector = new float[]{0,0,0};
        uploaderVector3Stamped.setLoopPeriod(10);//ms
        uploaderVector3Stamped.setInjector(injector);
        uploaderVector3Stamped.setMessageToStringCallable(new MessageCallable<Vector3Stamped, float[]>() {
            @Override
            public Vector3Stamped call(float[] injector) {
                Vector3Stamped vecS = uploaderVector3Stamped.getMsg();
                vecS.getHeader().setSeq(uploaderVector3Stamped.addSeq());
                vecS.getHeader().setStamp(new WallTimeProvider().getCurrentTime());
                vecS.getHeader().setFrameId("etabot");
                vecS.getVector().setX(injector[0]);
                vecS.getVector().setY(injector[1]);
                vecS.getVector().setZ(injector[2]);
                return vecS;
            }
        });
        uploaderVector3Stamped.on();
        Log.i("UploaderSet","Uploader set");
    }

    private Camera getCamera() {
        Camera cam = Camera.open(cameraId);
        Camera.Parameters camParams = cam.getParameters();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            if (camParams.getSupportedFocusModes().contains(
                    Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                camParams.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            } else {
                camParams.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            }
        }
        cam.setParameters(camParams);
        return cam;
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
        if (requestingLocationUpdates)registerLocators();
        updateOrientationAngles();
        if(cam!=null){
            cam = getCamera();
            rosCameraPreviewView.setCamera(cam);
        }
        Log.i("OnResume","Camera registered again");
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
        if(SwitchSensorsShown.isChecked()) {
            vector_AM.setText("X:" + java.lang.String.valueOf(orientationAngles[0]) + "\nY:" + java.lang.String.valueOf(orientationAngles[1]) + "/nZ:" + java.lang.String.valueOf(orientationAngles[2]));
            vector_RV.setText("X:" + java.lang.String.valueOf(RVReading[0]) + "\nY:" + java.lang.String.valueOf(RVReading[1]) + "\nZ:" + java.lang.String.valueOf(RVReading[2]));
            vector_RV_q.setText("Qx:" + java.lang.String.valueOf(RVRotationMatrix[0]) + "Qy:" + java.lang.String.valueOf(RVRotationMatrix[1]) + "Qz:" + java.lang.String.valueOf(RVRotationMatrix[2]) + "Qw:" + java.lang.String.valueOf(RVRotationMatrix[3]));
            vector_GameRV.setText("X:" + java.lang.String.valueOf(GameRVReading[0]) + "\nY:" + java.lang.String.valueOf(GameRVReading[1]) + "\nZ:" + java.lang.String.valueOf(GameRVReading[2]));
            vector_GeoRV.setText("X:" + java.lang.String.valueOf(GeoRVReading[0]) + "\nY:" + java.lang.String.valueOf(GeoRVReading[1]) + "\nZ:" + java.lang.String.valueOf(GeoRVReading[2]));
            vector_G.setText("X:" + java.lang.String.valueOf(GReading[0]) + "\nY:" + java.lang.String.valueOf(GReading[1]) + "\nZ:" + java.lang.String.valueOf(GReading[2]));
            vector_LAcc.setText("X:" + java.lang.String.valueOf(LAccReading[0]) + "\nY:" + java.lang.String.valueOf(LAccReading[1]) + "\nZ:" + java.lang.String.valueOf(LAccReading[2]));
        }
        //update the vector
        if(uploaderLinear != null){
            uploaderLinear.setInjector(LAccReading);
        }
        if(uploaderRotation != null){
            uploaderRotation.setInjector(orientationAngles);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        // Do something here if sensor accuracy changes.
        // You must implement this callback in your code.
    }

    public void showLocation() { //
        Log.i("LocationChanged", "Location changed");
        if(!requestingLocationUpdates) {
            mLocation = null;
            return;
        }
        if(mLocation !=null){
            float latitude = (float) mLocation.getLatitude();
            float longitude = (float) mLocation.getLongitude();
            float altitude = (float) mLocation.getAltitude();
            LocationToSendView.setText(
                    "La:" + String.format("%.2f", latitude) +
                            " Lo:" + String.format("%.2f", longitude) +
                            " Lo:" + String.format("%.2f", altitude));
            if(uploaderLocation != null){
                float[] location = new float[]{latitude,longitude,altitude};
                uploaderLocation.setInjector(location);
            }
        }
    }

    private static final int TWO_MINUTES = 1000 * 60 * 2;

    /** Determines whether one Location reading is better than the current Location fix
     * @param location  The new Location that you want to evaluate
     * @param currentBestLocation  The current Location fix, to which you want to compare the new one
     */
    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    public void updateOrientationAngles() {
        // Update rotation matrix, which is needed to update orientation angles.
        SensorManager.getRotationMatrix(rotationMatrix, null,
                accelerometerReading, magnetometerReading);
        // "mRotationMatrix" now has up-to-date information.
        SensorManager.getOrientation(rotationMatrix, orientationAngles);
        //Log.i("OrientationAngles","OrientationAngles updated");
    }

    //fusedLocationClient requires Play Service of Version 11 or higher.
    public void onLocationResult(LocationResult locationResult) {
        List<Location> locationList = locationResult.getLocations();
        if (locationList.size() > 0) {
            //The last location in the list is the newest
            Location newlocation = locationList.get(locationList.size() - 1);
            Log.i("MapsActivity", "Location: " + newlocation.getLatitude() + " " + newlocation.getLongitude());
            mLocation = newlocation;
        }
    }

    //fusedLocationClient requires Play Service of Version 11 or higher.
    @SuppressLint("MissingPermission")
    private void updateStartLocation() {
        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback,
                Looper.getMainLooper());
    }

    public void registerLocators(){
        if (locationManager!=null)
        {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "No permission for locating", Toast.LENGTH_SHORT).show();
                finish();
            }
            else{
                    if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                    {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 200,0, gpsListener);
                        Log.i("Locator","GPS provider requested");
                    }
                    if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
                    {
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 200,0, networkListener);
                        Log.i("Locator","NETWORK provider requested");
                    }
                    /*if (locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)) {
                        locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 200, 0, (LocationListener) this);
                        Log.i("Locator", "PASSIVE provider requested");
                    }*/
            }
        }
        Log.i("Locator","Locators registered");
    }

    LocationListener gpsListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            if (isBetterLocation(location, mLocation)) {
                //locationManager.removeUpdates(networkListener);
                mLocation = location;
                showLocation();
                Log.i("Locator","GpsListener reported");
            }
            /*if (mLocation != null) {
                locationManager.removeUpdates(this);
            }*/
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }
    };

    LocationListener networkListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            if (isBetterLocation(location, mLocation)) {
                mLocation = location;
                showLocation();
                Log.i("Locator","NetworkListener reported");
            }
            /*if (mLocation != null) {
                locationManager.removeUpdates(this);
            }*/
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

    };

    public void stopLocating(){
        if(locationManager != null){
            locationManager.removeUpdates(gpsListener);
            locationManager.removeUpdates(networkListener);
            locationManager.removeUpdates((LocationListener) this);
            mLocation = null;
        }
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
        rosCameraPreviewView.releaseCamera();
        stopLocating();
    }


    @Override
    protected void onStop() {
        super.onStop();
        stopLocating();
        Log.i("OnStop","OnStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLocating();
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
