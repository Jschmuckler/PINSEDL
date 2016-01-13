package com.example.owner.pins;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.Series;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

/**
 * @author Jordan Schmuckler
 * @version 4.4
 */


public class MainActivity extends AppCompatActivity implements AsyncResponse {

    private static final String TAG = "MainActivity";
    SharedPreferences settings;
    TextView zero;
    TextView one;
    TextView two;
    TextView three;
    public String url = "http://192.168.1.154";
    boolean active = false;
    private Handler handler = new Handler(Looper.getMainLooper());
    private TaskCanceler taskCanceler;
    private DataPoint[] values0 = new DataPoint[50];
    private int xRange;
    private LineGraphSeries<DataPoint> series0, series1, series2, series3;
    int time = 0;
    int smoothing;
    Double threshold;
    Switch mySwitch;
    boolean switchPreviouslyActiveFlag;
    GraphView graph;
    private MainActivity mainActivity = this;
    boolean[] isSeriesActive = new boolean[4];
    private ResponseReceiver receiver;
    private BluetoothAdapter btAdapter;
    private BluetoothDevice btDevice;
    Handler btHandler;
    Scanner getDoublesScanner;
    Double[] currentData;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        settings =  PreferenceManager.getDefaultSharedPreferences(this);
        xRange = 20;
        smoothing = Integer.parseInt(settings.getString("SMOOTHING", "10"));
        threshold = Double.parseDouble(settings.getString("THRESHOLD", "1.5"));
        url = "http://" + settings.getString("ADDRESS", "");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mySwitch = (Switch) findViewById(R.id.active);
        mySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    mySwitch.setText("ON");
                    active = isChecked;
                    Log.w(TAG, "Active is " + active);
                    Log.d(TAG, "Active is " + active);
                    connectBT();

