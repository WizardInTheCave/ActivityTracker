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
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.content.ContextCompat;

import android.location.LocationManager;
import android.location.Location;
import android.util.Log;

public class LocationManagementService extends Service {

    private Messenger messenger;

    public LocationManager locationManager = null;
    public ALocationListener listener = null;
    public Location previousBestLocation = null;

    public static final String RECEIVE_LOCATION = "com.example.jack.coursework4_activitytracker.RECEIVE_LOCATION";

    public static final int STOP_LOGGING = 1;
    public static final int START_LOGGING = 2;
    public static final int UPDATE_INSERT_KEY = 3;

    public static final int MY_PERMISSION_COURSE_LOCATION_REQ_CODE = 1;

    boolean currentlyLogging = false;

    IntentFilter intentFilter;
    ServiceReceiver receiver;

    class ServiceReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            GoogleMapPos currentLocation = (GoogleMapPos)intent.getExtras().getSerializable("Location");

            ContentValues newValues = new ContentValues();

            newValues.put(LocationsContentProviderContract._ID, currentLocation._id);
            newValues.put(LocationsContentProviderContract.ALTITUDE, currentLocation.alt);
            newValues.put(LocationsContentProviderContract.LONGITUDE, currentLocation.longitude);
            newValues.put(LocationsContentProviderContract.LATITUDE, currentLocation.latitude);
            newValues.put(LocationsContentProviderContract.IMAGE_PATH, "null");

            try {
                Log.d("Next primary key", Integer.toString(currentLocation._id));
                getContentResolver().insert(LocationsContentProviderContract.GENERAL_QUERY_URI, newValues);
            }
            catch (SQLException e){
                Log.d("what?", "Thing");
                e.printStackTrace();
            }
        }
    }
    @Override
    public void onCreate() {

        messenger = new Messenger(new MyHandler());

        // create the location listener
        listener = new ALocationListener(getApplicationContext());

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
                    if (locationManager != null && listener != null) {
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

                        // try {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                                5, // minimum time interval between updates
                                5, // minimum distance between updates, in metres
                                listener);
                        currentlyLogging = true;
                    }
                    break;


                case UPDATE_INSERT_KEY:
                    if (listener != null) {

                        // this will give the currently selected table, now need to find max primary key value in table
                        // increment by one and hand to the listener

                        Cursor cursor = getContentResolver().query(LocationsContentProviderContract.GET_HIGHEST_PRIMARY,
                                null, null, null, null);

                        if (cursor != null) {
                            // get all the recipes in the database stored in the form of a hash map so can send back to main activity
                            if (cursor.moveToFirst()) {
                                int startingPrimaryKey = cursor.getInt(cursor.getColumnIndex(LocationsContentProviderContract._ID) + 1);
                                listener.updateInsertKey(startingPrimaryKey);
                            }
                            // carry on from where we left off inserting
                        }
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
