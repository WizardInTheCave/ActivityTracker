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
import java.net.URI;
import java.util.ArrayList;

public class MapsActivity extends android.support.v4.app.FragmentActivity implements
        OnMapReadyCallback, GoogleMap.OnMarkerClickListener  {


    private static final String TAG = MapsActivity.class.getSimpleName();

    GoogleMap googleMap;
    ArrayList<Marker> markers;

    Marker currentMarker = null;


    IntentFilter intentFilter;
    MapsReceiver receiver;


    String currentTitle;
    String currentAlt;
    String currentImage;
    String currentLat;
    String currentLong;

    String imagePath;

    boolean takingPhoto = false;

    float originalZoomAmount;

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

        if(currentMarker != null) {
            currentMarker.remove();
            String whereClause = LocationsContentProviderContract._ID + "= ?";
            String[] whereArgs = new String[]{currentMarker.getTitle()};

            // getContentResolver().update(LocationContentProviderContract.LOCATION_URI, contentValues ,whereClause, whereArgs);
            getContentResolver().delete(LocationsContentProviderContract.GENERAL_QUERY_URI, whereClause, whereArgs);
        }

    }

    /**
     * Remove all the markers from the map and within the DB
     * @param v
     */
    public void deleteAll(View v){
        for(Marker marker : markers){
            if(marker != null) {
                marker.remove();
            }
        }

        String whereClause = null; // LocationContentProviderContract._ID + "=?";
        String[] whereArgs = null; // new String[]{"*"};

        getContentResolver().delete(LocationsContentProviderContract.GENERAL_QUERY_URI, whereClause, whereArgs);
    }

    /**
     * When a marker is selected we want to display its information to the user
     * @param marker
     * @return
     */
    @Override
    public boolean onMarkerClick(final Marker marker) {
        currentMarker = marker;

        currentTitle = marker.getTitle();
        // currentAlt = Double.toString(marker.);

        // the google map markers don't contain alt data so will need to cross reference to get this
        // altValText.setText(D);
        currentLat = Double.toString(marker.getPosition().latitude);
        currentLong = Double.toString(marker.getPosition().longitude);

        // ImageView markerImage = (ImageView)findViewById(R.id.markerImage);

        getPhoto(marker);

        // markerImage.setImageURI(imageUri);
        return true;
    }

    private void setDefaults(){

        final String TITLE = "(Mark ID)";
        final String ALTITUDE = "(Altitude)";
        final String LATITUDE = "(Latitude)";
        final String LONGITUDE = "(Longitude)";

        ImageView markerImageView = (ImageView) findViewById(R.id.markerImage);



        TextView idValText = (TextView)findViewById(R.id.idValText);

        // TextView altValText = (TextView)findViewById(R.id.altValText);
        TextView latValText = (TextView)findViewById(R.id.latValText);
        TextView longValText = (TextView)findViewById(R.id.longValText);

        idValText.setText(TITLE);
        markerImageView.setImageResource(R.drawable.marker_graphic);
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

    }

    /**
     * Get an zoomed in image of the marker
     * @return Uri for the image
     */
    private void getPhoto(Marker marker){

        verifyStoragePermissions();

        final String title = marker.getTitle();

        imagePath ="/mnt/sdcard/map_"+ title + ".png";
        File image = new File(imagePath);

        if(!image.exists()) {
            // zoom in to get a good photo of the marker
            originalZoomAmount = googleMap.getCameraPosition().zoom;
            googleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                @Override
                public void onMapLoaded() {
                    // we want to only take a photo once the camera has moved to the correct position
                    googleMap.snapshot(new GoogleMap.SnapshotReadyCallback() {
                        public void onSnapshotReady(Bitmap bitmap) {
                            // Write image to storage
                            FileOutputStream out = null;
                            try {
                                out = new FileOutputStream(imagePath);
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);

                            addImageToDB();

                            // set the mini image of the marker on the display
                            currentImage = imagePath;
                            updateUI();

                            googleMap.animateCamera(CameraUpdateFactory.zoomTo(originalZoomAmount));
                        }
                    });
                }
            });

            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 15));
        }else{
            currentImage = imagePath;
            updateUI();
        }
    }

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
            GoogleMapPos currentLocation = (GoogleMapPos)intent.getExtras().getSerializable("Location");
            addMarker(currentLocation);

            // move the camera to keep track of the latest marker that has been placed
            try {
                googleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(currentLocation.latitude, currentLocation.longitude)));
            }
            catch (Exception e){
               e.printStackTrace();
            }

            Log.d("something happened", "something seems to have happened");
        }
    }


    /**
     * We want to place a new marker on the map which we have either received from the broadcast reciever or loaded from the database
     * initially.
     * @param loc a GoogleMapPosition object which contains location data
     */
    private void addMarker(GoogleMapPos loc){

        LatLng location = new LatLng(loc.latitude, loc.longitude);
        // add a new marker to the list of map markers and display
        MarkerOptions newMarker = new MarkerOptions().position(location).title(Integer.toString(loc._id));
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
        googleMap.setOnMarkerClickListener(this);

        this.googleMap = googleMap;
        this.markers = new ArrayList<>();

        ArrayList<GoogleMapPos> locations = loadFromDB();

        if(locations.size() > 0) {
            // add all the markers in the database from the start
            for (GoogleMapPos loc : locations) {
                addMarker(loc);
            }

            // zoom to a reasonable position
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(locations.get(0).latitude, locations.get(0).longitude), 5));
        }
    }
        // }
        //LatLng lastLatLang = null// updatePoints = new UpdatePoints();

    /**
     * when we get an image record for a marker we want to add it to the back end database managed by the content provider
     */
    private void addImageToDB(){

        String whereClause = LocationsContentProviderContract._ID + "= ?";
        String[] whereArgs = new String[]{currentTitle};
                // new String[]{LocationContentProviderContract.IMAGE_PATH + "=" + currentImage};

        ContentValues contentValues = new ContentValues();
        contentValues.put(LocationsContentProviderContract.IMAGE_PATH, "\"" + currentImage + "\"");

        getContentResolver().update(LocationsContentProviderContract.GENERAL_QUERY_URI, contentValues ,whereClause, whereArgs);
    }

    /**
     * Load points that have so far been logged within the DB into the locations list for displaying
     */
    private ArrayList<GoogleMapPos>  loadFromDB() {

        ArrayList<GoogleMapPos> locations = new ArrayList<>();

        final String[] projection = {LocationsContentProviderContract._ID, LocationsContentProviderContract.ALTITUDE, LocationsContentProviderContract.LATITUDE, LocationsContentProviderContract.LONGITUDE};

        // Uri currentJourneyUri = Uri.parse("content://\"+AUTHORITY+\"/Journeys");

        Cursor cursor = getContentResolver().query(LocationsContentProviderContract.GENERAL_QUERY_URI,
                projection,
                null, null, null, null);

        if (cursor != null) {
            // get all the recipes in the database stored in the form of a hash map so can send back to main activity
            if (cursor.moveToFirst()) {
                do {
                    int _id = cursor.getInt(cursor.getColumnIndex(LocationsContentProviderContract._ID));
                    double alt = cursor.getDouble(cursor.getColumnIndex(LocationsContentProviderContract.ALTITUDE));
                    double latitude = cursor.getDouble(cursor.getColumnIndex(LocationsContentProviderContract.LATITUDE));
                    double longitude = cursor.getDouble(cursor.getColumnIndex(LocationsContentProviderContract.LONGITUDE));

                    locations.add(new GoogleMapPos(_id, alt, latitude, longitude, -1));

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
