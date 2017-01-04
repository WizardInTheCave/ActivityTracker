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

    public LocationManager locationManager = null;
    public ALocationListener listener = null;
    public Location previousBestLocation = null;

    public static final String RECEIVE_LOCATION = "com.example.jack.coursework4_activitytracker.RECEIVE_LOCATION";


    public static final int STOP_LOGGING = 1;
    public static final int START_LOGGING = 2;
    public static final int MY_PERMISSION_COURSE_LOCATION_REQ_CODE = 1;

    boolean currentlyLogging = false;

    IntentFilter intentFilter;
    ServiceReceiver receiver;

    class ServiceReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            // get the latest values from the ALocationListener activity
            int _id = intent.getIntExtra("_id", 0);
            double altitude = intent.getDoubleExtra("Altitude", 0);
            double latitude = intent.getDoubleExtra("Latitude", 0);
            double longitude = intent.getDoubleExtra("Longitude", 0);

            ContentValues newValues = new ContentValues();

            newValues.put(LocationContentProviderContract._ID, _id);
            newValues.put(LocationContentProviderContract.ALTITUDE, altitude);
            newValues.put(LocationContentProviderContract.LONGITUDE, longitude);
            newValues.put(LocationContentProviderContract.LATITUDE, latitude);

            // getContentResolver().insert(LocationContentProviderContract.LOCATION_URI, newValues);
        }
    }
    @Override
    public void onCreate() {

        messenger = new Messenger(new MyHandler());

        receiver = new ServiceReceiver();
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
            Messenger replyto = message.replyTo;
            Message reply = Message.obtain();

            switch (message.what) {

                case STOP_LOGGING:

                    // isLogging == 1 &&
                    if(locationManager != null && listener != null){
                        locationManager.removeUpdates(listener);
                        currentlyLogging = false;
                    }

                    break;

                // tell
                case START_LOGGING:

                    // Check that the user has provided permissions for the app to use their GPS coordinates
                    if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            && ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                        Bundle trackData = message.getData();
                        ListenerParcel commandParcel = trackData.getParcelable("myParcel");

                        // isLogging == 1 &&
                        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                        listener = new ALocationListener(getApplicationContext());
                        // try {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                                5, // minimum time interval between updates
                                5, // minimum distance between updates, in metres
                                listener);
                        currentlyLogging = true;
                    }
                    break;
                default:
                    super.handleMessage(message);
            }
            try {
                replyto.send(reply);
            } catch (RemoteException e) {
                e.printStackTrace();
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
