package com.example.owner.pins;

import android.os.AsyncTask;
import android.util.Log;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Owner on 12/15/2015.
 */
public class RestartArduino extends AsyncTask<String, Void, Void> {
    private String url;
    private String TAG = "RestartArduiono";

    @Override
    protected Void doInBackground(String... params) {
        this.url = params[0];
        networkWork();
        return null;
    }

    /**
     * Connects to the arduino and sends it a signal over IP to reset itself.
     * Note, this method will fail when other connections are still open
     * as the arduino can only handle one connection at a time.
     */
    public void networkWork() {
        try {
            Log.w(TAG, "2 Inside first try");

            URL url1 = new URL(url);
            HttpURLConnection connection;

            connection = (HttpURLConnection) url1.openConnection();
            Log.w(TAG, "3 Opened connection");
            connection.setConnectTimeout(4000);
            connection.setReadTimeout(4000);
            connection.setDoOutput(true);
            connection.connect();
            Log.w(TAG, "4 Right after connect");
            int statusCode = connection.getResponseCode();
            // Log.w(TAG, "4.05 Response code is");
            Log.w(TAG, "" + connection.getResponseCode());

            if (statusCode / 100 != 2) {
                Log.w(TAG, "Connection Error");
                connection.disconnect();
                //  Log.e(TAG, "Error-connection.getResponseCode returned " + Integer.toString(statusCode));

            } else {
                connection.disconnect();
            }

        } catch (Exception e) {
            Log.w(TAG, "Threw exception" + e);

        }
    }

}

