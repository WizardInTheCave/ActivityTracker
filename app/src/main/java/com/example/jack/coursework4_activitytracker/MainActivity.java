package com.example.jack.coursework4_activitytracker;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.android.gms.nearby.messages.internal.ClientAppContext;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private Messenger messenger;
    Messenger replyMessenger;
    boolean activityIsBound = false;

    ArrayList<GoogleMapPos> locations = new ArrayList<>();

    IntentFilter intentFilter;
    MainReceiver receiver;

    static final int SHOW_ON_MAP = 1;

    boolean isTracking = false;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            messenger = new Messenger(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    public void showMap(View v){


        Intent intent = new Intent(MainActivity.this, MapsActivity.class);
        intent.putExtra("locationData", locations);
        startActivityForResult(intent, SHOW_ON_MAP);
    }
    private void updateUI(){

    }

    /**
     * get updates
     */
    class MainReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            // get the latest values from the ALocationListener activity
            int _id = intent.getIntExtra("_id", 0);
            double alt = intent.getDoubleExtra("Altitude", 0);
            double latitude = intent.getDoubleExtra("Latitude", 0);
            double longitude = intent.getDoubleExtra("Longitude", 0);

            TableLayout tl = (TableLayout)findViewById(R.id.mainTableTL);

            Context currentContext = getApplicationContext();
            TableRow tr = new TableRow(currentContext);

            TextView idTextView = new TextView(currentContext);
            TextView altTextView = new TextView(currentContext);
            TextView latTextView = new TextView(currentContext);
            TextView longTextView = new TextView(currentContext);

            idTextView.setText(Integer.toString(_id));
            altTextView.setText(Double.toString(alt));
            latTextView.setText(Double.toString(latitude));
            longTextView.setText(Double.toString(longitude));

            tr.addView(idTextView);
            tr.addView(altTextView);
            tr.addView(latTextView);
            tr.addView(longTextView);


            tl.addView(tr);
            Log.d("something happened", "something seems to have happened");
        }
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


        receiver = new MainReceiver();
        intentFilter = new IntentFilter();
        intentFilter.addAction(LocationManagementService.RECEIVE_LOCATION);
        registerReceiver(receiver, intentFilter);
    }

    public void changeTrackingStatus(View v){

        if ( ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions( this, new String[] {  android.Manifest.permission.ACCESS_COARSE_LOCATION  },
                    LocationManagementService.MY_PERMISSION_COURSE_LOCATION_REQ_CODE );
        }

        if(isTracking) {
            Message message = Message.obtain(null, LocationManagementService.STOP_LOGGING, 0, 0);
            sendMessageToService(message);

            Button trackingButton = (Button)findViewById(R.id.trackingButton);
            trackingButton.setText("Start Tracking");
            isTracking = false;
        }
        // tell the service to stop logging our users gps coordinates
        else{
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Message message = Message.obtain(null, LocationManagementService.START_LOGGING, 0, 0);
                sendMessageToService(message);

                Button trackingButton = (Button)findViewById(R.id.trackingButton);
                trackingButton.setText("Stop Tracking");
                isTracking = true;
            }
        }
    }
    private void sendMessageToService(Message message){
        ListenerParcel parcel = new ListenerParcel();

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
