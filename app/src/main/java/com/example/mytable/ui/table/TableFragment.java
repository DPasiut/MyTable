package com.example.mytable.ui.table;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
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
import com.example.mytable.database.AppDatabase;
import com.example.mytable.database.Setting;
import com.example.mytable.service.bluetooth.BluetoothCommunicationState;
import com.example.mytable.service.bluetooth.BluetoothService;

import java.util.ArrayList;
import java.util.List;


public class TableFragment extends Fragment {

    protected BluetoothService bluetoothService;
    private boolean mBound = false;
    private Button upButton, downButton, userButton1, userButton2, userButton3;
    private TextView position;
    private int tableHigh = 0;
    private int count = 0;

    private Integer currentPosition;


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

        AppDatabase appDb = AppDatabase.getInstance(this.getContext());
        //new GetSetting(appDb, root).execute();


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
                if(bluetoothService.getState() != BluetoothCommunicationState.CONNECTED){
                    Toast.makeText(root.getContext(),"No Connection!", Toast.LENGTH_LONG).show();

                }else {
                    new GetSetting(appDb, root, 1).execute();
                }
            }
        });

        userButton1.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                new UpsertSetting(appDb, root, "first_high",1).execute();
                return true;
            }
        });
        userButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }

        });

        userButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
                        if(count == 25){
                            Integer tmp = tableHigh/25;
                            currentPosition = tmp;
                            position.setText(String.valueOf(tmp));
                            tableHigh = 0;
                            count = 0;
                        }
                        if(o != null){
                            tableHigh += Integer.parseInt(o);
                            count++;
                        }
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

    private class UpsertSetting extends AsyncTask<Void, Void, Void> {
        AppDatabase appDb;
        View view;
        String name;
        Integer id;

        public UpsertSetting(AppDatabase database, View view, String name, Integer id) {
            this.appDb = database;
            this.view = view;
            this.name = name;
            this.id = id;
        }
        @Override
        protected Void doInBackground(Void... arg0) {
            try {
                String tmp = appDb.settingsDao().getValueBySettingId(id);
                appDb.settingsDao().updateSettingValueByName(id, String.valueOf(currentPosition));

            }catch (Exception e){
                appDb.settingsDao().insert(id, name, String.valueOf(currentPosition));
            }
                return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            Toast.makeText(view.getContext(), currentPosition + " cm has been set", Toast.LENGTH_LONG).show();
        }

    }

    private class GetSetting extends AsyncTask<Void, Void, Void> {
        AppDatabase appDb;
        View view;
        String tmp = null;
        Integer id;

        public GetSetting(AppDatabase database, View view, Integer id) {
            this.appDb = database;
            this.view = view;
            this.id = id;
        }
        @Override
        protected Void doInBackground(Void... arg0) {
            try {
                 tmp = appDb.settingsDao().getValueBySettingId(id);
            }catch (Exception e){
                Log.d("TABLE_FRAGMENT", "something went wrong on GetSetting");
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            if (tmp != null){
                moveToPoint(tmp);
            }else {
                Toast.makeText(view.getContext(),"Please long press on button first", Toast.LENGTH_LONG).show();
            }

        }
    }
}