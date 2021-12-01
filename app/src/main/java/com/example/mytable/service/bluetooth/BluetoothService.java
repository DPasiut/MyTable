package com.example.mytable.service.bluetooth;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.mytable.R;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;


public class BluetoothService extends Service {
    private static final String TAG = "[BLUETOOTH_SERVICE]";
    private NotificationManager mNM;
    private final int NOTIFICATION = 1;

    private final IBinder binder = new LocalBinder();

    private BluetoothConnectionThread connectionThread;
    private BluetoothCommunicationState state;
    private boolean isBluetoothEnabled;
    private boolean isLightOn = false;
    private final BluetoothAdapter bluetoothAdapter;
    private Handler bluetoothHandler;
    private Handler stateHandler;

    @SuppressLint("HardwareIds")
    public BluetoothService() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        state = BluetoothCommunicationState.DISCONNECTED;
        isBluetoothEnabled = bluetoothAdapter.isEnabled();
    }

    public BluetoothDevice getDevice(String name) {
        return bluetoothAdapter.getBondedDevices().stream()
                .filter(bluetoothDevice -> bluetoothDevice.getName().equals(name))
                .findFirst().orElseThrow(NullPointerException::new);
    }

    public List<String> getBluetoothDevices() {
        Set<BluetoothDevice> pairedDevices = getPairedDevices();
        List<String> devices = new ArrayList();
        for (BluetoothDevice device : pairedDevices) {
            devices.add(device.getName());
        }
        return devices;
    }

    public void connectDevice(BluetoothDevice device) {
        stopConnectionThread();
        try {
            connectionThread = new BluetoothConnectionThread(bluetoothAdapter, device, this);
            connectionThread.connect();
            connectionThread.start();
            connectionThread.setHandler(new Handler(Looper.getMainLooper()) {
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


    private synchronized void sendStateChangedMessage() {
        if (stateHandler != null) {
            Message msg = stateHandler.obtainMessage();
            Bundle bundle = new Bundle();
            bundle.putString("state", state.toString());
            msg.setData(bundle);
            msg.what = 1;
            stateHandler.sendMessage(msg);
        }

    }

    public synchronized void setBluetoothHandler(Handler bluetoothHandler) {
        this.bluetoothHandler = bluetoothHandler;
    }

    public synchronized void setStateHandler(Handler stateHandler) {
        this.stateHandler = stateHandler;
    }


    private synchronized void sendMessageToDevice(String message) {
        if (state == BluetoothCommunicationState.CONNECTED) {
            connectionThread.write(message.getBytes());
        } else {
            Log.d(TAG, "Device is disconnected");
        }
    }

    public void disconnect() {
        try {
            stopConnectionThread();
        } catch (RuntimeException e) {
            Log.d(TAG, "Something went wrong with disconnection");
        } finally {
            state = BluetoothCommunicationState.DISCONNECTED;
        }
    }

    public void up(String s) {
        sendMessageToDevice(s);
    }

    public void down(String s) {
        sendMessageToDevice(s);
    }

    public void moveToPoint(String s) {
        sendMessageToDevice(s);
    }

    public void stopEngine(String s) {
        sendMessageToDevice(s);
    }

    public String turnLightOn(String s) {
        //  sendMessageToDevice(s);
        isLightOn = true;
        return s;
    }

    public void turnLightOFF(String s) {
        sendMessageToDevice(s);
        isLightOn = false;
    }

    public BluetoothCommunicationState getState() {
        return state;
    }

    public void setState(BluetoothCommunicationState state) {
        this.state = state;
        sendStateChangedMessage();
    }

    public boolean isBluetoothEnabled() {
        return isBluetoothEnabled;
    }

    public void enableBluetooth() {
        try {
            bluetoothAdapter.enable();
            isBluetoothEnabled = true;
            sendStateChangedMessage();
        } catch (Exception e) {
            Log.d(TAG, "Something went wrong on Bluetooth enable");
        }
    }

    public void disableBluetooth() {
        try {
            stopConnectionThread();
            bluetoothAdapter.disable();
            isBluetoothEnabled = false;
            sendStateChangedMessage();

        } catch (Exception e) {
            Log.d(TAG, "Something went wrong on Bluetooth disable");
        }
    }

    private Set<BluetoothDevice> getPairedDevices() {
        return bluetoothAdapter.getBondedDevices();
    }


    private synchronized void stopConnectionThread() {
        if (connectionThread != null) {
            connectionThread.cancel();
            connectionThread = null;
            state = BluetoothCommunicationState.DISCONNECTED;
            Log.d(TAG, "State: " + state);
        }
    }

    public boolean isBluetoothConnected() {
        return state == BluetoothCommunicationState.CONNECTED;
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
        Log.i("BluetoothService", "Received start id " + startId + ": " + intent);
        return START_NOT_STICKY;
    }


}
