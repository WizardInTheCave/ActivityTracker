package com.example.jack.coursework4_activitytracker;

/**
 * Created by Jack on 16/11/2016.
 */

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.content.ContextCompat;

import android.location.LocationManager;
import android.location.Location;

public class LocationManagementService extends Service {

    private Messenger messenger;

    public LocationManager locationManager;
    public ALocationListener listener;
    public Location previousBestLocation = null;

    public static final String RECEIVE_LOCATION = "com.example.jack.coursework4_activitytracker.RECEIVE_TIME";

    public static final int GET_ALL_POINTS = 1;
    public static final int START_LOGGING = 2;
    public static final int MY_PERMISSION_COURSE_LOCATION_REQ_CODE = 1;

    boolean alreadyLogging = false;

    double altitude;
    double longitude;
    double latitude;

    IntentFilter intentFilter;
    MyReceiver receiver;

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
    public void onCreate() {

        messenger = new Messenger(new MyHandler());

        receiver = new MyReceiver();
        intentFilter = new IntentFilter();
        intentFilter.addAction(RECEIVE_LOCATION);
        registerReceiver(receiver, intentFilter);
    }

    @Override
    public IBinder onBind(Intent intent) {

        return messenger.getBinder();
    }


    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {

                case GET_ALL_POINTS:

                    break;

                // tell
                case START_LOGGING:

                    // Check that the user has provided permissions for the app to use their GPS coordinates
                    if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            && ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                        Bundle trackData = message.getData();
                        ListenerParcel mp3Parcel = trackData.getParcelable("myParcel");

                        int isLogging = mp3Parcel.isLogging;

                        Messenger replyto = message.replyTo;
                        Message reply = Message.obtain();

                        if (isLogging == 1 && !alreadyLogging) {
                            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                            listener = new ALocationListener(getApplicationContext());
                            // try {
                            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                                    5, // minimum time interval between updates
                                    5, // minimum distance between updates, in metres
                                    listener);
                            alreadyLogging = true;
                        }

                        try {
                            replyto.send(reply);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }

                    break;
                default:
                    super.handleMessage(message);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }
}
