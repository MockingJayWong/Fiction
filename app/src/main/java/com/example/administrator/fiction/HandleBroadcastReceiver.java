package com.example.administrator.fiction;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

/**
 * Created by Administrator on 2016/4/15.
 */
public class HandleBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("Test", "BroadCast: start");
        switch (intent.getAction()) {
            case  Protocols.MY_ACTION_NOTIFICATION:
                Notification notification = new NotificationCompat.Builder(context).setSmallIcon(R.drawable.common_ic_googleplayservices)
                        .setContentTitle("WHAT IS NEW").setContentText(intent.getExtras().getString("newlist").toString()).build();
                NotificationManager notificationManager = (NotificationManager)context.getSystemService(context.NOTIFICATION_SERVICE);
                notificationManager.notify(Protocols.MY_NOTIFICATION_ID,notification);
                break;
            case Protocols.MY_ACTION_SEARCH:
                break;
        }
    }
}
