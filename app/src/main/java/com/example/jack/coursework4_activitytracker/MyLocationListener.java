package com.example.jack.coursework4_activitytracker;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

import java.util.Calendar;
import java.util.ArrayList;

/**
 * Created by Jack on 16/11/2016.
 */

public class MyLocationListener implements LocationListener {

    int _id = 0;
    Context serviceContext;
    double totalDistance = 0;

    ArrayList<GoogleMapPos> mapPosistions = new ArrayList<>();
    ArrayList<Location> gpsLocations = new ArrayList<>();

    // Calendar calendar;

    public MyLocationListener(Context serviceContext){

        this.serviceContext = serviceContext;
    }

    /**
     * Send a broadcast for the actovities and main Service class to pick up containing all the useful data
     * about the users current speed/location
     * @param location
     */
    @Override
    public void onLocationChanged(Location location)
    {

        gpsLocations.add(location);

        Intent locationIntent = new Intent("locationData");

        double alt = location.getAltitude();
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        Calendar calendar = Calendar.getInstance();
        long now = calendar.getTimeInMillis();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        long passed = now - calendar.getTimeInMillis();
        long secondsSinceMidnight = passed / 1000;

        double speed = 0;
        if(mapPosistions.size() >= 2){
            speed = calcSpeed();
        }

        // the reason I store so many values in this object is to avoid computation needing to
        // take place on the activities end
        GoogleMapPos currentLocation = new GoogleMapPos(_id, alt, latitude, longitude, speed, secondsSinceMidnight);

        mapPosistions.add(currentLocation);

        // broadcast to other components
        locationIntent.putExtra("Location", currentLocation);

        locationIntent.setAction(LocationManagementService.RECEIVE_LOCATION);
        serviceContext.sendBroadcast(locationIntent);
        _id++;
    }

    /**
     * Change the value for _id so that new records are inserted into the correct place in the table
     * Because this has changed need to also 
     */
    public void updateJourneySettings(int new_id){
        this._id = new_id;
    }

    /**
     * This method calculates the speed of the user as they walk
     * speed = distance/time
     * @return speed (meters per second)
     */
    private double calcSpeed(){

        // to get current speed need to take last two markers in mapPositions calculate distance between those and
        // look at the time interval between the two

        int lastIdx = gpsLocations.size() - 1;
        int penultimateIdx = gpsLocations.size() - 2;

        Location gpsCurrentLocation = gpsLocations.get(lastIdx);
        Location gpsPreviousLocation = gpsLocations.get(penultimateIdx);

        float distanceInMeters = gpsCurrentLocation.distanceTo(gpsPreviousLocation);
        totalDistance += distanceInMeters;
        // float distance = currentLocation.distanceTo(previousLocation);
        // double distanceInMeters = calculateDistanceInMeters(currentLocation, previousLocation);

        lastIdx = mapPosistions.size() - 1;
        penultimateIdx = mapPosistions.size() - 2;

        GoogleMapPos mapCurrentLocation = mapPosistions.get(lastIdx);
        GoogleMapPos mapPreviousLocation = mapPosistions.get(penultimateIdx);

        long secondTime = mapCurrentLocation.timeSeconds;
        long firstTime = mapPreviousLocation.timeSeconds;

        long secondsDelta = secondTime - firstTime;

        double speed = distanceInMeters / secondsDelta;

        return speed;
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
