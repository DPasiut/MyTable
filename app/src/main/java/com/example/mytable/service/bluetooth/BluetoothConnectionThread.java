package com.example.mytable.service.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;


public class BluetoothConnectionThread extends Thread {
    private static final String TAG = "[BLUETOOTH_CONNECTION_THREAD]";
    private Handler handler;
    private final BluetoothAdapter bluetoothAdapter;
    private final BluetoothDevice device;
    private final BluetoothSocket mmSocket;
    private InputStream mmInStream;
    private OutputStream mmOutStream;
    private BluetoothService bluetoothService;
    private StringBuilder stringBuilder;

    private volatile boolean isWorking;

    private interface MessageConstants {
        public static final int MESSAGE_READ = 0;
        public static final int MESSAGE_WRITE = 1;
        public static final int MESSAGE_TOAST = 2;
    }

    public BluetoothConnectionThread(BluetoothAdapter bluetoothAdapter, BluetoothDevice device, BluetoothService bluetoothService) {
        this.bluetoothService = bluetoothService;
        this.bluetoothAdapter = bluetoothAdapter;
        this.device = device;
        mmSocket = createSocket();
        try {
            mmInStream = mmSocket.getInputStream();
            mmOutStream = mmSocket.getOutputStream();
        } catch (IOException e) {
            Log.e(TAG, "Could not get Input or Output stream", e);
        }
        isWorking = false;
    }

    public void run() {
        connect();
        isWorking = true;
        stringBuilder = new StringBuilder();
        char lastChar = '.';

        while (isWorking) {
            try {
                while (mmInStream.available() > 0 && lastChar != ';') {
                    lastChar = (char) mmInStream.read();
                    if (lastChar != ';') {
                        stringBuilder.append(lastChar);
                    }
                    if(stringBuilder.length() > 0 && lastChar == ';'){
                      sendMessage(stringBuilder.toString());
                        stringBuilder = new StringBuilder();
                        lastChar = '.';
                    }
                }
            } catch (IOException e) {
                isWorking = false;
                bluetoothService.setState(BluetoothCommunicationState.DISCONNECTED);
            }
        }
    }

    // Call this from the main activity to send data to the remote device.
    public synchronized void write(byte[] bytes) {
        try {
            Log.d("write", new String(bytes));
            mmOutStream.write(bytes);
            mmOutStream.flush();

        } catch (IOException e) {
            Log.e(TAG, "Error occurred when sending data", e);
        }
    }

    // Call this method from the main activity to shut down the connection.
    public void cancel() {
        try {
            mmSocket.close();
            isWorking = false;
            Log.d(TAG, "Connection closed success");
            bluetoothService.setState(BluetoothCommunicationState.DISCONNECTED);
        } catch (IOException e) {
            Log.e(TAG, "Could not close the connect socket", e);
        }
    }

    private void connect() {
        bluetoothAdapter.cancelDiscovery();
        try {
            mmSocket.connect();
            Log.d(TAG, "Connected success");
            bluetoothService.setState(BluetoothCommunicationState.CONNECTED);
        } catch (IOException connectException) {
            try {
                mmSocket.close();
            } catch (IOException closeException) {
                Log.e(TAG, "Could not close the client socket", closeException);
                bluetoothService.setState(BluetoothCommunicationState.DISCONNECTED);
            }
        }

    }

    private BluetoothSocket createSocket() {
        BluetoothSocket tmp = null;
        try {
            tmp = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
            Log.d(TAG, "Socket's create() method success");
        } catch (IOException e) {
            Log.e(TAG, "Socket's create() method failed", e);
        }
        return tmp;
    }

    private synchronized void sendMessage(String message){
        if (handler != null){
            Message msg = handler.obtainMessage();
            Bundle bundle = new Bundle();
            bundle.putString("message", message);
            msg.setData(bundle);
            msg.what = 1;
            handler.sendMessage(msg);
        }

    }
    public void setHandler(Handler handler){
        this.handler = handler;
    }

}