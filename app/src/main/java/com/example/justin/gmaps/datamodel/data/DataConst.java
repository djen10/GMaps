package com.example.justin.gmaps.datamodel.data;

import android.net.Uri;

/**
 * Created by Justin on 2017-09-08.
 */

public interface DataConst {

    String ORDER = "_ORDER";
    String LATI = "Lati";
    String LONGI = "Longi";
    String DATE = "Date";
    String ADDRESS = "Address";
    String TABLENAME = "GPS_INFO";
    Uri CONTENTURL =  Uri.parse("content://com.example.justin.gmaps.db.GPSDBProvider/GPS_INFO");

}
