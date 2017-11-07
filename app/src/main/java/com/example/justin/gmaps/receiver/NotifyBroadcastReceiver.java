package com.example.justin.gmaps.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Justin on 2017-08-25.
 */

public abstract class NotifyBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        update();
    }

    public abstract void update();
}
