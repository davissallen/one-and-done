package me.davisallen.oneanddone.pojo;

/**
 * Package Name:   me.davisallen.oneanddone.pojo
 * Project:        one-and-done
 * Created by davis, on 9/18/17
 */

public class Goal {

    private String goal;
    private long dateInMillis;
    private boolean isCompleted;
    private String userId;

    public Goal() {
    }

    public Goal(String goal, String userId) {
        this.goal = goal;
        this.userId = userId;
        this.dateInMillis = System.currentTimeMillis();
        this.isCompleted = false;
    }

    public Goal(String goal, long dateInMillis, boolean isCompleted, String userId) {
        this.goal = goal;
        this.dateInMillis = dateInMillis;
        this.isCompleted = isCompleted;
        this.userId = userId;
    }

    public String getGoal() {
        return goal;
    }

    public void setGoal(String goal) {
        this.goal = goal;
    }

    public long getDateInMillis() {
        return dateInMillis;
    }

    public void setDateInMillis(long dateInMillis) {
        this.dateInMillis = dateInMillis;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
