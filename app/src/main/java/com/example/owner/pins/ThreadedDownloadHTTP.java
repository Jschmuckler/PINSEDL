package com.example.owner.pins;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

/**
 * Created by Owner on 12/15/2015.
 * WARNING CLASS ONLY USED IN HTTP VERSION OF PROGRAM.
 * Do not realease with bluetooth build.
 */
public class ThreadedDownloadHTTP extends IntentService {

    public ThreadedDownloadHTTP() {
        super("ThreadedDownloadHTTP");
    }

    private final String TAG = "ThreadedDownloadHTTP";
    private Scanner scan;
    private HttpURLConnection connection;
    private InputStream is;
    private BufferedReader bis;
    private boolean go = true;
    private boolean sent = false;

    @Override
    protected void onHandleIntent(Intent intent) {

        try {
            Log.w(TAG, "2 Inside first try");
            URL url1 = new URL(intent.getExtras().getString("URL"));

            connection = (HttpURLConnection) url1.openConnection();
            Log.w(TAG, "3 Opened connection");

            connection.connect();
            Log.w(TAG, "4 Right after connect");
            int statusCode = connection.getResponseCode();
            if (statusCode / 100 != 2
                    ) {
                Log.w(TAG, "Connection Error");

                Intent broadcastIntent = new Intent();
                broadcastIntent.setAction("response");
                broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
                sendBroadcast(broadcastIntent);
                sent = true;
                go = false;
            }/*
what would you do if i sang out of tune would you stand up and and instance walk out on me lend me your ears and i'll sing you a song and i'll try not to sing out of key
the good dinosaur went to the phone market and said that he needed to practice how fast he could truly type whether or not there are mistakes in what he is typing is really unimportant
why did that take so long hey I am in that crowd but this is a poor description of that method which is designed to make the actual connection and did I really use if(go) I
mean come on that is rediculous jean national richmond va happy happy happy having fun fun fun cause everything is happy and everything is nice for today
at least it is warm i dislike that somebody turned on the heat I mean come on that is redic*/
            if (go) {
                is = connection.getInputStream();
                bis = new BufferedReader(new InputStreamReader(is));
                Log.w(TAG, "5 Just made bis");
                try {
                    Log.w(TAG, "5.1 In the try");
                    byte[] contents = new byte[1024];
                    Log.w(TAG, "5.2 Made the byte[]");
                    int bytesRead = 0;
                    Log.w(TAG, "5.3 Made the num BytesRead");
                    Log.w(TAG, "5.4 Made the strNumsAll");
                    StringBuilder strNumsAll = new StringBuilder();
                    String line;
                    try {
                        if ((bis != null) && (((line = bis.readLine())) != null)) {
                            Log.w(TAG, "5.5 Before the append");
                            strNumsAll.append(line);
                            Log.w(TAG, "5.6 After the append");
                        }
                    } catch (Exception e) {
                        Log.w(TAG, "Catch 1 " + e);

                        if (scan != null) {
                            scan.close();
                        }
                        if (connection != null) {
                            Log.w(TAG, "Connection to disconnect inside of exception");
                            connection.disconnect();
                            Log.w(TAG, "Connection disconnected inside of exception");
                        }
                        if (is != null) {
                            try {
                                is.close();
                            } catch (Exception g) {
                                e.printStackTrace();
                            }
                        }
                        if (bis != null) {
                            try {
                                bis.close();
                            } catch (IOException g) {
                                e.printStackTrace();
                            }
                        }
                        go = false;
                    }

                    Log.w(TAG, "Before the printing go");
                    if (go) {
                        Log.w(TAG, "inside the printing go");

                        Double[] allFourNumbers = new Double[4];
                        scan = new Scanner(strNumsAll.toString());
                        for (int k = 0; k < allFourNumbers.length; k++) {

                            if (scan.hasNextDouble()) {
                                Intent broadcastIntent = new Intent();
                                broadcastIntent.setAction("response");
                                broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
                                switch (k) {
                                    case 0:
                                        broadcastIntent.putExtra("zero", scan.nextDouble());
                                    case 1:
                                        broadcastIntent.putExtra("one", scan.nextDouble());
                                    case 2:
                                        broadcastIntent.putExtra("two", scan.nextDouble());
                                    case 3:
                                        broadcastIntent.putExtra("three", scan.nextDouble());
                                }
                                if (!sent)
                                    Log.w(TAG, "sending broadcast");

                                sendBroadcast(broadcastIntent);

                            } else {
                                break;
                            }

                        }
                        scan.close();
                        Log.w(TAG, "6 returning the string " + strNumsAll);

                    }

                } finally {
                    Log.w(TAG, "7 Closing bis down");
                    bis.close();
                    is.close();
                    Log.w(TAG, "8 bis closed down");
                    connection.disconnect();
                    if (!sent) {
                        Intent broadcastIntent = new Intent();
                        broadcastIntent.setAction("response");
                        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
                        sendBroadcast(broadcastIntent);
                    }

                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Catch 3 " + e);
            e.printStackTrace();

            if (scan != null) {
                scan.close();
            }
            if (connection != null) {
                Log.w(TAG, "Connection to disconnect inside of exception");
                connection.disconnect();
                Log.w(TAG, "Connection disconnected inside of exception");
            }
            if (is != null) {
                try {
                    Log.w(TAG, "Trying to close is");
                    is.close();
                    Log.w(TAG, "Closed is");
                } catch (Exception g) {
                    e.printStackTrace();
                }
            }
            if (bis != null) {
                try {
                    Log.w(TAG, "Trying to close bis");
                    bis.close();
                    Log.w(TAG, "Closed bis");
                } catch (IOException g) {
                    e.printStackTrace();
                }

            }
            go = false;
            if (!sent) {
                Intent broadcastIntent = new Intent();
                broadcastIntent.setAction("response");
                broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
                sendBroadcast(broadcastIntent);
            }
        }
    }


}

