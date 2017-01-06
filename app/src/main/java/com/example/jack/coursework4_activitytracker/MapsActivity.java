package com.example.jack.coursework4_activitytracker;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class MapsActivity extends android.support.v4.app.FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener,  {


    private static final String TAG = MapsActivity.class.getSimpleName();

    GoogleMap googleMap;
    ArrayList<Marker> markers;

    Marker currentMarker = null;

    boolean mapLoaded = false;

    IntentFilter intentFilter;
    MapsReceiver receiver;


    String currentTitle;
    String currentAlt;
    String currentImage;
    String currentLat;
    String currentLong;

    static final String CURRENT_TITLE_ID = "markerTitle";
    static final String CURRENT_ALTITUDE_ID = "markerAlt";
    static final String CURRENT_IMAGE_ID = "markerImg";
    static final String CURRENT_LAT_ID = "markerLat";
    static final String CURRENT_LONG_ID = "markerLong";

    // Storage Permissions
    private final int REQUEST_EXTERNAL_STORAGE = 1;
    private String[] PERMISSIONS_STORAGE = {
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_maps);

        // if we have rotated the screen then we want the information we were displaying previously to still be displayed
        if (savedInstanceState != null) {
            // Restore value of members from saved state
            currentTitle = savedInstanceState.getString(CURRENT_TITLE_ID);
            currentAlt = savedInstanceState.getString(CURRENT_ALTITUDE_ID);
            currentImage = savedInstanceState.getString(CURRENT_IMAGE_ID);
            currentLat = savedInstanceState.getString(CURRENT_LAT_ID);
            currentLong = savedInstanceState.getString(CURRENT_LONG_ID);
            updateUI();

        } else {
            // Probably initialize members with default values for a new instance
            setDefaults();
        }



        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        receiver = new MapsReceiver();
        intentFilter = new IntentFilter();
        intentFilter.addAction(LocationManagementService.RECEIVE_LOCATION);
        registerReceiver(receiver, intentFilter);

    }

    /**
     * when the maps activity is rotated want to preserve the display state
     * @param savedInstanceState
     */
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString(CURRENT_TITLE_ID, currentTitle);
        savedInstanceState.putString(CURRENT_ALTITUDE_ID, currentAlt);
        savedInstanceState.putString(CURRENT_IMAGE_ID, currentImage);
        savedInstanceState.putString(CURRENT_LAT_ID, currentLat);
        savedInstanceState.putString(CURRENT_LONG_ID, currentLong);
        super.onSaveInstanceState(savedInstanceState);
    }

    /**
     * Remove the marker the user has selected from the map and the database
     * @param v
     */
    public void deletePoint(View v){

        getContentResolver().delete(LocationContentProviderContract.LOCATION_URI, LocationContentProviderContract._ID +
                "=" + Integer.valueOf(currentMarker.getTitle())
                , null);

        currentMarker.remove();

//        LocationContentProviderContract.LOCATION_URI,
//                LocationContentProviderContract.LATITUDE + "=\"" + currentMarker.getPosition().latitude + "\"" +
//                        " AND " + LocationContentProviderContract.LONGITUDE + "=\"" + currentMarker.getPosition().longitude + "\"", null
    }

    /**
     * Remove all the markers from the map and within the DB
     * @param v
     */
    public void deleteAll(View v){
        for(Marker marker : markers){
            marker.remove();
        }

        getContentResolver().delete(LocationContentProviderContract.LOCATION_URI, null, new String[]{"*"});
    }

    /**
     * When a marker is selected we want to display its information to the user
     * @param marker
     * @return
     */
    @Override
    public boolean onMarkerClick(final Marker marker) {
        currentMarker = marker;

        final String title = marker.getTitle();
        currentTitle = marker.getTitle();
        // currentAlt = Double.toString(marker.);

        // the google map markers don't contain alt data so will need to cross reference to get this
        // altValText.setText(D);
        currentLat = Double.toString(marker.getPosition().latitude);
        currentLong = Double.toString(marker.getPosition().longitude);

        // ImageView markerImage = (ImageView)findViewById(R.id.markerImage);

        getPhoto(title);

        // markerImage.setImageURI(imageUri);
        return true;
    }

    private void setDefaults(){

        final String TITLE = "(Mark ID)";
        final String ALTITUDE = "(Altitude)";
        final String LATITUDE = "(Latitude)";
        final String LONGITUDE = "(Longitude)";


        TextView idValText = (TextView)findViewById(R.id.idValText);

        // TextView altValText = (TextView)findViewById(R.id.altValText);
        TextView latValText = (TextView)findViewById(R.id.latValText);
        TextView longValText = (TextView)findViewById(R.id.longValText);

        idValText.setText(TITLE);
        // altValText.setText(currentAlt);

        latValText.setText(LATITUDE);
        longValText.setText(LONGITUDE);

    }

    private void updateUI(){


        TextView idValText = (TextView)findViewById(R.id.idValText);
        // TextView altValText = (TextView)findViewById(R.id.altValText);
        ImageView markerImage = (ImageView)findViewById(R.id.markerImage);
        TextView latValText = (TextView)findViewById(R.id.latValText);
        TextView longValText = (TextView)findViewById(R.id.longValText);


        idValText.setText(currentTitle);

        Uri imageUri = Uri.parse(currentImage);
        markerImage.setImageURI(imageUri);
        // altValText.setText(currentAlt);
        latValText.setText(currentLat);
        longValText.setText(currentLong);

        // the google map markers don't contain alt data so will need to cross reference to get this


        //getPhoto(currentTitle);


    }


    /**
     * Get an zoomed in image of the marker
     * @param title the key we are going to use to reference the image in internal storage
     * @return Uri for the image
     */
    private void getPhoto(String title){

        verifyStoragePermissions();

        final float originalZoom = googleMap.getCameraPosition().zoom;
        final String imagePath ="/mnt/sdcard/map_"+ title + ".png";

        File image = new File(imagePath);

        if(!image.exists()) {

            // zoom in to get a good photo of the marker
            // googleMap.animateCamera(CameraUpdateFactory.zoomTo(10));

            // we want to only take a photo once the camera has moved to the correct position

            googleMap.snapshot(new GoogleMap.SnapshotReadyCallback() {
                public void onSnapshotReady(Bitmap bitmap) {
                    // Write image to disk
                    FileOutputStream out = null;
                    try {
                        out = new FileOutputStream(imagePath);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);

                    // set the mini image of the marker on the display
                    currentImage = imagePath;
                    updateUI();

                    // zoom out again
                    // googleMap.animateCamera(CameraUpdateFactory.zoomTo(originalZoom));
                }
            });
            // zoom back out again for the user
        }
    }
