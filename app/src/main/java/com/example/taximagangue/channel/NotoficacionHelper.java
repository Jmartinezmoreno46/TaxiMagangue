package com.example.taximagangue.channel;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.example.taximagangue.R;

public class NotoficacionHelper extends ContextWrapper {


    private static final String CHANNEL_ID = "com.example.taximagangue";
    private static final String CHANNEL_NAME = "TaxiFast";


    private NotificationManager manager;

    public NotoficacionHelper(Context base) {
        super(base);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            createChannel();
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.O)// NECESARIO PARA UN CANAL DE NOTIFICACION EN VERSIONES DE ANDROID  OREO
    private void createChannel(){
        NotificationChannel notificationChannel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
        );

        notificationChannel.enableLights(true);
        notificationChannel.enableVibration(true);
        notificationChannel.setLightColor(Color.DKGRAY);
        notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        getManager().createNotificationChannel(notificationChannel);

    }

    public NotificationManager getManager(){
        if (manager ==null){
            manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return  manager;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Notification.Builder getNotificacionActions(String title ,
                                                       String body,
                                                       Uri sounduri,
                                                       Notification.Action acceptAction,
                                                       Notification.Action cancelAction){
        return  new Notification.Builder(getApplicationContext(),CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(sounduri)
                .setSmallIcon(R.drawable.ic_car)
                .addAction(acceptAction)
                .addAction(cancelAction)
                .setStyle(new Notification.BigTextStyle()
                        .bigText(body).setBigContentTitle(title));
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Notification.Builder getNotificacion(String title , String body, PendingIntent intent, Uri sounduri){
        return  new Notification.Builder(getApplicationContext(),CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setContentIntent(intent)
                .setSound(sounduri)
                .setSmallIcon(R.drawable.ic_car)
                .setStyle(new Notification.BigTextStyle()
                        .bigText(body).setBigContentTitle(title));
    }

    public NotificationCompat.Builder getNotificacionOldApi(String title , String body, PendingIntent intent, Uri sounduri){
        return  new NotificationCompat.Builder(getApplicationContext(),CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(sounduri)
                .setContentIntent(intent)
                .setSmallIcon(R.drawable.ic_car)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(body).setBigContentTitle(title));
    }
    public NotificationCompat.Builder getNotificacionOldApiAction(String title,
                                                                  String body,
                                                                  Uri sounduri,
                                                                  NotificationCompat.Action acceptAcion,
                                                                  NotificationCompat.Action cancelAction){
        return  new NotificationCompat.Builder(getApplicationContext(),CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(sounduri)
                .setSmallIcon(R.drawable.ic_car)
                .addAction(acceptAcion)
                .addAction(cancelAction)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(body).setBigContentTitle(title));
    }

}
