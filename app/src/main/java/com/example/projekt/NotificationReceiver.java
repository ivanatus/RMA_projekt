package com.example.projekt;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.auth.FirebaseAuth;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra("title");
        String content = intent.getStringExtra("content");
        String receiver = intent.getStringExtra("receiver_id");

        Log.d("Notification", "Title on receive: " + title);
        Log.d("Notification", "Content on receive: " + content);
        Log.d("Notification", "Receiver on receive: " + content);

        // Create the notification channel
        createNotificationChannel(context);

        if(receiver != null && !receiver.isEmpty()) {
            if(FirebaseAuth.getInstance().getUid().equals(receiver)) {
                // Create and display the notification
                Notification notification = createNotification(context, title, content);
                showNotification(context, notification);
            }
        } else {
            // Create and display the notification
            Notification notification = createNotification(context, title, content);
            showNotification(context, notification);
        }
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);

            String channelId = "1"; // Change this to a unique ID for your channel
            CharSequence channelName = "Notifications"; // Change this to a user-friendly name

            // The importance determines how to interrupt the user with sound, vibration, etc.
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);

            notificationManager.createNotificationChannel(channel);
        }
    }

    private Notification createNotification(Context context, String title, String content) {
        int icon = R.drawable.weight_notification;

        String channelId = "1"; // Use the same channel ID you defined
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(icon) // Set your notification icon
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true) // Automatically remove the notification when the user taps on it
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(content));

        return builder.build();
    }

    @SuppressLint("MissingPermission")
    private void showNotification(Context context, Notification notification) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(1, notification);
    }
}
