package com.example.mytable.service.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;


public class BluetoothConnectionThread extends Thread {
    private static final String TAG = "[BLUETOOTH_CONNECTION_THREAD]";
    private final Handler handler; // handler that gets info from Bluetooth service
    private final BluetoothAdapter bluetoothAdapter;
    private final BluetoothDevice device;
    private final BluetoothSocket mmSocket;
    private InputStream mmInStream;
    private OutputStream mmOutStream;
    private BluetoothService bluetoothService;

    public boolean isWorking() {
        return isWorking;
    }

    private volatile boolean isWorking;
    private byte[] mmBuffer; // mmBuffer store for the stream
    Set<BluetoothDevice> pairedDevices;


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
        handler = new Handler();
    }

    public void run() {
        connect();
        isWorking = true;
        StringBuilder stringBuilder = new StringBuilder();
        char lastChar = '.';

        mmBuffer = new byte[1024];

        while (isWorking) {
            try {
                while (mmInStream.available() > 0 && lastChar != ';') {
                    Message msg = handler.obtainMessage(mmInStream.read());
                    handler.sendMessage(msg);
                    Log.d("odebrane", Integer.valueOf(mmInStream.read()).toString());
                    lastChar = (char) mmInStream.read();
                    if (lastChar != ';') {
                        stringBuilder.append(lastChar);
                    }
                }
            } catch (IOException e) {
                isWorking = false;
            }
        }
    }

    public Handler getMessage(){
        return this.handler;
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

//    private BluetoothDevice getDevice() {
//        return bluetoothAdapter.getBondedDevices().stream()
//                .filter(bluetoothDevice -> bluetoothDevice.getName().equals("XM-15"))
//                .findFirst().orElseThrow(NullPointerException::new);
//    }

//    public Set<BluetoothDevice> getPairedDevices(){
//        pairedDevices = bluetoothAdapter.getBondedDevices();
//        return bluetoothAdapter.getBondedDevices();
//    }

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
}