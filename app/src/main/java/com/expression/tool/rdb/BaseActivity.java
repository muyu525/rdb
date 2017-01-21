package com.expression.tool.rdb;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * BaseActivity
 */

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(getLayoutId());
        initIntentData();
        initViews();
        initParams();
        initListener();
    }

    protected abstract int getLayoutId();

    protected void initIntentData() {}

    protected void initViews() {}

    protected void initParams() {}

    protected void initListener() {}
}
