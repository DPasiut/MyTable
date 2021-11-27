package com.example.mytable.service.time;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.mytable.R;
import com.example.mytable.ui.table.TableFragment;

import java.util.Timer;
import java.util.TimerTask;

import antonkozyriatskyi.circularprogressindicator.CircularProgressIndicator;

public class TimerService extends Service {

    private static final String CHANNEL_ID = "NotificationChannelID";

    private final IBinder binder = new LocalBinder();
    private Handler timerHandler;
    private Timer timer;
    private boolean isTimerOn = false;
    private Ringtone ringtone;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    private void startAlarm(Context context) {
        Uri alarmUri = RingtoneManager.getValidRingtoneUri(context);
        ringtone = RingtoneManager.getRingtone(context, alarmUri);
        ringtone.play();
    }
    public void cancelAlarm() {
        if (ringtone != null && ringtone.isPlaying()){
            ringtone.stop();
        }
    }

    public class LocalBinder extends Binder {
        public TimerService getService() {
            return TimerService.this;
        }
    }

    public synchronized void setTimerHandler(Handler stateHandler) {
        this.timerHandler = stateHandler;
    }

    protected synchronized void sendTimerChangedMessage(Integer integer) {
        if (timerHandler != null) {
            Message msg = timerHandler.obtainMessage();
            Bundle bundle = new Bundle();
            bundle.putString("timer", integer.toString());
            msg.setData(bundle);
            msg.what = 1;
            timerHandler.sendMessage(msg);
        }

    }

    public void startTimer(Integer timerValue, Context context) {
        if (!isTimerOn) {
            isTimerOn = true;
            final Integer[] timeRemaining = {timerValue};
            timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    timeRemaining[0]--;
                    if(timeRemaining[0] > 60 && timeRemaining[0] % 60 == 0){
                        sendTimerChangedMessage(timeRemaining[0]);
                    }
                    if(timeRemaining[0] < 60){
                        sendTimerChangedMessage(timeRemaining[0]);
                    }
//                    NotificationUpdate(timeRemaining[0]);
                    if (timeRemaining[0] <= 0) {
                        timer.cancel();
                        isTimerOn = false;
                        NotificationUpdate();
                        startAlarm(context);
                    }
                }
            }, 0, 100);
        }
    }

    public void stopTimer() {
        if(isTimerOn){
            timer.cancel();
            isTimerOn = false;
        }
    }



    public Integer convertTimeToSeconds(int hours, int minutes, int seconds) {
        return hours * 3600 + minutes * 60 + seconds;
    }

    public Integer convertTimeToMinutes(int hour, int minutes, int seconds) {
        return hour * 60 + minutes + seconds / 60;
    }


    public boolean isTimerOn() {
        return isTimerOn;
    }

    public void setTimerProgress(CircularProgressIndicator progressBar, int secondsLeft, int maxTimeValue) {
        //ostatnia minuta wyświetlana w sekundach
        if (secondsLeft < 60) {
            progressBar.setProgress(secondsLeft, maxTimeValue * 60);
        } else {
            //progres ustawiany w minutach
            Integer timer = convertTimeToMinutes(0, 0, secondsLeft);
            progressBar.setProgress(timer, maxTimeValue);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopTimer();
        timerHandler = null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    public void NotificationUpdate(){
        try {
            Intent notificationIntent = new Intent(this, TableFragment.class);
            final PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
            final Notification[] notification = {new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("Time's up!")
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
