package com.example.mytable.ui.bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

    protected BluetoothService bluetoothService;
    private boolean mBound = false;

    private BluetoothViewAdapter bluetoothViewAdapter;

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch bluetoothSwitch;
    private Button disconnectButton;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        Intent intent = new Intent(getActivity(), BluetoothService.class);
        bluetoothViewAdapter = new BluetoothViewAdapter(getActivity());
        requireActivity().bindService(intent, connection, Context.BIND_AUTO_CREATE);

        View root = inflater.inflate(R.layout.fragment_bluetooth, container, false);
        disconnectButton = root.findViewById(R.id.disconnectButton);
        RecyclerView recyclerView = root.findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(bluetoothViewAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(root.getContext()));
        bluetoothSwitch = root.findViewById(R.id.bluetooth_switch);

        if(BluetoothAdapter.getDefaultAdapter().isEnabled()){
            bluetoothSwitch.setChecked(true);
        }

        bluetoothSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                new SetBluetoothDevicesList(recyclerView, root).execute();

            } else {
                clearBluetoothDevicesList();
            }
        });

        disconnectButton.setOnClickListener(v -> {
            bluetoothService.disconnect();
            new SetBluetoothDevicesList(recyclerView, root).execute();
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
        bluetoothService.disableBluetooth();
    }

    private final ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            BluetoothService.LocalBinder binder = (BluetoothService.LocalBinder) service;
            bluetoothService = binder.getService();

            SharedPreferences preferences = getActivity().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE);
            String deviceName = preferences.getString("deviceName", "");

            if(deviceName != null
                    && !deviceName.equals("")
                    && bluetoothService.getState() != BluetoothCommunicationState.CONNECTED
            ) {
                bluetoothService.connectDevice(bluetoothService.getDevice(deviceName));
            } else {
                if (bluetoothService.getState() != BluetoothCommunicationState.CONNECTED){
                    bluetoothViewAdapter.setData();
                    disconnectButton.setVisibility(View.INVISIBLE);
                }
            }

            bluetoothService.setStateHandler(new Handler(Looper.myLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    Bundle bundle = msg.getData();
                    String connectionState = (String) bundle.get("state");

                    switch (connectionState) {
                        case "CONNECTED":
                            disconnectButton.setVisibility(View.VISIBLE);
                            break;
                        case "DISCONNECTED":
                            disconnectButton.setVisibility(View.INVISIBLE);
                    }
                }
            });

            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
            bluetoothService.setBluetoothHandler(null);
        }
    };


}