package com.example.mytable.ui.table;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
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
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.mytable.R;
import com.example.mytable.service.time.TimerService;
import com.example.mytable.service.bluetooth.BluetoothCommunicationState;
import com.example.mytable.service.bluetooth.BluetoothService;

import antonkozyriatskyi.circularprogressindicator.CircularProgressIndicator;

import static com.example.mytable.PreferencesConstants.*;

public class TableFragment extends Fragment {

    protected BluetoothService bluetoothService;
    protected TimerService timerService;
    private boolean mBound = false;
    private boolean mBoundTimer = false;
    private TextView position;
    private int tableHigh = 0;
    private int count = 0;
    private Boolean canClick = true;
    private Integer currentPosition;
    private Integer currentTimeValue = 0;
    private Integer maxTimeValue = 0;

    private String firstPosition, secondPosition, thirdPosition;
    private CircularProgressIndicator progressBar;

    @SuppressLint("ClickableViewAccessibility")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_table, container, false);
        Button upButton = root.findViewById(R.id.up_btn);
        Button downButton = root.findViewById(R.id.down_btn);
        Button userButton1 = root.findViewById(R.id.user_1);
        Button userButton2 = root.findViewById(R.id.user_2);
        Button userButton3 = root.findViewById(R.id.user_3);
        Button startTimer = root.findViewById(R.id.start_button);
        Button pauseTimer = root.findViewById(R.id.pause_button);
        Button stopTimer = root.findViewById(R.id.stop_button);

        position = root.findViewById(R.id.position);
        progressBar = root.findViewById(R.id.time_progress_bar);


        progressBar.setOnTouchListener((v, event) -> {
            getTimeDialog();
            return false;
        });

        bindBluetoothService();
        bindTimerService();
        getPreferences();

        setButtonText(userButton1, firstPosition);
        setButtonText(userButton2, secondPosition);
        setButtonText(userButton3, thirdPosition);

        upButton.setOnTouchListener((v, event) -> {
            if (mBound) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (bluetoothService.getState() == BluetoothCommunicationState.CONNECTED) {
                            up();
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
        });

        downButton.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (bluetoothService.getState() == BluetoothCommunicationState.CONNECTED) {
                        down();
                    } else {
                        Toast.makeText(root.getContext(), "No Connection!", Toast.LENGTH_LONG).show();
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
            if (bluetoothService.getState() == BluetoothCommunicationState.CONNECTED) {
                canClick = false;
                new MoveToPoint(firstPosition).execute();
            } else {
                if (!canClick) {
                    Toast.makeText(root.getContext(), "Table is moving now", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(root.getContext(), "No Connection!", Toast.LENGTH_LONG).show();
                }
            }
        });

        userButton1.setOnLongClickListener(v -> {
            if (currentPosition != null) {
                firstPosition = currentPosition.toString();
                saveToPreferences(FIRST_POSITION, firstPosition);
                setButtonText(userButton1, firstPosition);
                return true;
            } else {
                Toast.makeText(root.getContext(), "Unknown position :(", Toast.LENGTH_LONG).show();
            }
            return false;
        });

        userButton2.setOnClickListener(v -> {
            if (bluetoothService.getState() == BluetoothCommunicationState.CONNECTED) {
                canClick = false;
                new MoveToPoint(secondPosition).execute();
            } else {
                if (!canClick) {
                    Toast.makeText(root.getContext(), "Table is moving now", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(root.getContext(), "No Connection!", Toast.LENGTH_LONG).show();
                }
            }
        });

        userButton2.setOnLongClickListener(v -> {
            if (currentPosition != null) {
                secondPosition = currentPosition.toString();
                saveToPreferences(SECOND_POSITION, secondPosition);
                setButtonText(userButton2, secondPosition);
                return true;
            } else {
                Toast.makeText(root.getContext(), "Unknown position :(", Toast.LENGTH_LONG).show();
            }
            return false;
        });

        userButton3.setOnClickListener(v -> {
            if (bluetoothService.getState() == BluetoothCommunicationState.CONNECTED && canClick) {
                canClick = false;
                new MoveToPoint(thirdPosition).execute();
            } else {
                if (!canClick) {
                    Toast.makeText(root.getContext(), "Table is moving now", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(root.getContext(), "No Connection!", Toast.LENGTH_LONG).show();
                }
            }
        });

        userButton3.setOnLongClickListener(v -> {
            if (currentPosition != null) {
                thirdPosition = currentPosition.toString();
                saveToPreferences(THIRD_POSITION, thirdPosition);
                setButtonText(userButton3, thirdPosition);
                return true;
            } else {
                Toast.makeText(root.getContext(), "Unknown position :(", Toast.LENGTH_LONG).show();
            }
            return false;
        });

        startTimer.setOnClickListener(v -> {
            if(currentTimeValue > 0){
                timerService.startTimer(currentTimeValue);
                setProgressColor();
            }
        });

        pauseTimer.setOnClickListener(v -> {
            saveToPreferences(CURRENT_TIMER_VALUE, currentTimeValue.toString());
            timerService.stopTimer();
            setProgressColor();
        });

        stopTimer.setOnClickListener(v -> {
            if (timerService.isTimerOn()){
                timerService.stopTimer();
            }
            resetTimerPreferences();
            progressBar.setProgress(0,0);
            setProgressColor();
        });
        return root;


    }

    private void resetTimerPreferences(){
        saveToPreferences(MAX_TIMER_VALUE, "0");
        saveToPreferences(CURRENT_TIMER_VALUE, "0");
    }
    private void setButtonText(Button button, String text) {
        String s = String.valueOf(MIN_TABLE_POSITION + Integer.parseInt(text) / 14);
        button.setText(s);
    }

    private void saveToPreferences(String key, String value) {
        SharedPreferences sharedPreferences = this.requireActivity().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sharedPreferences.edit();
        myEdit.putString(key, value);
        myEdit.apply();
    }

    private void getPreferences() {
        SharedPreferences preferences = this.requireActivity().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE);
        firstPosition = preferences.getString(FIRST_POSITION, DEFAULT_POSITION_VALUE);
        secondPosition = preferences.getString(SECOND_POSITION, DEFAULT_POSITION_VALUE);
        thirdPosition = preferences.getString(THIRD_POSITION, DEFAULT_POSITION_VALUE);
        maxTimeValue = Integer.valueOf(preferences.getString(MAX_TIMER_VALUE, "0"));
        currentTimeValue = Integer.valueOf(preferences.getString(CURRENT_TIMER_VALUE, "0"));
    }

    private void moveToPoint(String s) {
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

    private void getTimeDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Chose time in minutes");
        View view = LayoutInflater.from(getContext()).inflate(R.layout.get_time_dialog, (ViewGroup) getView(), false);
        NumberPicker hourPicker = (NumberPicker) view.findViewById(R.id.hour_picker);
        NumberPicker minutesPicker = (NumberPicker) view.findViewById(R.id.minutes_picker);
        hourPicker.setMaxValue(24);
        hourPicker.setMinValue(0);
        minutesPicker.setMinValue(0);
        minutesPicker.setMaxValue(60);
        builder.setView(view);

        builder.setPositiveButton("set", (dialog, which) -> {
            Integer timeInSeconds = timerService.getTimeInSeconds(hourPicker.getValue(), minutesPicker.getValue());
            Integer timeInMinutes = timerService.getTimeInMinutes(hourPicker.getValue(), minutesPicker.getValue());

            progressBar.setMaxProgress(timeInMinutes);
            progressBar.setCurrentProgress(timeInMinutes);
            maxTimeValue = timeInMinutes;
            currentTimeValue = timeInSeconds;

            saveToPreferences(MAX_TIMER_VALUE, timeInMinutes.toString());
            dialog.dismiss();
        });
        builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void bindTimerService(){
        Intent timerIntent = new Intent(getActivity(), TimerService.class);
        requireActivity().bindService(timerIntent, timerServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private void bindBluetoothService(){
        Intent timerIntent = new Intent(getActivity(), BluetoothService.class);
        requireActivity().bindService(timerIntent, bluetoothSerciceConnection, Context.BIND_AUTO_CREATE);
    }
    private final ServiceConnection bluetoothSerciceConnection = new ServiceConnection() {

        @SuppressLint("HandlerLeak")
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            BluetoothService.LocalBinder binder = (BluetoothService.LocalBinder) service;
            bluetoothService = binder.getService();
            bluetoothService.setBluetoothHandler(new Handler(Looper.myLooper()) {
                @SuppressLint("SetTextI18n")
                @Override
                public void handleMessage(Message msg) {
                    if (position != null) {
                        Bundle bundle = msg.getData();
                        String o = (String) bundle.get("message");
                        if (count == 10) {
                            int tmp = tableHigh / 10;
                            currentPosition = tmp;
                            position.setText(String.valueOf(MIN_TABLE_POSITION + tmp / 14));
                            tableHigh = 0;
                            count = 0;
                        }
                        if (o != null) {
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

    private final ServiceConnection timerServiceConnection = new ServiceConnection() {

        @SuppressLint("HandlerLeak")
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            TimerService.LocalBinder binder = (TimerService.LocalBinder) service;
            timerService = binder.getService();
            timerService.setTimerHandler(new Handler(Looper.myLooper()) {
                @SuppressLint("SetTextI18n")
                @Override
                public void handleMessage(Message msg) {
                    if (progressBar != null) {
                        Bundle bundle = msg.getData();
                        String o = (String) bundle.get("timer");

                        //aktualna wartość zapamiętywana w sekundach
                        currentTimeValue = Integer.parseInt(o);

                        //progres ustawiany w minutach
                        Integer timer = parseToMinutes(Integer.parseInt(o));
                        progressBar.setProgress(timer, maxTimeValue);
                    }
                }
            });
            setProgressColor();
            mBoundTimer = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBoundTimer = false;
            timerService.setTimerHandler(null);
        }
    };

    private Integer parseToMinutes(Integer time){
        return time/60;
    }

    @SuppressLint("StaticFieldLeak")
    private class MoveToPoint extends AsyncTask<Void, Void, Void> {
        String destinationPosition;

        public MoveToPoint(String destinationPosition) {
            this.destinationPosition = destinationPosition;
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            try {
                moveToPoint(destinationPosition);
                boolean needWait = doesMotorWorking();
                while (needWait) {
                    Thread.sleep(100);
                    needWait = doesMotorWorking();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            canClick = true;
        }

        private boolean doesMotorWorking() {
            return Math.abs(Integer.parseInt(destinationPosition) - currentPosition) > 14;
        }
    }


    private void setProgressColor(){
        if(timerService.isTimerOn()){
            progressBar.setTextColor(Color.parseColor(TimerService.getPlayProgressColor()));
            progressBar.setProgressColor(Color.parseColor(TimerService.getPlayProgressColor()));
        }else {
            progressBar.setTextColor(Color.parseColor(TimerService.getDefaultProgressColor()));
            progressBar.setProgressColor(Color.parseColor(TimerService.getDefaultProgressColor()));
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        getPreferences();
        progressBar.setProgress(currentTimeValue/60,maxTimeValue);
    }

    @Override
    public void onPause() {
        super.onPause();
        saveToPreferences(CURRENT_TIMER_VALUE, currentTimeValue.toString());
    }
}