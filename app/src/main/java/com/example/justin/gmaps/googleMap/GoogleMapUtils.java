package com.example.justin.gmaps.googleMap;

import android.content.Context;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Justin on 2017-09-05.
 */

public class GoogleMapUtils {
    //MarkerOptions 의 속성을 설정
    public static MarkerOptions setMarketOptions(MarkerOptions markerOptions, LatLng latLng, String title, String address, String date, String stayTime) {
        markerOptions.position(latLng);
        markerOptions.title(title);
        markerOptions.snippet(address + "\n" + date + "\n" + stayTime);
        return markerOptions;
    }

    //PolylineOptions 의 속성을 설정
    public static PolylineOptions setPolylineOptions(PolylineOptions polylineOptions, ArrayList<LatLng> latLngs) {
        polylineOptions.color(Color.RED);
        polylineOptions.width(5);
        polylineOptions.addAll(latLngs);
        return polylineOptions;
    }

    //GoogleMap 의 처음 zoom 정도를 설정
    public static GoogleMap setFirstZoom(GoogleMap googleMap) {
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(18);
        googleMap.animateCamera(zoom);
        return googleMap;
    }

    //GoogleMap 에 marker 와 polyline 을 붙이고 지점으로 위치 이동
    public static GoogleMap setGoogleMap(GoogleMap googleMap, MarkerOptions markerOptions, PolylineOptions polylineOptions) {
        googleMap.addMarker(markerOptions);
        googleMap.addPolyline(polylineOptions);
        return googleMap;
    }

    public static void moveCamera(GoogleMap googleMap, LatLng latLng) {
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    public static String getAddress(Context context, double lati, double longi) {
        String realAddress = " NULL ";
        Geocoder geocoder = new Geocoder(context, Locale.KOREA);
        List<Address> address;
        try {
            if (geocoder != null) {
                address = geocoder.getFromLocation(lati, longi, 1);
                if (address != null && address.size() > 0) {
                    String currentAddress = address.get(0).getAddressLine(0).toString();
                    realAddress = currentAddress;

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return realAddress;
    }
}
