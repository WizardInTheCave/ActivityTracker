package com.example.jack.coursework4_activitytracker;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
public class MainActivity extends AppCompatActivity {

    boolean activityIsBound = false;

    ArrayList<double[]> locations = new ArrayList<double[]>();

    /**
     * Load location data out of our content provider and into the activity for display purposes
     */
    public void loadFromDB(View v) {
        locations.clear();
        // String queryFieldClean = "\"" + currentRecipeTitle + "\"";

        final String[] projection = {LocationContentProviderContract.ALTITUDE, LocationContentProviderContract.LONGITUDE, LocationContentProviderContract.LATITUDE};

        Cursor cursor = getContentResolver().query(LocationContentProviderContract.LOCATION_URI,
                projection,
                LocationContentProviderContract.TITLE + "=" + queryFieldClean, null, null, null);


        if (cursor != null) {
            // get all the recipes in the database stored in the form of a hash map so can send back to main activity
            if (cursor.moveToFirst()) {
                do {
                    double[] location = new double[3];
                    location[0] = cursor.getDouble(cursor.getColumnIndex(LocationContentProviderContract.ALTITUDE));
                    location[1] = cursor.getDouble(cursor.getColumnIndex(LocationContentProviderContract.LONGITUDE));
                    location[2] = cursor.getDouble(cursor.getColumnIndex(LocationContentProviderContract.LATITUDE));
                    locations.add(location);
                    // read each query record

                } while (cursor.moveToNext());
            }
            cursor.close();
        }
    }
    private void updateUI(){

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setContentView(R.layout.activity_maps);
    }

    public void startTracking(View v){

        Intent initialBindIntent = new Intent(this, LocationManagementService.class);
        initialBindIntent.putExtra("bindWithHandler", true);

        this.startService(initialBindIntent);
        activityIsBound = this.bindService(initialBindIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private Messenger messenger;
    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Log.d("g53mdp", "reply received");
        }
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            messenger = new Messenger(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    public void onDestroy(){
        if(activityIsBound) {
            unbindService(serviceConnection);
        }
        super.onDestroy();
    }
}
