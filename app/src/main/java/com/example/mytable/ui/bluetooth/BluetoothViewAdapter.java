package com.example.mytable.ui.bluetooth;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
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
    private final BluetoothService bluetoothService;

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
            String name = this.textView.getText().toString();
            new ConnectWithBluetoothDevice(v, name).execute();
           // bluetoothService.connectDevice(bluetoothService.getDevice(this.textView.getText().toString()));
        }
    }

    public BluetoothViewAdapter(BluetoothService bluetoothService) {
        this.bluetoothService = bluetoothService;
        localDataSet = new ArrayList<>();
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
        localDataSet.addAll(bluetoothService.getBluetoothDevices());
        notifyItemRangeInserted(0, localDataSet.size());
    }

    @SuppressLint("StaticFieldLeak")
    private class ConnectWithBluetoothDevice extends AsyncTask<Void, Void, Void> {
        View view;
        String name;
        public ConnectWithBluetoothDevice(View view, String name) {
            this.view = view;
            this.name = name;

        }
        @Override
        protected Void doInBackground(Void... arg0) {
            try {
                bluetoothService.connectDevice(bluetoothService.getDevice(name));
                boolean needWait = bluetoothService.getState() == BluetoothCommunicationState.DISCONNECTED;
                int waitingTime = 0;
                while (needWait && waitingTime <= 200){
                    Thread.sleep(10);
                    waitingTime += 1;
                    needWait = bluetoothService.getState() == BluetoothCommunicationState.DISCONNECTED;
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
                    break;
                case DISCONNECTED:
                    Toast.makeText(view.getContext(), "Connection Failed ", Toast.LENGTH_LONG).show();
                    break;
            }
        }

    }
}