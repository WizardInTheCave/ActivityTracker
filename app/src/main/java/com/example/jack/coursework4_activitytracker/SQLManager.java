package com.example.jack.coursework4_activitytracker;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Jack on 22/12/2016.
 */

public class SQLManager extends SQLiteOpenHelper {

    public static final String RECIPE_TABLE_NAME = "Locations";

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
        dataBase.execSQL("CREATE TABLE " + RECIPE_TABLE_NAME + "(" +
                "_id INTEGER PRIMARY KEY, " +
                "Altitude FLOAT, " +
                "Longitude FLOAT," +
                "Latitude FLOAT," +
                "ImagePath TEXT" +
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
        dataBase.execSQL("DROP TABLE IF EXISTS " + RECIPE_TABLE_NAME);
        onCreate(dataBase);
    }

}
