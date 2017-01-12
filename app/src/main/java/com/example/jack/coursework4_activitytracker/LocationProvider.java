package com.example.jack.coursework4_activitytracker;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

/**
 * Created by Jack on 22/12/2016.
 */

public class LocationProvider extends ContentProvider {

    private SQLManager dbHelper = null;
    private static final UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        uriMatcher.addURI(LocationsContentProviderContract.AUTHORITY, "General", 1);
        uriMatcher.addURI(LocationsContentProviderContract.AUTHORITY, "GetHigestPrimary", 2);

        uriMatcher.addURI(LocationsContentProviderContract.AUTHORITY, "ChangeTable", 3);

        // for when we need to select between add and rename activity must be explicit which one it wants
        uriMatcher.addURI(LocationsContentProviderContract.AUTHORITY, "Journeys/Query", 4);
        uriMatcher.addURI(LocationsContentProviderContract.AUTHORITY, "Journeys/Add", 5);
        uriMatcher.addURI(LocationsContentProviderContract.AUTHORITY, "Journeys/Rename", 6);

        // uriMatcher.addURI(LocationContentProviderContract., "Journeys", 2);
    }

    final static int ACT_ON_CURRENTLY_SELECTED = 1;
    final static int GET_HIGHEST_PRIMARY_KEY_SELECTED = 2;

    final static int CHANGE_CURRENTLY_SELECTED_TABLE = 3;
    final static int QUERY_JOURNEYS = 4;
    final static int ADD_NEW_JOURNEY = 5;
    final static int RENAME_JOURNEY = 6;


    // the name given to tables when they are first created
    final String DEFAULT_TABLE_NAME = "Original";

    // this variable keeps track of what table we are currently working with
    String journeyTableName = DEFAULT_TABLE_NAME;

    /**
     * re name the default journey table that has been made
     */
    public void renameCurrentJourney(Uri uri, ContentValues values){


        String journeyName =  (String)values.get("JourneyName");

        SQLiteDatabase dataBase = dbHelper.getWritableDatabase();

        // ALTER TABLE orig_table_name RENAME TO tmp_table_name;
        dataBase.execSQL("ALTER TABLE " + "\"" + journeyTableName + "\"" + " RENAME TO " + "\"" + journeyName + "\"");

        // now the name of the able has been altered it needs to be added to the lookup table
        ContentValues contentValues = new ContentValues();
        contentValues.put(LocationsContentProviderContract.JOURNEY_NAMES_FIELD, journeyName);

        long id = dataBase.insert(LocationsContentProviderContract.JOURNEY_NAMES_TABLE, null, contentValues);
        Uri pathWithInsertRef = ContentUris.withAppendedId(uri, id);
        getContext().getContentResolver().notifyChange(pathWithInsertRef, null);

        journeyTableName = journeyName;

        dataBase.close();
    }

    /**
     * Check to see if a table exists before we try to update a record in it or perform an insert
     * @param table
     * @return
     */
    private boolean tableExists(String table){

        SQLiteDatabase dataBase = dbHelper.getWritableDatabase();
        Cursor cursor = dataBase.rawQuery("SELECT COUNT(*) FROM sqlite_master WHERE type = ? AND name = ?", new String[] {"table", table});

        if (!cursor.moveToFirst()) {
            cursor.close();
            return false;
        }
        int count = cursor.getInt(0);
        cursor.close();
        dataBase.close();

        return count > 0;
    }
    /**
     * Add a database to the contnet provider representing a new journey.
     * also need to insert this database into the main database for looking up the different journeys
     */
    public void makeNewJourney(Uri uri, ContentValues values){


        // check if default table already exists
        //try {
            if (!tableExists(DEFAULT_TABLE_NAME)) {

                SQLiteDatabase dataBase = dbHelper.getWritableDatabase();
                // there was no error so table already exists
                // make the new table for our journey
                dataBase.execSQL(
                        "CREATE TABLE " + DEFAULT_TABLE_NAME + "(" +
                                "_id INTEGER PRIMARY KEY, " +
                                "Altitude FLOAT, " +
                                "Longitude FLOAT, " +
                                "Latitude FLOAT, " +
                                "ImagePath TEXT" +
                                ");");

                ContentValues contentValues = new ContentValues();
                contentValues.put(LocationsContentProviderContract.JOURNEY_NAMES_FIELD, "Original");

                long id = dataBase.insert(LocationsContentProviderContract.JOURNEY_NAMES_TABLE, null, contentValues);
                dataBase.close();

                Uri pathWithInsertRef = ContentUris.withAppendedId(uri, id);
                getContext().getContentResolver().notifyChange(pathWithInsertRef, null);

                // Uri pathWithInsertRef = ContentUris.withAppendedId(uri, id);
                // getContext().getContentResolver().notifyChange(pathWithInsertRef, null);
                // return pathWithInsertRef;
                // }
            }
        //}
        // were unable to create the table
//        catch(SQLException createTableException){
//            throw createTableException;
//        }
    }

    @Override
    public boolean onCreate() {
        this.dbHelper = new SQLManager(this.getContext(), "LocationDB15", null, 6);
        return true;
    }

    @Override
    public String getType(Uri uri) {

        String contentType;
        if (uri.getLastPathSegment()==null)
        {
            contentType = LocationsContentProviderContract.CONTENT_TYPE_MULTIPLE;
        }
        else
        {
            contentType = LocationsContentProviderContract.CONTENT_TYPE_SINGLE;
        }
        return contentType;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values){

        try {
            // if this specific table we want to insert a record into exists then perform the insert
            if(tableExists(journeyTableName)) {

                SQLiteDatabase dataBase = dbHelper.getWritableDatabase();

                long id = dataBase.insert(journeyTableName, null, values);
                dataBase.close();

                Uri pathWithInsertRef = ContentUris.withAppendedId(uri, id);
                getContext().getContentResolver().notifyChange(pathWithInsertRef, null);
                return pathWithInsertRef;
            }
            else{
                throw new SQLException("Cannot find table we are trying to insert into");
            }
        }
        catch (SQLException e){
            throw e;
        }
    }

    /**
     * Want the ability to update primarily to add images of the locations
     * @param uri
     * @param whereClause
     * @param whereArgs the values we are using as arguments for the update where clause
     * @return
     */
    @Override
    public int update(Uri uri, ContentValues contentValues, String whereClause, String[] whereArgs) {
        // throw new UnsupportedOperationException("not implemented");
        SQLiteDatabase dataBase = dbHelper.getWritableDatabase();

        // establish the table name by looking at the end of the URI, if we have

        switch(uriMatcher.match(uri))
        {
            case ACT_ON_CURRENTLY_SELECTED:
                // here we want to update the current journey the user has selected
                long id = dataBase.update(journeyTableName, contentValues, whereClause, whereArgs);
                Uri pathWithDelRef = ContentUris.withAppendedId(uri, id);
                getContext().getContentResolver().notifyChange(pathWithDelRef, null);
                break;

            case ADD_NEW_JOURNEY:
                makeNewJourney(uri, contentValues);
                break;

            case RENAME_JOURNEY:
                if( contentValues!= null){
                    if(contentValues.containsKey(LocationsContentProviderContract.JOURNEY_NAMES_FIELD)) {
                        renameCurrentJourney(uri, contentValues);
                    }
                }
                break;
            case CHANGE_CURRENTLY_SELECTED_TABLE:
                journeyTableName = (String)contentValues.get(LocationsContentProviderContract.JOURNEY_NAMES_FIELD);
                break;

            default:
                throw new SQLException("Cannot use this URL with update method");

                // here we want to query the main journeys table to get all the journeys
                // update main name
                // want to add a new default table
        }
        dataBase.close();

        // dataBase.execSQL("UPDATE Locations SET ImagePath=\"/mnt/sdcard/map_0.png\" WHERE _id=0");
        return 0;
    }

    @Override
    public Cursor query(Uri uri, String[] tableColumns, String whereCaluse, String[] whereArgs, String sortOrder) {

        SQLiteDatabase dataBase = dbHelper.getWritableDatabase();

        Cursor returnCursor = null;
        switch(uriMatcher.match(uri))
        {
            case ACT_ON_CURRENTLY_SELECTED:

                // here we want to query based on the journey the user has selected
                returnCursor = dataBase.query(journeyTableName, tableColumns, whereCaluse, whereArgs, null, null, sortOrder);
            break;
            case QUERY_JOURNEYS:
                // here we want to query the main journeys table to get all the journeys
                returnCursor = dataBase.query(LocationsContentProviderContract.JOURNEY_NAMES_TABLE,
                        tableColumns, whereCaluse, whereArgs, null, null, sortOrder);

            break;


            case GET_HIGHEST_PRIMARY_KEY_SELECTED:
                String[] projection = {LocationsContentProviderContract._ID};
                String bespokeWhereCaluse = "SELECT MAX(_id) FROM " + journeyTableName;

                returnCursor = dataBase.rawQuery(bespokeWhereCaluse, null);
//                returnCursor = dataBase.query(journeyTableName,
//                        tableColumns, bespokeWhereCaluse, null, null, null, sortOrder);

                break;
            default:
                throw new SQLException("Cannot use this URL with query method");
        }
        return returnCursor;
    }

    @Override
    public int delete(Uri uri, String where, String[] whereClause) {

        SQLiteDatabase dataBase = dbHelper.getWritableDatabase();

        switch(uriMatcher.match(uri))
        {
            case ACT_ON_CURRENTLY_SELECTED:
                long id = dataBase.delete(journeyTableName, where, whereClause);
                dataBase.close();

                Uri pathWithDelRef = ContentUris.withAppendedId(uri, id);
                getContext().getContentResolver().notifyChange(pathWithDelRef, null);

                break;

            default:
                throw new SQLException("Cannot use this URL with delete method");
        }
        return 0;
    }
}
