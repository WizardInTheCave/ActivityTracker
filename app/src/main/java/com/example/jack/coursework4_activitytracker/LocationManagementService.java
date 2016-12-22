package com.example.jack.coursework4_activitytracker;

/**
 * Created by Jack on 16/11/2016.
 */

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

import android.location.LocationManager;
import android.location.Location;

public class LocationManagementService extends Service  {

    private Messenger messenger;

    public LocationManager locationManager;
    public ALocationListener listener;
    public Location previousBestLocation = null;

    public static final String RECEIVE_LOCATION = "com.example.jack.coursework4_activitytracker.RECEIVE_TIME";

    public static final int GET_ALL_POINTS = 1;
    // public static final int CHANGE_PLAY_STATE = 3;

    double altitude;
    double longitude;
    double latitude;

    class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            // get the latest values from the ALocationListener activity
            altitude = intent.getDoubleExtra("Altitude", 0);
            longitude = intent.getDoubleExtra("Longitude", 0);
            latitude = intent.getDoubleExtra("Latitude", 0);

            ContentValues newValues = new ContentValues();

            newValues.put(LocationContentProviderContract.ALTITUDE, altitude);
            newValues.put(LocationContentProviderContract.LONGITUDE, longitude);
            newValues.put(LocationContentProviderContract.LATITUDE, latitude);

            getContentResolver().insert(LocationContentProviderContract.LOCATION_URI, newValues);
        }
    }

    @Override
    public void onCreate()
    {

    }
    @Override
    public IBinder onBind(Intent intent) {
        return messenger.getBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub

        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        listener = new ALocationListener();

        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    5, // minimum time interval between updates
                    5, // minimum distance between updates, in metres
                    listener);
            // locationManager.getProvider();

        } catch(SecurityException e) {
            Log.d("g53mdp", e.toString());
        }

        Log.d("g53mdp", "onStartCommand");
        throw new RuntimeException("You should not start this service");
    }

    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message message){
        switch(message.what) {

        case GET_ALL_POINTS:

            break;

        default:
            super.handleMessage(message);
        }
    }

//    @Override
//    public void onDestroy() {
//        // TODO Auto-generated method stub
//        Log.d("g53mdp", "onDestroy");
//        super.onDestroy();
//    }
//
//    @Override
//    public void onRebind(Intent intent) {
//        // TODO Auto-generated method stub
//        Log.d("g53mdp", "onRebind");
//        super.onRebind(intent);
//    }
//
//    @Override
//    public boolean onUnbind(Intent intent) {
//        // TODO Auto-generated method stub
//        Log.d("g53mdp", "onUnbind");
//        return super.onUnbind(intent);

    }
}
