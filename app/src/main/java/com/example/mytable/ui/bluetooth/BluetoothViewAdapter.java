package com.example.mytable.ui.bluetooth;

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
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.example.mytable.R;
import com.example.mytable.service.bluetooth.BluetoothCommunicationState;
import com.example.mytable.service.bluetooth.BluetoothService;

import java.util.ArrayList;
import java.util.List;


public class BluetoothViewAdapter extends RecyclerView.Adapter<BluetoothViewAdapter.ViewHolder> {
    private final List<String> localDataSet;
    private BluetoothService bluetoothService;
    private boolean mBound = false;

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
            if(!bluetoothService.isBluetoothOn()){
                Toast.makeText(v.getContext(), "Bluetooth is OFF", Toast.LENGTH_LONG).show();
            }else {
                String name = this.textView.getText().toString();
                bluetoothService.connectDevice(bluetoothService.getDevice(name));
                ConnectWithBluetoothDevice c = new ConnectWithBluetoothDevice(v, name);
                c.execute();
            }

        }
    }

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

    @SuppressLint("StaticFieldLeak")
    private class ConnectWithBluetoothDevice extends AsyncTask<Void, Void, Void> {
        private static final int MAX_WAITING_TIME = 200;
        View view;
        String name;
        public ConnectWithBluetoothDevice(View view, String name) {
            this.view = view;
            this.name = name;
        }
        @Override
        protected Void doInBackground(Void... arg0) {
            try {
                boolean needWait = doNeedWait();
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
                    Toast.makeText(view.getContext(), "Connected with  " + name, Toast.LENGTH_LONG).show();
                    clear();
                    break;
                case DISCONNECTED:
                    Toast.makeText(view.getContext(), "Connection Failed ", Toast.LENGTH_LONG).show();
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
            Log.d("MAIN_ACTIVITY", "SERVICE CONNECTED");
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }

    };
}