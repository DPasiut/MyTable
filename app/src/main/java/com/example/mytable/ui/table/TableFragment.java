package com.example.mytable.ui.table;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
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
import com.example.mytable.service.bluetooth.BluetoothCommunicationState;
import com.example.mytable.service.bluetooth.BluetoothService;


public class TableFragment extends Fragment {

    private static final String FIRST_POSITION = "firstPosition";
    private static final String SECOND_POSITION = "secondPosition";
    private static final String THIRD_POSITION = "thirdPosition";
    private static final String DEFAULT_POSITION_VALUE = "0";

    protected BluetoothService bluetoothService;
    private boolean mBound = false;
    private TextView position;
    private int tableHigh = 0;
    private int count = 0;

    private Integer currentPosition;

    private String firstPosition, secondPosition, thirdPosition;

    @SuppressLint("ClickableViewAccessibility")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_table, container, false);
        Button upButton = root.findViewById(R.id.up_btn);
        Button downButton = root.findViewById(R.id.down_btn);
        Button userButton1 = root.findViewById(R.id.user_1);
        Button userButton2 = root.findViewById(R.id.user_2);
        Button userButton3 = root.findViewById(R.id.user_3);
        position = root.findViewById(R.id.position);

        Intent intent = new Intent(getActivity(), BluetoothService.class);
        requireActivity().bindService(intent, connection, Context.BIND_AUTO_CREATE);

        getPreferences();

        setButtonText(userButton1, firstPosition);
        setButtonText(userButton2, secondPosition);
        setButtonText(userButton3, thirdPosition);

        upButton.setOnTouchListener((v, event) -> {
            if (mBound) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if(bluetoothService.getState() == BluetoothCommunicationState.CONNECTED){
                            up();
                        }else {
                            Toast.makeText(root.getContext(),"No Connection!", Toast.LENGTH_LONG).show();
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        stop();
                        break;
                }
            }
            return true;
        });

        downButton.setOnTouchListener((v, event) -> {
            switch(event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if(bluetoothService.getState() == BluetoothCommunicationState.CONNECTED){
                        down();
                    }else {
                        Toast.makeText(root.getContext(),"No Connection!", Toast.LENGTH_LONG).show();
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    stop();
                    break;
            }
            return false;
        });

        userButton1.setOnClickListener(v -> {
            if(bluetoothService.getState() == BluetoothCommunicationState.CONNECTED){
                moveToPoint(firstPosition);
            }else {
                Toast.makeText(root.getContext(),"No Connection!", Toast.LENGTH_LONG).show();
            }
        });

        userButton1.setOnLongClickListener(v -> {
            if (currentPosition != null){
                firstPosition = currentPosition.toString();
                savePositionToPreferences(FIRST_POSITION, firstPosition);
                setButtonText(userButton1, firstPosition);
                return true;
            } else {
                Toast.makeText(root.getContext(), "Unknown position :(", Toast.LENGTH_LONG).show();
            }
            return false;
        });

        userButton2.setOnClickListener(v -> {
            if(bluetoothService.getState() == BluetoothCommunicationState.CONNECTED){
                moveToPoint(secondPosition);
            }else {
                Toast.makeText(root.getContext(),"No Connection!", Toast.LENGTH_LONG).show();
            }
        });

        userButton2.setOnLongClickListener(v -> {
            if (currentPosition != null){
                secondPosition = currentPosition.toString();
                savePositionToPreferences(SECOND_POSITION, secondPosition);
                setButtonText(userButton2, secondPosition);
                return true;
            } else {
                Toast.makeText(root.getContext(), "Unknown position :(", Toast.LENGTH_LONG).show();
            }
            return false;
        });

        userButton3.setOnClickListener(v -> {
            if(bluetoothService.getState() == BluetoothCommunicationState.CONNECTED){
                moveToPoint(thirdPosition);
            }else {
                Toast.makeText(root.getContext(),"No Connection!", Toast.LENGTH_LONG).show();
            }
        });

        userButton3.setOnLongClickListener(v -> {
            if (currentPosition != null){
                thirdPosition = currentPosition.toString();
                savePositionToPreferences(THIRD_POSITION, thirdPosition);
                setButtonText(userButton3, thirdPosition);
                return true;
            } else {
                Toast.makeText(root.getContext(), "Unknown position :(", Toast.LENGTH_LONG).show();
            }
            return false;
        });

        return root;


    }

    private void setButtonText(Button button, String text){
        button.setText(text);
    }

    private void savePositionToPreferences(String key, String value) {
            SharedPreferences sharedPreferences = this.requireActivity().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE);
            SharedPreferences.Editor myEdit = sharedPreferences.edit();
            myEdit.putString(key, value);
            myEdit.apply();
    }

    private void getPreferences(){
        SharedPreferences preferences = this.requireActivity().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE);
        firstPosition = preferences.getString(FIRST_POSITION, DEFAULT_POSITION_VALUE);
        secondPosition = preferences.getString(SECOND_POSITION, DEFAULT_POSITION_VALUE);
        thirdPosition = preferences.getString(THIRD_POSITION, DEFAULT_POSITION_VALUE);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferences();
    }

    public void onPause() {
        super.onPause();
    }
    private void moveToPoint(String s){
        bluetoothService.moveToPoint(s);
    }

    private void up() {
        bluetoothService.up("q");
    }

    private void down() {
        bluetoothService.down("e");
    }

    private void stop() {
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
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
            bluetoothService.setBluetoothHandler(null);
        }
    };

    //Asynctasks to save and read values from RoomDatabase
//    private class UpsertSetting extends AsyncTask<Void, Void, Void> {
//        AppDatabase appDb;
//        View view;
//        String name;
//        Integer id;
//
//        public UpsertSetting(AppDatabase database, View view, String name, Integer id) {
//            this.appDb = database;
//            this.view = view;
//            this.name = name;
//            this.id = id;
//        }
//        @Override
//        protected Void doInBackground(Void... arg0) {
//            try {
//                String tmp = appDb.settingsDao().getValueBySettingId(id);
//                appDb.settingsDao().updateSettingValueByName(id, String.valueOf(currentPosition));
//
//            }catch (Exception e){
//                appDb.settingsDao().insert(id, name, String.valueOf(currentPosition));
//            }
//                return null;
//        }
//        @Override
//        protected void onPostExecute(Void result) {
//            Toast.makeText(view.getContext(), currentPosition + " cm has been set", Toast.LENGTH_LONG).show();
//        }
//
//    }


//    private class GetSetting extends AsyncTask<Void, Void, Void> {
//        AppDatabase appDb;
//        View view;
//        String tmp = null;
//        Integer id;
//
//        public GetSetting(AppDatabase database, View view, Integer id) {
//            this.appDb = database;
//            this.view = view;
//            this.id = id;
//        }
//        @Override
//        protected Void doInBackground(Void... arg0) {
//            try {
//                 tmp = appDb.settingsDao().getValueBySettingId(id);
//            }catch (Exception e){
//                Log.d("TABLE_FRAGMENT", "something went wrong on GetSetting");
//            }
//            return null;
//        }
//        @Override
//        protected void onPostExecute(Void result) {
//            if (tmp != null){
//                moveToPoint(tmp);
//            }else {
//                Toast.makeText(view.getContext(),"Please long press on button first", Toast.LENGTH_LONG).show();
//            }
//
//        }
//    }
}