package com.example.jack.coursework4_activitytracker;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
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

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private Messenger messenger;
    Messenger replyMessenger;
    boolean activityIsBound = false;

    IntentFilter intentFilter;
    MainReceiver receiver;

    static final int SHOW_ON_MAP = 1;

    boolean isTracking = false;

    LineGraphSeries<DataPoint> series;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            messenger = new Messenger(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    static final String LOCATION_ID = "locations";
    static final String SPEEDS_ID = "speeds";
    static final String TIMES_ID = "times";

    ArrayList<GoogleMapPos> locations = new ArrayList<>();
    ArrayList<Double> speeds = new ArrayList<>();
    ArrayList<Long> times = new ArrayList<>();

    //    /**
//     * when the maps activity is rotated want to preserve the display state
//     * @param savedInstanceState
//     */
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putSerializable(SPEEDS_ID, speeds);
        savedInstanceState.putSerializable(TIMES_ID, times);
        savedInstanceState.putSerializable(LOCATION_ID, locations);

        super.onSaveInstanceState(savedInstanceState);
    }

    public void showMap(View v){

        Intent intent = new Intent(MainActivity.this, MapsActivity.class);
        // intent.putExtra("locationData", locations);
        startActivityForResult(intent, SHOW_ON_MAP);
    }
    private void updateUI(){

    }



    /**
     * get updates on the location which we can use to generate graphs for the user
     */
    class MainReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            // get the latest values from the ALocationListener activity

            GoogleMapPos currentLocation = (GoogleMapPos)intent.getExtras().getSerializable("Location");
            locations.add(currentLocation);

            Double speed = intent.getDoubleExtra("Speed", 0);
            speeds.add(speed);

            // get a list of all the seconds from when we started recording gps coordinates
            long startTime = locations.get(0).timeSeconds;
            times.add(currentLocation.timeSeconds - startTime);


            // speed over time graph
            GraphView speedVsTime = (GraphView)findViewById(R.id.speedOverTimeGraph);
            plotGraph(speedVsTime, times, speeds);

            ArrayList<Double> alts = new ArrayList<>();

            for(GoogleMapPos loc : locations){
                alts.add(loc.alt);
            }

            // elevation over time graph
            GraphView altVsTime = (GraphView)findViewById(R.id.elevationOverTimeGraph);
            plotGraph(altVsTime, times, alts);


            Log.d("something happened", "something seems to have happened");
        }
    }

    /**
     * This method plots a graph using a graph view and some Arraylists which form the x and y values for the data points
     * @param graphView GraphView object
     * @param xAxis X values
     * @param yAxis Y values
     */
    private void plotGraph(GraphView graphView, ArrayList<Long> xAxis, ArrayList<Double> yAxis){

        series = new LineGraphSeries<>();
        int graphEnd = xAxis.size();

        if(graphEnd == yAxis.size()){
            for(int ii = 0; ii < graphEnd; ii++) {
                Long x = xAxis.get(ii);
                Double y = yAxis.get(ii);
                series.appendData(new DataPoint(x, y), true, graphEnd);
            }
        }
        graphView.addSeries(series);
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


        // locations = savedInstanceState.get(LOCATION_ID);
        //times = savedInstanceState.getSerializable(TIMES_ID);



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
            unregisterReceiver(receiver);
        }
        super.onDestroy();
    }
}
