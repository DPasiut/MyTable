package com.example.mytable.ui.table;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.mytable.R;

public class TableFragment extends Fragment {

    private TableViewModel tableViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        tableViewModel =
                new ViewModelProvider(this).get(TableViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        return root;
    }
}