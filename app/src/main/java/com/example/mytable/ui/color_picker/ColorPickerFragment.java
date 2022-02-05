package com.example.mytable.ui.color_picker;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.mytable.R;
import com.example.mytable.service.bluetooth.BluetoothCommunicationState;
import com.example.mytable.service.bluetooth.BluetoothService;

import java.util.Locale;

import top.defaults.colorpicker.ColorPickerView;

public class ColorPickerFragment extends Fragment {

    private BluetoothService bluetoothService;
    private boolean mBound = false;

    ColorPickerView colorPickerView;
    View pickedColor;
    TextView txtColorHex;
    Button powerBtn;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_color_picker, container, false);

        bindBluetoothService();

        colorPickerView = root.findViewById(R.id.colorPicker);
        pickedColor = root.findViewById(R.id.pickedColor);
        txtColorHex = root.findViewById(R.id.colorHex);
        powerBtn = root.findViewById(R.id.powerBtn);

        colorPickerView.setOnlyUpdateOnTouchEventUp(true);
        colorPickerView.subscribe((color, fromUser, shouldPropagate) -> {
            pickedColor.setBackgroundColor(color);
            txtColorHex.setText(color(color));
            if (mBound) {
//                new Handler().postDelayed(() -> bluetoothService.lightChangeColor(color(color)), 500);
                bluetoothService.lightChangeColor(color(color));

            }
        });

        powerBtn.setOnClickListener(v -> {
            if (!bluetoothService.isBluetoothEnabled() || bluetoothService.getState() != BluetoothCommunicationState.CONNECTED) {
                Toast.makeText(v.getContext(), "Bluetooth is OFF or DISCONNECTED", Toast.LENGTH_SHORT).show();
            } else {
                if (!bluetoothService.isLightOn()) {
                    bluetoothService.lightTurnOn("o");
                } else {
                    bluetoothService.lightTurnOff("f");
                }
            }
        });
        return root;
    }


    private String color(int color){
        String red = String.valueOf(Color.red(color));
        String green = String.valueOf(Color.green(color));
        String blue = String.valueOf(Color.blue(color));
        return "c" + red + "," + green + "," + blue + ";";
    }
    private String colorHex(int color) {
        int a = Color.alpha(color);
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        return String.format(Locale.getDefault(), "0x%02X%02X%02X%02X", a, r, g, b);
    }


    private void bindBluetoothService() {
        Intent intent = new Intent(getActivity(), BluetoothService.class);
        requireActivity().bindService(intent, bluetoothServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private final ServiceConnection bluetoothServiceConnection = new ServiceConnection() {
        @SuppressLint("HandlerLeak")
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            BluetoothService.LocalBinder binder = (BluetoothService.LocalBinder) service;
            bluetoothService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
            bluetoothService.setBluetoothHandler(null);
        }
    };

    @Override
    public void onPause() {
        super.onPause();
        requireActivity().unbindService(bluetoothServiceConnection);
    }

}








