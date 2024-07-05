package com.rayman.ble.app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.ParcelUuid;
//import android.support.annotation.Nullable;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 蓝牙扫描服务
 * Created by Rayman on 2016/8/17.
 */
public class BlueToothService extends Service {
    private static final String TAG = "BlueToothService";
    private static final int SCANNING = 102;
    private static final int SCAN_FINISHED = 101;
    private static final int BLE_CONNECT_SUCCESS = 1011;
    private static final int BLE_CONNECT_FAIL = 1012;
    private static final int SCAN_TIME_LIMIT = 15000;

    private UUID serviceUUID = Const.UUID_SERVICE;
    private UUID characteristicUUID = Const.UUID_CHARACTERISTIC_WRITE;

    //private UUID mx4 = UUID.fromString("00002a00-0000-1000-8000-00805f9b34fb");
    //private UUID serviceUUID = UUID.fromString("d22f30b8-2716-41d2-84f2-000000000008");
    //private UUID characteristicUUID = UUID.fromString("d22f30b8-2716-41d2-84f2-000000000009");

    //UUID服务列表
    private UUID uuidList[] = new UUID[]{serviceUUID};

    private BluetoothAdapter mBlueToothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private BluetoothScanCallBack mCallBack;
    private BluetoothConnectCallBack mGattCallback;

    public static Handler handler;
    private Context mContext;

    //蓝牙初期配置
    private boolean mScanning = false;
    private String state = Const.SCAN_START;
    private String mBluetoothAddress = "", bt_id = "", bt_password = "";
    private HashMap<String, String> mBluetoothData = new HashMap<>();

    private List<BluetoothDevice> devices = new ArrayList<>();

    //Gatt服务列表
    private List<BluetoothGattService> mServices = new ArrayList<>();

