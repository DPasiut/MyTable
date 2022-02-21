package com.example.mytable.service.bluetooth;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.example.mytable.PreferencesConstants.MIN_TABLE_POSITION_CM;


public class BluetoothService extends Service {
    private static final String TAG = "[BLUETOOTH_SERVICE]";
    private static final Double SCALE = 40.5;
    private final IBinder binder = new LocalBinder();

    private BluetoothConnectionThread connectionThread;
    private BluetoothCommunicationState state;
    private boolean isBluetoothEnabled;
    private boolean isLightOn = false;
    private final BluetoothAdapter bluetoothAdapter;
    private Handler bluetoothHandler;
    private Handler stateHandler;
    private String connectedDevice;


    private Integer currentPosition;
    private boolean canClick = true;

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

    @SuppressLint("ShowToast")
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
                    if (bluetoothHandler != null) {
                        Message message = bluetoothHandler.obtainMessage();
                        message.setData(msg.getData());
                        message.what = 1;
                        bluetoothHandler.sendMessage(message);
                        currentPosition = Integer.valueOf(message.getData().get("message").toString());
                    }
                }
            });
            setConnectedDevice(device.getName());
            if (connectionThread.isWorking()) {
                setState(BluetoothCommunicationState.CONNECTED);
            }
        } catch (Exception e) {
            setConnectedDevice("");
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

    public void calibrate(String s){
        sendMessageToDevice(s);
    }

    public void moveToPoint(String s) {
        new MoveToPoint(s).execute();
    }

    public void stopEngine(String s) {
        sendMessageToDevice(s);
    }

    public String lightTurnOn(String s) {
        sendMessageToDevice(s);
        isLightOn = true;
        return s;
    }

    public void lightTurnOff(String s) {
        sendMessageToDevice(s);
        isLightOn = false;
    }

    public void lightChangeColor(String s) {
        sendMessageToDevice(s);
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

    public boolean isLightOn() {
        return isLightOn;
    }

    public String getConnectedDevice() {
        return connectedDevice;
    }

    public void setConnectedDevice(String connectedDevice) {
        this.connectedDevice = connectedDevice;
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


    @SuppressLint("StaticFieldLeak")
    private class MoveToPoint extends AsyncTask<Void, Void, Void> {
        String destinationPosition;

        public MoveToPoint(String destinationPosition) {
            int tmp = Integer.parseInt(destinationPosition) - MIN_TABLE_POSITION_CM;

            //Te skomplikowane obliczenia to po prostu przeskalowanie z wartosći wyświetlanej w cm do wartości przetwatzanej w arduino -> odwrotność operacji z handlera w TableFragment
            this.destinationPosition = String.valueOf(Integer.valueOf((int) (Math.max(tmp, 1) * SCALE)));
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            canClick = false;
            try {
                sendMessageToDevice(destinationPosition);
                boolean needWait = doesMotorWorking();
                while (needWait) {
                    Thread.sleep(100);
                    needWait = doesMotorWorking();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            canClick = true;
        }

        private boolean doesMotorWorking() {
            return currentPosition != null && Math.abs(Integer.parseInt(destinationPosition) - currentPosition) > 41;
        }
    }

    public boolean isCanClick() {
        return canClick;
    }

}
