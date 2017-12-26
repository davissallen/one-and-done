package me.davisallen.oneanddone;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import me.davisallen.oneanddone.pojo.Goal;

import static me.davisallen.oneanddone.DailyGoalAppWidget.EXTRA_GOAL;

public class AppWidgetGoalIntentService extends IntentService {
    public static final String ACTION_GET_MOST_RECENT_GOAL =
            "me.davisallen.oneanddone.action.ACTION_GET_MOST_RECENT_GOAL";

    public AppWidgetGoalIntentService() {
        super("AppWidgetGoalIntentService");
    }

    public static void startActionGetMostRecentGoal(Context context, Goal goal) {
        Intent intent = new Intent(context, AppWidgetGoalIntentService.class);
        intent.setAction(ACTION_GET_MOST_RECENT_GOAL);
        intent.putExtra(EXTRA_GOAL, goal);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_GET_MOST_RECENT_GOAL.equals(action)) {
                final Goal goal = intent.getParcelableExtra(EXTRA_GOAL);
                handleActionGetMostRecentGoal(getApplicationContext(), goal);
            }
        }
    }

    private void handleActionGetMostRecentGoal(Context context, Goal mostRecentGoal) {
        Intent intent = new Intent(getApplicationContext(), DailyGoalAppWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int[] ids = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(new ComponentName(context, DailyGoalAppWidget.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        intent.putExtra(EXTRA_GOAL, mostRecentGoal);
        sendBroadcast(intent);
    }
}
