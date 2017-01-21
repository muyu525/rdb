package com.expression.tool.rdb.event;

/**
 * DataEvent
 */

public class DataEvent {
    public static final int TAG_TIP = 0x01010100;
    public static final int TAG_START = 0x01010101;
    public static final int TAG_DATA = 0x01010113;
    public static final int TAG_END = 0x01011101;

    public DataEvent() {
    }

    public DataEvent(int tag, String info) {
        this.tag = tag;
        this.info = info;
    }

    public int tag;
    public String info;
}
