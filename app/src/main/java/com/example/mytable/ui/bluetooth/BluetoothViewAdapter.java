package com.example.mytable.ui.bluetooth;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.example.mytable.R;
import com.example.mytable.service.bluetooth.BluetoothCommunicationState;
import com.example.mytable.service.bluetooth.BluetoothService;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;


public class BluetoothViewAdapter extends RecyclerView.Adapter<BluetoothViewAdapter.ViewHolder> {
    private final List<String> localDataSet;
    private BluetoothService bluetoothService;
    private boolean mBound = false;
    private String name;
    private Context context;
    private View view;

    public BluetoothViewAdapter(Activity activity) {
        localDataSet = new ArrayList<>();
        Intent intent = new Intent(activity, BluetoothService.class);
        activity.bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private final TextView textView;

        public ViewHolder(View view) {
            super(view);
            textView = (TextView) view.findViewById(R.id.bt_device_name);
            itemView.setOnClickListener(this);
        }

        public TextView getTextView() {
            return textView;
        }

        @Override
        public void onClick(View v) {
            if(!bluetoothService.isBluetoothEnabled()){
                Toast.makeText(v.getContext(), "Bluetooth is OFF", Toast.LENGTH_SHORT).show();
            }else {
                name = this.textView.getText().toString();
                context = v.getContext();
                view = v;
                connect(name, v);
//                SharedPreferences preferences = v.getContext().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE);
//                String deviceName = preferences.getString("deviceName", "");
//
//                if(!name.equals(deviceName)){
//                    AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
//                    builder.setMessage("Do you want save " + name.toUpperCase() +" as default device for automatic connection?").setPositiveButton("Yes", dialogClickListener)
//                            .setNegativeButton("No", dialogClickListener).show();
//                }else {
//                    connect(name, v);
//                }
            }
        }
    }

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.bluetooth_item, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        viewHolder.getTextView().setText(localDataSet.get(position));
    }

    @Override
    public int getItemCount() {
        return localDataSet.size();
    }

    public void clear(){
        localDataSet.clear();
        notifyDataSetChanged();
    }

    public void setData(){
        localDataSet.clear();
        localDataSet.addAll(bluetoothService.getBluetoothDevices());
        notifyItemRangeInserted(0, localDataSet.size());
    }

//    DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
//        switch (which){
//            case DialogInterface.BUTTON_POSITIVE:
//                connect(name, view);
//                saveDeviceName("deviceName", name, context);
//                break;
//
//            case DialogInterface.BUTTON_NEGATIVE:
//                connect(name, view);
//                break;
//        }
//    };

//    private void saveDeviceName(String key, String value, Context context) {
//        SharedPreferences sharedPreferences = context.getSharedPreferences("UserPreferences", Context.MODE_PRIVATE);
//        SharedPreferences.Editor myEdit = sharedPreferences.edit();
//        myEdit.putString(key, value);
//        myEdit.apply();
//    }
    private void connect(String name, View view){
        new ConnectWithBluetoothDevice(view, name).execute();
    }


    private class ConnectWithBluetoothDevice extends AsyncTask<Void, Void, Void> {
        View view;
        String name;
        public ConnectWithBluetoothDevice(View view, String name) {
            this.view = view;
            this.name = name;
        }
        @Override
        protected Void doInBackground(Void... arg0) {
            bluetoothService.connectDevice(bluetoothService.getDevice(name));
            try {
                boolean needWait = doNeedWait();
                int MAX_WAITING_TIME = 100;
                int waitingTime = 0;
                while (needWait && waitingTime < MAX_WAITING_TIME){
                    Thread.sleep(10);
                    waitingTime += 1;
                    needWait = doNeedWait();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            BluetoothCommunicationState state = bluetoothService.getState();
            switch (state){
                case CONNECTED:
                    Toast.makeText(view.getContext(), "Connected with  " + name, Toast.LENGTH_SHORT).show();
//                    clear();
                    break;
                case DISCONNECTED:
                    Toast.makeText(view.getContext(), "Connection Failed ", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
        private boolean doNeedWait(){
            return bluetoothService.getState() != BluetoothCommunicationState.CONNECTED;
        }
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
}