                    //getAndSetData();

                } else {
                    mySwitch.setText("OFF");
                    active = isChecked;
                    Log.w(TAG, "Active is " + active);
                    Log.d(TAG, "Active is " + active);
                }
            }
        });

        MainActivity oldMain = (MainActivity) getLastCustomNonConfigurationInstance();
        graph = (GraphView) findViewById(R.id.graph);

        if (oldMain != null) {
            series0 = oldMain.series0;
            series1 = oldMain.series1;
            series2 = oldMain.series2;
            series3 = oldMain.series3;

            if (oldMain.time > xRange) {
                graph.getViewport().setMinX(oldMain.time - xRange);
                graph.getViewport().setMaxX(oldMain.time + 1);
            } else {
                graph.getViewport().setMinX(0);
                graph.getViewport().setMaxX(xRange);
            }
            time = oldMain.time;

            mySwitch.setChecked(oldMain.switchPreviouslyActiveFlag);

            isSeriesActive = oldMain.isSeriesActive;
        } else {
            series0 = new LineGraphSeries<DataPoint>(new DataPoint[]{});
            series1 = new LineGraphSeries<DataPoint>(new DataPoint[]{});
            series2 = new LineGraphSeries<DataPoint>(new DataPoint[]{});
            series3 = new LineGraphSeries<DataPoint>(new DataPoint[]{});

            graph.getViewport().setMinX(0);
            graph.getViewport().setMaxX(xRange);

            series0.setTitle("Zero");
            series1.setTitle("One");
            series2.setTitle("Two");
            series3.setTitle("Three");


            for (int k = 0; k < 4; k++) {
                isSeriesActive[k] = true;
            }
        }

        graph.getViewport().setScrollable(true);
        graph.getViewport().setScalable(true);

        graph.getGridLabelRenderer().setHorizontalAxisTitle("Points");
        graph.getGridLabelRenderer().setVerticalAxisTitle("Voltage");
        if (isSeriesActive[0])
            graph.addSeries(series0);
        if (isSeriesActive[1])
            graph.addSeries(series1);
        if (isSeriesActive[2])
            graph.addSeries(series2);
        if (isSeriesActive[3])
            graph.addSeries(series3);

        series0.setColor(Color.RED);
        series1.setColor(Color.GREEN);
        series2.setColor(Color.BLUE);
        series3.setColor(Color.BLACK);

        graph.getGridLabelRenderer().setNumVerticalLabels(15);
        graph.getGridLabelRenderer().setNumHorizontalLabels(20);
        graph.getLegendRenderer().setVisible(true);

        zero = (TextView) findViewById(R.id.DisplayValue0);
        one = (TextView) findViewById(R.id.DisplayValue1);
        two = (TextView) findViewById(R.id.DisplayValue2);
        three = (TextView) findViewById(R.id.DisplayValue3);
        zero.setText("0");
        one.setText("1");
        two.setText("2");
        three.setText("3");





        series0.setOnDataPointTapListener(new OnDataPointTapListener() {
            @Override
            public void onTap(Series series, DataPointInterface dataPoint) {
                //zero.setText("" + dataPoint);
                Toast.makeText(mainActivity, "Series0: " + dataPoint, Toast.LENGTH_SHORT).show();
            }
        });

        series1.setOnDataPointTapListener(new OnDataPointTapListener() {
            @Override
            public void onTap(Series series, DataPointInterface dataPoint) {
                //zero.setText("" + dataPoint);
                Toast.makeText(mainActivity, "Series1: " + dataPoint, Toast.LENGTH_SHORT).show();
            }
        });

        series2.setOnDataPointTapListener(new OnDataPointTapListener() {
            @Override
            public void onTap(Series series, DataPointInterface dataPoint) {
                //zero.setText("" + dataPoint);
                Toast.makeText(mainActivity, "Series2: " + dataPoint, Toast.LENGTH_SHORT).show();
            }
        });

        series3.setOnDataPointTapListener(new OnDataPointTapListener() {
            @Override
            public void onTap(Series series, DataPointInterface dataPoint) {
                //zero.setText("" + dataPoint);
                Toast.makeText(mainActivity, "Series3: " + dataPoint, Toast.LENGTH_SHORT).show();
            }
        });

        IntentFilter filter = new IntentFilter("response");
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        receiver = new ResponseReceiver();
        registerReceiver(receiver, filter);

        checkBTState();
        getBondedDevices();
         currentData = new Double[4];

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * Take the selected item from the list and do the according actions.
     * Settings: Open settings fragment
     * About: Open information about the application
     * Eraser: Erase all of the data on the graph
     * Zero,One,Two,Three: Toggle on or off the corresponding series on the chart
     * @param item item that has been selected
     * @return true the action has been handled, false the action has not been handled
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {

            case R.id.action_settings:
                Intent openSettings = new Intent(this, SettingsActivity.class);
                startActivity(openSettings);
                break;

            case R.id.action_about:
                String newline = System.getProperty("line.separator");
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder
                        .setTitle("About")
                        .setMessage("PINS is designed for the reading and analysis of a four input signal system." + newline + "Author: Jordan Schmuckler" + newline + "EDL")
                        .setCancelable(true)
                        .show();
                break;

            case R.id.action_eraser:
                clearData();
                break;

            case R.id.action_series_zero:
                if (isSeriesActive[0]) {
                    graph.removeSeries(series0);
                    isSeriesActive[0] = false;
                } else {
                    graph.addSeries(series0);
                    isSeriesActive[0] = true;
                }
                break;

            case R.id.action_series_one:
                if (isSeriesActive[1]) {
                    graph.removeSeries(series1);
                    isSeriesActive[1] = false;
                } else {
                    graph.addSeries(series1);
                    isSeriesActive[1] = true;
                }
                break;

            case R.id.action_series_two:
                if (isSeriesActive[2]) {
                    graph.removeSeries(series2);
                    isSeriesActive[2] = false;
                } else {
                    graph.addSeries(series2);
                    isSeriesActive[2] = true;
                }
                break;

            case R.id.action_series_three:
                if (isSeriesActive[3]) {
                    graph.removeSeries(series3);
                    isSeriesActive[3] = false;
                } else {
                    graph.addSeries(series3);
                    isSeriesActive[3] = true;
                }
                break;

            case R.id.action_resetArduino:
                RestartArduino restart = new RestartArduino();
                restart.execute(url);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Class:Receive the broadcast back from the thread, extract the data and set it to the graph and labels.
     */
    public class ResponseReceiver extends BroadcastReceiver {
        public String response = "response";
        @Override
        public void onReceive(Context context, Intent intent) {

            Log.w(TAG, "Process Finished");
            Double[] output =
                    {       intent.getDoubleExtra("zero",-1),
                            intent.getDoubleExtra("one",-1),
                            intent.getDoubleExtra("two",-1),
                            intent.getDoubleExtra("three",-1),};

            if (output[0] != -1 && output[1] != -1 && output[2] != -1 && output[3] != -1) {

                zero.setText(output[0].toString());
                one.setText(output[1].toString());
                two.setText(output[2].toString());
                three.setText(output[3].toString());
                Log.w(TAG, "Set the text");
                DataPoint output0 = new DataPoint(time,output[0]);
                DataPoint output1 = new DataPoint(time,output[1]);
                DataPoint output2 = new DataPoint(time,output[2]);
                DataPoint output3 = new DataPoint(time,output[3]);

                if(time<xRange) {
                    series0.appendData(output0, false, 500);
                    series1.appendData(output1, false, 500);
                    series2.appendData(output2, false, 500);
                    series3.appendData(output3, false, 500);

                }
                else {
                    series0.appendData(output0, true, 500);
                    series1.appendData(output1, true, 500);
                    series2.appendData(output2, true, 500);
                    series3.appendData(output3, true, 500);
                }

                time++;
                //threshold,series
                TextView textZeroData = (TextView)findViewById(R.id.DisplayValue0);
                TextView textOneData = (TextView)findViewById(R.id.DisplayValue1);
                TextView textTwoData = (TextView)findViewById(R.id.DisplayValue2);
                TextView textThreeData = (TextView)findViewById(R.id.DisplayValue3);

                if(isThresholdBiggerThanAverage(threshold,0))
                {
                    textZeroData.setBackgroundColor(Color.GREEN);
                }
                else{
                    textZeroData.setBackgroundColor(Color.RED);
                }

                if(isThresholdBiggerThanAverage(threshold,1))
                {
                    textOneData.setBackgroundColor(Color.GREEN);
                }
                else{
                    textOneData.setBackgroundColor(Color.RED);
                }

                if(isThresholdBiggerThanAverage(threshold,2))
                {

                    textTwoData.setBackgroundColor(Color.GREEN);
                }
                else{
                    textTwoData.setBackgroundColor(Color.RED);
                }
                if(isThresholdBiggerThanAverage(threshold,3))
                {

                    textThreeData.setBackgroundColor(Color.GREEN);
                }
                else{
                    textThreeData.setBackgroundColor(Color.RED);
                }
            }
            if(active)
            {
                Log.w(TAG, "Launching a new helper");
                getAndSetData();
            }
        }
    }

    /**
     * Allows the state to be saved when the app is paused or stopped
     * @return the instance of the activity that the app is in
     */
    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return this;
    }

    /**
     * For use with the async task strategy of data collection.
     * Assigns the data to the appropriate series on the graph and assigns the values to the appropriate text fields,
     * then calls another async task to begin to make a network connection.
     * @param output A Double array that contains all of the data the asynctask has collected from the network
     */
    @Override
    public void processFinish(Double[] output) {
        Log.w(TAG, "Process Finished");
        if (output[0] != null && output[1] != null && output[2] != null && output[3] != null) {
            zero.setText(output[0].toString());
            one.setText(output[1].toString());
            two.setText(output[2].toString());
            three.setText(output[3].toString());

            Log.w(TAG, "Set the text");

            DataPoint output0 = new DataPoint(time, output[0]);
            DataPoint output1 = new DataPoint(time, output[1]);
            DataPoint output2 = new DataPoint(time, output[2]);
            DataPoint output3 = new DataPoint(time, output[3]);

            if (time < xRange) {
                series0.appendData(output0, false, 300);
                series1.appendData(output1, false, 300);
                series2.appendData(output2, false, 300);
                series3.appendData(output3, false, 300);

            } else {
                series0.appendData(output0, true, 300);
                series1.appendData(output1, true, 300);
                series2.appendData(output2, true, 300);
                series3.appendData(output3, true, 300);
            }
            time++;

            TextView textZeroData = (TextView) findViewById(R.id.DisplayValue0);
            TextView textOneData = (TextView) findViewById(R.id.DisplayValue1);
            TextView textTwoData = (TextView) findViewById(R.id.DisplayValue2);
            TextView textThreeData = (TextView) findViewById(R.id.DisplayValue3);

            if (isThresholdBiggerThanAverage(threshold, 0)) {
                textZeroData.setBackgroundColor(Color.GREEN);
            } else {
                textZeroData.setBackgroundColor(Color.RED);
            }

            if (isThresholdBiggerThanAverage(threshold, 1)) {
                textOneData.setBackgroundColor(Color.GREEN);
            } else {
                textOneData.setBackgroundColor(Color.RED);
            }

            if (isThresholdBiggerThanAverage(threshold, 2)) {

                textTwoData.setBackgroundColor(Color.GREEN);
            } else {
                textTwoData.setBackgroundColor(Color.RED);
            }
            if (isThresholdBiggerThanAverage(threshold, 3)) {

                textThreeData.setBackgroundColor(Color.GREEN);
            } else {
                textThreeData.setBackgroundColor(Color.RED);
            }
        }
        if (active) {
            Log.w(TAG, "Launching a new helper");
            getAndSetData();
        }
    }

    public void getAndSetData() {
        Log.w(TAG, "started getAndSetData");
        getAndSetDataHelper();
    }

    private void getAndSetDataHelper() {

        //Comment out to switch to async task
        Intent httpDownload = new Intent(this, ThreadedDownloadHTTP.class);
        httpDownload.putExtra("URL", url);
        startService(httpDownload);

        //uncomment to switch to Async task
      /*  if (taskCanceler != null && handler != null) {
            handler.removeCallbacks(taskCanceler);
        }
        AsyncDownloadHTTPData asyncGetData = new AsyncDownloadHTTPData();
        taskCanceler = new TaskCanceler(asyncGetData, this);
        asyncGetData.delegate = this;
        handler.postDelayed(taskCanceler, 12000);
        asyncGetData.execute(url);*/
    }

    /**
     * Clears all of the data that has been recording to the graph
     */
    public void clearData() {
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(xRange);

        series0.resetData(new DataPoint[]{});
        series1.resetData(new DataPoint[]{});
        series2.resetData(new DataPoint[]{});
        series3.resetData(new DataPoint[]{});
        time = 0;
    }
    /**
     * Averages the number of data points specified in settings, and compares the average value to the threshold passed in.
     * If the minimum number of data points do not exist then it compares only those that do exist.
     * @param threshold The threshold that is being compared to
     * @param seriesNumber The number of the series whos values should be evaluated
     * @return true if the threshold is larger than the overall average, false if the overall average is larger than the threshold
     */
    private boolean isThresholdBiggerThanAverage(double threshold, int seriesNumber) {
        Iterator<DataPoint> compValues;
        Log.w(TAG, "Threshold: " + threshold);

        if (seriesNumber == 0) {
            compValues = series0.getValues(time - smoothing, time );
            Log.w(TAG, "  Series 0 ");
        }
        else if (seriesNumber == 1) {
            compValues = series1.getValues(time - smoothing, time );
            Log.w(TAG, "  Series 1 ");
        }
        else if (seriesNumber == 2) {
            compValues = series2.getValues(time - smoothing, time );
            Log.w(TAG, "  Series 2 ");
        }
        else {
            compValues = series3.getValues(time - smoothing, time );
            Log.w(TAG, "  Series 3 ");
        }
        double sum = 0;
        while (compValues.hasNext()) {
            DataPoint tempData = compValues.next();
            Log.w(TAG, "  Data point:  " + tempData.getY());
            sum = sum + tempData.getY();
        }
        Log.w(TAG, "         Sum:  " + sum);
        Log.w(TAG, " Smoothing  " + smoothing);

        double average;
        if (time < smoothing) {
            average = sum / time;
            Log.w(TAG, "  Average: " + average);
        } else {
            average = sum / ((double)smoothing+1);
            Log.w(TAG, "  Average: " + average);
        }
        if (threshold > average) {
            Log.w(TAG, "True ");
            return true;
        } else {
            Log.w(TAG, "False ");
            return false;
        }
    }
    /**
     * Saves the neccesary values when the activity is paused.
     */
    @Override
    public void onPause() {
        super.onPause();
        try{unregisterReceiver(receiver);}
        catch (IllegalArgumentException e){}
        switchPreviouslyActiveFlag = mySwitch.isChecked();
        mySwitch.setChecked(false);
    }

    private void checkBTState() {
        // Check for Bluetooth support and then check to make sure it is turned on

        // Emulator doesn't support Bluetooth and will return null
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null) {
// Device does not support Bluetooth
        }
        if(btAdapter==null) {
            Log.w(TAG,"Device does not support bluetooth, abort.");
        } else {
            if (btAdapter.isEnabled()) {
                Log.d(TAG, "...Bluetooth is enabled...");
            } else {
                //Prompt user to turn on Bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    private void getBondedDevices()
    {
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
        String deviceList = " ";
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                deviceList = deviceList + device.getName() + "/n";
                if(device.getName().equals("Adafruit EZ-Link 80f2"))
                {
                    Log.w(TAG,device.getName()+ " found.");
                btDevice = device;
                }

                else
                {
                    Log.w(TAG,"Adafruit EZ-Link 80f2 not found");
                }
            }
        }
        Toast.makeText(this, deviceList, Toast.LENGTH_LONG).show();


    }


    private void connectBT()
    {

            if (btDevice != null)
            {

                btHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    byte[] writeBuf = (byte[]) msg.obj;
                    int begin = (int)msg.arg1;
                    int end = (int)msg.arg2;

                    switch(msg.what) {
                        case 1:
                            String writeMessage = new String(writeBuf);
                            writeMessage = writeMessage.substring(begin, end);
                            getDoublesScanner = new Scanner(writeMessage);
                            for(int k = 0;k<4;k++) {
                                if (getDoublesScanner.hasNextDouble()) {
                                    currentData[k] = getDoublesScanner.nextDouble();
                                }
                            }

                            addDataManagement(currentData);
                            break;
                    }
                }
            };


                BlueConnectThread btConnectThread = new BlueConnectThread(btDevice, btAdapter,btHandler);
            btConnectThread.start();
        }
    }

