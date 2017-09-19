package me.davisallen.oneanddone.pojo;

import java.util.Date;

/**
 * Package Name:   me.davisallen.oneanddone.pojo
 * Project:        one-and-done
 * Created by davis, on 9/18/17
 */

public class Goal {

    private String goal;
    private Date date;
    private boolean isCompleted;

    public Goal(String goal, Date date, boolean isCompleted) {
        this.goal = goal;
        this.date = date;
        this.isCompleted = isCompleted;
    }

    public String getGoal() {
        return goal;
    }

    public Date getDate() {
        return date;
    }

    public boolean isCompleted() {
        return isCompleted;
    }
}
