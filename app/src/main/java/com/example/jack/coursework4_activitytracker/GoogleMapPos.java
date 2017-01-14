package com.example.jack.coursework4_activitytracker;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Jack on 24/12/2016.
 */

public class GoogleMapPos implements Serializable{

    int _id;
    double alt;
    double latitude;
    double longitude;
    double speed;
    long timeSeconds;

    /**
     * package data about a location in one class to make the broadcast a bit cleaner
     * @param _id the id of the position within the journey
     * @param alt the altitude of the position in the journey
     * @param latitude the latitude of the position in the journey
     * @param longitude the longitude of the position in the journey
     * @param speed the current speed the user is travelling at.
     * @param timeSeconds the time in seconds into the journey in which the
     */
    public GoogleMapPos(int _id, double alt, double latitude, double longitude, double speed, long timeSeconds){
        this._id = _id;
        this.alt = alt;
        this.latitude = latitude;
        this.longitude = longitude;
        this.speed = speed;
        this.timeSeconds = timeSeconds;
    }
}
