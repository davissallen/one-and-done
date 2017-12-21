package me.davisallen.oneanddone;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.davisallen.oneanddone.pojo.Goal;

/**
 * Package Name:   me.davisallen.oneanddone
 * Project:        one-and-done
 * Created by davis, on 9/10/17
 */

public class CalendarFragment extends Fragment {

    @BindView(R.id.calendarView) CalendarView mCalendar;
    @BindView(R.id.count_completed) TextView mCountCompleted;
    @BindView(R.id.count_neutral) TextView mCountNeutral;
    @BindView(R.id.count_uncompleted) TextView mCountUncompleted;

    private MainActivity mParentActivity;

    // TODO: Update the view on every calendar swipe (asynchronously).

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);
        ButterKnife.bind(this, view);

        mParentActivity = (MainActivity) getActivity();

        // Set the min and max dates on the calendar.
        // Update the count text views with the appropriate data.
        initializeCalendarSettings();

        return view;
    }

    private void initializeCalendarSettings() {
        mCalendar.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView, int year, int month, int day) {
                month += 1;

                return;
            }
        });

        calculateGoalCounts();

        long firstGoalDateInMillis, lastGoalDateInMillis;
        if (mParentActivity.mGoals != null && mParentActivity.mGoals.size() > 0) {
            // Get the date of first goal ever.
            firstGoalDateInMillis = mParentActivity.mGoals.get(0).getDateInMillis();
            // Get the date of last goal ever.
            lastGoalDateInMillis = mParentActivity.mGoals.get(
                    mParentActivity.mGoals.size() - 1).getDateInMillis();
        } else {
            firstGoalDateInMillis = System.currentTimeMillis();
            lastGoalDateInMillis = System.currentTimeMillis();
        }
        mCalendar.setMinDate(firstGoalDateInMillis);
        mCalendar.setMaxDate(lastGoalDateInMillis);
    }

    private void calculateGoalCounts() {

        long dateInMillis = mCalendar.getDate();

        long begOfMonthInMillis = getBeginningOfMonthInMillis(dateInMillis);
        long endOfMonthInMillis = getEndOfMonthInMillis(dateInMillis);

        int count_completed = 0;
        int count_neutral = Math.round((endOfMonthInMillis - begOfMonthInMillis) / 1000f / 60f / 60f / 24f);
        int count_uncompleted = 0;
        for (Goal goal : mParentActivity.mGoals) {
            long goalMillis = goal.getDateInMillis();
            if (goalMillis >= begOfMonthInMillis && goalMillis <= endOfMonthInMillis) {
                if (goal.getIsCompleted()) {
                    count_completed += 1;
                } else {
                    count_uncompleted += 1;
                }
                count_neutral -= 1;
            }
        }

        updateCounts(count_completed, count_neutral, count_uncompleted);
    }

    private void updateCounts(int count_completed, int count_neutral, int count_uncompleted) {
        mCountCompleted.setText(String.valueOf(count_completed));
        mCountNeutral.setText(String.valueOf(count_neutral));
        mCountUncompleted.setText(String.valueOf(count_uncompleted));
    }

    private long getBeginningOfMonthInMillis(long dateInMillis) {
        // Get month from millis
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(dateInMillis);
        String yyyy = String.valueOf(c.get(Calendar.YEAR));
        String mm  = String.valueOf(c.get(Calendar.MONTH) + 1);

        // Get beginning of month in millis
        String myDate = String.format("%s/%s/01 00:00:00", yyyy, mm);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US);
        Date date = null;
        try {
            date = sdf.parse(myDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long begOfMonthInMillis = 0;
        if (date != null) {
            begOfMonthInMillis = date.getTime();
        }
        return begOfMonthInMillis;
    }

    private long getEndOfMonthInMillis(long dateInMillis) {
        // Get month from millis
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(dateInMillis);

        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH) + 1;

        if (month == 12) {
            // If it was December, get the Jan of next year.
            year += 1;
            month = 1;
        } else {
            month += 1;
        }

        String yyyy = String.valueOf(year);
        String mm  = String.valueOf(month);

        // Get beginning of month in millis
        String myDate = String.format("%s/%s/01 00:00:00", yyyy, mm);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US);
        Date date = null;
        try {
            date = sdf.parse(myDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long endOfMonthInMillis = 0;
        if (date != null) {
            // Get the start of next month - 1 == very end of last month.
            endOfMonthInMillis = date.getTime() - 1;
        }

        return endOfMonthInMillis;
    }
}