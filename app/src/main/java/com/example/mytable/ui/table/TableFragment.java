package com.example.mytable.ui.table;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.mytable.R;
import com.example.mytable.service.bluetooth.BluetoothService;



public class TableFragment extends Fragment {

    protected BluetoothService bluetoothService;
    private boolean mBound = false;
    private Button upButton, downButton, userButton1, userButton2, userButton3;
    private TextView position;
    private int tableHigh = 0;
    private int count = 0;

    private int high1;
    private int high2;
    private int high3;

    private int currentPosition = 0;

    @SuppressLint("ClickableViewAccessibility")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_table, container, false);
        upButton = root.findViewById(R.id.up_btn);
        downButton = root.findViewById(R.id.down_btn);
        userButton1 = root.findViewById(R.id.user_1);
        userButton2 = root.findViewById(R.id.user_2);
        userButton3 = root.findViewById(R.id.user_3);
        position = root.findViewById(R.id.position);

        Intent intent = new Intent(getActivity(), BluetoothService.class);
        requireActivity().bindService(intent, connection, Context.BIND_AUTO_CREATE);

        position.setText(String.valueOf(currentPosition));

        upButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mBound) {
                    switch(event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            up(this);
                            break;
                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_CANCEL:
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
                moveToPoint("u" + Integer.valueOf(high1).toString());
            }
        });

        userButton1.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                high1 = currentPosition;
                Toast.makeText(v.getContext(), currentPosition + " cm has been set", Toast.LENGTH_LONG).show();
                return true;
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

        @SuppressLint("HandlerLeak")
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            BluetoothService.LocalBinder binder = (BluetoothService.LocalBinder) service;
            bluetoothService = binder.getService();


            bluetoothService.setBluetoothHandler(new Handler(Looper.myLooper()) {
                @SuppressLint("SetTextI18n")
                @Override
                public void handleMessage(Message msg) {
                    if(position != null){
                        Bundle bundle = msg.getData();
                        String o = (String) bundle.get("message");
                        if(count == 50){
                            int tmp = tableHigh/50;
                            currentPosition = tmp;
                            position.setText(String.valueOf(tmp));
                            tableHigh = 0;
                            count = 0;
                        }
                        tableHigh += Integer.parseInt(o);
                        count++;
                    }
                }
            });

            mBound = true;
            Log.d("MAIN_ACTIVITY", "SERVICE CONNECTED FROM TABLE_FRAGMENT" );
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
            Log.d("MAIN_ACTIVITY", "SERVICE DISCONNECTED FROM TABLE_FRAGMENT" );
            bluetoothService.setBluetoothHandler(null);
        }
    };

}