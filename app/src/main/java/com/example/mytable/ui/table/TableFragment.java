package com.example.mytable.ui.table;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mytable.R;
import com.example.mytable.database.Setting;
import com.example.mytable.database.SettingsRepository;
import com.example.mytable.service.bluetooth.BluetoothService;
import com.example.mytable.service.bluetooth.BluetoothViewAdapter;
import com.example.mytable.service.bluetooth.SetBluetoothDevicesList;
import com.example.mytable.service.time.TimerService;

import java.util.Objects;

import antonkozyriatskyi.circularprogressindicator.CircularProgressIndicator;

import static com.example.mytable.PreferencesConstants.CURRENT_TIMER_VALUE;
import static com.example.mytable.PreferencesConstants.DEFAULT_PROGRESS_COLOR;
import static com.example.mytable.PreferencesConstants.MAX_TABLE_POSITION_CM;
import static com.example.mytable.PreferencesConstants.MAX_TIMER_VALUE;
import static com.example.mytable.PreferencesConstants.MIN_TABLE_POSITION_CM;
import static com.example.mytable.PreferencesConstants.PLAY_PROGRESS_COLOR;

public class TableFragment extends Fragment {


    private BluetoothService bluetoothService;
    private TimerService timerService;
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
    private String connectedDevice = null;
    private CircularProgressIndicator progressBar;
    private boolean mShouldUnbind;
    private SettingRecycleViewAdapter settingRecycleViewAdapter;
    private SettingsRepository settingsRepository;

    Button startTimer;
    Button stopTimer;
    Button addSetting;
    ImageButton bluetoothButton;
    TextView btName;
    RecyclerView settingsRecyclerView;


