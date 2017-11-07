package com.example.justin.gmaps.db;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.example.justin.gmaps.datamodel.data.DataConst;
import com.example.justin.gmaps.location.LocationHandler;


/**
 * Created by Justin on 2017-09-05.
 */

public class DBUtils implements DataConst{
    private final static Uri CONTENTURL = Uri.parse("content://com.example.justin.gmaps.db.GPSDBProvider/GPS_INFO");
    //private final static Uri CONTENT_URI_LIMIT = Uri.parse("content://com.example.justin.gmaps.db.GPSDBProvider/GPS_INFO/1");


    //ContentResolver 를 이용하여 Provider에 접근. insert 를 한다.
    public static void insertValues(Context context, ContentResolver contentResolver, double lati, double longi) {
        ContentValues values = new ContentValues();
        long now = System.currentTimeMillis();
        String address = LocationHandler.getAddress(context, lati, longi);
        values.put(LATI, lati);
        values.put(LONGI, longi);
        values.put(DATE, now + "");
        values.put(ADDRESS, address);
        contentResolver.insert(CONTENTURL, values);
    }

    public static Cursor query(Context context){
        SQLiteDatabase mGPSdb;
        GPSDB mGPSDB_Helper = new GPSDB(context, TABLENAME, null, 1);
        mGPSdb = mGPSDB_Helper.getReadableDatabase();
        //ContentResolver contentResolver = context.getContentResolver();
        //Cursor c = contentResolver.query(CONTENT_URI, null, null, null, null);
        Cursor c = mGPSdb.query(TABLENAME, null, null, null, null, null, null, null);
        return c;
    }
    public static Cursor lastQuery(Context context){
        SQLiteDatabase mGPSdb;
        GPSDB mGPSDB_Helper = new GPSDB(context,TABLENAME, null, 1);;
        mGPSdb = mGPSDB_Helper.getReadableDatabase();
        //ContentResolver contentResolver = context.getContentResolver();
       // Cursor c = contentResolver.query(CONTENT_URI2,null,null,null,null);
        Cursor c = mGPSdb.query(TABLENAME, null, null, null, null, null, "_Order DESC", "1");
        return c;
    }
}
