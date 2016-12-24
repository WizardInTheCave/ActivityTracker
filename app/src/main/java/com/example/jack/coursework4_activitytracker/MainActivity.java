package com.example.jack.coursework4_activitytracker;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private Messenger messenger;
    Messenger replyMessenger;
    boolean activityIsBound = false;

    ArrayList<double[]> locations = new ArrayList<>();

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            messenger = new Messenger(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    /**
     * Load location data out of our content provider and into the activity for display purposes
     */
    public void loadFromDB(View v) {
        locations.clear();
        // String queryFieldClean = "\"" + currentRecipeTitle + "\"";

        final String[] projection = {LocationContentProviderContract.ALTITUDE, LocationContentProviderContract.LONGITUDE, LocationContentProviderContract.LATITUDE};

        Cursor cursor = getContentResolver().query(LocationContentProviderContract.LOCATION_URI,
                projection,
                null, null, null, null);
        // LocationContentProviderContract.TITLE + "=" + queryFieldClean
        // getContentResolver().q


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

        String locationsData = "";

        for(double[] location : locations){
             locationsData += Double.toString(location[0]) + Double.toString(location[1]) + Double.toString(location[2])+ "\n";
        }

        TextView locationView = (TextView) findViewById(R.id.dbContentView);
        locationView.setText(locationsData);
    }
    private void updateUI(){

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // setContentView(R.layout.activity_maps);

        Intent initialBindIntent = new Intent(this, LocationManagementService.class);
        initialBindIntent.putExtra("bindWithHandler", true);

        this.startService(initialBindIntent);
        activityIsBound = this.bindService(initialBindIntent, serviceConnection, Context.BIND_AUTO_CREATE);

        replyMessenger = new Messenger(new MyHandler());
    }

    public void startTracking(View v){

        if ( ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions( this, new String[] {  android.Manifest.permission.ACCESS_COARSE_LOCATION  },
                    LocationManagementService.MY_PERMISSION_COURSE_LOCATION_REQ_CODE );
        }

        if ( ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION ) == PackageManager.PERMISSION_GRANTED ) {
            Message message = Message.obtain(null, LocationManagementService.START_LOGGING, 0, 0);
            ListenerParcel parcel = new ListenerParcel();
            parcel.isLogging = 1;

            Bundle bundle = new Bundle();
            bundle.putParcelable("myParcel", parcel);
            message.setData(bundle);
            message.replyTo = replyMessenger;

            try {
                messenger.send(message);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Log.d("g53mdp", "reply received");
        }
    }

    @Override
    public void onPause(){
        super.onPause();
    }
    @Override
    public void onResume(){ super.onResume(); }

    public void onDestroy(){
        if(activityIsBound) {
            unbindService(serviceConnection);
        }
        super.onDestroy();
    }
}
