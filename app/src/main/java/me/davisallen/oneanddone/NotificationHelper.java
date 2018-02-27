package me.davisallen.oneanddone;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;

import java.util.Random;

/**
 * Helper class to manage notification channels, and create notifications.
 */
class NotificationHelper extends ContextWrapper {
    private static final String CHANNEL_GOAL_REMINDER = "channel_goal_reminder";
//    private static final String CHANNEL_GOAL_STREAK = "channel_goal_streak";
//    private static final String CHANNEL_CREATE_GOAL = "channel_create_goal";
//    private static final String CHANNEL_COMPLETE_GOAL = "channel_complete_goal";

    private NotificationManager manager;

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

        NotificationChannel goalReminderChannel = new NotificationChannel(
                CHANNEL_GOAL_REMINDER,
                getString(R.string.channel_create_goal),
                NotificationManager.IMPORTANCE_DEFAULT);
        goalReminderChannel.setLightColor(getColor(R.color.colorPrimary));
        goalReminderChannel.setDescription(getString(R.string.channel_goal_reminder));
        goalReminderChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        getManager().createNotificationChannel(goalReminderChannel);
    }

    public NotificationCompat.Builder getNotificationCreateGoal() {
        Intent openAppIntent = new Intent(this, MainActivity.class);
        openAppIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent openAppPendingIntent = PendingIntent.getActivity(this, 0, openAppIntent, 0);
        return new NotificationCompat.Builder(getApplicationContext(), CHANNEL_GOAL_REMINDER)
                .setContentTitle("Feeling a little misguided?")
                .setContentText("Set a goal for today!")
                .setSmallIcon(R.drawable.ic_notification)
                .setColorized(true)
                .setColor(getResources().getColor(R.color.colorPrimary))
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.logo))
                .setAutoCancel(true)
                .addAction(R.drawable.ic_send, getString(R.string.notification_create_action), openAppPendingIntent)
                .setContentIntent(openAppPendingIntent);
    }

    public NotificationCompat.Builder getNotificationCompleteGoal(String goal) {
        Intent openAppIntent = new Intent(this, MainActivity.class);
        PendingIntent openAppPendingIntent = PendingIntent.getActivity(this, 0, openAppIntent, 0);
        openAppIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return new NotificationCompat.Builder(getApplicationContext(), CHANNEL_GOAL_REMINDER)
                .setContentTitle(goal)
                .setContentText(getMotivationalPrompt())
                .setSmallIcon(R.drawable.ic_notification)
                .setColorized(true)
                .setColor(getResources().getColor(R.color.colorPrimary))
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.logo))
                .setAutoCancel(true)
                .addAction(R.drawable.ic_check_green_36dp, getString(R.string.notification_complete_action), openAppPendingIntent)
                .setContentIntent(openAppPendingIntent);
    }

    private CharSequence getMotivationalPrompt() {
        CharSequence[] motivation = {
                "Did you finish it yet?",
                "You can do it!",
                "Don't give up on your dreams!",
                "Do it for you.",
                "Believe in yourself!"
        };
        Random r = new Random();
        return motivation[r.nextInt(motivation.length)];
    }

    public void notify(int id, NotificationCompat.Builder notification) {
        getManager().notify(id, notification.build());
    }

    private NotificationManager getManager() {
        if (manager == null) {
            manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return manager;
    }
}