//
//    /**
//     * Checks if the app has permission to write to device storage
//     *
//     * If the app does not has permission then the user will be prompted to grant permissions
//     *
//     * @param activity
//     */
    public void verifyStoragePermissions() {
       // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);

        // try to get permission granted
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    /**
     * For new points that are provided by the location listener we want to recieve these and add them to the map display
     */
    class MapsReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            // get the latest values from the ALocationListener activity
            int _id = intent.getIntExtra("_id", 0);
            double alt = intent.getDoubleExtra("Altitude", 0);
            double latitude = intent.getDoubleExtra("Latitude", 0);
            double longitude = intent.getDoubleExtra("Longitude", 0);
            GoogleMapPos location = new GoogleMapPos(_id, alt, latitude, longitude);
            addMarker(location);
            Log.d("something happened", "something seems to have happened");
        }
    }


    private void addMarker(GoogleMapPos loc){

        LatLng location = new LatLng(loc.latitude, loc.longitude);
        // add a new marker to the list of map markers and display
        MarkerOptions newMarker = new MarkerOptions().position(location).title(Integer.toString(loc._id));

        // "Lat: " + Double.toString(loc.latitude) + " Long: " +
        //Double.toString(loc.longitude) + " Alt: " + Double.toString(loc.alt)

        Marker mark = googleMap.addMarker(newMarker);
        markers.add(mark);


    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {


        // if(mapLoaded) {
            googleMap.setOnMarkerClickListener(this);
            this.googleMap = googleMap;
            this.markers = new ArrayList<>();

            ArrayList<GoogleMapPos> locations = loadFromDB();

            // add all the markers in the database from the start

            for (GoogleMapPos loc : locations) {
                addMarker(loc);
            }
            mapLoaded = false;



        // }
        //LatLng lastLatLang = null// updatePoints = new UpdatePoints();
//        try {
//            googleMap.moveCamera(CameraUpdateFactory.newLatLng(lastLatLang));
//        }
//        catch (Exception e){
//            e.printStackTrace();
//        }
    }

    /**
     * Load points that have so far been logged within the DB into the locations list for displaying
     */
    private ArrayList<GoogleMapPos>  loadFromDB() {

        ArrayList<GoogleMapPos> locations = new ArrayList<>();

        final String[] projection = {LocationContentProviderContract._ID, LocationContentProviderContract.ALTITUDE, LocationContentProviderContract.LATITUDE, LocationContentProviderContract.LONGITUDE};

        Cursor cursor = getContentResolver().query(LocationContentProviderContract.LOCATION_URI,
                projection,
                null, null, null, null);

        if (cursor != null) {
            // get all the recipes in the database stored in the form of a hash map so can send back to main activity
            if (cursor.moveToFirst()) {
                do {
                    int _id = cursor.getInt(cursor.getColumnIndex(LocationContentProviderContract._ID));
                    double alt = cursor.getDouble(cursor.getColumnIndex(LocationContentProviderContract.ALTITUDE));
                    double latitude = cursor.getDouble(cursor.getColumnIndex(LocationContentProviderContract.LATITUDE));
                    double longitude = cursor.getDouble(cursor.getColumnIndex(LocationContentProviderContract.LONGITUDE));
                    locations.add(new GoogleMapPos(_id, alt, latitude, longitude));

                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return locations;
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);
        super.onDestroy();
    }
}
