package com.example.jack.coursework4_activitytracker;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;


/**
 * Created by Jack on 16/11/2016.
 */

public class ALocationListener implements LocationListener {

    double altitude;
    double latitude;
    double longitude;

    Context serviceContext;

    public ALocationListener(Context serviceContext){
        this.serviceContext = serviceContext;
    }

    /**
     * Send a broadcast to the LocationManagement Service containing all  the
     * @param location
     */
    @Override
    public void onLocationChanged(Location location)
    {
        Intent locationIntent = new Intent("locationData");

        locationIntent.putExtra("Latitude", location.getAltitude());
        locationIntent.putExtra("Longitude", location.getLatitude());
        locationIntent.putExtra("Provider", location.getLongitude());

        locationIntent.setAction(LocationManagementService.RECEIVE_LOCATION);
        serviceContext.sendBroadcast(locationIntent);
    }

    @Override
    public void onProviderDisabled(String provider)
    {

    }

    @Override
    public void onProviderEnabled(String provider)
    {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras)
    {

    }
}
