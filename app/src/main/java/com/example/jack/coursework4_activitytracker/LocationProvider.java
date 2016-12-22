package com.example.jack.coursework4_activitytracker;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
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

        this.dbHelper = new SQLManager(this.getContext(), "LocatioDB", null, 6);
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

        SQLiteDatabase dataBase = dbHelper.getWritableDatabase();

        String tableName = "Locations";
        long id = dataBase.insert(tableName, null, values);
        dataBase.close();

        Uri pathWithInsertRef = ContentUris.withAppendedId(uri, id);
        getContext().getContentResolver().notifyChange(pathWithInsertRef, null);
        return pathWithInsertRef;
    }

    @Override
    public Cursor query(Uri uri, String[] tableColumns, String whereCaluse, String[] whereArgs, String sortOrder) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        switch(uriMatcher.match(uri))
        {
            case 1:
                return db.query("Recipes", tableColumns, whereCaluse, whereArgs, null, null, sortOrder);
            default:
                return null;
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public int delete(Uri uri, String where, String[] selectionArgs) {

        SQLiteDatabase dataBase = dbHelper.getWritableDatabase();

        String[] splitUri = uri.toString().split("/");
        int tableNameIdx = splitUri.length - 1;
        String tableName = splitUri[tableNameIdx];

        long id = dataBase.delete(tableName, where, selectionArgs);
        dataBase.close();

        Uri pathWithDelRef = ContentUris.withAppendedId(uri, id);
        getContext().getContentResolver().notifyChange(pathWithDelRef, null);

        return 0;
    }
}
