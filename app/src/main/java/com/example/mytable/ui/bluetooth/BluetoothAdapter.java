package com.example.mytable.ui.bluetooth;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.example.mytable.R;
import com.example.mytable.service.bluetooth.BluetoothService;

import java.util.List;


public class BluetoothAdapter extends RecyclerView.Adapter<BluetoothAdapter.ViewHolder> {

    private final BluetoothService bluetoothService = new BluetoothService();
    private List<String> localDataSet;

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
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
            Toast.makeText(v.getContext(), "position : " + getLayoutPosition() + " text : " + this.textView.getText(), Toast.LENGTH_SHORT).show();
        }
    }

    public BluetoothAdapter(List<String> dataSet) {
        localDataSet = dataSet;
    }

    public BluetoothAdapter() {
        localDataSet = bluetoothService.getBluetoothDevices();
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
}