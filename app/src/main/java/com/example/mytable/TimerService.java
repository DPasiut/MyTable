package com.example.mytable;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.mytable.service.bluetooth.BluetoothService;

import java.sql.Time;
import java.util.Timer;
import java.util.TimerTask;

public class TimerService extends Service {

    private static final String CHANNEL_ID = "NotificationChannelID";
    private final IBinder binder = new LocalBinder();
    private Handler timerHandler;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class LocalBinder extends Binder {
        public TimerService getService() {
            return TimerService.this;
        }
    }

    public synchronized void setTimerHandler(Handler stateHandler){
        this.timerHandler = stateHandler;
    }

    private synchronized void sendTimerChangedMessage(Integer integer){
        if (timerHandler != null){
            Message msg = timerHandler.obtainMessage();
            Bundle bundle = new Bundle();
            bundle.putString("timer", integer.toString());
            msg.setData(bundle);
            msg.what = 1;
            timerHandler.sendMessage(msg);
        }

    }
    public void startTimer(Integer timerValue) {

        final Integer[] timeRemaining = {timerValue};
        final Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
//                Intent intent1local = new Intent();
//                intent1local.setAction("Counter");
                timeRemaining[0]--;
                sendTimerChangedMessage(timeRemaining[0]);
                NotificationUpdate(timeRemaining[0]);
                if (timeRemaining[0] <= 0){
                    timer.cancel();
                }
//                intent1local.putExtra("TimeRemaining", timeRemaining[0]);
//                sendBroadcast(intent1local);
            }
        }, 0,1000);
    }

    public void NotificationUpdate(Integer timeLeft){
        try {
            Intent notificationIntent = new Intent(this, MainActivity.class);
            final PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
            final Notification[] notification = {new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("My Stop Watch")
                    .setContentText("Time Remaing : " + timeLeft.toString())
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentIntent(pendingIntent)
                    .build()};
            startForeground(1, notification[0]);
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, "My Counter Service", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
