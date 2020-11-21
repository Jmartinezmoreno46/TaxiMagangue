package com.example.taximagangue.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.example.taximagangue.R;
import com.example.taximagangue.channel.NotoficacionHelper;
import com.example.taximagangue.receivers.AcceptReceiver;
import com.example.taximagangue.receivers.CancelReceiver;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class MyFirebaseMessagingCloud extends FirebaseMessagingService {

    private static  final int NOTIFICACION_CODE = 100;

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);

    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        RemoteMessage.Notification notification = remoteMessage.getNotification();

        Map<String, String> data = remoteMessage.getData();
        String title = data.get("title");
        String body = data.get("body");

        if (title != null){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (title.contains("SOLICITUD DE SERVICIO")) {
                    String idCliente = data.get("idClient");
                    showNotificationApiOreoAction(title, body, idCliente);
                } else {
                    showNotificationApiOreo(title, body);
                }
            }
            else{
                if (title.contains("SOLICITUD DE SERVICIO")) {
                    String idCliente = data.get("idClient");
                    showNoticacionAction(title,body,idCliente);
                } else {
                    showNoticacion(title, body);
                }
            }
        }

    }

    private void showNoticacion(String title, String body ) {
        PendingIntent intent = PendingIntent.getActivity(getBaseContext(),0, new Intent(), PendingIntent.FLAG_ONE_SHOT);
        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotoficacionHelper notoficacionHelper = new NotoficacionHelper(getBaseContext());
        NotificationCompat.Builder builder = notoficacionHelper.getNotificacionOldApi(title, body, intent, sound);
        notoficacionHelper.getManager().notify(1, builder.build());
    }

    private void showNoticacionAction(String title, String body , String idClient) {
        //Aceptar
        Intent acceptIntent = new Intent(this, AcceptReceiver.class);
        acceptIntent.putExtra("idClient", idClient);
        PendingIntent acceptPendingIntent = PendingIntent.getBroadcast(this, NOTIFICACION_CODE, acceptIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Action acceptAction = new NotificationCompat.Action.Builder(
                R.mipmap.ic_launcher,
                "Aceptar",
                acceptPendingIntent
        ).build();
        //cancelar
        Intent cancelIntent = new Intent(this, CancelReceiver.class);
        acceptIntent.putExtra("idClient", idClient);
        PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(this, NOTIFICACION_CODE, cancelIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Action canceltAction = new NotificationCompat.Action.Builder(
                R.mipmap.ic_launcher,
                "Cancelar",
                cancelPendingIntent
        ).build();

        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotoficacionHelper notoficacionHelper = new NotoficacionHelper(getBaseContext());
        NotificationCompat.Builder builder = notoficacionHelper.getNotificacionOldApiAction(title, body, sound, acceptAction,canceltAction);
        notoficacionHelper.getManager().notify(2, builder.build());
    }



    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showNotificationApiOreo(String title, String body ) {
        PendingIntent intent = PendingIntent.getActivity(getBaseContext(),0, new Intent(), PendingIntent.FLAG_ONE_SHOT);
        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotoficacionHelper notoficacionHelper = new NotoficacionHelper(getBaseContext());
        Notification.Builder builder = notoficacionHelper.getNotificacion(title, body, intent, sound);
        notoficacionHelper.getManager().notify(1, builder.build());

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showNotificationApiOreoAction(String title, String body, String idClient ) {
        //Aceptar
        Intent acceptIntent = new Intent(this, AcceptReceiver.class);
        acceptIntent.putExtra("idClient", idClient);
        PendingIntent acceptPendingIntent = PendingIntent.getBroadcast(this, NOTIFICACION_CODE, acceptIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Action acceptAction = new Notification.Action.Builder(
                R.mipmap.ic_launcher,
                "Aceptar",
                acceptPendingIntent
        ).build();

        //Cancelar
        Intent cancelIntent = new Intent(this, CancelReceiver.class);
        acceptIntent.putExtra("idClient", idClient);
        PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(this, NOTIFICACION_CODE, cancelIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Action cancelAction = new Notification.Action.Builder(
                R.mipmap.ic_launcher,
                "Cancelar",
                cancelPendingIntent
        ).build();

        PendingIntent intent = PendingIntent.getActivity(getBaseContext(),0, new Intent(), PendingIntent.FLAG_ONE_SHOT);
        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotoficacionHelper notoficacionHelper = new NotoficacionHelper(getBaseContext());
        Notification.Builder builder = notoficacionHelper.getNotificacionActions(title, body,sound,acceptAction,cancelAction);
        notoficacionHelper.getManager().notify(2, builder.build());

    }
}
