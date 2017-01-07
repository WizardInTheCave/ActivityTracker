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
        uriMatcher.addURI(LocationContentProviderContract.AUTHORITY, "Recipes", 1);
    }


    @Override
    public boolean onCreate() {
        this.dbHelper = new SQLManager(this.getContext(), "LocationDB11", null, 6);
        return true;
    }

    @Override
    public String getType(Uri uri) {

        String contentType;
        if (uri.getLastPathSegment()==null)
        {
            contentType = LocationContentProviderContract.CONTENT_TYPE_MULTIPLE;
        }
        else
        {
            contentType = LocationContentProviderContract.CONTENT_TYPE_SINGLE;
        }
        return contentType;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values){

        try {
            SQLiteDatabase dataBase = dbHelper.getWritableDatabase();

            String tableName = "Locations";
            long id = dataBase.insert(tableName, null, values);
            dataBase.close();

            Uri pathWithInsertRef = ContentUris.withAppendedId(uri, id);
            getContext().getContentResolver().notifyChange(pathWithInsertRef, null);
            return pathWithInsertRef;
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

        // dataBase.execSQL("UPDATE Locations SET ImagePath=\"/mnt/sdcard/map_0.png\" WHERE _id=0");

        long id = dataBase.update("Locations", contentValues, whereClause, whereArgs);

        //dataBase.up
        // long id =dataBase.updateWithOnConflict("Locations", contentValues, whereClause, whereArgs, SQLiteDatabase.CONFLICT_REPLACE);
        // long id = dataBase.updateWithOnConflict("Locations", contentValues, whereClause, null, SQLiteDatabase.CONFLICT_REPLACE);
        // long id = 0;

        dataBase.close();

        Uri pathWithDelRef = ContentUris.withAppendedId(uri, id);
        getContext().getContentResolver().notifyChange(pathWithDelRef, null);

        return 0;
    }

    @Override
    public Cursor query(Uri uri, String[] tableColumns, String whereCaluse, String[] whereArgs, String sortOrder) {

        SQLiteDatabase dataBase = dbHelper.getWritableDatabase();
        switch(uriMatcher.match(uri))
        {
            case 1:
                return dataBase.query("Locations", tableColumns, whereCaluse, whereArgs, null, null, sortOrder);
            default:
                return null;
        }
    }

    @Override
    public int delete(Uri uri, String where, String[] whereClause) {

        SQLiteDatabase dataBase = dbHelper.getWritableDatabase();

//        String[] splitUri = uri.toString().split("/");
//        int tableNameIdx = splitUri.length - 1;
//        String tableName = splitUri[tableNameIdx];

        // dataBase.upda
        // dataBase.de

        long id = dataBase.delete("Locations", where, whereClause);
        dataBase.close();

        Uri pathWithDelRef = ContentUris.withAppendedId(uri, id);
        getContext().getContentResolver().notifyChange(pathWithDelRef, null);

        return 0;
    }
}
