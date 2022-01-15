package com.example.mytable.ui.settings;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.mytable.R;
import com.example.mytable.service.bluetooth.BluetoothService;


public class SettingsFragment extends Fragment {

    private BluetoothService bluetoothService;
    boolean mBound = false;
    private boolean mShouldUnbind;

    @SuppressLint("ClickableViewAccessibility")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_settings, container, false);

        Button calibrateBtn = root.findViewById(R.id.calibration_btn);

        calibrateBtn.setOnTouchListener((v, event) -> onCalibrateButtonTouch(root, event));

        bindBluetoothService();
        return root;

    }

    private void calibrate(){
        bluetoothService.calibrate("p");
    }
    private void stop() {
        bluetoothService.stopEngine("w");
    }

    private boolean onCalibrateButtonTouch(View root, MotionEvent event) {
        Log.d("cal_btn", "touch");
        if (mBound) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (bluetoothService.isBluetoothConnected()) {
                       calibrate();
                    } else {
                        Toast.makeText(root.getContext(), "No Connection!", Toast.LENGTH_LONG).show();
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    stop();
                    break;
            }
        }
        return true;
    }

    private final ServiceConnection bluetoothServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            BluetoothService.LocalBinder binder = (BluetoothService.LocalBinder) service;
            bluetoothService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    private void bindBluetoothService() {
        Intent intent = new Intent(getContext(), BluetoothService.class);
        if (requireActivity().bindService(intent, bluetoothServiceConnection, Context.BIND_AUTO_CREATE)) {
            mShouldUnbind = true;
        } else {
            Log.e("MY_APP_TAG", "Error: The requested service doesn't " +
                    "exist, or this client isn't allowed access to it.");
        }

    }

    void unbindService() {
        if (mShouldUnbind) {
            requireActivity().unbindService(bluetoothServiceConnection);
            mShouldUnbind = false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbindService();
    }
}
