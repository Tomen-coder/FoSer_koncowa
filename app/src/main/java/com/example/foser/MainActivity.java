package com.example.foser;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;



public class MainActivity extends AppCompatActivity {

    private Button buttonStart, buttonStop, buttonRestart;
    private TextView textInfoService, textInfoSettings;
    private String message, time_step;
    private Boolean show_time, work, work_double, dont_reset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonStart = (Button)findViewById(R.id.buttonStart);
        buttonStop = (Button)findViewById(R.id.buttonStop);
        buttonRestart = (Button)findViewById(R.id.buttonRestart);
        textInfoService = (TextView)findViewById(R.id.textInfoServiceState);
        textInfoSettings = (TextView) findViewById(R.id.textInfoSettings);

        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickStart(v);
            }
        });
        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickStop(v);
            }
        });
        buttonRestart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickRestart(v);
            }
        });

        updateUI();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.itemSettings:
                startActivity(new Intent(
                        this,
                        SettingsActivity.class
                ));
                return true;
            case R.id.itemExit:
                finishAndRemoveTask();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void clickStart(View view) {
        getPreferences();

        Intent startIntent = new Intent(this,MyForegroundService.class);
        startIntent.putExtra(MyForegroundService.MESSAGE,message);
        startIntent.putExtra(MyForegroundService.TIME,show_time);
        startIntent.putExtra(MyForegroundService.WORK,work);
        startIntent.putExtra(MyForegroundService.WORK_DOUBLE,work_double);
        startIntent.putExtra(MyForegroundService.TIME_STEP, time_step);
        startIntent.putExtra(MyForegroundService.DONT_RESET, dont_reset);

        ContextCompat.startForegroundService(this, startIntent);
        updateUI();
    }

    public void clickStop(View view) {
        Intent stopIntent = new Intent(this, MyForegroundService.class);
        stopService(stopIntent);
        updateUI();
    }

    public void clickRestart(View view) {
        clickStop(view);
        clickStart(view);
    }

    private String getPreferences(){

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        message = sharedPreferences.getString("message","ForSer");
        show_time = sharedPreferences.getBoolean("show_time", true);
        work = sharedPreferences.getBoolean("sync",true);
        work_double = sharedPreferences.getBoolean("double", false);
        time_step = sharedPreferences.getString("time_step", "2s");
        dont_reset = sharedPreferences.getBoolean("dont_reset", false);

        return "Message: " + message + "\n"
                +"Show_time: " + show_time.toString() +"\n"
                +"Work: " + work.toString() + "\n"
                +"Double: " + work_double.toString() + "\n"
                +"Time step: " + time_step + "\n"
                +"Dont reset: " + dont_reset.toString();
    }

    private void updateUI(){
        if(isMyForegroundServiceRunning()){
            buttonStart.setEnabled(false);
            buttonStop.setEnabled(true);
            buttonRestart.setEnabled(true);
            textInfoService.setText(getString(R.string.info_service_running));
        }
        else {
            buttonStart.setEnabled(true);
            buttonStop.setEnabled(false);
            buttonRestart.setEnabled(false);
            textInfoService.setText(getString(R.string.info_service_not_running));
        }

        textInfoSettings.setText(getPreferences());
    }

    @SuppressWarnings("deprecation")
    private boolean isMyForegroundServiceRunning(){

        String myServiceName = MyForegroundService.class.getName();
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

        for(ActivityManager.RunningServiceInfo runningService : activityManager.getRunningServices(Integer.MAX_VALUE)) {
            String runningServiceName = runningService.service.getClassName();
            if(runningServiceName.equals(myServiceName)){
                return true;
            }
        }
        return false;
    }
}