    @SuppressLint("ClickableViewAccessibility")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_table, container, false);
        Button upButton = root.findViewById(R.id.up_btn);
        Button downButton = root.findViewById(R.id.down_btn);
        startTimer = root.findViewById(R.id.start_button);
        stopTimer = root.findViewById(R.id.stop_button);
        bluetoothButton = root.findViewById(R.id.bluetooth_button);
        btName = root.findViewById(R.id.bt_name_text);
        addSetting = root.findViewById(R.id.add_setting_btn);

        position = root.findViewById(R.id.position);
        progressBar = root.findViewById(R.id.time_progress_bar);


        addSetting.setOnClickListener(v -> showAddSettingDialog());
        progressBar.setOnTouchListener((v, event) -> {
            showSetTimeDialog();
            return false;
        });

        bindBluetoothService();
        bindTimerService();
        getPreferences();

        bluetoothButton.setOnTouchListener((v, event) -> onBluetoothButtonTouch(inflater, container));

        upButton.setOnTouchListener((v, event) -> onUpButtonTouch(root, event));
        downButton.setOnTouchListener((v, event) -> onDownButtonTouch(root, event));

        startTimer.setOnClickListener(v -> onStartTimer());
        stopTimer.setOnClickListener(v -> onStopTimer());

        initRecyclerView(root);
        return root;
    }


    private boolean onBluetoothButtonTouch(@NonNull LayoutInflater inflater, ViewGroup container) {
        if (!isDialogDisplayed) {
            if (bluetoothService.isBluetoothEnabled()) {
                showDevicesListDialog(inflater, container);
            } else {
                showEnableBluetoothDialog();
            }
            isDialogDisplayed = true;
            return false;
        }
        return false;
    }

    private boolean onDownButtonTouch(View root, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (bluetoothService.isBluetoothConnected()) {
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
    }

    private boolean onUpButtonTouch(View root, MotionEvent event) {
        if (mBound) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (bluetoothService.isBluetoothConnected()) {
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
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void onStartTimer() {
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
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void onStopTimer() {
        timerService.stopTimer();
        timeLeft = 0;
        resetTimerPreferences();
        progressBar.setProgress(0, 0);
        setProgressColor();
        startTimer.setBackground(requireContext().getDrawable(R.drawable.play_circle));
        timerService.cancelAlarm();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void setBluetoothButtonColor() {
        if (bluetoothService.isBluetoothEnabled()) {
            if (bluetoothService.isBluetoothConnected()) {
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

    private void saveToPreferences(String key, String value) {
        SharedPreferences sharedPreferences = this.requireActivity().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sharedPreferences.edit();
        myEdit.putString(key, value);
        myEdit.apply();
    }

    private void getPreferences() {
        SharedPreferences preferences = this.requireActivity().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE);
        timeMaxValue = Integer.valueOf(preferences.getString(MAX_TIMER_VALUE, "0"));
        timeLeft = Integer.valueOf(preferences.getString(CURRENT_TIMER_VALUE, "0"));
        connectedDevice = preferences.getString("connectedDevice", "");
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

    private void initRecyclerView(View view) {
        settingRecycleViewAdapter = new SettingRecycleViewAdapter(requireActivity());
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        settingsRecyclerView = view.findViewById(R.id.settings_recycler_view);
        settingsRecyclerView.setHasFixedSize(true);
        settingsRecyclerView.setLayoutManager(layoutManager);
        settingsRecyclerView.setAdapter(settingRecycleViewAdapter);
        SetSettingsList setSettingsList = new SetSettingsList(settingsRecyclerView, view);
        setSettingsList.execute();
    }

    private boolean showDevicesListDialog(LayoutInflater inflater, ViewGroup container) {
        BluetoothViewAdapter bluetoothViewAdapter = new BluetoothViewAdapter(getActivity());
        View devicesView = inflater.inflate(R.layout.layout_paired_devices_dialog, container, false);

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
            isDialogDisplayed = false;
            dialog.cancel();
        });

        builder.setNeutralButton("Disable BT", (dialog, which) -> {
            bluetoothService.disableBluetooth();
            isDialogDisplayed = false;
            saveToPreferences("connectedDevice", "");
            dialog.cancel();
        });
        if (bluetoothService.isBluetoothConnected()) {
            builder.setNeutralButton("Disconnect", (dialog, which) -> {
                isDialogDisplayed = false;
                bluetoothService.disconnect();
                saveToPreferences("connectedDevice", "");
                dialog.cancel();
            });
        }
        builder.show();

        return true;
    }

    private void showEnableBluetoothDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Enable Bluetooth?");
        builder.setNegativeButton("NO", (dialog, which) -> {
            isDialogDisplayed = false;
            dialog.cancel();
        });
        builder.setPositiveButton("Yes", (dialog, which) -> {
            isDialogDisplayed = false;
            bluetoothService.enableBluetooth();
            dialog.cancel();
        });
        builder.show();
    }

    @SuppressLint("SetTextI18n")
    private void showAddSettingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Add setting");
        View view = LayoutInflater.from(getContext()).inflate(R.layout.layout_add_setting_dialog, (ViewGroup) getView(), false);
        EditText description = view.findViewById(R.id.setting_description);
        EditText valueInput = view.findViewById(R.id.value_input);
        Button setCurrent = view.findViewById(R.id.set_curr_poss_btn);
        Button setValue = view.findViewById(R.id.set_value_btn);
        TextView textView = view.findViewById(R.id.position_setting_value);

        setCurrent.setOnClickListener(v -> textView.setText(position.getText().toString()));
        setValue.setOnClickListener(v -> {
            if (valueInput.getText().length() > 0) {
                int value = Integer.parseInt(String.valueOf(valueInput.getText()));
                if (value >= MIN_TABLE_POSITION_CM && value <= MAX_TABLE_POSITION_CM) {
                    textView.setText(valueInput.getText());
                }
                if (value > MAX_TABLE_POSITION_CM) {
                    textView.setText(String.valueOf(MAX_TABLE_POSITION_CM));
                }
                if (value < MIN_TABLE_POSITION_CM) {
                    textView.setText(String.valueOf(MIN_TABLE_POSITION_CM));
                }
            }
        });

        builder.setView(view);

        builder.setPositiveButton("add", (dialog, which) -> {
            saveNewSetting(description.getText().toString(), textView.getText().toString());
            settingRecycleViewAdapter.setData();
            Objects.requireNonNull(settingsRecyclerView.getAdapter()).notifyDataSetChanged();
            settingsRecyclerView.invalidate();
            dialog.dismiss();
        });

        builder.setNegativeButton("cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void saveNewSetting(String description, String value) {
        Setting setting = new Setting(description, value);
        settingsRepository = new SettingsRepository(requireActivity().getApplication());
        settingsRepository.insert(setting);
    }

    private void showSetTimeDialog() {
        if (!isTimeDialogDisplayed) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Chose time in minutes");
            View view = LayoutInflater.from(getContext()).inflate(R.layout.layout_time_dialog, (ViewGroup) getView(), false);
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
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void showTimesUpDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Times's Up. Would you like repeat interval?");
        builder.setNegativeButton("NO", (dialog, which) -> {
            startTimer.setBackground(requireContext().getDrawable(R.drawable.play_circle));
            timerService.cancelAlarm();
            isTimeDialogDisplayed = false;
            dialog.cancel();
        });
        builder.setPositiveButton("Yes", (dialog, which) -> {
            isTimeDialogDisplayed = false;
            timerService.cancelAlarm();
            progressBar.setProgress(timeMaxValue, timeMaxValue);
            timerService.startTimer(timerService.convertTimeToSeconds(0, timeMaxValue, 0), getContext());
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
        Intent intent = new Intent(requireContext(), TimerService.class);
        requireActivity().bindService(intent, timerServiceConnection, Context.BIND_AUTO_CREATE);
    }

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

    private final ServiceConnection bluetoothServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            BluetoothService.LocalBinder binder = (BluetoothService.LocalBinder) service;
            bluetoothService = binder.getService();
            setBluetoothButtonColor();

            bluetoothService.setBluetoothHandler(new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    if (position != null) {
                        Bundle bundle = msg.getData();
                        String o = (String) bundle.get("message");
                        if (count == 10) {
                            int tmp = tableHigh / 10;
                            currentPosition = tmp;
                            position.setText(String.valueOf(MIN_TABLE_POSITION_CM + tmp / 14));
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
                            btName.setText(bluetoothService.getConnectedDevice());
                            break;
                        case "DISCONNECTED":
                            if (bluetoothService.isBluetoothEnabled()) {
                                bluetoothButton.setBackground(requireContext().getDrawable(R.drawable.bluetooth_on));
                            } else {
                                bluetoothButton.setBackground(requireContext().getDrawable(R.drawable.bluetooth_off));
                            }
                            btName.setText("");
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
            bluetoothService.setStateHandler(null);
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
                        if (timeLeft < 1) {
                            showTimesUpDialog();
                            isTimeDialogDisplayed = true;
                            startTimer.setBackground(requireContext().getDrawable(R.drawable.play_circle));
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
        btName.setText(connectedDevice);
    }

    @Override
    public void onPause() {
        super.onPause();
        saveToPreferences(CURRENT_TIMER_VALUE, timeLeft.toString());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbindService();
        saveToPreferences("connectedDevice", "");
        connectedDevice = "";
    }
}