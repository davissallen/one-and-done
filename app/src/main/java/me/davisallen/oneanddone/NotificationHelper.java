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
    private static final String CHANNEL_CREATE_GOAL = "channel_create_goal";
    private static final String CHANNEL_COMPLETE_GOAL = "channel_complete_goal";

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

        NotificationChannel createGoalChannel = new NotificationChannel(
                CHANNEL_CREATE_GOAL,
                getString(R.string.channel_create_goal),
                NotificationManager.IMPORTANCE_DEFAULT);
        createGoalChannel.setLightColor(getColor(R.color.colorPrimary));
        createGoalChannel.setDescription(getString(R.string.channel_create_goal_desc));
        createGoalChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        getManager().createNotificationChannel(createGoalChannel);

        NotificationChannel completeGoalChannel = new NotificationChannel(
                CHANNEL_COMPLETE_GOAL,
                getString(R.string.channel_complete_goal),
                NotificationManager.IMPORTANCE_DEFAULT);
        completeGoalChannel.setLightColor(getColor(R.color.colorAccent));
        completeGoalChannel.setDescription(getString(R.string.channel_complete_goal_desc));
        completeGoalChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        getManager().createNotificationChannel(completeGoalChannel);
    }

    public NotificationCompat.Builder getNotificationCreateGoal() {
        return new NotificationCompat.Builder(getApplicationContext(), CHANNEL_CREATE_GOAL)
                .setContentTitle("COME HERE YA BOY MAKE A GOAL ALREDY K")
                .setContentText("COME HERE YA BOY MAKE A GOAL ALREDY K")
                .setSmallIcon(getSmallIcon())
                .setAutoCancel(true);
    }

    public NotificationCompat.Builder getNotificationCompleteGoal(String goal) {
        return new NotificationCompat.Builder(getApplicationContext(), CHANNEL_COMPLETE_GOAL)
                .setContentTitle(goal)
                .setContentText("DID YOU DO IT YET HUH?")
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