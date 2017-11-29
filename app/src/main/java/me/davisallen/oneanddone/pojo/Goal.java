package me.davisallen.oneanddone.pojo;

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

    public long getDate() {
        return dateInMillis;
    }

    public void setDate(long dateInMillis) {
        this.dateInMillis = dateInMillis;
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
    private long dateInMillis;
    private String userId;
    private boolean isCompleted;

    public Goal() {

    }

    public Goal(String goal, String userId) {
        this.goal = goal;
        this.userId = userId;
        this.dateInMillis = System.currentTimeMillis();
        this.isCompleted = false;
    }
}
