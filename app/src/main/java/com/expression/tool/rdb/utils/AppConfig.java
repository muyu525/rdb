package com.expression.tool.rdb.utils;

import android.os.Environment;

import java.io.File;

/**
 * AppConfig
 */

public class AppConfig {
    public static final String ROOT =
            Environment.getExternalStorageDirectory().getAbsolutePath() +
                    File.separator + "rdb";
    public static final String APP_PATH = ROOT + File.separator + "app";
}
