package com.example.mytable.ui.table;

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
import androidx.lifecycle.ViewModelProvider;

import com.example.mytable.R;
import com.example.mytable.service.bluetooth.BluetoothService;
import com.example.mytable.ui.bluetooth.BluetoothViewAdapter;

import java.util.Objects;

public class TableFragment extends Fragment {

    protected BluetoothService bluetoothService;
    private boolean mBound = false;
    private BluetoothViewAdapter bluetoothViewAdapter;
    private Button upButton, downButton, userButton1, userButton2, userButton3;

    @SuppressLint("ClickableViewAccessibility")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_table, container, false);
        upButton = root.findViewById(R.id.up_btn);
        downButton = root.findViewById(R.id.down_btn);
        userButton1 = root.findViewById(R.id.user_1);
        userButton2 = root.findViewById(R.id.user_2);
        userButton3 = root.findViewById(R.id.user_3);

        Intent intent = new Intent(getActivity(), BluetoothService.class);
        requireActivity().bindService(intent, connection, Context.BIND_AUTO_CREATE);

        upButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mBound) {
                    switch(event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            Log.d("GET ACTION", "UP");
                            up(this);
                            break;
                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_CANCEL:
                            Log.d("GET ACTION", "STOP");
                            stop(this);
                            break;
                    }
                }
                return true;
            }
        });

        downButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        down(this);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        stop(this);
                        break;
                }
                return false;
            }
        });

        userButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveToPoint("u" + Integer.valueOf(100).toString());
            }
        });

        userButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveToPoint("d100");
            }

        });

        userButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveToPoint("u500");
            }
        });
        return root;


    }


    private void moveToPoint(String s){
        bluetoothService.moveToPoint(s);
    }
    private void up(View.OnTouchListener v) {
        bluetoothService.up("q");
    }

    private void down(View.OnTouchListener v) {
        bluetoothService.down("e");
    }

    private void stop(View.OnTouchListener v) {
        bluetoothService.stopEngine("w");
    }

    private final ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            BluetoothService.LocalBinder binder = (BluetoothService.LocalBinder) service;
            bluetoothService = binder.getService();
            mBound = true;
            Log.d("MAIN_ACTIVITY", "SERVICE CONNECTED");
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }

    };

}