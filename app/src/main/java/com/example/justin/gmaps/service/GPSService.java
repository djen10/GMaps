package com.example.justin.gmaps.service;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;


import com.example.justin.gmaps.datamodel.data.DataConst;
import com.example.justin.gmaps.db.DBUtils;
import com.example.justin.gmaps.receiver.NotifyBroadcastReceiver;
import com.example.justin.gmaps.ui.MainActivity;
import com.example.justin.gmaps.R;
import com.google.android.gms.maps.model.LatLng;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created blongitude Justin on 2017-08-24.
 */

public class GPSService extends Service implements DataConst{
    double mLatitude;
    double mLongitude;

    LocationManager mLocationManager;

    Context mContext = this;


    TimerTask mTask;
    Timer mTimer;

    Location mLocationA;
    Location mLocationB;

    ContentResolver mContentResolver;

    NotificationCompat.Builder mBuilder;
    NotificationManager mNotificationManager;

    Random random = new Random(System.currentTimeMillis());

    NotifyBroadcastReceiver mBroadCastReceiver;

    public class BinderService extends Binder {
        public GPSService getService() {
            return GPSService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        mLocationA = new Location("");
        mLocationB = new Location("");

        mContentResolver = getContentResolver();
    }

    public LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            } else {
                if (location != null) {
                    mLatitude = location.getLatitude();
                    mLongitude = location.getLongitude();
                    DBUtils.insertValues(mContext, mContentResolver, mLatitude, mLongitude);
                }
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    //Task가 종료되었을때 호출되는거 확인.
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.e("MY_START", "떠라");
        unregisterReceiver(mBroadCastReceiver);
        stopSelf();
        mNotificationManager.cancelAll();

        Intent intent = new Intent(this, GPSService.class);
        startService(intent);
    }


    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        //처음 gps 값을 얻어오기위해 업데이트를 함.
        Log.e("MY_START", "START");
        mTimer = new Timer();
        //현재 임의의데이터를 넣어서 테스트하는 코드이다. LocationManager 를 이용하면 GPS 기반 위치 추적 가능!
        /*if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // 권한 부여가 안되었을 때 작업
        }
        else{
            // 권한 부여가 되었을 때 작업
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, mLocationListener);
            Log.e("MY_START", "GPS!!");
        }*/
        mTask = new TimerTask() {
            LatLng latLng;
            double a = 37.378406;
            double b = 127.112869;
            int c = 0;

            @Override
            public void run() {
                latLng = new LatLng(a, b);
                c = random.nextInt(9);
                if (c == 0) {
                    a += 0.0001;
                } else if (c == 1) {
                    a -= 0.0001;
                } else if (c == 2) {
                    b += 0.0001;
                } else if (c == 3) {
                    b -= 0.0001;
                } else if (c == 4) {
                    a += 0.0001;
                    b += 0.0001;
                } else if (c == 5) {
                    a += 0.0001;
                    b -= 0.0001;
                } else if (c == 6) {
                    a -= 0.0001;
                    b += 0.0001;
                } else if (c == 7){
                    a -= 0.0001;
                    b -= 0.0001;
                }
                else{

                }
                Log.e("MY_START", "YE!!");
                DBUtils.insertValues(mContext, mContentResolver, a, b);
            }
        };
        mTimer.schedule(mTask, 0, 5000);


        mBroadCastReceiver = new NotifyBroadcastReceiver() {
            @Override
            public void update() {
                makeNoti();
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.example.justin.gmaps.UPDATE");
        registerReceiver(mBroadCastReceiver, intentFilter);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                makeNoti();
            }
        }, 100);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.e("MY_START", "DIE");
        super.onDestroy();
        mTimer.cancel();
        try{
            unregisterReceiver(mBroadCastReceiver);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //notification
    public void makeNoti() {
        Cursor c = DBUtils.lastQuery(mContext);
        String address = "정보 없음.";
        if(c.moveToLast()){
            address = c.getString(c.getColumnIndex(ADDRESS));
        }
        mBuilder = new NotificationCompat.Builder(mContext)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("GPS")
                .setContentText(address)
                .setOngoing(true);
        Intent intent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(intent);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pendingIntent);
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(0, mBuilder.build());
    }
}
