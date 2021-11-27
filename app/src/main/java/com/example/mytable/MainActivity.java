package com.example.mytable;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.mytable.service.bluetooth.BluetoothService;
import com.example.mytable.service.time.TimerService;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    Intent serviceIntent;
    Intent timerServiceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);

//        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
//                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
//                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
//        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
    }


    @Override
    protected void onStart() {
        super.onStart();
       // startService();
    }

    @Override
    protected void onStop() {
        super.onStop();
       // stopService();
    }

    private void startService() {
        serviceIntent = new Intent(this, BluetoothService.class);
        ContextCompat.startForegroundService(this, serviceIntent);

        Intent timerServiceIntent = new Intent(this, TimerService.class);
        ContextCompat.startForegroundService(this, timerServiceIntent);
    }
    private void stopService() {
        stopService(serviceIntent);

        Intent timerServiceIntent = new Intent(this, TimerService.class);
        stopService(timerServiceIntent);
    }

}