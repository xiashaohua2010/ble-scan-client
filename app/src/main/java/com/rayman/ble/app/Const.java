package com.rayman.ble.app;

import java.util.UUID;

/**
 * Created by Administrator on 2016/4/8.
 */
public class Const {

    private static final String HOST = "http://mj.juabc.com";

    public static final UUID UUID_SERVICE = java.util.UUID.fromString("e89c82e7-aa9d-e7a7-91e6-8a80206d6a73");
    public static final UUID UUID_CHARACTERISTIC_READ = java.util.UUID.fromString("e89c82e7-aa9d-e7a7-91e6-8a80206d6a63");
    public static final UUID UUID_CHARACTERISTIC_WRITE = java.util.UUID.fromString("e89c82e7-aa9d-e7a7-91e6-8a80206d6a64");
    public static final UUID UUID_DESCRIPTOR = java.util.UUID.fromString("e89c82e7-aa9d-e7a7-91e6-8a80206d6a62");
    public static final String SCAN_START = "scan_start";
    public static final String SCAN_STOP = "scan_stop";
    public static final String CONNECT = "connect";

}