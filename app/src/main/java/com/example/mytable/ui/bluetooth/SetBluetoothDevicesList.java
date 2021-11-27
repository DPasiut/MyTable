package com.example.mytable.ui.bluetooth;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mytable.service.bluetooth.BluetoothService;
import com.example.mytable.ui.bluetooth.BluetoothViewAdapter;

import java.util.Objects;

@SuppressLint("StaticFieldLeak")
public class SetBluetoothDevicesList extends AsyncTask<Void, Void, Void> {
    RecyclerView recyclerView;
    View view;
    BluetoothService bluetoothService;

    public SetBluetoothDevicesList(RecyclerView recyclerView, View view, BluetoothService bluetoothService) {
        this.bluetoothService = bluetoothService;
        this.recyclerView = recyclerView;
        this.view = view;
    }
    @Override
    protected Void doInBackground(Void... arg0) {
        try {
            bluetoothService.enableBluetooth();
            boolean needWait = bluetoothService.getBluetoothDevices().size() == 0;
            while (needWait){
                Thread.sleep(10);
                needWait = bluetoothService.getBluetoothDevices().size() == 0;
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;

    }
    @Override
    protected void onPostExecute(Void result) {
        ((BluetoothViewAdapter) Objects.requireNonNull(recyclerView.getAdapter())).setData();
        Objects.requireNonNull(recyclerView.getAdapter()).notifyDataSetChanged();
        recyclerView.invalidate();
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
    }

}