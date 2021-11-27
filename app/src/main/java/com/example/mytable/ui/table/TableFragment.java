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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mytable.R;
import com.example.mytable.service.bluetooth.BluetoothCommunicationState;
import com.example.mytable.service.bluetooth.BluetoothService;
import com.example.mytable.service.time.TimerService;
import com.example.mytable.ui.bluetooth.BluetoothViewAdapter;
import com.example.mytable.ui.bluetooth.SetBluetoothDevicesList;

import antonkozyriatskyi.circularprogressindicator.CircularProgressIndicator;

import static com.example.mytable.PreferencesConstants.CURRENT_TIMER_VALUE;
import static com.example.mytable.PreferencesConstants.DEFAULT_POSITION_VALUE;
import static com.example.mytable.PreferencesConstants.DEFAULT_PROGRESS_COLOR;
import static com.example.mytable.PreferencesConstants.FIRST_POSITION;
import static com.example.mytable.PreferencesConstants.MAX_TIMER_VALUE;
import static com.example.mytable.PreferencesConstants.MIN_TABLE_POSITION;
import static com.example.mytable.PreferencesConstants.PLAY_PROGRESS_COLOR;
import static com.example.mytable.PreferencesConstants.SECOND_POSITION;
import static com.example.mytable.PreferencesConstants.THIRD_POSITION;

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
    private Integer timeLeft = 0;
    private Integer timeMaxValue = 0;
    private boolean isDialogDisplayed = false;
    private boolean isTimeDialogDisplayed = false;

    private String firstPosition, secondPosition, thirdPosition;
    private CircularProgressIndicator progressBar;

    private BluetoothViewAdapter bluetoothViewAdapter;

    Button startTimer;
    Button stopTimer;
    ImageButton bluetoothButton;


    @SuppressLint({"ClickableViewAccessibility", "UseCompatLoadingForDrawables"})
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_table, container, false);

        Button upButton = root.findViewById(R.id.up_btn);
        Button downButton = root.findViewById(R.id.down_btn);
        Button userButton1 = root.findViewById(R.id.user_1);
        Button userButton2 = root.findViewById(R.id.user_2);
        Button userButton3 = root.findViewById(R.id.user_3);
        startTimer = root.findViewById(R.id.start_button);
        stopTimer = root.findViewById(R.id.stop_button);
        bluetoothButton = root.findViewById(R.id.bluetooth_button);

        position = root.findViewById(R.id.position);
        progressBar = root.findViewById(R.id.time_progress_bar);


        progressBar.setOnTouchListener((v, event) -> {
            showSetTimeDialog();
            return false;
        });

        bindBluetoothService();
        bindTimerService();
        getPreferences();

        bluetoothButton.setOnTouchListener((v, event) -> {
            if(!isDialogDisplayed){
                if(bluetoothService.isBluetoothOn()){
                    showDevicesListDialog(inflater, container);
                }
                else {
                    showEnableBluetoothDialog();
                }
                isDialogDisplayed = true;
                return false;
            }
            return false;
        });

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
            boolean isTimerOn = timerService.isTimerOn();
            if (timeLeft > 0 && !isTimerOn) {
                timerService.startTimer(timeLeft, getContext());
                setProgressColor();
                startTimer.setBackground(requireContext().getDrawable(R.drawable.pause_circle));
            }
            if (isTimerOn) {
                saveToPreferences(CURRENT_TIMER_VALUE, timeLeft.toString());
                timerService.stopTimer();
                startTimer.setBackground(requireContext().getDrawable(R.drawable.play_circle));
                setProgressColor();
            }
        });

        stopTimer.setOnClickListener(v -> {
            timerService.stopTimer();
            timeLeft = 0;
            resetTimerPreferences();
            progressBar.setProgress(0, 0);
            setProgressColor();
            startTimer.setBackground(requireContext().getDrawable(R.drawable.play_circle));
            timerService.cancelAlarm();
        });

        return root;


    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void setBluetoothButtonColor() {
        if (bluetoothService.isBluetoothOn()) {
            if (bluetoothService.getState() == BluetoothCommunicationState.CONNECTED) {
                bluetoothButton.setBackground(requireContext().getDrawable(R.drawable.bluetooth_connected));
            } else {
                bluetoothButton.setBackground(requireContext().getDrawable(R.drawable.bluetooth_on));
            }
        } else {
            bluetoothButton.setBackground(requireContext().getDrawable(R.drawable.bluetooth_off));
        }
    }

    private void resetTimerPreferences() {
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
        timeMaxValue = Integer.valueOf(preferences.getString(MAX_TIMER_VALUE, "0"));
        timeLeft = Integer.valueOf(preferences.getString(CURRENT_TIMER_VALUE, "0"));
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

    private void showDevicesListDialog(LayoutInflater inflater, ViewGroup container){
        bluetoothViewAdapter = new BluetoothViewAdapter(getActivity());
        View devicesView = inflater.inflate(R.layout.get_devices_dialog, container, false);

        RecyclerView recyclerView = devicesView.findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(bluetoothViewAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(devicesView.getContext()));
        SetBluetoothDevicesList setBluetoothDevicesList = new SetBluetoothDevicesList(recyclerView, devicesView, bluetoothService);
        setBluetoothDevicesList.execute();

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Select device");
        builder.setView(devicesView);


        builder.setNegativeButton("Back", (dialog, which) -> {
            isDialogDisplayed=false;
            dialog.cancel();
        });

        builder.setNeutralButton("Disable BT", (dialog, which) -> {
            bluetoothService.disableBluetooth();
            isDialogDisplayed=false;
            dialog.cancel();
        });
        if (bluetoothService.getState() == BluetoothCommunicationState.CONNECTED) {
            builder.setNeutralButton("Disconnect", (dialog, which) -> {
                isDialogDisplayed=false;
                bluetoothService.disconnect();
                dialog.cancel();
            });
        }

        builder.show();
    }

    private void showEnableBluetoothDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Enable Bluetooth?");
        builder.setNegativeButton("NO", (dialog, which) -> {
            isDialogDisplayed=false;
            dialog.cancel();
        });
        builder.setPositiveButton("Yes", (dialog, which) -> {
            isDialogDisplayed=false;
            bluetoothService.enableBluetooth();
            dialog.cancel();
        });
        builder.show();
    }

    private void showSetTimeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Chose time in minutes");
        View view = LayoutInflater.from(getContext()).inflate(R.layout.get_time_dialog, (ViewGroup) getView(), false);
        NumberPicker hourPicker = view.findViewById(R.id.hour_picker);
        NumberPicker minutesPicker = view.findViewById(R.id.minutes_picker);
        hourPicker.setMaxValue(24);
        hourPicker.setMinValue(0);
        minutesPicker.setMinValue(0);
        minutesPicker.setMaxValue(60);
        builder.setView(view);

        builder.setPositiveButton("set", (dialog, which) -> {
            Integer timeInSeconds = timerService.convertTimeToSeconds(hourPicker.getValue(), minutesPicker.getValue(), 0);
            Integer timeInMinutes = timerService.convertTimeToMinutes(hourPicker.getValue(), minutesPicker.getValue(), 0);

            progressBar.setMaxProgress(timeInMinutes);
            progressBar.setCurrentProgress(timeInMinutes);
            timeMaxValue = timeInMinutes;
            timeLeft = timeInSeconds;

            saveToPreferences(MAX_TIMER_VALUE, timeInMinutes.toString());
            dialog.dismiss();
        });
        builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.cancel());
        builder.show();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void showTimesUpDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Times's Up. Would you like repeat interval?");
        builder.setNegativeButton("NO", (dialog, which) -> {
            startTimer.setBackground(requireContext().getDrawable(R.drawable.play_circle));
            timerService.cancelAlarm();
            isTimeDialogDisplayed=false;
            dialog.cancel();
        });
        builder.setPositiveButton("Yes", (dialog, which) -> {
            isTimeDialogDisplayed=false;
            timerService.cancelAlarm();
            progressBar.setProgress(timeMaxValue, timeMaxValue);
            timerService.startTimer(timerService.convertTimeToSeconds(0,timeMaxValue,0),getContext());
            dialog.cancel();
        });
        builder.setNeutralButton("stop alarm", (dialog, which) -> {
            timerService.cancelAlarm();
            dialog.cancel();
            isTimeDialogDisplayed = false;
        });
        builder.show();
    }

    private void bindTimerService() {
        Intent intent = new Intent(getActivity(), TimerService.class);
        requireActivity().bindService(intent, timerServiceConnection, Context.BIND_AUTO_CREATE);
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
            setBluetoothButtonColor();

            bluetoothService.setBluetoothHandler(new Handler(Looper.myLooper()) {
                @SuppressLint("SetTextI18n")
                @Override
                public void handleMessage(Message msg) {
                    if (position != null) {
                        Log.d("[TABLE FRAGMENT]", " BLUETOOTH HANDLE MESSAGE");
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

            bluetoothService.setStateHandler(new Handler(Looper.myLooper()) {
                @SuppressLint("UseCompatLoadingForDrawables")
                @Override
                public void handleMessage(Message msg) {
                    Bundle bundle = msg.getData();
                    String connectionState = (String) bundle.get("state");

                    switch (connectionState) {
                        case "CONNECTED":
                            bluetoothButton.setBackground(requireContext().getDrawable(R.drawable.bluetooth_connected));
                            break;
                        case "DISCONNECTED":
                            if (bluetoothService.isBluetoothOn()) {
                                bluetoothButton.setBackground(requireContext().getDrawable(R.drawable.bluetooth_on));
                            } else {
                                bluetoothButton.setBackground(requireContext().getDrawable(R.drawable.bluetooth_off));
                            }
                            break;
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
                        Log.d("Timer Service", "Update ProgressBar");
                        //aktualna wartość zapamiętywana w sekundach
                        timeLeft = Integer.parseInt(o);
                        timerService.setTimerProgress(progressBar, timeLeft, timeMaxValue);
                        if(timeLeft < 1){
                            showTimesUpDialog();
                            isTimeDialogDisplayed = true;
                        }
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

    private void setProgressColor() {
        if (timerService.isTimerOn()) {
            progressBar.setTextColor(Color.parseColor(PLAY_PROGRESS_COLOR));
            progressBar.setProgressColor(Color.parseColor(PLAY_PROGRESS_COLOR));
        } else {
            progressBar.setTextColor(Color.parseColor(DEFAULT_PROGRESS_COLOR));
            progressBar.setProgressColor(Color.parseColor(DEFAULT_PROGRESS_COLOR));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferences();
        progressBar.setProgress(timeLeft / 60, timeMaxValue);
    }

    @Override
    public void onPause() {
        super.onPause();
        saveToPreferences(CURRENT_TIMER_VALUE, timeLeft.toString());
    }
}