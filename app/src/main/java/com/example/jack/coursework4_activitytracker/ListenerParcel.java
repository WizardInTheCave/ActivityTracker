package com.example.jack.coursework4_activitytracker;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;


/**
 * Created by Jack on 22/12/2016.
 */

public class ListenerParcel implements Parcelable {

    int isLogging;
    Context context;

    public ListenerParcel()
    {
    }

    public ListenerParcel(Parcel in)
    {
        readFromParcel(in);
    }

    @Override
    public int describeContents()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * Write the variables that need to be communicated to the service to the message parcel
     * @param out
     * @param flags
     */
    @Override
    public void writeToParcel(Parcel out, int flags)
    {
    }

    /**
     * Read variables from the parcel
     * @param in The parcel that the data is being read from
     */
    private void readFromParcel(Parcel in)
    {

    }

    public static final Parcelable.Creator<ListenerParcel> CREATOR = new Parcelable.Creator<ListenerParcel>() {
        public ListenerParcel createFromParcel(Parcel in) {
            return new ListenerParcel(in);
        }
        public ListenerParcel[] newArray(int size) {
            return new ListenerParcel[size];
        }
    };
}


