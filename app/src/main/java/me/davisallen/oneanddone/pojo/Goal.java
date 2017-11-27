package me.davisallen.oneanddone.pojo;

import java.util.Calendar;
import java.util.Date;

/**
 * Package Name:   me.davisallen.oneanddone.pojo
 * Project:        one-and-done
 * Created by davis, on 9/18/17
 */

public class Goal {

    public String getGoal() {
        return goal;
    }

    public void setGoal(String goal) {
        this.goal = goal;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    private String goal;
    private Date date;
    private String userId;
    private boolean isCompleted;

    public Goal() {

    }

    public Goal(String goal, String userId) {
        this.goal = goal;
        this.userId = userId;
        this.date = Calendar.getInstance().getTime();
        this.isCompleted = false;
    }
}
