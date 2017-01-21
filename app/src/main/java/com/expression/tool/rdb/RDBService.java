package com.expression.tool.rdb;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.expression.tool.rdb.event.DataEvent;
import com.expression.tool.rdb.utils.BusProvider;

/**
 * RDBService
 */

public class RDBService extends Service {
    RDBServer mServer;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (null != intent) {
            int port = intent.getIntExtra("port", 8080);
            mServer = new RDBServer(this, port);
            mServer.start();

            DataEvent event = new DataEvent();
            event.tag = DataEvent.TAG_TIP;
            event.info = "Service started,port:" + port;
            BusProvider.getBus().post(event);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        mServer.setRunning(false);
        super.onDestroy();

        DataEvent event = new DataEvent();
        event.tag = DataEvent.TAG_TIP;
        event.info = "Service Stop";
        BusProvider.getBus().post(event);
    }
}
