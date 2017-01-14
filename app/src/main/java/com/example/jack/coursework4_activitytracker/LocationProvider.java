package com.example.jack.coursework4_activitytracker;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

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

        uriMatcher.addURI(LocationsContentProviderContract.AUTHORITY, "GetCurrentlySelectedTable", 7);
    }

    final static int ACT_ON_CURRENTLY_SELECTED = 1;
    final static int GET_HIGHEST_PRIMARY_KEY_SELECTED = 2;

    final static int CHANGE_CURRENTLY_SELECTED_TABLE = 3;
    final static int QUERY_JOURNEYS = 4;
    final static int ADD_NEW_JOURNEY = 5;
    final static int RENAME_JOURNEY = 6;
    final static int GET_CURRENTLY_SELECTED = 7;


    // the name given to tables when they are first created
    final String DEFAULT_TABLE_NAME = "Original";

    // this variable keeps track of what table we are currently working with
    String journeyTableName = DEFAULT_TABLE_NAME;

    /**
     * re name the default journey table that has been made called "Original" to some other name that the user would like
     */
    public void renameCurrentJourney(Uri uri, ContentValues values){

        SQLiteDatabase dataBase = dbHelper.getWritableDatabase();

        try {
            String newName = (String) values.get("JourneyName");

            // rename the table we were currently working with to the new name the user has provided
            dataBase.execSQL("ALTER TABLE " + "\"" + journeyTableName + "\"" + " RENAME TO " + "\"" + newName + "\"");


            // now the name of the able has been altered it needs to be added to the lookup table
            ContentValues contentValues = new ContentValues();
            contentValues.put(LocationsContentProviderContract.JOURNEY_NAMES_FIELD, newName);

            // need to remove the old name from the lookup table.
            dataBase.delete(LocationsContentProviderContract.JOURNEY_NAMES_TABLE,
                    LocationsContentProviderContract.JOURNEY_NAMES_FIELD + " = " + "?", new String[]{journeyTableName});

            // insert the new table name into the lookup table.
            long id = dataBase.insert(LocationsContentProviderContract.JOURNEY_NAMES_TABLE, null, contentValues);
            Uri pathWithInsertRef = ContentUris.withAppendedId(uri, id);
            getContext().getContentResolver().notifyChange(pathWithInsertRef, null);
            dataBase.close();

            // Now need to make a new original table having renamed the previous one so the user can start a fresh journey.
            makeOriginalJourneyTable(uri, values);
            journeyTableName = DEFAULT_TABLE_NAME;
        }
        catch (SQLException e){
            Log.e("Journey name conflict", "cannot insert a journey with a name that already exists");
        }

        // need to make a new "original" table so operation can carry on as per usual
    }

    /**
     * Check to see if a table exists before we try to update a record in it or perform an insert
     * @param table
     * @return
     */
    private boolean tableExists(String table, SQLiteDatabase dataBase){

        Cursor cursor = dataBase.rawQuery("SELECT COUNT(*) FROM sqlite_master WHERE type = ? AND name = ?", new String[] {"table", table});
        if (!cursor.moveToFirst()) {
            cursor.close();
            return false;
        }
        int count = cursor.getInt(0);
        cursor.close();
        return count > 0;
    }

    /**
     * Called when the application starts, sets up the "Original" table to begin logging the first journey in and adds it to
     * lookup table so it can be found again.
     * @param uri
     * @param values
     */
    public void initialiseOriginalJourney(Uri uri, ContentValues values){


        makeOriginalJourneyTable(uri, values);

        SQLiteDatabase dataBase = dbHelper.getWritableDatabase();

        if (tableExists(DEFAULT_TABLE_NAME, dataBase)) {


            ContentValues contentValues = new ContentValues();
            contentValues.put(LocationsContentProviderContract.JOURNEY_NAMES_FIELD, DEFAULT_TABLE_NAME);

            // use conflict ignore so there isn't a problem if the record already exists in the lookup.
            long id = dataBase.insertWithOnConflict(LocationsContentProviderContract.JOURNEY_NAMES_TABLE,
                    null, contentValues, SQLiteDatabase.CONFLICT_IGNORE);

            Uri pathWithInsertRef = ContentUris.withAppendedId(uri, id);
            getContext().getContentResolver().notifyChange(pathWithInsertRef, null);
        }
        dataBase.close();

    }

    /**
     * Make a replacement for the "Original" table once the previous "Original"
     */
    private void makeOriginalJourneyTable(Uri uri, ContentValues values){

        SQLiteDatabase dataBase = dbHelper.getWritableDatabase();

        // check if default table already exists if it does because it wasn't deleted from a previous running of the
        // application then we want to delete it.
        if (tableExists(DEFAULT_TABLE_NAME, dataBase)) {
            dataBase.execSQL("DROP TABLE " + DEFAULT_TABLE_NAME);
        }
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

        dataBase.close();
    }

    /**
     * When the content provider is first created make a manager with a database
     * @return
     */
    @Override
    public boolean onCreate() {
        this.dbHelper = new SQLManager(this.getContext(), "testDB5", null, 6);
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

        SQLiteDatabase dataBase = dbHelper.getWritableDatabase();
        // if this specific table we want to insert a record into exists then perform the insert
        if(tableExists(journeyTableName, dataBase)) {
            try {
                long id = dataBase.insertOrThrow(journeyTableName, null, values);
                Uri pathWithInsertRef = ContentUris.withAppendedId(uri, id);
                getContext().getContentResolver().notifyChange(pathWithInsertRef, null);
                return pathWithInsertRef;
            }
            catch (Exception e){
                Log.d("Insert Key", "problem with retrieved insert key, will move to next one and eveything carries on fine");
            }
        }
        dataBase.close();
        return null;
    }


    /**
     * Want the ability to update primarily to add images of the locations
     * @param uri The URI to the table that we want to run the update on.
     * @param whereClause the where condition applied when running the SQL update statement.
     * @param whereArgs the values we are using as arguments for the update where clause.
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
                initialiseOriginalJourney(uri, contentValues);
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

        }
        dataBase.close();

        return 0;
    }

    /**
     * Want to run a query on a table in the database
     * @param uri The URI to the table that we want to run the update on.
     * @param tableColumns columns we want to retrieve from our query, in SQL terms what we are selecting
     * @param whereCaluse the where condition applied when running the SQL query statement.
     * @param whereArgs the values we are using as arguments for the update where clause.
     * @param sortOrder the other
     * @return database cursor object which can be iterated through to get all values in the table
     */
    @Override
    public Cursor query(Uri uri, String[] tableColumns, String whereCaluse, String[] whereArgs, String sortOrder) {

        SQLiteDatabase dataBase = dbHelper.getWritableDatabase();

        Cursor returnCursor = null;

        switch(uriMatcher.match(uri)) {
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
                String bespokeWhereCaluse = "SELECT MAX(_id) FROM " + journeyTableName;

                returnCursor = dataBase.rawQuery(bespokeWhereCaluse, null);
                break;
            default:
                throw new SQLException("Cannot use this URL with query method");

            case GET_CURRENTLY_SELECTED:

                // want to store the current table that is selected within a cursor so it can be returned as a query
                MatrixCursor matrixCursor = new MatrixCursor(new String[]{LocationsContentProviderContract.JOURNEY_NAMES_FIELD});
                matrixCursor.addRow(new Object[]{journeyTableName});

                returnCursor = new MergeCursor(new Cursor[]{matrixCursor, returnCursor});
                break;
        }
        return returnCursor;
    }

    public String getCurrentWorkingTable(){
        return journeyTableName;
    }

    /**
     * Perform a delete operation on a table within the database.
     * @param uri The URI to the table that we want to run the update on.
     * @param whereClase the where condition applied when running the SQL query statement.
     * @param whereArgs the values we are using as arguments for the update where clause.
     * @return return code
     */
    @Override
    public int delete(Uri uri, String whereClase, String[] whereArgs) {

        switch(uriMatcher.match(uri))
        {
            case ACT_ON_CURRENTLY_SELECTED:

                SQLiteDatabase dataBase = dbHelper.getWritableDatabase();
                long id = dataBase.delete(journeyTableName, whereClase, whereArgs);
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
