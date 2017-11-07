package com.example.justin.gmaps.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.example.justin.gmaps.service.GPSService;

/**
 * Created by Justin on 2017-08-28.
 */

public class BootBroadCastReceiver extends BroadcastReceiver {
    private final String TAG = this.getClass().getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();

        Log.d(TAG, "onReceive : action _" + action);

        switch (action) {
            case Intent.ACTION_BOOT_COMPLETED:
            case Intent.ACTION_MY_PACKAGE_REPLACED:
                Intent serviceIntent=  new Intent(context, GPSService.class);
                context.startService(serviceIntent);
                break;
        }
    }
}
