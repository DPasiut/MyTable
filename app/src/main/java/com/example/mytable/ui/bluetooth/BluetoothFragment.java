package com.example.mytable.ui.bluetooth;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mytable.R;
import com.example.mytable.service.bluetooth.BluetoothCommunicationState;
import com.example.mytable.service.bluetooth.BluetoothService;

import java.util.Objects;



public class BluetoothFragment extends Fragment {

    private BluetoothService bluetoothService;
    private BluetoothViewAdapter bluetoothViewAdapter;
    private BluetoothCommunicationState state;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        bluetoothService = new BluetoothService();
        bluetoothViewAdapter = new BluetoothViewAdapter(bluetoothService);

        View root = inflater.inflate(R.layout.fragment_bluetooth, container, false);
        RecyclerView recyclerView = root.findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(bluetoothViewAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(root.getContext()));


        @SuppressLint("UseSwitchCompatOrMaterialCode")
        Switch bluetoothSwitch = root.findViewById(R.id.bluetooth_switch);

        if(bluetoothService.isBluetoothOn()){
            bluetoothSwitch.setChecked(true);
            bluetoothViewAdapter.setData();
        }

        bluetoothSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                new SetBluetoothDevicesList(recyclerView, root).execute();

            } else {
                clearBluetoothDevicesList();
            }
        });

        return root;
    }

    @SuppressLint("StaticFieldLeak")
    private class SetBluetoothDevicesList extends AsyncTask<Void, Void, Void> {
        RecyclerView recyclerView;
        View view;
        public SetBluetoothDevicesList(RecyclerView recyclerView, View view) {
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

    private void clearBluetoothDevicesList(){
        bluetoothViewAdapter.clear();
        bluetoothViewAdapter.notifyDataSetChanged();
        bluetoothService.disableBluetooth();
    }
}