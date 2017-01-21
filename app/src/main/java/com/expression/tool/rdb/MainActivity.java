package com.expression.tool.rdb;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.expression.tool.rdb.event.DataEvent;
import com.expression.tool.rdb.model.StartInfo;
import com.expression.tool.rdb.utils.AppConfig;
import com.expression.tool.rdb.utils.BusProvider;
import com.expression.tool.rdb.utils.CommonUtils;
import com.google.gson.Gson;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.io.File;
import java.lang.ref.WeakReference;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    private TextView mTvIp;
    private EditText mTvPort;
    private Button mBtn;
    private TextView mOut;

    private boolean mServiceRunning;

    private UserHandler mHandler = new UserHandler(this);

    private static class UserHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;

        public UserHandler(MainActivity activity) {
            mActivity = new WeakReference<MainActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity activity = mActivity.get();
            if (null != activity) {

            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mServiceRunning = false;
        Bus bus = new Bus();
        File appPath = new File(AppConfig.APP_PATH);
        if (!appPath.exists()) {
            appPath.mkdirs();
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initViews() {
        mTvIp = (TextView) findViewById(R.id.ip);
        mTvPort = (EditText) findViewById(R.id.port);
        mBtn = (Button) findViewById(R.id.btn);
        mOut = (TextView) findViewById(R.id.output);

        mTvIp.setText(CommonUtils.getIpAddress(this));
    }

    @Override
    protected void initListener() {
        mBtn.setOnClickListener(this);
    }

    private void toggleService() {
        if (mServiceRunning) {
            mServiceRunning = false;
            Intent it = new Intent(this, RDBService.class);
            stopService(it);
            mBtn.setText(R.string.service_start);
        } else {
            mServiceRunning = true;
            Intent it = new Intent(this, RDBService.class);
            String sport = mTvPort.getText().toString();
            if (TextUtils.isEmpty(sport)) {
                sport = mTvPort.getHint().toString();
            }
            int port = Integer.valueOf(sport);
            it.putExtra("port", port);
            startService(it);
            mBtn.setText(R.string.service_stop);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn:
                toggleService();
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        BusProvider.getBus().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        BusProvider.getBus().unregister(this);
    }

    @Subscribe
    public void subscribeDataEvent(DataEvent event) {
        if (DataEvent.TAG_START == event.tag) {
            Gson gson = new Gson();
            StartInfo startInfo = gson.fromJson(event.info, StartInfo.class);
            String log = CommonUtils.getCurrentStringTime() + " recv "
                    + event.info + "\n";
            mOut.append(log);
        } else if (DataEvent.TAG_END == event.tag) {
            String log = CommonUtils.getCurrentStringTime() + " recv end\n";
            mOut.append(log);
        } else if (DataEvent.TAG_TIP == event.tag) {
            String log = CommonUtils.getCurrentStringTime() + " " + event.info + "\n";
            mOut.append(log);
        }
    }

}
