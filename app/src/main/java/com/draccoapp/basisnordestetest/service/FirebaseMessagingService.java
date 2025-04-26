package com.draccoapp.basisnordestetest.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.draccoapp.basisnordestetest.R;
import com.draccoapp.basisnordestetest.ui.activities.MainActivity;
import com.google.firebase.messaging.RemoteMessage;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {
    private static final String TAG = "FirebaseMsgService";
    private static final String CHANNEL_ID = "person_notifications";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Verificar se a mensagem contém dados
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            // Processar os dados da mensagem
            String title = remoteMessage.getData().get("title");
            String message = remoteMessage.getData().get("message");

            // Enviar notificação mesmo se o app estiver em primeiro plano
            sendNotification(title != null ? title : "Nova Notificação",
                    message != null ? message : "Você tem uma nova notificação");
        }

        // Verificar se a mensagem contém uma notificação
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            sendNotification(
                    remoteMessage.getNotification().getTitle(),
                    remoteMessage.getNotification().getBody()
            );
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        Log.d(TAG, "Refreshed token: " + token);

        // Enviar o token para o servidor
        sendRegistrationToServer(token);
    }

    private void sendRegistrationToServer(String token) {
        // Implementar a lógica para enviar o token para o servidor
        // Isso pode ser feito através de uma chamada de API ou salvando no Firestore
        Log.d(TAG, "Sending FCM token to server: " + token);

        // TODO: Implementar o envio do token para o servidor
    }

    private void sendNotification(String title, String messageBody) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        // Adicionar um timestamp para garantir que o PendingIntent seja único
        intent.putExtra("timestamp", System.currentTimeMillis());

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(title)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setPriority(NotificationCompat.PRIORITY_HIGH) // Prioridade alta para garantir exibição
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Desde o Android O, canais de notificação são necessários
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Person Notifications",
                    NotificationManager.IMPORTANCE_HIGH); // Importância alta para garantir exibição
            channel.enableLights(true);
            channel.enableVibration(true);
            notificationManager.createNotificationChannel(channel);
        }

        // Usar um ID único para cada notificação
        int notificationId = (int) System.currentTimeMillis();
        notificationManager.notify(notificationId, notificationBuilder.build());

        Log.d(TAG, "Notification sent with ID: " + notificationId);
    }
}
