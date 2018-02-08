package me.davisallen.oneanddone;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.MotionEvent;
import android.widget.CalendarView;
import android.view.GestureDetector;

/**
 * Package Name:   me.davisallen.oneanddone
 * Project:        one-and-done
 * Created by davis, on 2/7/18
 */

public class TalkativeCalendarView extends CalendarView implements GestureDetector.OnGestureListener {

    // TODO: Implement a calendarview that has a swipe listener
    // https://developer.android.com/training/gestures/detector.html

    // Can decide to do this too instead....
    // https://stackoverflow.com/questions/45752408/is-there-any-way-to-detect-month-change-in-android-calendar-viewi-e-when-user
    // And specifically use the daydecorator features...
    // https://github.com/prolificinteractive/material-calendarview/blob/master/docs/DECORATORS.md

    public TalkativeCalendarView(@NonNull Context context) {
        super(context);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }
}
