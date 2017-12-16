package me.davisallen.oneanddone.pojo;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Calendar;

/**
 * Package Name:   me.davisallen.oneanddone.pojo
 * Project:        one-and-done
 * Created by davis, on 9/18/17
 */

public class Goal implements Parcelable {

    private String goal;
    private long dateInMillis;
    private boolean isCompleted;

    public Goal() {
        // Needed for firebase.
    }

    public Goal(String goal) {
        this.goal = goal;
        this.dateInMillis = System.currentTimeMillis();
        this.isCompleted = false;
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

    public boolean getIsCompleted() {
        return isCompleted;
    }

    public void setIsCompleted(boolean completed) {
        isCompleted = completed;
    }

    public String monthFromMillis() {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(this.dateInMillis);
        int month = c.get(Calendar.MONTH);
        switch (month) {
            case 0: return "JAN";
            case 1: return "FEB";
            case 2: return "MAR";
            case 3: return "APR";
            case 4: return "MAY";
            case 5: return "JUN";
            case 6: return "JUL";
            case 7: return "AUG";
            case 8: return "SEP";
            case 9: return "OCT";
            case 10: return "NOV";
            case 11: return "DEC";
            default: return "???";
        }
    }

    public String dayOfMonthFromMillis() {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(this.dateInMillis);
        return String.valueOf(c.get(Calendar.DAY_OF_MONTH));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.goal);
        dest.writeLong(this.dateInMillis);
        dest.writeByte(this.isCompleted ? (byte) 1 : (byte) 0);
    }

    protected Goal(Parcel in) {
        this.goal = in.readString();
        this.dateInMillis = in.readLong();
        this.isCompleted = in.readByte() != 0;
    }

    public static final Parcelable.Creator<Goal> CREATOR = new Parcelable.Creator<Goal>() {
        @Override
        public Goal createFromParcel(Parcel source) {
            return new Goal(source);
        }

        @Override
        public Goal[] newArray(int size) {
            return new Goal[size];
        }
    };
}
