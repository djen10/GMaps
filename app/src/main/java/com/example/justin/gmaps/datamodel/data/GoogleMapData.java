package com.example.justin.gmaps.datamodel.data;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

/**
 * Created by Justin on 2017-09-06.
 */

public class GoogleMapData {
    public PolylineOptions polylineOptions; // 경로에 따라 라인을 그림
    public MarkerOptions markerOptions;
    public ArrayList<LatLng> arrayPoints; // 좌표를 저장
    public OnMapReadyCallback callback;
    public GoogleMap googleMap;
    public MapFragment mapFragment;
    public GoogleMapData(){

    }
}
