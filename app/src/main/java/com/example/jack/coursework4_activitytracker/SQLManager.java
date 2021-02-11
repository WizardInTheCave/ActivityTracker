package com.example.jack.coursework4_activitytracker;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Simple class designed to initiate creation and renewal of the SQLite database if necessary
 */

public class SQLManager extends SQLiteOpenHelper {

    public SQLManager(Context context, String name, SQLiteDatabase.CursorFactory factory,
                      int version) {
        super(context, name, factory, version);
    }

    /**
     * Generate the table within the SQLite database to contain the main table with recipe titles and instructions
     * @param dataBase SQLiteDatabase object
     */
    @Override
    public void onCreate(SQLiteDatabase dataBase) {

        // this is a table which stores the names of all the walks so we can look one up at a later date
        // I essentially just want a list of strings that is stored permanently and can be used as a lookup
        // for the other tables
        dataBase.execSQL("CREATE TABLE " + LocationsContentProviderContract.JOURNEY_NAMES_TABLE + "(" +
                LocationsContentProviderContract.JOURNEY_NAMES_FIELD + " TEXT PRIMARY KEY" +
                ");");
    }

    /**
     * Upgrade the database
     * @param dataBase SQLiteDatabase object
     * @param oldVersion Old database version
     * @param newVersion New database version
     */
    @Override
    public void onUpgrade(SQLiteDatabase dataBase, int oldVersion, int newVersion) {
        dataBase.execSQL("DROP TABLE IF EXISTS " + LocationsContentProviderContract.JOURNEY_NAMES_TABLE);
        onCreate(dataBase);
    }

}
