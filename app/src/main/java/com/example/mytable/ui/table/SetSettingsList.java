package com.example.mytable.ui.table;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.view.View;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;


import java.util.Objects;

public class SetSettingsList extends AsyncTask<Void, Void, Void> {
    @SuppressLint("StaticFieldLeak")
    RecyclerView recyclerView;
    @SuppressLint("StaticFieldLeak")
    View view;

    public SetSettingsList(RecyclerView recyclerView, View view) {
        this.recyclerView = recyclerView;
        this.view = view;
    }
    @Override
    protected Void doInBackground(Void... arg0) {
        ((SettingRecycleViewAdapter) Objects.requireNonNull(recyclerView.getAdapter())).setData();
        Objects.requireNonNull(recyclerView.getAdapter()).notifyDataSetChanged();
        recyclerView.invalidate();
        return null;

    }
    @Override
    protected void onPostExecute(Void result) {
    }

}