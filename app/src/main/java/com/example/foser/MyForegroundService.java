package com.example.foser;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.preference.PreferenceManager;

import java.util.Timer;
import java.util.TimerTask;

public class MyForegroundService extends Service {

    public static final String CHANNEL_ID = "MyForegroundServiceChannel";
    public static final String CHANNEL_NAME = "FoSer service channel";

    public static final String MESSAGE = "message";
    public static final String TIME = "time";
    public static final String WORK = "work";
    public static final String WORK_DOUBLE = "work_double";
    public static final String TIME_STEP = "time_step";
    public static final String DONT_RESET = "dont_reset";

    private String message, periods_time;
    private Boolean show_time, do_work, double_speed, dont_reset;

    private Context ctx;
    private Intent notificationIntent;
    private PendingIntent pendingIntent;

    private int counter;
    private Timer timer;
    private TimerTask timerTask;
    final Handler handler = new Handler();
    private long time_period = 2000;

    SharedPreferences sharedPreferences;

    @Override
    public void onCreate() {
        super.onCreate();
        ctx = this;
        notificationIntent = new Intent(ctx, MainActivity.class);
        pendingIntent = PendingIntent.getActivity(this,0,notificationIntent,0);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        timer = new Timer();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        message = intent.getStringExtra(MESSAGE);
        show_time = intent.getBooleanExtra(TIME, false);
        do_work = intent.getBooleanExtra(WORK, false);
        double_speed = intent.getBooleanExtra(WORK_DOUBLE, false);

        periods_time = intent.getStringExtra(TIME_STEP);
        dont_reset = intent.getBooleanExtra(DONT_RESET, false);
        switch(periods_time){
            case "2s":
                time_period = 2000;
                break;
            case "5s":
                time_period = 5000;
                break;
            case "10s":
                time_period = 10000;
                break;
        }

        if(dont_reset){
            counter = sharedPreferences.getInt("counter",0);
        }else {
            counter = 0 ;
        }
        timerTask = new TimerTask() {
            @Override
            public void run() {
                counter++;
                handler.post(runnable);
            }
        };

        createNotificationChannel();

        Notification notification = new Notification.Builder(ctx, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_my_icon)
                .setContentTitle(getString(R.string.ser_title))
                .setShowWhen(show_time)
                .setContentText(message + " " + String.valueOf(counter))
                .setLargeIcon(BitmapFactory.decodeResource (getResources() , R.drawable.circle ))
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);
        doWork();
        return START_NOT_STICKY;
    }

    private void doWork() {
        if(do_work) {
            timer.schedule(timerTask, 0L, double_speed ? time_period / 2L : time_period);
        }

        String info = "show_time=" + show_time.toString()
                + "\n do_work=" + do_work.toString()
                + "\n double_speed=" + double_speed.toString()
                + "\n period= " + time_period
                + "\n restart= " + dont_reset.toString();

        Toast.makeText(this, info, Toast.LENGTH_LONG).show();
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChannel() {
        NotificationChannel serviceChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(serviceChannel);

    }

    final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            Notification notification = new Notification.Builder(ctx, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_my_icon)
                    .setContentTitle(getString(R.string.ser_title))
                    .setShowWhen(show_time)
                    .setContentText(message + " " + String.valueOf(counter))
                    .setLargeIcon(BitmapFactory.decodeResource (getResources() , R.drawable.circle ))
                    .setContentIntent(pendingIntent)
                    .build();

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.notify(1,notification);
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
        timer.cancel();
        timer.purge();
        timer = null;
        sharedPreferences.edit().putInt("counter" ,counter).commit();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

