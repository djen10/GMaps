package com.example.justin.gmaps.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.justin.gmaps.datamodel.data.DataConst;

/**
 * Created by Justin on 2017-08-24.
 */

public class GPSDB extends SQLiteOpenHelper implements DataConst{

    public GPSDB(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLENAME + " (" +
                ORDER + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,  "
                + LATI +  " DOUBLE, "
                + LONGI + " DOUBLE, "
                + DATE + " TEXT, "
                + ADDRESS + " TEXT );";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String sql = "DROP TABLE IF EXISTS " + TABLENAME;
        db.execSQL(sql);
        onCreate(db);
    }

}
