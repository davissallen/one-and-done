package me.davisallen.oneanddone;

import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.text.format.DateUtils;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.Date;
import java.util.NoSuchElementException;

import me.davisallen.oneanddone.pojo.Goal;
import me.davisallen.oneanddone.utils.FirebaseUtils;
import timber.log.Timber;

/**
 * Package Name:   PACKAGE_NAME
 * Project:        one-and-done
 * Created by davis, on 1/25/18
 */

public class NotificationJobService extends JobService {

    NotificationHelper mNotificationHelper;
    private DatabaseReference mGoalsByUserDbReference;
    private FirebaseUser mUser;

    @Override
    public boolean onStartJob(JobParameters job) {
        // Do some work here

        // TODO: Make this thread safe and async
        // *** NOTE ***
        // THIS WILL RUN ON THE MAIN THREAD! MAKE SURE TO USE ASYNCTASK FOR NETWORK TASKS

        new Thread(new Runnable() {
            @Override
            public void run() {
                String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
                Timber.d(String.format("%s: Notification sending!!", currentDateTimeString));
            }
        }).start();

        // Get the user info from FirebaseAuth.
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mUser == null) {
            Timber.e(getResources().getString(R.string.firebase_auth_error));
        } else {
            Timber.d(String.format("Actually got a user wow: %s.", mUser.getUid()));

        }

        // Obtain the FirebaseStorage instance.
        FirebaseDatabase firebaseDatabase = FirebaseUtils.getDatabase();
        mGoalsByUserDbReference = firebaseDatabase.getReference(getString(R.string.goals_db_name)).child(mUser.getUid());
        mGoalsByUserDbReference.orderByChild("dateInMillis").limitToLast(1).addListenerForSingleValueEvent(getMostRecentGoalByUser);

        mNotificationHelper = new NotificationHelper(getApplicationContext());

        return false; // Answers the question: "Is there still work going on?"
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        return false; // Answers the question: "Should this job be retried?"
    }

    /** Post the notifications */
    public void postNotification(int id, @Nullable String goal) {
        NotificationCompat.Builder notificationBuilder = null;
        switch (id) {
            case NotificationHelper.NOTIFICATION_CREATE_GOAL:
                notificationBuilder = mNotificationHelper.getNotificationCreateGoal();
                break;

            case NotificationHelper.NOTIFICATION_COMPLETE_GOAL:
                notificationBuilder = mNotificationHelper.getNotificationCompleteGoal(goal);
                break;
        }

        if (notificationBuilder != null) {
            mNotificationHelper.notify(id, notificationBuilder);
        }
    }

    final ValueEventListener getMostRecentGoalByUser = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {

            Goal goal = null;

            try {
                goal = dataSnapshot.getChildren().iterator().next().getValue(Goal.class);
            } catch (NoSuchElementException e) {
                Timber.e("No goal was found in the database.");
                e.printStackTrace();
                return;
            }

            if (goal == null || !DateUtils.isToday(goal.getDateInMillis())) {
                postNotification(NotificationHelper.NOTIFICATION_CREATE_GOAL, null);
            } else {
                postNotification(NotificationHelper.NOTIFICATION_COMPLETE_GOAL, goal.getGoal());
            }

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
        }
    };
}