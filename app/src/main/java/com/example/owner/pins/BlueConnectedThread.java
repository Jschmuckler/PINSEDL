package com.example.owner.pins;

import android.bluetooth.BluetoothSocket;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import android.os.Handler;

public class BlueConnectedThread extends Thread implements Runnable {
    private final BluetoothSocket btSocket;
    private final InputStream btInStream;
    private final OutputStream btOutStream;
    private final Handler btHandler;

    public BlueConnectedThread(BluetoothSocket socket, Handler btHandler) {
        btSocket = socket;
        this.btHandler = btHandler;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        btInStream = tmpIn;
        btOutStream = tmpOut;
    }

    public void run() {
        byte[] buffer = new byte[1024];
        int begin = 0;
        int bytes = 0;
        while (true) {
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

    public void cancel() {
        try {
            btSocket.close();
        } catch (IOException e) {
        }
    }
}