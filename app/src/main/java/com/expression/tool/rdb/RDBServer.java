package com.expression.tool.rdb;

import android.content.Context;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;

/**
 * RDBServer
 */

public class RDBServer extends Thread {

    private Context mContext;
    private int mPort;
    private ServerSocket mServerSocket;
    private boolean mRunning;

    public RDBServer(Context context, int port) {
        super();
        mRunning = true;
        mContext = context;
        mPort = port;
    }

    public void setRunning(boolean running) {
        try {
            mRunning = running;
            if (!running) {
                mServerSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void gardRun() {
        try {
            mServerSocket = new ServerSocket(mPort);
        } catch (IOException e) {
            e.printStackTrace();
        }

        while(mRunning) {
            try {
                Socket clientSocket = mServerSocket.accept();
                RDBWorker worker = new RDBWorker(mContext, clientSocket);
                worker.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        try {
            gardRun();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
