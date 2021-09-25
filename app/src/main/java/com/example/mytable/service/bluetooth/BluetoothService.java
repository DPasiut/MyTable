package com.example.mytable.service.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class BluetoothService extends android.app.Service {
    private static final String TAG = "[BLUETOOTH_SERVICE]";

    private BluetoothConnectionThread connectionThread;
    private String deviceMacAddress;
    private BluetoothCommunicationState state;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice device;

    public BluetoothService() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        state = BluetoothCommunicationState.DISCONNECTED;
    }

    public String getMessage(){
        return connectionThread.getMessage().toString();
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
        }
        return devices;
    }
    private Set<BluetoothDevice> getPairedDevices(){
        return bluetoothAdapter.getBondedDevices();
    }

    public void connectDevice(BluetoothDevice device) {
        stop();
        state = BluetoothCommunicationState.CONNECTING;
        try {
            state = BluetoothCommunicationState.CONNECTED;
            connectionThread = new BluetoothConnectionThread(bluetoothAdapter, device);
            connectionThread.start();
        }catch (RuntimeException e){
            state = BluetoothCommunicationState.DISCONNECTED;
            Log.d(TAG, "Something went wrong with connection");
        }
    }

    public void disconnect() {
        Log.d(TAG, "I am trying disconnect device. My current state: " + state);
        try {
            stop();
        } catch (RuntimeException e){
            Log.d(TAG, "Something went wrong with disconnection");
        } finally {
            state = BluetoothCommunicationState.DISCONNECTED;
        }
    }

    public synchronized void sendMessage(String message) {
        if(state == BluetoothCommunicationState.CONNECTED){
            connectionThread.write(message.getBytes());
        }else {
            Log.d(TAG, "Device is disconnected");
        }
    }

    public BluetoothCommunicationState getState() {
        return state;
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
            bluetoothAdapter.disable();

        }catch (Exception e){
            Log.d(TAG, "Something went wrong on Bluetooth disable");
        }
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private synchronized void stop() {
        if (connectionThread != null) {
            connectionThread.cancel();
            connectionThread = null;
            state = BluetoothCommunicationState.DISCONNECTED;
            Log.d(TAG, "State: " + state);
        }
    }


}
