package com.example.jack.coursework4_activitytracker;

import android.net.Uri;

/**
 * Created by Jack on 22/12/2016.
 */

public class LocationsContentProviderContract {

    public static final String DEFAULT_JOURNEY_TABLE = "Original";

    // Fields for rows in a specific journey table
    public static final String _ID = "_id";
    public static final String ALTITUDE = "Altitude";
    public static final String IMAGE_PATH = "ImagePath";
    public static final String LONGITUDE = "Longitude";
    public static final String LATITUDE = "Latitude";

    public static final String AUTHORITY = "com.example.jack.coursework4_activitytracker.TripsProvider";

    // the activity doesn't know the current table we are working with so find this our from the content
    // provider object
    // public static final String GET_CURRENT_TABLE = Uri.parse("content://"+AUTHORITY+"/");

    // public static final Uri LOCATION_URI = Uri.parse("content://"+AUTHORITY+"/Locations");
    public static final String CONTENT_TYPE_SINGLE = "vnd.android.cursor.item/LocationProvider.data.text";
    public static final String CONTENT_TYPE_MULTIPLE = "vnd.android.cursor.dir/LocationProvider.data.text";

    // The main lookup table listing all the journeys which have been saved
    public static final String JOURNEY_NAMES_TABLE = "JourneyNames";
    public static final String JOURNEY_NAMES_FIELD =  "JourneyName";

    // we don't know exactly what table we need to load from so do a general query which can be resolved within the content
    // provider
    public static final Uri GENERAL_QUERY_URI = Uri.parse("content://"+AUTHORITY+"/General");

    // Get the highest primary key from the currently selected table
    public static final Uri GET_HIGHEST_PRIMARY = Uri.parse("content://"+AUTHORITY+"/GetHigestPrimary");

    public static final Uri CHANGE_CURRENTLY_SELECTED_TABLE_URI = Uri.parse("content://"+AUTHORITY+"/ChangeTable");


    public static final Uri JOURNEY_NAMES_QUERY_URI = Uri.parse("content://"+AUTHORITY+"/Journeys/Query");
    public static final Uri JOURNEY_NAMES_ADD_URI = Uri.parse("content://"+AUTHORITY+"/Journeys/Add");
    public static final Uri JOURNEY_NAMES_RENAME_URI = Uri.parse("content://"+AUTHORITY+"/Journeys/Rename");

    public static final Uri GET_CURRENTLY_SELECTED_TABLE = Uri.parse("content://"+AUTHORITY+"/GetCurrentlySelectedTable");

    // public static final String AUTHORITY = "com.example.jack.coursework4_activitytracker.JourneyProvider";
    // public static final String AUTHORITY = "com.example.jack.coursework4_activitytracker.TripsProvider";


}
