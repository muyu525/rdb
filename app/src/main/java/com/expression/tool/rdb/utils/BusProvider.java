package com.expression.tool.rdb.utils;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

/**
 * BusProvider
 */

public class BusProvider {

    public static Bus bus;

    public static Bus getBus() {
        if (null == bus) {
            bus = new Bus(ThreadEnforcer.ANY);
        }
        return bus;
    }
}
