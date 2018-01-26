package me.davisallen.oneanddone;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

import java.text.DateFormat;
import java.util.Date;

import timber.log.Timber;

/**
 * Package Name:   PACKAGE_NAME
 * Project:        one-and-done
 * Created by davis, on 1/25/18
 */

public class NotificationJobService extends JobService {
    @Override
    public boolean onStartJob(JobParameters job) {
        // Do some work here

        // *** NOTE ***
        // THIS WILL RUN ON THE MAIN THREAD! MAKE SURE TO USE ASYNCTASK FOR NETWORK TASKS

        new Thread(new Runnable() {
            @Override
            public void run() {
                String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
                Timber.d(String.format("%s: Job is going off!!", currentDateTimeString));
            }
        }).start();

        return false; // Answers the question: "Is there still work going on?"
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        return false; // Answers the question: "Should this job be retried?"
    }
}