package com.example.jack.coursework4_activitytracker;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
import android.widget.EditText;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private Messenger messenger;
    Messenger replyMessenger;
    boolean activityIsBound = false;

    IntentFilter intentFilter;
    MainReceiver receiver;

    boolean isTracking = false;

    static final int SHOW_THE_MAP = 1;
    static final int GET_JOURNEY_NAME = 2;

    static final int MAX_GRAPH_TIME_VAL = 25;

    /**
     * This collections keep track of all the data points we are plotting
     */
    LineGraphSeries<DataPoint> speedVsTimeSeries;
    LineGraphSeries<DataPoint> altVsTimeSeries;

    final String MAX_SPEED_ID = "maxSpeed";
    final String MAX_ALT_ID = "maxAlt";
    final int NOTIFICATION_ID = 1;


    int broadcastCount = 0;

    /**
     * Keep track of all the locations that have been received from the GPS service
     */
    ArrayList<GoogleMapPos> locations = new ArrayList<>();
    double locationsSpeedTotal = 0;


    /**
     * If user is moving faster than this there was a bug in the calculation of the speed so don't count it.
     */
    final double MAX_ALLOWED_SPEED = 200;

    double maxSpeed = 0;
    double maxAlt = 0;

    ArrayList<Long> times = new ArrayList<>();

    GraphView speedVsTimeGraph;
    GraphView altVsTimeGraph;

    static final String SPEED_OVER_TIME_TITLE = "Speed over time";
    static final String ALT_OVER_TIME_TITLE = "Altitude over time";

    /**
     * Get messenger object to allow further communication with the bound service
     */
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
     * When the maps activity is rotated want to preserve the display state
     * @param savedInstanceState
     */
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putDouble(MAX_ALT_ID, maxAlt);
        savedInstanceState.putDouble(MAX_SPEED_ID, maxSpeed);

        super.onSaveInstanceState(savedInstanceState);
    }

    /**
     * Start the maps activity to view the current journey on a map
     * @param v The current view
     */
    public void showMap(View v){
        Intent intent = new Intent(MainActivity.this, MapsActivity.class);
        startActivityForResult(intent, SHOW_THE_MAP);
    }


    /**
     * Get updates on the location which we can use to generate graphs for the user
     */
    class MainReceiver extends BroadcastReceiver {


        /**
         * This method is called every time a broadcast is received
         * providing an update on the users position
         */
        @Override
        public void onReceive(Context context, Intent intent) {

            GoogleMapPos currentLocation = (GoogleMapPos)intent.getExtras().getSerializable("Location");

            locations.add(currentLocation);
            locationsSpeedTotal += currentLocation.speed;

            // Update the labels and the graphs
            UpdateInfoLabels(currentLocation);
            UpdateGraphs(currentLocation);

            broadcastCount++;
        }

        /**
         * Update all labels containing infomation about the statistics associated with the journey
         * @param currentLocation Information about the most recent location the user
         *                        was detected but as a google maps position
         */
        private void UpdateInfoLabels(GoogleMapPos currentLocation) {

            // Average Speed
            double avgSpeed = locationsSpeedTotal / locations.size();
            TextView avgSpeedValText = (TextView)findViewById(R.id.avgSpeedValText);
            avgSpeedValText.setText(String.format("%.2f",avgSpeed));

            // Check if there is a new max speed and that the user isn't going faster than should be possible
            // if so update the max speed recorded so far on the journey
            if(currentLocation.speed > maxSpeed && currentLocation.speed < MAX_ALLOWED_SPEED){
                maxSpeed = currentLocation.speed;
                TextView maxSpeedValText = (TextView)findViewById(R.id.maxSpeedValText);
                maxSpeedValText.setText(String.format("%.2f",maxSpeed));
            }
            if(currentLocation.alt > maxAlt){
                maxAlt = currentLocation.alt;
                TextView maxAltValText = (TextView)findViewById(R.id.maxAltValText);
                maxAltValText.setText(String.format("%.2f",maxAlt));
            }


        }

        /**
         * Update the graph displays showing the change in speeed and altitude over time
         * @param currentLocation Information about the most recent location the user
         *                        was detected but as a google maps position
         */
        private void UpdateGraphs(GoogleMapPos currentLocation) {

            long startTime = locations.get(0).timeSeconds;
            long timeIntoJourney = currentLocation.timeSeconds - startTime;
            times.add(timeIntoJourney);

            // reset the buffer every 5th broadcast recieved so the Activity doesn't freeze
            // when trying to render a really large graph
            if(broadcastCount % 5 == 0 && broadcastCount >= 10) {

                int count = 5;
                DataPoint[] speedVals = new DataPoint[count];
                DataPoint[] altVals = new DataPoint[count];

                int locationSize = locations.size();
                int jj = 0;
                for(int ii = locationSize - 5; ii < locationSize; ii++){
                    double time = times.get(ii);
                    speedVals[jj] = new DataPoint(time, locations.get(ii).speed);
                    altVals[jj] = new DataPoint(time, locations.get(ii).alt);
                    jj++;
                }

                speedVsTimeSeries.resetData(speedVals);
                altVsTimeSeries.resetData(altVals);
            }
            else {
                // otherwise just load all the points normally
                speedVsTimeSeries.appendData(new DataPoint(timeIntoJourney, currentLocation.speed), true, MAX_GRAPH_TIME_VAL);
                altVsTimeSeries.appendData(new DataPoint(timeIntoJourney, currentLocation.alt), true, MAX_GRAPH_TIME_VAL);
                speedVsTimeGraph.addSeries(speedVsTimeSeries);
                altVsTimeGraph.addSeries(altVsTimeSeries);
            }
        }
    }

    /**
     * Set up the graphs when the activity is created
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        broadcastCount = 0;
        locationsSpeedTotal = 0;

        if (savedInstanceState != null) {
            // Restore value of members from saved state
            maxAlt = savedInstanceState.getDouble(MAX_ALT_ID);
            maxSpeed = savedInstanceState.getDouble(MAX_SPEED_ID);

        }

        speedVsTimeGraph = (GraphView)findViewById(R.id.speedOverTimeGraph);
        speedVsTimeGraph.setTitle(SPEED_OVER_TIME_TITLE);
        speedVsTimeSeries = new LineGraphSeries<>();

        GridLabelRenderer speedVsTimeGridLabel = speedVsTimeGraph.getGridLabelRenderer();
        speedVsTimeGridLabel.setHorizontalAxisTitle("Time (seconds)");
        speedVsTimeGridLabel.setVerticalAxisTitle("Speed (m/s)");


        altVsTimeGraph = (GraphView)findViewById(R.id.elevationOverTimeGraph);
        altVsTimeGraph.setTitle(ALT_OVER_TIME_TITLE);
        altVsTimeSeries = new LineGraphSeries<>();

        GridLabelRenderer altVsTimeGridLabel = altVsTimeGraph.getGridLabelRenderer();
        altVsTimeGridLabel.setHorizontalAxisTitle("Time (seconds)");
        altVsTimeGridLabel.setVerticalAxisTitle("Altitude (meters)");

        // make a new "Original" table for temporary storage
        getContentResolver().update(LocationsContentProviderContract.JOURNEY_NAMES_ADD_URI, null, null, null);

        // start the service so we can start gathering GPS data
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

    /**
     * Tell the service to stop tracking and broadcasting data
     */
    private void stopTracking(){
        if ( ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions( this, new String[] {  android.Manifest.permission.ACCESS_COARSE_LOCATION  },
                    LocationManagementService.MY_PERMISSION_COURSE_LOCATION_REQ_CODE );
        }
        Message message = Message.obtain(null, LocationManagementService.STOP_LOGGING, 0, 0);
        sendMessageToService(message);

        Button trackingButton = (Button)findViewById(R.id.trackingButton);
        trackingButton.setText("Start Tracking");

        //re set the max speed and alt as it is no longer relevant
        resetDisplays();

        isTracking = false;
    }


    /**
     * Tell the service to resume tracking and broadcasting data
     */
    private void startTracking(){
        if ( ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions( this, new String[] {  android.Manifest.permission.ACCESS_COARSE_LOCATION  },
                    LocationManagementService.MY_PERMISSION_COURSE_LOCATION_REQ_CODE );
        }
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Message message = Message.obtain(null, LocationManagementService.START_LOGGING, 0, 0);
            sendMessageToService(message);

            Button trackingButton = (Button)findViewById(R.id.trackingButton);
            trackingButton.setText("Stop Tracking");

            resetDisplays();
            isTracking = true;
        }
    }

    /**
     * Flip whatever the current tracking statis is
     * @param v
     */
    public void changeTrackingStatus(View v){
        if(isTracking) {
            stopTracking();
        }
        // tell the service to stop logging our users gps coordinates
        else{
            startTracking();
        }
    }

    /**
     * Load a journey that was previously recorded from the database
     */
    public void loadPreviousJourney(View v){
        Bundle bundle = new Bundle();
        Intent intent = new Intent(MainActivity.this, LoadJourneyActivity.class);
        intent.putExtras(bundle);
        startActivityForResult(intent, GET_JOURNEY_NAME);
    }

    /**
     * Reset the GUI components as we are starting a new tracking cycle
     * data previously displayed was not relivent
     */
    private void resetDisplays(){
        GraphView altVsTime = (GraphView)findViewById(R.id.elevationOverTimeGraph);
        altVsTime.removeAllSeries();

        GraphView speedVsTime = (GraphView)findViewById(R.id.speedOverTimeGraph);
        speedVsTime.removeAllSeries();

        maxAlt = 0;
        maxSpeed = 0;

        TextView maxAltValText = (TextView)findViewById(R.id.maxAltValText);
        maxAltValText.setText(Double.toString(maxAlt));

        TextView maxSpeedValText = (TextView)findViewById(R.id.avgSpeedValText);
        maxSpeedValText.setText(Double.toString(maxSpeed));

        broadcastCount = 0;
    }
    /**
     * We already have a default database we want to change the name of it so that we can store another one but reload it later
     * check that the user has entered an OK name and then store the current journey in the database with an official name
     */
    public void saveJourneyName(View v){

        EditText journeyNameText = (EditText) findViewById(R.id.journeyNameText);
        String journeyName = journeyNameText.getText().toString();

        // check the name input
        if(journeyName.contains(" ")) {
            // clear the text to indicate that the user has saved their journey
            journeyNameText.setTextColor(Color.RED);
            journeyNameText.setText("Name cannot contain space");
        }
        else if(journeyName.isEmpty()){
            journeyNameText.setTextColor(Color.RED);
            journeyNameText.setText("Must provide a name");
        }
        else {

            stopTracking();

            ContentValues contentValues = new ContentValues();
            contentValues.put(LocationsContentProviderContract.JOURNEY_NAMES_FIELD, journeyName);
            getContentResolver().update(LocationsContentProviderContract.JOURNEY_NAMES_RENAME_URI, contentValues, null, null);

            // clear the text to indicate that the user has saved their journey
            journeyNameText.setText("");


            // switch back to the orignal journey table and delete all the markers in original since we have now saved the journey and so want
            // to work with a new one containing no prior locations
            contentValues = new ContentValues();
            contentValues.put(LocationsContentProviderContract.JOURNEY_NAMES_FIELD, LocationsContentProviderContract.DEFAULT_JOURNEY_TABLE);
            getContentResolver().update(LocationsContentProviderContract.CHANGE_CURRENTLY_SELECTED_TABLE_URI, contentValues, null, null);

            // delete the original table
            getContentResolver().delete(LocationsContentProviderContract.GENERAL_QUERY_URI, null, null);

        }
    }

    /**
     * Get the journey we have selected back from the LoadJourneyActivity
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case GET_JOURNEY_NAME:
                if (resultCode == RESULT_OK) {

                    // We now have the journey that we want to load from the database, so now update the content provider to
                    // switch to this database so that until the user changes the database again it continues to work
                    // with this one
                    String selectedJourneyTitle = data.getStringExtra(LoadJourneyActivity.SELECTED_JOURNEY_TITLE);

                    // tell our service to stop tracking because new GPS coordinates are no longer applicable to this journey.
                    stopTracking();

                    // send message to location listener telling it to update the id values to the end of the table we have switched to +1
                    // otherwise the system will try to add valus into the currently selected table with primary key indexes for the previous table
                    Message message = Message.obtain(null, LocationManagementService.UPDATE_INSERT_KEY, 0, 0);
                    sendMessageToService(message);

                    // tell the content provider we are back
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(LocationsContentProviderContract.JOURNEY_NAMES_FIELD, selectedJourneyTitle);
                    getContentResolver().update(LocationsContentProviderContract.CHANGE_CURRENTLY_SELECTED_TABLE_URI, contentValues, null, null);

                    // reset speeds and locations since the journey is now different
                    locations = new ArrayList<>();
                    times = new ArrayList<>();

                    speedVsTimeSeries = new LineGraphSeries<>();
                    altVsTimeSeries =  new LineGraphSeries<>();

                } else if (resultCode == RESULT_CANCELED) {
                    Log.e("RequestCode error", "Recieved an error as a return when browsing for an previous journey");
                }
                break;

            case SHOW_THE_MAP:
                if(resultCode == RESULT_OK){
                    resetDisplays();
                }
                else if (resultCode == RESULT_CANCELED) {
                    Log.e("RequestCode error", "Recieved an error as a return when looking at the map");
                }
                break;
        }
    }

    /**
     * Generic method for sending a message to the LocationManagementService using a parcel
     * @param message
     */
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

    /**
     *  before destroying the activity unbind from
     *  the service so it does not continue running and unregister from the broadcast recever.

     */
    public void onDestroy(){
        if(activityIsBound) {
            unbindService(serviceConnection);
            unregisterReceiver(receiver);
        }
        super.onDestroy();
    }
}
