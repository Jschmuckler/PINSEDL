package com.example.owner.pins;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.util.UUID;
import android.os.Handler;

public class BlueConnectThread extends Thread {
    private final BluetoothSocket btSocket;
    private final BluetoothDevice btDevice;
    private final BluetoothAdapter btAdapter;
    private final Handler btHandler;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    public BlueConnectThread(BluetoothDevice device,BluetoothAdapter btAdapter,Handler btHandler) {
        BluetoothSocket tmp = null;
        btDevice = device;
        this.btAdapter = btAdapter;
        this.btHandler = btHandler;
        try {
            tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) { }
        btSocket = tmp;
    }
    public void run() {
        btAdapter.cancelDiscovery();
        try {
            btSocket.connect();
            BlueConnectedThread btConnectedThread = new BlueConnectedThread(btSocket,btHandler);
            btConnectedThread.start();
            String ast = "*";
            btConnectedThread.write(ast.getBytes());
        } catch (IOException connectException) {
            try {
                btSocket.close();
            } catch (IOException closeException) { }
            return;
        }
    }
    public void cancel() {
        try {
            btSocket.close();
        } catch (IOException e) { }
    }
}
