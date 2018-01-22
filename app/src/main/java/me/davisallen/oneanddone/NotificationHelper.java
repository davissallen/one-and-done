package me.davisallen.oneanddone;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.support.v4.app.NotificationCompat;

/**
 * Helper class to manage notification channels, and create notifications.
 */
class NotificationHelper extends ContextWrapper {
    private NotificationManager manager;

    public static final String CHANNEL_REMINDER_NAME = "reminder";
    public static final int NOTIFICATION_CREATE_GOAL = 100;
    public static final int NOTIFICATION_COMPLETE_GOAL = 101;

    /**
     * Registers notification channels, which can be used later by individual notifications.
     *
     * @param ctx The application context
     */
    public NotificationHelper(Context ctx) {
        super(ctx);

        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.O) {
            return;
        }

        // Create reminder notification channel
        NotificationChannel reminder_channel = new NotificationChannel(
                CHANNEL_REMINDER_NAME,
                getString(R.string.notification_channel1),
                NotificationManager.IMPORTANCE_DEFAULT);
        reminder_channel.setLightColor(getColor(R.color.colorPrimary));
        reminder_channel.setDescription(getString(R.string.notif_channel_reminder_description));
        reminder_channel.setName(CHANNEL_REMINDER_NAME);
        reminder_channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        getManager().createNotificationChannel(reminder_channel);
    }

    public NotificationCompat.Builder getNotificationCreateGoal() {
        return new NotificationCompat.Builder(getApplicationContext(), CHANNEL_REMINDER_NAME)
                .setContentTitle("Placeholder to create goal")
                .setContentText("Placeholder to create goal")
                .setSmallIcon(getSmallIcon())
                .setAutoCancel(true);
    }

    public NotificationCompat.Builder getNotificationCompleteGoal() {
        return new NotificationCompat.Builder(getApplicationContext(), CHANNEL_REMINDER_NAME)
                .setContentTitle("Placeholder to complete goal")
                .setContentText("Placeholder to complete goal")
                .setSmallIcon(getSmallIcon())
                .setAutoCancel(true);
    }

    public void notify(int id, NotificationCompat.Builder notification) {
        getManager().notify(id, notification.build());
    }

    private int getSmallIcon() {
        return R.drawable.ic_notification;
    }

    private NotificationManager getManager() {
        if (manager == null) {
            manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return manager;
    }
}