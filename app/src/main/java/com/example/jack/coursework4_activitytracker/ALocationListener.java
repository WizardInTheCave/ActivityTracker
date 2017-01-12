package com.example.jack.coursework4_activitytracker;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import java.util.Calendar;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;


/**
 * Created by Jack on 16/11/2016.
 */

public class ALocationListener implements LocationListener {

    double speed = 0;

    int _id = 0;
    Context serviceContext;

    ArrayList<GoogleMapPos> mapPosistions = new ArrayList<>();

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


        double alt = location.getAltitude();
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        Calendar c = Calendar.getInstance();
        long now = c.getTimeInMillis();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        long passed = now - c.getTimeInMillis();
        long secondsSinceMidnight = passed / 1000;

        // int seconds = c.get(Calendar.SECOND);

        GoogleMapPos currentLocation = new GoogleMapPos(_id, alt, latitude, longitude, secondsSinceMidnight);
        mapPosistions.add(currentLocation);

        double speed = 0;
        if(mapPosistions.size() >= 2){
             speed = calcSpeed();
        }

        // broadcast
        locationIntent.putExtra("Location", currentLocation);
        locationIntent.putExtra("Speed", speed);

        locationIntent.setAction(LocationManagementService.RECEIVE_LOCATION);
        serviceContext.sendBroadcast(locationIntent);
        _id++;
    }

    /**
     * Change the value for _id so that new records are inserted into the correct place
     */
    public void updateInsertKey(int new_id){
        this._id = new_id;
    }

    /**
     * This method calculates the speed of the user as they walk
     * speed = distance/time
     * @return
     */
    private double calcSpeed(){

        // to get current speed need to take last two markers in mapPositions calculate distance between those and
        // look at the time interval between the two

        int lastIdx = mapPosistions.size() - 1;
        int penultimateIdx = mapPosistions.size() - 2;

        GoogleMapPos currentLocation = mapPosistions.get(lastIdx);
        GoogleMapPos previousLocation = mapPosistions.get(penultimateIdx);

        double distanceInMeters = calculateDistanceInMeters(currentLocation, previousLocation);

        long firstTime = previousLocation.timeSeconds;
        long secondTime = currentLocation.timeSeconds;

        long secondsDelta = secondTime - firstTime;

        double speed = distanceInMeters / secondsDelta;

        return speed;
    }

    /**
     * using calculations from here to get distance in meters as the crow flies: http://www.movable-type.co.uk/scripts/latlong.html
     * @param currentLocation The latest GPS location recorded
     * @param previousLocation The previous GPS location recorded
     * @return
     */
    private double calculateDistanceInMeters(GoogleMapPos currentLocation, GoogleMapPos previousLocation){

        long R = 6371000;
        double phi1 = Math.toRadians(previousLocation.latitude);
        double phi2 = Math.toRadians(currentLocation.latitude);

        double deltaPhi = Math.toRadians(currentLocation.latitude - previousLocation.latitude);
        double deltaLambda =  Math.toRadians(currentLocation.longitude - previousLocation.longitude);

        double a = Math.sin(deltaPhi/2) * Math.sin(deltaPhi/2) +
                Math.cos(phi1) * Math.cos(phi2) *
                        Math.sin(deltaLambda/2) * Math.sin(deltaLambda/2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        double d = R * c;
        return d;
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