public void addDataManagement(Double[] output)
{
    if (output[0] != null && output[1] != null && output[2] != null && output[3] != null) {
        zero.setText(output[0].toString());
        one.setText(output[1].toString());
        two.setText(output[2].toString());
        three.setText(output[3].toString());


        DataPoint output0 = new DataPoint(time, output[0]);
        DataPoint output1 = new DataPoint(time, output[1]);
        DataPoint output2 = new DataPoint(time, output[2]);
        DataPoint output3 = new DataPoint(time, output[3]);

        if (time < xRange) {
            series0.appendData(output0, false, 300);
            series1.appendData(output1, false, 300);
            series2.appendData(output2, false, 300);
            series3.appendData(output3, false, 300);

        } else {
            series0.appendData(output0, true, 300);
            series1.appendData(output1, true, 300);
            series2.appendData(output2, true, 300);
            series3.appendData(output3, true, 300);
        }
        time++;

        TextView textZeroData = (TextView) findViewById(R.id.DisplayValue0);
        TextView textOneData = (TextView) findViewById(R.id.DisplayValue1);
        TextView textTwoData = (TextView) findViewById(R.id.DisplayValue2);
        TextView textThreeData = (TextView) findViewById(R.id.DisplayValue3);

        threshold = Double.parseDouble(settings.getString("THRESHOLD", "1.5"));
        smoothing = Integer.parseInt(settings.getString("SMOOTHING", "10"));


        if (isThresholdBiggerThanAverage(threshold, 0)) {
            textZeroData.setBackgroundColor(Color.GREEN);
        } else {
            textZeroData.setBackgroundColor(Color.RED);
        }

        if (isThresholdBiggerThanAverage(threshold, 1)) {
            textOneData.setBackgroundColor(Color.GREEN);
        } else {
            textOneData.setBackgroundColor(Color.RED);
        }

        if (isThresholdBiggerThanAverage(threshold, 2)) {

            textTwoData.setBackgroundColor(Color.GREEN);
        } else {
            textTwoData.setBackgroundColor(Color.RED);
        }
        if (isThresholdBiggerThanAverage(threshold, 3)) {

            textThreeData.setBackgroundColor(Color.GREEN);
        } else {
            textThreeData.setBackgroundColor(Color.RED);
        }
    }
}

    /**
     * Activates when the app is resumed.
     * Checks to ensure that the IP address from settings is not malformed/is a valid IP.
     */
    @Override
    public void onResume() {
        super.onResume();
        String urlToValidate = settings.getString("ADDRESS", "");
        IPAddressValidate IPValidator = new IPAddressValidate();
        if (IPValidator.validate(urlToValidate)) {
            mySwitch.setClickable(true);
            url = "http://" + settings.getString("ADDRESS", "");
        } else {
            mySwitch.setClickable(false);
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder
                    .setTitle("MALFORMED IP")
                    .setMessage("The IP Address entered is not valid. Please enter a valid address.")
                    .setCancelable(true)
                    .show();
        }
    }
}