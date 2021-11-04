package com.example.mytable.service.bluetooth;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.mytable.MainActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class BluetoothService extends Service {
    private static final String TAG = "[BLUETOOTH_SERVICE]";
    public static final String CHANNEL_ID = "ForegroundServiceChannel";

    private final IBinder binder = new LocalBinder();

    private BluetoothConnectionThread connectionThread;
    private BluetoothCommunicationState state;
    private final BluetoothAdapter bluetoothAdapter;
    private Handler bluetoothHandler;
    private Handler stateHandler;

    @SuppressLint("HardwareIds")
    public BluetoothService() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        state = BluetoothCommunicationState.DISCONNECTED;
    }

    public BluetoothDevice getDevice(String name) {
        return bluetoothAdapter.getBondedDevices().stream()
                .filter(bluetoothDevice -> bluetoothDevice.getName().equals(name))
                .findFirst().orElseThrow(NullPointerException::new);
    }

    public List<String> getBluetoothDevices(){
        Set<BluetoothDevice> pairedDevices = getPairedDevices();
        List<String> devices = new ArrayList();
        for(BluetoothDevice device : pairedDevices) {
            devices.add(device.getName());
            Log.d("ADD DEVICE TO LIST ",device.getName() +" "+  device.getAddress());

        }
        return devices;
    }

    public void connectDevice(BluetoothDevice device) {
        stopConnectionThread();
        //TODO zrobić cos z tym stanem CONNECTING
//        state = BluetoothCommunicationState.CONNECTING;
        try {
            connectionThread = new BluetoothConnectionThread(bluetoothAdapter, device, this);
            connectionThread.start();
            connectionThread.setHandler(new Handler(Looper.myLooper()) {
                @SuppressLint("SetTextI18n")
                @Override
                public void handleMessage(Message msg) {
                    if(bluetoothHandler != null){
                        Message message = bluetoothHandler.obtainMessage();
                        message.setData(msg.getData());
                        message.what = 1;
                        bluetoothHandler.sendMessage(message);
                    }
                }
            });
//            state = BluetoothCommunicationState.CONNECTED;
        }catch (Exception e){
            state = BluetoothCommunicationState.DISCONNECTED;
            Log.d(TAG, "Something went wrong with connection");
        }
    }

    public void disconnect() {
        Log.d(TAG, "I am trying disconnect device. My current state: " + state);
        try {
            stopConnectionThread();
        } catch (RuntimeException e){
            Log.d(TAG, "Something went wrong with disconnection");
        } finally {
            state = BluetoothCommunicationState.DISCONNECTED;
        }
    }

    public void up(String s){
        sendMessageToDevice(s);
    }

    public void down(String s){
        sendMessageToDevice(s);
    }

    public void moveToPoint(String s){
        sendMessageToDevice(s);
    }

    public void stopEngine(String s){
        sendMessageToDevice(s);
    }

    public BluetoothCommunicationState getState() {
        return state;
    }

    public void setState(BluetoothCommunicationState state) {
        this.state = state;
        sendStateChangedMessage();
    }

    public boolean isBluetoothOn(){
        return bluetoothAdapter.isEnabled();
    }

    public void enableBluetooth(){
        try {
            bluetoothAdapter.enable();
        }catch (Exception e){
            Log.d(TAG, "Something went wrong on Bluetooth enable");
        }
    }

    public void disableBluetooth(){
        try {
            stopConnectionThread();
            bluetoothAdapter.disable();

        }catch (Exception e){
            Log.d(TAG, "Something went wrong on Bluetooth disable");
        }
    }

    public synchronized void setBluetoothHandler(Handler bluetoothHandler){
        this.bluetoothHandler = bluetoothHandler;
    }

    public synchronized void setStateHandler(Handler stateHandler){
        this.stateHandler = stateHandler;
    }

    private synchronized void sendStateChangedMessage(){
        if (stateHandler != null){
            Message msg = stateHandler.obtainMessage();
            Bundle bundle = new Bundle();
            bundle.putString("state", state.toString());
            msg.setData(bundle);
            msg.what = 1;
            stateHandler.sendMessage(msg);
        }

    }
    private Set<BluetoothDevice> getPairedDevices(){
        return bluetoothAdapter.getBondedDevices();
    }

    private synchronized void sendMessageToDevice(String message) {
        if(state == BluetoothCommunicationState.CONNECTED){
            connectionThread.write(message.getBytes());
        }else {
            Log.d(TAG, "Device is disconnected");
        }
    }

    private synchronized void stopConnectionThread() {
        if (connectionThread != null) {
            connectionThread.cancel();
            connectionThread = null;
            state = BluetoothCommunicationState.DISCONNECTED;
            Log.d(TAG, "State: " + state);
        }
    }

    public class LocalBinder extends Binder {
        public BluetoothService getService() {
            return BluetoothService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String input = intent.getStringExtra("inputExtra");
        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Foreground Service")
                .setContentText(input)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1, notification);
        //do heavy work on a background thread
        //stopSelf();
        return START_NOT_STICKY;
    }

    private void createNotificationChannel() {
        NotificationChannel serviceChannel = new NotificationChannel(
                CHANNEL_ID,
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(serviceChannel);
    }

}
