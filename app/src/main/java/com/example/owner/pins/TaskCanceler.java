package com.example.owner.pins;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class TaskCanceler implements Runnable {
    private static final String TAG = "TaskCanceler";

    private AsyncTask task;
    private MainActivity context;

    public TaskCanceler(AsyncTask task, MainActivity context) {
        this.task = task;
        this.context = context;
    }

    @Override
    public void run() {
        if (task.getStatus() == AsyncTask.Status.RUNNING) {
            Log.w(TAG, "CANCELLED THE TASK");
            //context.active = false;
            task.cancel(true);
            //context.getAndSetDataAsync();

        }
    }
}