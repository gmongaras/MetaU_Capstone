package com.example.metau_capstone;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;

import com.example.metau_capstone.BootReceiver;
import com.parse.ParseUser;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.legacy.content.WakefulBroadcastReceiver;

import java.util.Calendar;
import java.util.Date;


/**
 * THANK YOU SO MUCH!
 * https://stackoverflow.com/questions/36902667/how-to-schedule-notification-in-android
 */



/**
 * When the alarm fires, this WakefulBroadcastReceiver receives the broadcast Intent
 * and then posts the notification.
 */
public class WakefulReceiver extends WakefulBroadcastReceiver {
    // provides access to the system alarm services.
    private AlarmManager mAlarmManager;

    translationManager manager;

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void onReceive(Context context, Intent intent) {
        int notifyId = 1;
        String channelId = "my_channel_id";
        CharSequence channelName = "My Channel";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;

        NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName, importance);
        notificationChannel.enableLights(true);
        notificationChannel.setLightColor(Color.RED);
        notificationChannel.enableVibration(true);
        notificationChannel.setVibrationPattern(new long[]{1000, 2000});

        manager.getText("New Fortune!", new translationManager.onCompleteListener() {
            @Override
            public void onComplete(String title) {
                manager.getText("It's time! A new fortune cookie is available to open", new translationManager.onCompleteListener() {
                    @Override
                    public void onComplete(String text) {
                        NotificationManager notificationManager =
                                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                        Notification notification = new Notification.Builder(context, channelId)
                                .setContentTitle(title)
                                .setContentText(text)
                                .setSmallIcon(R.drawable.cookie)
                                .build();

                        // When clicked, go to the login activity
                        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                                new Intent(context, LoginActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
                        notification.contentIntent = contentIntent;

                        notificationManager.createNotificationChannel(notificationChannel);
                        notificationManager.notify(notifyId, notification);




                        WakefulReceiver.completeWakefulIntent(intent);
                    }
                });
            }
        });
    }

    /**
     * Sets the next alarm to run. When the alarm fires,
     * the app broadcasts an Intent to this WakefulBroadcastReceiver.
     *
     * @param context the context of the app's Activity.
     */
    public void setAlarm(Context context) {
        mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, WakefulReceiver.class);
        PendingIntent alarmIntent;
        if (Integer.parseInt(android.os.Build.VERSION.SDK) >= 31) {
            alarmIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_MUTABLE);
        }
        else {
            alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        }
        manager = new translationManager(ParseUser.getCurrentUser().getString("lang"));

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        //// TODO: use calendar.add(Calendar.SECOND,MINUTE,HOUR, int);
        calendar.add(Calendar.HOUR, 23); // After 23 hours, send the notification

        //ALWAYS recompute the calendar after using add, set, roll
        Date date = calendar.getTime();

        mAlarmManager.setExact(AlarmManager.RTC_WAKEUP, date.getTime(), alarmIntent);

        // Enable {@code BootReceiver} to automatically restart when the
        // device is rebooted.
        //// TODO: you may need to reference the context by ApplicationActivity.class
        ComponentName receiver = new ComponentName(context, BootReceiver.class);
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

    /**
     * Cancels the next alarm from running. Removes any intents set by this
     * WakefulBroadcastReceiver.
     *
     * @param context the context of the app's Activity
     */
    public void cancelAlarm(Context context) {
        Log.d("WakefulAlarmReceiver", "{cancelAlarm}");

        mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, WakefulReceiver.class);
        PendingIntent alarmIntent;
        if (Integer.parseInt(android.os.Build.VERSION.SDK) >= 31) {
            alarmIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_MUTABLE);
        }
        else {
            alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        }

        mAlarmManager.cancel(alarmIntent);

        // Disable {@code BootReceiver} so that it doesn't automatically restart when the device is rebooted.
        //// TODO: you may need to reference the context by ApplicationActivity.class
        ComponentName receiver = new ComponentName(context, BootReceiver.class);
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }
}