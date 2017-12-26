package me.davisallen.oneanddone;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.widget.RemoteViews;

import me.davisallen.oneanddone.pojo.Goal;

/**
 * Implementation of App Widget functionality.
 */
public class DailyGoalAppWidget extends AppWidgetProvider {

    public static final String EXTRA_GOAL = "extra_goal";

    Goal mGoal;

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId, Goal goal) {

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.daily_goal_app_widget);

        if (goal != null) {
            views.setTextViewText(R.id.tv_widget_goal, goal.getGoal());
            if (goal.getIsCompleted()) {
                views.setInt(R.id.tv_widget_goal, "setPaintFlags", Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
                views.setTextViewText(R.id.tv_widget_action_button, "Well done.");
                views.setTextColor(R.id.tv_widget_action_button, context.getResources().getColor(R.color.button_unselectable));
            } else {
                views.setInt(R.id.tv_widget_goal, "setPaintFlags", 0);
                views.setTextViewText(R.id.tv_widget_action_button, "I DID IT!");
                views.setTextColor(R.id.tv_widget_action_button, context.getResources().getColor(R.color.colorAccent));
            }
        } else {
            views.setInt(R.id.tv_widget_goal, "setPaintFlags", 0);
            views.setTextViewText(R.id.tv_widget_goal, "No goal set.");
            views.setTextViewText(R.id.tv_widget_action_button, "Click me!");
            views.setTextColor(R.id.tv_widget_action_button, context.getResources().getColor(R.color.colorAccent));
        }

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && intent.hasExtra(EXTRA_GOAL)) {
            mGoal = intent.getParcelableExtra(EXTRA_GOAL);
        }
        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        // If there is not a goal for today, make the tv_widget_goal GONE and update button to say
        // "Set a goal!".
//        updateAppWidgetNoGoalToday(context, appWidgetManager, appWidgetId);
        // If there is a goal in progress for today, make the tv_widget_goal VISIBLE and update
        // button to say "I did it!".
//        updateAppWidgetGoalInProgress(context, appWidgetManager, appWidgetId);
        // If there is a goal completed for today, make the tv_widget_goal VISIBLE and update button
        // to say "Congratulations" abd be in black/gray.
        // optionally, show another text view that says "Come back tomorrow to keep up the streak!"
//        updateAppWidgetNoGoalCompleted(context, appWidgetManager, appWidgetId);

        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, mGoal);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

