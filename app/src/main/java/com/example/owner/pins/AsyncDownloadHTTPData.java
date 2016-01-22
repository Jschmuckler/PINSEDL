package com.example.owner.pins;

import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

/**
 * Created by Owner on 12/7/2015.
 * WARNING CLASS ONLY USED IN HTTP VERSION OF PROGRAM.
 * Do not realease with bluetooth build.
 */
public class AsyncDownloadHTTPData extends AsyncTask<String, Void, String> {

    private static final String TAG = "AsyncDownloadHTTPData";
    public AsyncResponse delegate = null;
    private Scanner scan;
    private HttpURLConnection connection;
    private InputStream is;
    private BufferedReader bis;

    @Override
    protected String doInBackground(String... params) {

        Log.w(TAG, "1 Inside doInBackground");

        String url = params[0];
        try {
            Log.w(TAG, "2 Inside first try");

            URL url1 = new URL(url);

            connection = (HttpURLConnection) url1.openConnection();
            connection.setRequestProperty("Accept-Encoding", "");
            Log.w(TAG, "3 Opened connection");
            connection.setConnectTimeout(19000);
            connection.setReadTimeout(19000);
            connection.connect();
            Log.w(TAG, "4 Right after connect");

            try {
                int statusCode = connection.getResponseCode();
                Log.w(TAG, "4.05 Response code is " + statusCode);

                if (statusCode / 100 != 2) {
                    Log.w(TAG, "Connection Error");
                    connection.disconnect();
                    //  Log.e(TAG, "Error-connection.getResponseCode returned " + Integer.toString(statusCode));
                    return null;
                }
            } catch (EOFException e) {
                Log.w(TAG, "EOF On the response code");
                e.printStackTrace();
            }
            // if(connection.getInputStream().available()!=0){
            //   throw new IOException();}
            is = connection.getInputStream();
            Log.w(TAG, "4.1 get the InputStream");

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
                    //while ((bis != null) && bis.ready() && (((line = bis.readLine())) != null)) {
                    boolean reading = true;
                    while (reading) {
                        // Log.w(TAG,"5.45 Right before bis.readLine()");
                        if (bis.ready()) {
                            if (((line = bis.readLine())) != null) {
                                Log.w(TAG, "5.5 Before the append value of" + line);
                                strNumsAll.append(line);
                                Log.w(TAG, "5.6 After the append");
                                reading = false;
                            }
                        } else {
                            reading = false;
                        }
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
                    return null;

                }
                //while ((bytesRead = bis.read(contents)) != -1) {
                //    strNumsAll += new String(contents, 0, bytesRead);
                // }
                Log.w(TAG, "6 returning the string " + strNumsAll);

                Log.w(TAG, "7 Closing bis down");
                bis.close();
                is.close();
                Log.w(TAG, "8 bis closed down");
                connection.disconnect();

                return strNumsAll.toString();
            } catch (Exception e) {
                Log.w(TAG, "Catch 2 " + e);

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
                return null;
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


            return null;
        }
    }

    @Override
    protected void onPostExecute(String result) {
        if (result != null) {
            //  System.out.println("9 onPostExecute");
            Double[] allFourNumbers = new Double[4];
            scan = new Scanner(result);
            for (int k = 0; k < allFourNumbers.length; k++) {

                if (scan.hasNextDouble()) {
                    allFourNumbers[k] = scan.nextDouble();
                } else {
                    break;
                }

            }
            scan.close();
            delegate.processFinish(allFourNumbers);
        } else {
            Double[] badData = new Double[4];
            delegate.processFinish(badData);
        }
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        Log.w(TAG, "ASYNC BEING CANCELLED onCancelled being run");
        if (scan != null) {
            scan.close();
        }
        if (connection != null) {
            connection.disconnect();
        }
        if (is != null) {
            try {
                is.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (bis != null) {
            try {
                bis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Double[] badData = new Double[4];
        delegate.processFinish(badData);
    }

}
