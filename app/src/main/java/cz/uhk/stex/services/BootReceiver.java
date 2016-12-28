package cz.uhk.stex.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Runs the notification service after the device is rebooted
 * Created by Karelp on 12.07.2016.
 */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("NotificationService", "Starting notification service");
        context.startService(new Intent(context, NotificationService.class));
    }
}
