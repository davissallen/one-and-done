package me.davisallen.oneanddone.pojo;

import java.util.Calendar;
import java.util.Date;

/**
 * Package Name:   me.davisallen.oneanddone.pojo
 * Project:        one-and-done
 * Created by davis, on 9/18/17
 */

public class Goal {

    private String goal;
    private Date date;

    private String userId;
    private boolean isCompleted;

    public Goal(String goal, String userId) {
        this.goal = goal;
        this.userId = userId;
        this.date = Calendar.getInstance().getTime();
        this.isCompleted = false;
    }

    public String getGoal() {
        return goal;
    }

    public String getUserId() {
        return userId;
    }

    public Date getDate() {
        return date;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }
}
