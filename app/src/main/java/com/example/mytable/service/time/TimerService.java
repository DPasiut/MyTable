package com.example.mytable.service.time;

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

import com.example.mytable.MainActivity;
import com.example.mytable.R;

import java.util.Timer;
import java.util.TimerTask;

import antonkozyriatskyi.circularprogressindicator.CircularProgressIndicator;

public class TimerService extends Service {

    private static final String CHANNEL_ID = "NotificationChannelID";
    private static final String DEFAULT_PROGRESS_COLOR = "#3F51B5";
    private static final String PLAY_PROGRESS_COLOR = "#027602";
    private final IBinder binder = new LocalBinder();
    private Handler timerHandler;
    private Timer timer;
    private boolean isTimerOn = false;

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
        if (!isTimerOn){
            isTimerOn = true;
            final Integer[] timeRemaining = {timerValue};
            timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    timeRemaining[0]--;
                    sendTimerChangedMessage(timeRemaining[0]);
                    NotificationUpdate(timeRemaining[0]);
                    if (timeRemaining[0] <= 0){
                        timer.cancel();
                        isTimerOn = false;
                    }
                }
            }, 0,1000);
        }
    }

    public void stopTimer(){
        timer.cancel();
        isTimerOn = false;
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
//            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, "My Counter Service", NotificationManager.IMPORTANCE_LOW);
//            NotificationManager notificationManager = getSystemService(NotificationManager.class);
//            notificationManager.createNotificationChannel(notificationChannel);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Integer getTimeInSeconds(Integer hour, Integer minutes){
        if (hour == 0 && minutes == 0){
            return 0;
        }
        if (hour == 0 && minutes > 0){
            return minutes * 60;
        }
        return hour * 3600 + minutes * 60;
    }

    public Integer getTimeInMinutes(Integer hour, Integer minutes){
        if (hour == 0 && minutes == 0){
            return 0;
        }
        if (hour == 0 && minutes > 0){
            return minutes;
        }
        return hour * 60 + minutes;
    }

    public static String getDefaultProgressColor() {
        return DEFAULT_PROGRESS_COLOR;
    }

    public static String getPlayProgressColor() {
        return PLAY_PROGRESS_COLOR;
    }

    public boolean isTimerOn() {
        return isTimerOn;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
