package com.example.mytable.ui.bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mytable.R;
import com.example.mytable.service.bluetooth.BluetoothService;

public class BluetoothFragment extends Fragment {


    private BluetoothService bluetoothService;
    private BluetoothAdapter bluetoothAdapter;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        bluetoothService = new BluetoothService();
        bluetoothAdapter = new BluetoothAdapter();


        View root = inflater.inflate(R.layout.fragment_bluetooth, container, false);
        RecyclerView recyclerView = root.findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(root.getContext()));

        @SuppressLint("UseSwitchCompatOrMaterialCode")
        Switch bluetoothSwitch = root.findViewById(R.id.bluetooth_switch);

        if(bluetoothService.isBluetoothOn()){
            bluetoothSwitch.setChecked(true);
            bluetoothAdapter = new BluetoothAdapter(bluetoothService.getBluetoothDevices());
        }

        bluetoothSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                bluetoothService.enableBluetooth();
                bluetoothAdapter = new BluetoothAdapter();
            } else {
                bluetoothService.disableBluetooth();
            }
        });

        return root;
    }

    private void connect(String name) {
        BluetoothDevice device = bluetoothService.getDevice(name);
        bluetoothService.connect(device);
    }

}