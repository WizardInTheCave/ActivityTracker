package com.example.jack.coursework4_activitytracker;

import android.net.Uri;

/**
 * Created by Jack on 22/12/2016.
 */

public class LocationContentProviderContract {

    public static final String _ID = "_id";
    public static final String ALTITUDE = "Altitude";
    public static final String IMAGE_PATH = "ImagePath";
    public static final String LONGITUDE = "Longitude";
    public static final String LATITUDE = "Latitude";


    public static final String AUTHORITY = "com.example.jack.coursework4_activitytracker.LocationProvider";
    public static final Uri LOCATION_URI = Uri.parse("content://"+AUTHORITY+"/Recipes");

    public static final String CONTENT_TYPE_SINGLE = "vnd.android.cursor.item/RecipeProvider.data.text";
    public static final String CONTENT_TYPE_MULTIPLE = "vnd.android.cursor.dir/RecipeProvider.data.text";

}
