package com.example.mytable.ui.table;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mytable.R;
import com.example.mytable.database.Setting;
import com.example.mytable.database.SettingsRepository;
import com.example.mytable.service.bluetooth.BluetoothService;

import java.util.ArrayList;
import java.util.List;

public class SettingRecycleViewAdapter extends RecyclerView.Adapter<SettingRecycleViewAdapter.ViewHolder> {

    private static final String TAG = "SettingRecycleViewAdapter";

    protected BluetoothService bluetoothService;
    private SettingsRepository repository;
    private boolean mBound = false;
    private List<Setting> settings;
    private Context context;
    private View view;

    public SettingRecycleViewAdapter(Activity activity) {
        settings = new ArrayList<>();
        repository = new SettingsRepository(activity.getApplication());
        bindBluetoothService(activity);
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView description;
        TextView value;
        Button removeButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            description = itemView.findViewById(R.id.setting_name);
            value = itemView.findViewById(R.id.setting_value);
            removeButton = itemView.findViewById(R.id.remove_setting_btn);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (bluetoothService.isCanClick()) {
                bluetoothService.moveToPoint(value.getText().toString());
            }
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "SettingRecycleViewAdapter: called");
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_setting_item, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.description.setText(settings.get(position).description);
        holder.value.setText(settings.get(position).value);
        holder.removeButton.setOnClickListener(v -> {
            repository.delete(settings.get(position).id);
            settings.remove(position);
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return settings.size();
    }

    public void setData() {
        new SetData().execute();
    }

    private void bindBluetoothService(Activity activity) {
        Intent intent = new Intent(activity, BluetoothService.class);
        activity.bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    private final ServiceConnection connection = new ServiceConnection() {

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

    @SuppressLint("StaticFieldLeak")
    private class SetData extends AsyncTask<Void, Void, Void> {
        public SetData() {
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            settings.clear();
            settings = repository.getAllSettings();
            notifyDataSetChanged();
            return null;
        }
    }
}
