package com.example.owner.pins;

import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import android.os.Handler;
import android.util.Log;

public class BlueConnectedThread extends Thread implements Runnable {
    private final String TAG = "BlueConnectedThread";
    private BluetoothSocket btSocket;
    private InputStream btInStream;
    private OutputStream btOutStream;
    private final Handler btHandler;

    public BlueConnectedThread(Handler btHandler) {

        this.btHandler = btHandler;
    }


    public void run() {
        InputStream tmpIn = null;
        OutputStream tmpOut = null;
        try {
            tmpIn = btSocket.getInputStream();
            tmpOut = btSocket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        btInStream = tmpIn;
        btOutStream = tmpOut;


        byte[] buffer = new byte[1024];
        int begin = 0;
        int bytes = 0;
        while (btSocket.isConnected()) {
            try {
                bytes += btInStream.read(buffer, bytes, buffer.length - bytes);
                for (int i = begin; i < bytes; i++) {
                    if (buffer[i] == "#".getBytes()[0]) {
                        btHandler.obtainMessage(1, begin, i, buffer).sendToTarget();
                        begin = i + 1;
                        if (i == bytes - 1) {
                            bytes = 0;
                            begin = 0;
                        }
                    }
                }
            } catch (IOException e) {
                Log.w(TAG, "Stopped connected thread");
                break;
            }
        }
    }

    public void write(byte[] bytes) {
        try {
            btOutStream.write(bytes);
        } catch (IOException e) {
        }
    }

    public void setBtSocket(BluetoothSocket btSocket) {
        this.btSocket = btSocket;
    }

    public void cancel() {
        try {
            if (btSocket != null) {
                btSocket.close();
            }

        } catch (IOException e) {
        }
    }
}