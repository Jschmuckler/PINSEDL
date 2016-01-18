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
import android.support.v4.content.ContextCompat;
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
import com.jjoe64.graphview.LegendRenderer;
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

        initializeOnOffSwitch();

        oldMain = (MainActivity) getLastCustomNonConfigurationInstance();
        graph = (GraphView) findViewById(R.id.graph);

        if (oldMain != null) {
            manageScreenRotationResets();
        } else {
            firstTimeOnlyInitializations();
        }
        assignGraphAttributes();

        mySwitch.setClickable(false);
        Log.w(TAG, "just set clickable to false");

        checkBTState();
        assignOnClickSeriesListeners();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.w(TAG, "In the onDestroy");
        if (btConnectedThread != null) {
            btConnectedThread.cancel();
        }
    }

    /**
     * Saves the neccesary values when the activity is paused.
     */
    @Override
    public void onPause() {
        super.onPause();
        switchPreviouslyActiveFlag = mySwitch.isChecked();
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
        int id = item.getItemId();

        switch (id) {

            case R.id.action_settings:
                Intent openSettings = new Intent(this, SettingsActivity.class);
                startActivity(openSettings);
                break;

            case R.id.action_about:
                String newline = System.getProperty("line.separator");
                AlertDialog.Builder alertDialogBuilderAbout = new AlertDialog.Builder(this);
                alertDialogBuilderAbout
                        .setTitle(getResources().getString(R.string.about_title))
                        .setMessage(getResources().getString(R.string.about_part_one) + newline +
                                        getResources().getString(R.string.about_part_two) + newline + newline +
                                        getResources().getString(R.string.about_part_three) +
                                        getResources().getString(R.string.about_part_four) + newline + newline +
                                        getResources().getString(R.string.about_part_five) + newline +
                                        getResources().getString(R.string.about_part_six) + newline + newline +
                                        getResources().getString(R.string.about_part_seven) + newline +
                                        getResources().getString(R.string.about_part_eight))
                                .setCancelable(true)
                                .show();
                break;

            case R.id.action_help:
                String newlineA = System.getProperty("line.separator");
                AlertDialog.Builder alertDialogBuilderHelp = new AlertDialog.Builder(this);
                alertDialogBuilderHelp
                        .setTitle(getResources().getString(R.string.help_title))
                        .setMessage(getResources().getString(R.string.help_part_one) + newlineA +
                                getResources().getString(R.string.help_part_two) + newlineA + newlineA +
                                getResources().getString(R.string.help_part_three) + newlineA +
                                getResources().getString(R.string.help_part_four) + newlineA + newlineA +
                                getResources().getString(R.string.help_part_five) + newlineA +
                                getResources().getString(R.string.help_part_six) +
                                getResources().getString(R.string.help_part_seven))
                        .setCancelable(true)
                        .show();
                break;
            case R.id.action_eraser:
                clearData();
                break;

            case R.id.action_series_zero:
                toggleSeriesOnOff(0, series0);
                break;

            case R.id.action_series_one:
                toggleSeriesOnOff(1, series1);
                break;

            case R.id.action_series_two:
                toggleSeriesOnOff(2, series2);
                break;

            case R.id.action_series_three:
                toggleSeriesOnOff(3, series3);
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
    private void clearData() {
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
                    .setTitle(getResources().getString(R.string.bluetooth_unsupported_title))
                    .setMessage(getResources().getString(R.string.bluetooth_unsupported_text))
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
     * Saves variables from the old activity to the new activty to continue
     * on proccessing after screen rotation or other pauses.
     */
    private void manageScreenRotationResets() {
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
    }

    /**
     * Toggles the series passed to it on or off.
     *
     * @param seriesNum The number associated with the series being passed in
     * @param series    The reference to the series to be toggled
     */
    private void toggleSeriesOnOff(int seriesNum, Series series) {
        if (isSeriesActive[seriesNum]) {
            graph.removeSeries(series);
            isSeriesActive[seriesNum] = false;
        } else {
            graph.addSeries(series);
            isSeriesActive[seriesNum] = true;
        }
    }

    public void initializeOnOffSwitch() {
        mySwitch = (Switch) findViewById(R.id.active);
        mySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    mySwitch.setText(getResources().getString(R.string.on_label));
                    active = isChecked;
                    Log.w(TAG, "Active is " + active);
                    connectBT();

                } else {
                    mySwitch.setText(getResources().getString(R.string.off_label));
                    active = isChecked;
                    if (btConnectedThread != null) {
                        btConnectedThread.cancel();
                    }
                    Log.w(TAG, "Active is " + active);
                }
            }
        });
    }

    /**
     * Initializes all variables that need to be initialized only when the activity
     * has not existed previously within this run.
     */
    private void firstTimeOnlyInitializations() {
        series0 = new LineGraphSeries<DataPoint>(new DataPoint[]{});
        series1 = new LineGraphSeries<DataPoint>(new DataPoint[]{});
        series2 = new LineGraphSeries<DataPoint>(new DataPoint[]{});
        series3 = new LineGraphSeries<DataPoint>(new DataPoint[]{});

        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(X_RANGE);


        series0.setTitle(getResources().getString(R.string.series_title_zero));
        series1.setTitle(getResources().getString(R.string.series_title_one));
        series2.setTitle(getResources().getString(R.string.series_title_two));
        series3.setTitle(getResources().getString(R.string.series_title_three));


        zero = (TextView) findViewById(R.id.DisplayValue0);
        one = (TextView) findViewById(R.id.DisplayValue1);
        two = (TextView) findViewById(R.id.DisplayValue2);
        three = (TextView) findViewById(R.id.DisplayValue3);


        for (int k = 0; k < 4; k++) {
            isSeriesActive[k] = true;
        }
    }

    /**
     * Sets up the graphview.
     */
    private void assignGraphAttributes() {
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
        graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);

        setLineThickness();
    }

    /**
     * Gives listeners to each individual series so they display their data
     * at whatever point the user taps on.
     */
    private void assignOnClickSeriesListeners() {
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
                    Toast.makeText(this, btDevice.getName() + getResources().getString(R.string.is_ready), Toast.LENGTH_SHORT).show();
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
                    int begin = (int)msg.arg1;
                    int end = (int) msg.arg2;
                    Double[] currentData = new Double[4];

                    switch (msg.what) {
                        case 1:

                            String writeMessage = new String(writeBuf);
                            writeMessage = writeMessage.substring(begin, end);
                            Scanner getDoublesScanner = new Scanner(writeMessage);
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
                        zero.setBackgroundColor(Color.TRANSPARENT);
                    } else {
                        zero.setBackgroundColor((ContextCompat.getColor(this, R.color.color_outside_threshold)));
                    }

                    if (isThresholdBiggerThanAverage(1)) {
                        one.setBackgroundColor(Color.TRANSPARENT);
                    } else {
                        one.setBackgroundColor((ContextCompat.getColor(this, R.color.color_outside_threshold)));
                    }

                    if (isThresholdBiggerThanAverage(2)) {

                        two.setBackgroundColor(Color.TRANSPARENT);
                    } else {
                        two.setBackgroundColor((ContextCompat.getColor(this, R.color.color_outside_threshold)));
                    }
                    if (isThresholdBiggerThanAverage(3)) {

                        three.setBackgroundColor(Color.TRANSPARENT);
                    } else {
                        three.setBackgroundColor((ContextCompat.getColor(this, R.color.color_outside_threshold)));
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
     * Manages the units variable for updating what units the app should be in based on
     * shared settings.
     */
    private void setUnits() {
        units = Double.parseDouble(settings.getString("UNIT", "1.0"));
        switch (units.toString()) {
            case "1000.0":
                graph.getGridLabelRenderer().setVerticalAxisTitle(getResources().getString(R.string.vertical_axis_millivolts));
                break;

            case "1.0":
                graph.getGridLabelRenderer().setVerticalAxisTitle(getResources().getString(R.string.vertical_axis_volts));
                break;

            case "0.001":
                graph.getGridLabelRenderer().setVerticalAxisTitle(getResources().getString(R.string.vertical_axis_kilovolts));
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