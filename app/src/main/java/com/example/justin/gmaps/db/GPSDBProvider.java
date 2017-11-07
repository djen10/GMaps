package com.example.justin.gmaps.db;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Intent;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by Justin on 2017-08-29.
 */

public class GPSDBProvider extends ContentProvider {
    final static String TABLE_NAME = "GPS_INFO";
    final static String AUTUORITY = "com.example.justin.gmaps.db.GPSDBProvider";

    SQLiteDatabase mGPSdb;
    GPSDB mGPSDB_Helper;

    Intent mIntent;

    // Main 에서 요구하는 2가지 경우의 쿼리문 구분하기 위한 리퀘스트 코드
    final static int SELECT = 1;
    final static int SELECT_LAST = 2;

    // UriMatcher 는 static 으로!
    static final UriMatcher mUriMatcher;

    // UriMatcher 가 동작해야하는 Uri 형식들을 UriMatcher 에 추가한다.
    static {
        mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        mUriMatcher.addURI(AUTUORITY, TABLE_NAME, SELECT);
        mUriMatcher.addURI(AUTUORITY, TABLE_NAME + "/*", SELECT_LAST);
    }

    @Override
    public boolean onCreate() {
        mGPSDB_Helper = new GPSDB(getContext(), "GPS_INFO", null, 1);
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        mGPSdb = mGPSDB_Helper.getReadableDatabase();
        Cursor c = null;
        int temp = mUriMatcher.match(uri);
        switch (temp) {
            case SELECT:
                // select * from GPS_INFO;
                c = mGPSdb.query("GPS_INFO", null, null, null, null, null, null, null);
                Log.e("Test", "MM");
                break;
            case SELECT_LAST:
                // select * from GPS_INFO ORDER BY _Order DESC LIMIT 1;
                c = mGPSdb.query("GPS_INFO", null, null, null, null, null, "_Order DESC", "1");
                Log.e("Test", "WW");
                break;

        }
        return c;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        mGPSdb = mGPSDB_Helper.getWritableDatabase();
        mGPSdb.insert("GPS_INFO", null, values);
        mIntent = new Intent("com.example.justin.gmaps.GPS");
        mIntent.putExtra("Update", true);
        getContext().sendBroadcast(mIntent);
        return null;
    }


    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }
}
