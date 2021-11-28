package com.example.mytable.service.time;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import com.example.mytable.R;

public class TimerNotification extends Notification {
    private static final String CHANNEL_ID = "TIMER_CHANNEL";

    public void NotificationUpdate(Context context, Class<?> tClass, NotificationManager notificationManager){
        try {
            Intent notificationIntent = new Intent(context, tClass);
            final PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
            final Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setContentTitle("Time's up!")
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentIntent(pendingIntent)
                    .build();


            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, "My Counter Service", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.notify(1, notification);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
