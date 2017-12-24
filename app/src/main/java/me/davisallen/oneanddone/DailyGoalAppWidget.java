package me.davisallen.oneanddone;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.widget.RemoteViews;

/**
 * Implementation of App Widget functionality.
 */
public class DailyGoalAppWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        CharSequence widgetText = context.getString(R.string.appwidget_text);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.daily_goal_app_widget);
        views.setTextViewText(R.id.tv_widget_goal, widgetText);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        // If there is not a goal for today, make the tv_widget_goal GONE and update button to say
        // "Set a goal!".
        updateAppWidgetNoGoalToday(context, appWidgetManager, appWidgetId);
        // If there is a goal in progress for today, make the tv_widget_goal VISIBLE and update
        // button to say "I did it!".
        updateAppWidgetGoalInProgress(context, appWidgetManager, appWidgetId);
        // If there is a goal completed for today, make the tv_widget_goal VISIBLE and update button
        // to say "Congratulations" abd be in black/gray.
        // optionally, show another text view that says "Come back tomorrow to keep up the streak!"
        updateAppWidgetNoGoalCompleted(context, appWidgetManager, appWidgetId);


        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
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

