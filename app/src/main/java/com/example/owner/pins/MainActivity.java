package com.example.owner.pins;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
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

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Iterator;
import java.util.Scanner;

/**
 * @author Jordan Schmuckler
 * @version 4.4
 */


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private MainActivity context = this;
    private MainActivity oldMain;

    private GraphView graph;
    private LineGraphSeries<DataPoint> series0, series1, series2, series3;
    private BluetoothAdapter btAdapter;
    private BluetoothDevice btDevice;

    private SharedPreferences settings;
    private TextView zero;
    private TextView one;
    private TextView two;
    private TextView three;

    private Switch mySwitch;
    private boolean switchPreviouslyActiveFlag;
    private boolean active = false;
    private boolean[] isSeriesActive = new boolean[4];

    private Handler btHandler;
    private BlueConnectedThread btConnectedThread;
    private Scanner getDoublesScanner;
    private Double units;
    private int time = 0;

    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 1;
    private static final int BLUETOOTH_ACTIVATE = 2;
    private final int SMOOTHING_DEFAULT = 10;
    private final double THRESHOLD_DEFAULT = 1.5;
    private final int X_RANGE = 20;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        settings = PreferenceManager.getDefaultSharedPreferences(this);

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
                    connectBT();

                } else {
                    mySwitch.setText("OFF");
                    active = isChecked;
                    if (btConnectedThread != null) {
                        btConnectedThread.cancel();
                    }
                    Log.w(TAG, "Active is " + active);
                }
            }
        });

        oldMain = (MainActivity) getLastCustomNonConfigurationInstance();
        graph = (GraphView) findViewById(R.id.graph);

        if (oldMain != null) {
            series0 = oldMain.series0;
            series1 = oldMain.series1;
            series2 = oldMain.series2;
            series3 = oldMain.series3;

            if (oldMain.time > X_RANGE) {
                graph.getViewport().setMinX(oldMain.time - X_RANGE);
                graph.getViewport().setMaxX(oldMain.time + 1);
            } else {
                graph.getViewport().setMinX(0);
                graph.getViewport().setMaxX(X_RANGE);
            }

            btConnectedThread = oldMain.btConnectedThread;

            time = oldMain.time;


            zero = (TextView) findViewById(R.id.DisplayValue0);
            one = (TextView) findViewById(R.id.DisplayValue1);
            two = (TextView) findViewById(R.id.DisplayValue2);
            three = (TextView) findViewById(R.id.DisplayValue3);
            Log.w(TAG, oldMain.switchPreviouslyActiveFlag + "is the old value before rotating");

            isSeriesActive = oldMain.isSeriesActive;


        } else {
            series0 = new LineGraphSeries<DataPoint>(new DataPoint[]{});
            series1 = new LineGraphSeries<DataPoint>(new DataPoint[]{});
            series2 = new LineGraphSeries<DataPoint>(new DataPoint[]{});
            series3 = new LineGraphSeries<DataPoint>(new DataPoint[]{});

            graph.getViewport().setMinX(0);
            graph.getViewport().setMaxX(X_RANGE);

            series0.setTitle("Zero");
            series1.setTitle("One");
            series2.setTitle("Two");
            series3.setTitle("Three");


            zero = (TextView) findViewById(R.id.DisplayValue0);
            one = (TextView) findViewById(R.id.DisplayValue1);
            two = (TextView) findViewById(R.id.DisplayValue2);
            three = (TextView) findViewById(R.id.DisplayValue3);


            for (int k = 0; k < 4; k++) {
                isSeriesActive[k] = true;
            }


        }

        graph.getViewport().setScrollable(true);
        graph.getViewport().setScalable(true);

        graph.getGridLabelRenderer().setHorizontalAxisTitle("Points");
        setUnits();
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


        graph.getGridLabelRenderer().setNumVerticalLabels(10);
        graph.getGridLabelRenderer().setNumHorizontalLabels(10);
        graph.getLegendRenderer().setVisible(true);

        setLineThickness();

        mySwitch.setClickable(false);
        Log.w(TAG, "just set clickable to false");

        checkBTState();

        series0.setOnDataPointTapListener(new OnDataPointTapListener() {
            @Override
            public void onTap(Series series, DataPointInterface dataPoint) {
                Toast.makeText(context, "Series0: " + dataPoint, Toast.LENGTH_SHORT).show();
            }
        });

        series1.setOnDataPointTapListener(new OnDataPointTapListener() {
            @Override
            public void onTap(Series series, DataPointInterface dataPoint) {
                Toast.makeText(context, "Series1: " + dataPoint, Toast.LENGTH_SHORT).show();
            }
        });

        series2.setOnDataPointTapListener(new OnDataPointTapListener() {
            @Override
            public void onTap(Series series, DataPointInterface dataPoint) {
                Toast.makeText(context, "Series2: " + dataPoint, Toast.LENGTH_SHORT).show();
            }
        });

        series3.setOnDataPointTapListener(new OnDataPointTapListener() {
            @Override
            public void onTap(Series series, DataPointInterface dataPoint) {
                Toast.makeText(context, "Series3: " + dataPoint, Toast.LENGTH_SHORT).show();
            }
        });


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.w(TAG, "In the onDestroy");
        if (btConnectedThread != null) {
            btConnectedThread.cancel();
        }
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
     *
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

            case R.id.action_bluetooth:
                checkBTState();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * Allows the state to be saved when the app is paused or stopped
     *
     * @return the instance of the activity that the app is in
     */
    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return this;
    }


    /**
     * Clears all of the data that has been recording to the graph
     */
    public void clearData() {
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(X_RANGE);

        series0.resetData(new DataPoint[]{});
        series1.resetData(new DataPoint[]{});
        series2.resetData(new DataPoint[]{});
        series3.resetData(new DataPoint[]{});
        time = 0;
    }

    /**
     * Averages the number of data points specified in settings, and compares the average value to the threshold passed in.
     * If the minimum number of data points do not exist then it compares only those that do exist.
     *
     * @param seriesNumber The number of the series whos values should be evaluated
     * @return true if the threshold is larger than the overall average, false if the overall average is larger than the threshold
     */
    private boolean isThresholdBiggerThanAverage(int seriesNumber) {
        Iterator<DataPoint> compValues;
        String smoothingSt = settings.getString("SMOOTHING", "10");
        int smoothing = SMOOTHING_DEFAULT;
        if (!(smoothingSt.equals(""))) {
            smoothing = Integer.parseInt(settings.getString("SMOOTHING", "10"));
        }

        if (seriesNumber == 0) {
            compValues = series0.getValues(time - smoothing, time);
        } else if (seriesNumber == 1) {
            compValues = series1.getValues(time - smoothing, time);
        } else if (seriesNumber == 2) {
            compValues = series2.getValues(time - smoothing, time);
        } else {
            compValues = series3.getValues(time - smoothing, time);
        }
        double sum = 0;
        while (compValues.hasNext()) {
            DataPoint tempData = compValues.next();
            sum = sum + tempData.getY();
        }

        String thresholdSt = settings.getString("THRESHOLD", "1.5");
        double threshold = THRESHOLD_DEFAULT;
        if (!(thresholdSt.equals(""))) {
            threshold = Double.parseDouble(settings.getString("THRESHOLD", "1.5"));
        }
        threshold = threshold * units;
        double average;

        if (time < smoothing) {
            average = sum / time;
        } else {
            average = sum / ((double) smoothing + 1);
        }
        if (threshold > average) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Saves the neccesary values when the activity is paused.
     */
    @Override
    public void onPause() {
        super.onPause();
        //try{unregisterReceiver(receiver);}
        //catch (IllegalArgumentException e){}
        switchPreviouslyActiveFlag = mySwitch.isChecked();
        //mySwitch.setChecked(false);
    }

    /**
     * Checks that the device supports bluetooth.
     * If it does, checks that bluetooth is inabled.
     * If not, requests bluetooth be started.
     * If so, calls getBondedDevices() method.
     */
    private void checkBTState() {

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null) {
            Log.w(TAG, "Device does not support bluetooth, abort.");
            new AlertDialog.Builder(this)
                    .setTitle("Device does not support bluetooth.")
                    .setMessage("Device does not have bluetooth and therefore cannot use this application.")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setCancelable(false)
                    .show();
        } else {
            if (btAdapter.isEnabled()) {
                Log.d(TAG, "...Bluetooth is enabled...");
                getBondedDevices();
            } else {
                //Prompt user to turn on Bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, BLUETOOTH_ACTIVATE);
            }
        }
    }

    /**
     * Double checks the user has not turned of bluetooth after turning it on.
     * Starts the DeviceListActivity in order to find devices for the user to then select.
     */
    private void getBondedDevices() {
        if (btAdapter.isEnabled()) {
            Intent deviceList = new Intent(this, DeviceListActivity.class);

            startActivityForResult(deviceList, REQUEST_CONNECT_DEVICE_INSECURE);
        }

    }

    /**
     * On the callback from activities:
     * REQUEST_CONNECT_DEVICE_INSECURE:
     * If a device was selected sets btDevice to that device
     * If no device was selected call checkBTState() to re-initialize user selection of a device.
     * BLUETOOTH_ACTIVATE:
     * Call checkBTState to check if the user has now turned on bluetooth or not.
     */

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_INSECURE:
                if (resultCode == Activity.RESULT_OK) {
                    String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    btDevice = btAdapter.getRemoteDevice(address);
                    Toast.makeText(this, btDevice.getName() + " is ready.", Toast.LENGTH_SHORT).show();
                    mySwitch.setClickable(true);
                    if (oldMain != null)
                        if (oldMain.mySwitch.isChecked()) {
                            mySwitch.setChecked(false);
                            mySwitch.setChecked(true);

                        }
                }
                if (resultCode == Activity.RESULT_CANCELED) {
                    checkBTState();
                }
                break;

            case BLUETOOTH_ACTIVATE:
                checkBTState();
                break;
        }
    }

    /**
     * Creates a BlueConnectedThread to connect the bluetooth device and start collecting data.
     * Uses a handler to receive the data back and call to add the data with addDataManagement.
     */
    private void connectBT() {

        if (btDevice != null) {

            btHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    byte[] writeBuf = (byte[]) msg.obj;
                    int begin = (int) msg.arg1;
                    int end = (int) msg.arg2;
                    Double[] currentData = new Double[4];

                    switch (msg.what) {
                        case 1:
                            String writeMessage = new String(writeBuf);
                            writeMessage = writeMessage.substring(begin, end);
                            getDoublesScanner = new Scanner(writeMessage);
                            for (int k = 0; k < 4; k++) {
                                if (getDoublesScanner.hasNextDouble()) {
                                    currentData[k] = getDoublesScanner.nextDouble();
                                }
                            }

                            addDataManagement(currentData);
                            break;
                    }
                }
            };

            btConnectedThread = new BlueConnectedThread(btHandler);
            BlueConnectThread btConnectThread = new BlueConnectThread(btDevice, btAdapter, btHandler, btConnectedThread);
            btConnectThread.start();
        }
    }

    /**
     * Adds the data to the graph.
     * Manages the labels zero-three in both value and color.
     */
    public void addDataManagement(Double[] output) {
        NumberFormat formatter = new DecimalFormat("#0.00");

        if (output[0] != null && output[1] != null && output[2] != null && output[3] != null) {
            for (int k = 0; k < 4; k++) {
                output[k] = output[k] * units;
                switch (units.toString()) {
                    case "1000.0":
                        formatter = new DecimalFormat("#0000.0");
                        break;

                    case "1.0":
                        formatter = new DecimalFormat("#0.00");
                        break;

                    case "0.001":
                        formatter = new DecimalFormat("#0.00000");
                        break;
                }

            }

            if (output[0] != null && output[1] != null && output[2] != null && output[3] != null) {
                if (zero != null && one != null && two != null && three != null) {
                    zero.setText("0: " + formatter.format(output[0]).toString());
                    one.setText("1: " + formatter.format(output[1]).toString());
                    two.setText("2: " + formatter.format(output[2]).toString());
                    three.setText("3: " + formatter.format(output[3]).toString());


                    DataPoint output0 = new DataPoint(time, output[0]);
                    DataPoint output1 = new DataPoint(time, output[1]);
                    DataPoint output2 = new DataPoint(time, output[2]);
                    DataPoint output3 = new DataPoint(time, output[3]);

                    if (time < X_RANGE) {
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

                    if (isThresholdBiggerThanAverage(0)) {
                        zero.setBackgroundColor(Color.GREEN);
                    } else {
                        zero.setBackgroundColor(Color.RED);
                    }

                    if (isThresholdBiggerThanAverage(1)) {
                        one.setBackgroundColor(Color.GREEN);
                    } else {
                        one.setBackgroundColor(Color.RED);
                    }

                    if (isThresholdBiggerThanAverage(2)) {

                        two.setBackgroundColor(Color.GREEN);
                    } else {
                        two.setBackgroundColor(Color.RED);
                    }
                    if (isThresholdBiggerThanAverage(3)) {

                        three.setBackgroundColor(Color.GREEN);
                    } else {
                        three.setBackgroundColor(Color.RED);
                    }
                } else {
                    Log.w(TAG, "Text fields are null");
                }
            } else {
                Log.w(TAG, "Getting blank data");
            }
        }
    }

    /**
     * Activates when the app is resumed.
     */
    @Override
    public void onResume() {
        super.onResume();
        setUnits();
        setLineThickness();

    }

    /**
     * Manages the units variable for updating what units the app should be in based on
     * shared settings.
     */
    private void setUnits() {
        units = Double.parseDouble(settings.getString("UNIT", "1.0"));
        switch (units.toString()) {
            case "1000.0":
                graph.getGridLabelRenderer().setVerticalAxisTitle("Millivolts");
                break;

            case "1.0":
                graph.getGridLabelRenderer().setVerticalAxisTitle("Volts");
                break;

            case "0.001":
                graph.getGridLabelRenderer().setVerticalAxisTitle("Kilovolts");
                break;
        }

    }

    /**
     * Manages resetting the line thickness based on shared
     * settings
     */
    private void setLineThickness() {
        String thicknessSt = settings.getString("THICKNESS", "10");
        int thickness = 10;
        if (!(thicknessSt.equals(""))) {
            thickness = Integer.parseInt(settings.getString("THICKNESS", "10"));
        }

        series0.setThickness(thickness);
        series1.setThickness(thickness);
        series2.setThickness(thickness);
        series3.setThickness(thickness);
    }
}