    private BluetoothGattService mService;
    private BluetoothDevice mDevice;

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            handler.sendEmptyMessage(SCAN_FINISHED);
            stopScan();
        }
    };

    @SuppressLint("HandlerLeak")
    @Override
    public void onCreate() {
        /*List<PropertyJson> data = LruCacheUtil.getInstance().getKeysLruCache(Const.KEYS_LIST);
        for (int i = 0; i < data.size(); i++) {
            mBluetoothData.put(data.get(i).getBt_id(), "(" + data.get(i).getLock_password() + ")");
        }*/

        Log.i(TAG, "onCreate sevice");
        mContext = this;
        //测试pwd和id
        mBluetoothData.put("102", "(" + 98524102 + ")");


        for (Map.Entry entry : mBluetoothData.entrySet()) {
            Log.i(TAG, "Key=" + entry.getKey());
            Log.i(TAG, "Value=" + entry.getValue());
        }

        mBlueToothAdapter = BluetoothAdapter.getDefaultAdapter();
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1440:
                        mBluetoothData = (HashMap<String, String>) msg.obj;
                        break;
                    case SCANNING:
                        BluetoothDevice device = (BluetoothDevice) msg.obj;/*
                        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return;
                        }*/
                        Log.i(TAG, "输出设备名：" + device.getName() + "\nmac地址：" + device.getAddress());
                        MainActivity.handler.obtainMessage(MainActivity.DEVICE, msg.obj).sendToTarget();
                        /*BluetoothDevice device = (BluetoothDevice) msg.obj;
                        if (device.getName().equals(bt_id)) {
                            mDevice = device;
                            connectDevice(device.getAddress());
                        } else {
                            Log.i(TAG, "设备BT_id不一致");
                            //mDevice = device;
                            //connectDevice(device.getAddress());
                            if (devices.size() == 0) {
                                scanStart(true);
                            }
                        }*/
                        break;
                    case SCAN_FINISHED:
                        Log.i(TAG, "列表大小：" + devices.size());
                        Log.i(TAG, "输出列表：\n");
                        if (devices.size() == 0) {
                            scanStart(true);
                        } else {
                            Set set = mBluetoothData.keySet();
                            for (BluetoothDevice target : devices) {
                                for (Iterator iterator = set.iterator(); iterator.hasNext(); ) {
                                    bt_id = (String) iterator.next();
                                    bt_password = mBluetoothData.get(bt_id);
//                                    Log.i(TAG, "BT_id：" + bt_id);
//                                    Log.i(TAG, "BT_password：" + bt_password);
                                    Log.i(TAG, "target地址：" + target.getAddress() + "\nname：" + target.getName());
                                    ParcelUuid[] uuids = target.getUuids();
                                    if (uuids != null)
                                        for (int i = 0; i < uuids.length; i++) {
                                            Log.i(TAG, "\ntargetUUID_" + i + ":" + uuids[i].toString());
                                        }
                                    mDevice = target;
                                    connectDevice(target.getAddress());

                                    /*if (target.getName().equals(bt_id)) {
                                        mDevice = target;
                                        connectDevice(target.getAddress());
                                    } else {
                                        Log.i(TAG, "设备BT_id不一致");
                                        scanStart(true);
                                    }*/
                                }
                                /*mDevice = target;
                                connectDevice(target.getAddress());*/
                            }
                        }
                        break;
                    case BLE_CONNECT_SUCCESS:
                        break;
                    case BLE_CONNECT_FAIL:
                        connectDevice(mDevice.getAddress());
                        break;

                }
            }
        };

        mCallBack = new BluetoothScanCallBack();

       /* DeviceReceiver deviceReceiver = new DeviceReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.EXTRA_DEVICE);
        registerReceiver(deviceReceiver, intentFilter);*/
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        state = intent.getStringExtra("Order");
        Log.i(TAG, "onStartCommand sevice");

        if (state.equals(Const.SCAN_START)) {
            scanStart(true);
        } else {
            Log.i(TAG, "Order：" + state);
            stopScan();/*
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return Service.START_STICKY_COMPATIBILITY;
            }*/
            mBlueToothAdapter.disable();
            stopSelf();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //unregisterReceiver(mReceiver);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void scanStart(final boolean isEnable) {
        if (isEnable) {
            handler.postDelayed(runnable, SCAN_TIME_LIMIT);
            Log.i(TAG, "开始搜索  uuidList："+uuidList[0].toString());
            mScanning = true;
            //BluetoothAdapter.startLeScan()具体有两个方法，可以根据指定的UUID找到对应的UUID设备。
            /*if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }*/
            mBlueToothAdapter.startLeScan(uuidList, mCallBack);   //使用startLeScan只能扫描出BLE设备，而且扫描的设备比较多
   //         mBlueToothAdapter.startLeScan(mCallBack);
        } else {
            stopScan();
        }
    }

    /*private class DeviceReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);  //这个就是所获得的蓝牙设备。
                if (!devices.contains(device)) {
                    handler.obtainMessage(SCANNING, device).sendToTarget();
                    devices.add(device);
                }
            }
        }
    }*/


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void stopScan() {
        Log.i(TAG, "停止搜索");
        mScanning = false;
        handler.removeCallbacks(runnable);/*
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }*/
        mBlueToothAdapter.stopLeScan(mCallBack);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public boolean connectDevice(String address) {
        stopScan();
        Log.i(TAG, "连接设备:" + address);
        if (mBlueToothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }
        // Previously connected device. Try to reconnect. (先前连接的设备。 尝试重新连接)
        if (mBluetoothAddress != null && address.equals(mBluetoothAddress) && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");/*
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return false;
            }*/
            if (mBluetoothGatt.connect()) {
                return true;
            } else {
                return false;
            }
        }


        BluetoothDevice bluetoothDevice = mBlueToothAdapter.getRemoteDevice(address);
        if (bluetoothDevice == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        mGattCallback = new BluetoothConnectCallBack();
        mBluetoothGatt = bluetoothDevice.connectGatt(this, false, mGattCallback); //该函数才是真正的去进行连接
        Log.d(TAG, "Trying to create a new connection.BLE");

        /*BluetoothSocket socket = null;
        try {
            socket = bluetoothDevice.createRfcommSocketToServiceRecord(UUID.fromString(MY_UUID));
            socket.connect();
            Log.d(TAG, "Trying to create a new connection.EDR");
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        return true;
    }

    /*private BluetoothDevice getCorrectDevice(List<BluetoothDevice> devices) {
     *//*
     * 假若上面使用mBlueToothAdapter.startLeScan(uuidList, mCallBack);通过UUID找不到对应设备，
     * 可以尝试获取到全部devices之后遍历devices然后通过device.getUuids () 获取对应device的UUID列表.
     * 判断该列表是否含有指定UUID，然后连接该设备。
     * *//*
        BluetoothDevice device = null;
        for (BluetoothDevice d : devices) {
            ParcelUuid[] deviceUuids = d.getUuids();
            device = d;
            if (deviceUuids != null) {
                for (int i = 0; i < deviceUuids.length; i++) {
                    UUID uuid = deviceUuids[i].getUuid();
                    if (uuid.equals(serviceUUID) || uuid.equals(characteristicUUID)) {
                    }
                }
            } else {
                return null;
            }
        }
        return device;
    }*/

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private List<BluetoothGattService> getSupportServices(BluetoothGatt mBluetoothGatt) {
        List<BluetoothGattService> data = new ArrayList<>();
        if (mBluetoothGatt == null) {
            return null;
        }
        if (mBluetoothGatt.getService(serviceUUID) != null) {
            data.add(mBluetoothGatt.getService(serviceUUID));
        }
        //return mBluetoothGatt.getServices();
        return data;
    }

    /**
     * 遍历出GattServices列表中的所有Charateristic
     * 以供程序的写入和读取数据
     *
     * @param gattServices GattServices列表
     * @param value        具体写入数据
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void displayGattServices(List<BluetoothGattService> gattServices, String value) {
        if (gattServices == null)
            return;
        for (BluetoothGattService gattService : gattServices) {                             // 遍历出gattServices里面的所有服务
            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
            Log.e(TAG, "-->service type:" + "");
            Log.e(TAG, "-->includedServices size:" + gattService.getIncludedServices().size());
            Log.e(TAG, "-->service uuid:" + gattService.getUuid());
            for (final BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {    // 遍历每条服务里的所有Characteristic
                Log.e(TAG, "---->char uuid:" + gattCharacteristic.getUuid());

                int permission = gattCharacteristic.getPermissions();
                Log.e(TAG, "---->char permission:" + permission);

                int property = gattCharacteristic.getProperties();
                Log.e(TAG, "---->char property:" + property);

                byte[] data = gattCharacteristic.getValue();
                if (data != null && data.length > 0) {
                    Log.e(TAG, "---->char value:" + new String(data));
                }
                if (gattCharacteristic.getUuid().toString().equals(characteristicUUID.toString())) {    //需要通信的UUID
                    // 有哪些UUID，每个UUID有什么属性及作用，一般硬件工程师都会给相应的文档。我们程序也可以读取其属性判断其属性。
                    // 此处可以可根据UUID的类型对设备进行读操作，写操作，设置notification等操作
                    // BluetoothGattCharacteristic gattNoticCharacteristic 假设是可设置通知的Characteristic
                    // BluetoothGattCharacteristic gattWriteCharacteristic 假设是可写的Characteristic
                    // BluetoothGattCharacteristic gattReadCharacteristic  假设是可读的Characteristic

                    //接受Characteristic被写的通知,收到蓝牙模块的数据后会触发mOnDataAvailable.onCharacteristicWrite()
                    /*if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }*/
                    mBluetoothGatt.setCharacteristicNotification(gattCharacteristic, true);
                    byte[] bytes = value.getBytes();
                    gattCharacteristic.setValue(bytes);            //具体写入的数据
                    //往蓝牙模块写入数据
                    /*if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }*/
                    boolean result = mBluetoothGatt.writeCharacteristic(gattCharacteristic);
                    Log.i(TAG, result ? "写入characteristic数据成功" : "写入数据失败");
                    break;
                } else {
                    Log.i(TAG, "当前UUID不是写入的UUID");
                }

                //暂时不需要desc数据写入
                //-----Descriptors的字段信息-----//
               /* List<BluetoothGattDescriptor> gattDescriptors = gattCharacteristic.getDescriptors();
                for (BluetoothGattDescriptor gattDescriptor : gattDescriptors) {
                    Log.e(TAG, "-------->desc uuid:" + gattDescriptor.getUuid());
                    int descPermission = gattDescriptor.getPermissions();
                    //Log.e(TAG, "-------->desc permission:" + Utils.getDescPermission(descPermission));

                    byte[] desData = gattDescriptor.getValue();
                    if (desData != null && desData.length > 0) {
                        Log.e(TAG, "-------->desc value:" + new String(desData));
                    }
                }*/
            }
        }
    }

    /**
     * 遍历出指定GattServices中的所有Charateristic
     * 以供程序的写入和读取数据
     *
     * @param gattService 指定Gatt服务
     * @param value       具体写入数据
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void displayGattServices(BluetoothGattService gattService, final String value) {
        if (gattService == null)
            return;

        //获取全部characteristic特征列表
        //List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();

        //获取指定characteristic特征列表
        BluetoothGattCharacteristic gattCharacteristics = gattService.getCharacteristic(characteristicUUID);
        Log.e(TAG, "-->service type:" + "");
        Log.e(TAG, "-->includedServices size:" + gattService.getIncludedServices().size());
        Log.e(TAG, "-->service uuid:" + gattService.getUuid());
        Log.e(TAG, "---->char uuid:" + gattCharacteristics.getUuid());
        int permission = gattCharacteristics.getPermissions();
        Log.e(TAG, "---->char permission:" + permission);

        int property = gattCharacteristics.getProperties();
        Log.e(TAG, "---->char property:" + property);

        byte[] data = gattCharacteristics.getValue();
        if (data != null && data.length > 0) {
            Log.e(TAG, "---->char value:" + new String(data));
        }

        //接受Characteristic被写的通知,收到蓝牙模块的数据后会触发mOnDataAvailable.onCharacteristicWrite()
        /*if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }*/
        mBluetoothGatt.setCharacteristicNotification(gattCharacteristics, true);
        byte[] bytes = value.getBytes();
        gattCharacteristics.setValue(bytes);            //具体写入的数据

        //往蓝牙模块写入数据
        boolean result = mBluetoothGatt.writeCharacteristic(gattCharacteristics);
        Log.i(TAG, "写入数据为：" + value);
        Log.i(TAG, result ? "写入characteristic数据成功" : "写入数据失败");

        /*for (final BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {    // 遍历每条服务里的所有Characteristic
            Log.i(TAG, "遍历BluetoothGattCharacteristic特性");
            Log.e(TAG, "---->char uuid:" + gattCharacteristic.getUuid());

            int permission = gattCharacteristic.getPermissions();
            Log.e(TAG, "---->char permission:" + permission);

            int property = gattCharacteristic.getProperties();
            Log.e(TAG, "---->char property:" + property);

            byte[] data = gattCharacteristic.getValue();
            if (data != null && data.length > 0) {
                Log.e(TAG, "---->char value:" + new String(data));
            }
            if (gattCharacteristic.getUuid().toString().equals(characteristicUUID.toString())) {    //需要通信的UUID
                // 有哪些UUID，每个UUID有什么属性及作用，一般硬件工程师都会给相应的文档。我们程序也可以读取其属性判断其属性。
                // 此处可以可根据UUID的类型对设备进行读操作，写操作，设置notification等操作
                // BluetoothGattCharacteristic gattNoticCharacteristic 假设是可设置通知的Characteristic
                // BluetoothGattCharacteristic gattWriteCharacteristic 假设是可写的Characteristic
                // BluetoothGattCharacteristic gattReadCharacteristic  假设是可读的Characteristic

                //接受Characteristic被写的通知,收到蓝牙模块的数据后会触发mOnDataAvailable.onCharacteristicWrite()
                mBluetoothGatt.setCharacteristicNotification(gattCharacteristic, true);
                byte[] bytes = value.getBytes();
                gattCharacteristic.setValue(bytes);            //具体写入的数据

                //往蓝牙模块写入数据
                boolean result = mBluetoothGatt.writeCharacteristic(gattCharacteristic);
                Log.i(TAG, "写入数据为：" + value);
                Log.i(TAG, result ? "写入characteristic数据成功" : "写入数据失败");
            } else {
                Log.i(TAG, "写入的UUID不匹配");
            }

            //-----Descriptors的字段信息-----//
            List<BluetoothGattDescriptor> gattDescriptors = gattCharacteristic.getDescriptors();
            for (BluetoothGattDescriptor gattDescriptor : gattDescriptors) {
                Log.e(TAG, "-------->desc uuid:" + gattDescriptor.getUuid());
                int descPermission = gattDescriptor.getPermissions();
                //Log.e(TAG, "-------->desc permission:" + Utils.getDescPermission(descPermission));

                byte[] desData = gattDescriptor.getValue();
                if (desData != null && desData.length > 0) {
                    Log.e(TAG, "-------->desc value:" + new String(desData));
                }
            }
        }*/
    }

    /**
     * BLE蓝牙搜索回调
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public class BluetoothScanCallBack implements BluetoothAdapter.LeScanCallback {

        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            Log.i(TAG, "BluetoothScanCallBack  onLeScan scanRecord："+scanRecord);
            if (!devices.contains(device)) {
                handler.obtainMessage(SCANNING, device).sendToTarget();
                devices.add(device);
            }
        }
    }

    /**
     * BLE蓝牙连接回调
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public class BluetoothConnectCallBack extends BluetoothGattCallback {

        @Override       //当连接上设备或者失去连接的时候会回调
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i(TAG, "连接设备成功！");
                handler.sendEmptyMessage(BLE_CONNECT_SUCCESS);
                /*if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }*/
                gatt.discoverServices();            //连接成功后就去找出该设备中的服务
                Log.i(TAG, "查找设备服务");
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(TAG, "连接设备失败！");
                handler.sendEmptyMessage(BLE_CONNECT_FAIL);
            }

        }

        @Override       //当搜索到服务的时候回调
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (gatt == null) {
                    return;
                }
                //找到了服务
                Log.i(TAG, "找到了GATT服务");
                mServices = getSupportServices(gatt);
                //只获取指定UUIDService
                mService = mServices.get(mServices.size() - 1);
                if (mService != null) {
                    displayGattServices(mService, bt_password);
                }
                /*for (int i = 0; i < mServices.size(); i++) {
                    Log.e(TAG, "GATT服务列表：" + mServices.get(i).toString());
                    if (mServices.get(i).getUuid().equals(serviceUUID)) {
                        Log.e(TAG, "找到了匹配的UUID服务：" + mServices.get(i).toString());
                        mService = mServices.get(i);
                    }
                }*/
            } else {
                Log.w(TAG, "onServicesDiscovered receiver：" + status);
            }
        }

        @Override       //读取设备Charateristic的时候回调
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.i(TAG, "读取设备Charateristic回调成功");
        }

        @Override       //当向设备的写入Charateristic的时候调用
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.i(TAG, "当向设备的写入Charateristic回调成功");
            /*if (mBlueToothAdapter.isEnabled()) {
                mBlueToothAdapter.disable();
                Toast.makeText(BlueToothService.this, "写入信息成功", Toast.LENGTH_SHORT).show();
                //MoreFragment.handler.sendEmptyMessage(1);
                stopSelf();         //关闭服务
            }*/
        }

        @Override       ////设备发出通知时会调用到该接口
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Log.i(TAG, "设备发出通知时回调成功");
        }

        @Override       //当向设备Descriptor中读取数据时，会回调该函数
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            Log.i(TAG, "当向设备Descriptor中读取数据回调成功");
        }

        @Override       //当向设备Descriptor中写数据时，会回调该函数
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            Log.i(TAG, "当向设备Descriptor中写数据回调成功");
        }

    }

    /**
     * 传统蓝牙搜索回调
     */
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //找到设备
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
               BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                /* if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }*/
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    Log.i(TAG, "搜索到新的传统蓝牙设备：" + device.toString());
                } else {
                    Log.i(TAG, "搜索到已匹配传统蓝牙设备：" + device.toString());
                }
                devices.add(device);
            }
            //搜索完成
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.i(TAG, "传统蓝牙搜索完毕");
            }
        }
    };
}
