package com.example.jack.coursework4_activitytracker;

import java.io.Serializable;

/**
 * Created by Jack on 24/12/2016.
 */

public class GoogleMapPos implements Serializable{

    int _id;
    double alt;
    double latitude;
    double longitude;


    public GoogleMapPos(int _id, double alt, double latitude, double longitude){
        this._id = _id;
        this.alt = alt;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
