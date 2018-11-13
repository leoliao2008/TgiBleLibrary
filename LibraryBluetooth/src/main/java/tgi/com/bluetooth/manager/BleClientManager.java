package tgi.com.bluetooth.manager;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.util.Pair;

import java.util.ArrayList;

import tgi.com.bluetooth.BtLibConstants;
import tgi.com.bluetooth.bean.TgiBtGattService;
import tgi.com.bluetooth.callbacks.BleClientEventHandler;
import tgi.com.bluetooth.service.BleBackgroundService;

import static android.app.Activity.RESULT_OK;
import static tgi.com.bluetooth.BtLibConstants.*;
import static tgi.com.bluetooth.BtLibConstants.ACTION_REQUEST_BLE_SERVICE;
import static tgi.com.bluetooth.BtLibConstants.KEY_BLE_REQUEST;

/**
 * Author: leo
 * Data: On 13/11/2018
 * Project: TgiFreeRtoBtDemo
 * Description: 这个是蓝牙BLE库的管理类，直接调用这个类的方法即可。该类通过启动一个后台服务专门和
 * 蓝牙设备做信息交互，通过广播操作该服务和获取该服务的操作结果。相关蓝牙BLE背景知识可参考
 * <a href="https://developer.android.com/guide/topics/connectivity/bluetooth-le">官网</a>。
 */
public class BleClientManager {
    private BleClientEventHandler mEventHandler;
    private BleClientManagerBroadcastReceiver mReceiver;
    private static BleClientManager bleClientManager;
    private AlertDialog mAlertDialog;

    private BleClientManager() {}

    /**
     * 单例模式获取管理类。
     * @return
     */
    public synchronized static BleClientManager getInstance() {
        if (bleClientManager == null) {
            bleClientManager = new BleClientManager();
        }
        return bleClientManager;
    }

    /**
     * 启动系统蓝牙。使用此方法必须在Activity的onActivityResult方法中调用
     *{@link BleClientManager#onActivityResult(int, int, Intent)}
     * @param activity
     */
    public void enableBt(Activity activity){
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        activity.startActivityForResult(intent,REQUEST_ENABLE_BT);
    }

    /**
     * 在onResume中注册广播接收器和启动后台服务（如果还没启动）
     * @param activity
     * @param eventHandler
     */
    public void onResume(Activity activity, BleClientEventHandler eventHandler) {
        BleBackgroundService.start(activity);//start the service if not already started.
        mEventHandler = eventHandler;
        mReceiver = new BleClientManagerBroadcastReceiver();
        activity.registerReceiver(mReceiver, new IntentFilter(ACTION_BLE_ACTIVITY_UPDATE));
    }

    /**
     * 在onPause中取消广播接收器
     * @param activity
     */
    public void onPause(Activity activity) {
        if (mReceiver != null) {
            activity.unregisterReceiver(mReceiver);
            mReceiver = null;
        }
    }

    @SafeVarargs
    private final void sendBroadcast(Context context, String request, Pair<String, String>... params) {
        Intent intent = new Intent(ACTION_REQUEST_BLE_SERVICE);
        intent.putExtra(KEY_BLE_REQUEST,request);
        for(Pair<String,String> pair:params){
            intent.putExtra(pair.first,pair.second);
        }
        context.sendBroadcast(intent);
    }

    /**
     * 请求后台服务扫描设备
     * @param context
     */
    public void startScanningDevices(Context context) {
        sendBroadcast(context, REQUEST_SCAN_DEVICE);
    }

    /**
     * 请求后台服务停止扫描设备
     * @param context
     */
    public void stopScanningDevices(Context context){
        sendBroadcast(context, REQUEST_STOP_SCANNING_DEVICE);
    }

    /**
     * 请求后台服务连接指定设备
     * @param context
     * @param device
     */
    public void connectDevice(Context context,BluetoothDevice device){
        Intent intent=new Intent(ACTION_REQUEST_BLE_SERVICE);
        intent.putExtra(KEY_BLE_REQUEST,REQUEST_CONNECT_DEVICE);
        intent.putExtra(BtLibConstants.KEY_BT_DEVICE,device);
        context.sendBroadcast(intent);
    }

    /**
     * 请求后台服务断开指定设备。后台服务在退出时会自动断开设备，一般不需要开发者操作。
     * @param context
     */
    public void disconnectDevice(Context context){
        sendBroadcast(context, REQUEST_DISCONNECT_DEVICE);
    }

    /**
     * 请求后台服务获取设备的service列表
     * @param activity
     */
    public void scanServices(Context activity){
        sendBroadcast(activity, REQUEST_START_DISCOVER_SERVICES);
    }

    /**
     * 请求后台服务读取设备的char值
     * @param activity
     * @param serviceUUID
     * @param charUUID
     */
    public void readBtChar(Context activity,String serviceUUID,String charUUID){
        sendBroadcast(
                activity,
                REQUEST_READ_CHAR,
                new Pair<String, String>(KEY_BLE_SERVICE_UUID,serviceUUID),
                new Pair<String, String>(KEY_BLE_CHAR_UUID,charUUID)
        );
    }

    /**
     * 请求后台服务修改设备的char值
     * @param context
     * @param serviceUUID
     * @param charUUID
     * @param value
     */
    public void writeBtChar(Context context,String serviceUUID,String charUUID,byte[] value){
        Intent intent=new Intent(ACTION_REQUEST_BLE_SERVICE);
        intent.putExtra(KEY_BLE_REQUEST, REQUEST_WRITE_CHAR);
        intent.putExtra(KEY_BLE_SERVICE_UUID,serviceUUID);
        intent.putExtra(KEY_BLE_CHAR_UUID,charUUID);
        intent.putExtra(KEY_BLE_CHAR_VALUE,value);
        context.sendBroadcast(intent);
    }

    /**
     * 请求后台服务订阅通知。
     * @param context
     * @param serviceUUID
     * @param charUUID
     * @param descUUID
     */
    public void registerNotification(Context context,String serviceUUID,String charUUID,String descUUID){
        toggleNotification(
                context,
                serviceUUID,
                charUUID,
                descUUID,
                true
        );
    }

    /**
     * 请求后台服务取消订阅通知。
     * @param context
     * @param serviceUUID
     * @param charUUID
     * @param descUUID
     */
    public void unRegisterNotification(Context context, String serviceUUID, String charUUID, String descUUID){
        toggleNotification(
                context,
                serviceUUID,
                charUUID,
                descUUID,
                false
        );
    }

    private void toggleNotification(Context context,String serviceUUID,String charUUID,String descUUID,boolean isToEnable){
        sendBroadcast(
                context,
                isToEnable?BtLibConstants.REQUEST_REGISTER_NOTIFICATION:BtLibConstants.REQUEST_UNREGISTER_NOTIFICATION,
                new Pair<String, String>(KEY_BLE_SERVICE_UUID,serviceUUID),
                new Pair<String, String>(KEY_BLE_CHAR_UUID,charUUID),
                new Pair<String, String>(KEY_BLE_DESC_UUID,descUUID)
        );
    }

    /**
     * 停止后台服务，中断与蓝牙设备的连接。通常退出程序时用。
     * @param context
     */
    public void killBleBgService(Context context){
        sendBroadcast(context, REQUEST_KILL_BLE_BG_SERVICE);
    }

    /**
     * 在Activity的onActivityResult调用此函数即可。
     * @param requestCode
     * @param resultCode
     * @param data
     * @return true 表示本函数已经处理了本次事件，不需要在Activity的onActivityResult中再做其他处理；
     * false表示本函数放弃处理本次事件，留给Activity的onActivityResult处理。
     */
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode != RESULT_OK) {
                mEventHandler.onUserRefusesToEnableBt();
            } else {
                mEventHandler.onUserEnableBt();
            }
            return true;
        }
        return false;
    }

    /**
     * 如果有涉及申请定位权限，必须要在Activity的onRequestPermissionsResult中调用此函数。
     * @param activity
     * @param requestCode
     * @param permissions
     * @param grantResults
     * @return true 表示本函数已经处理了本次事件，不需要在Activity的onRequestPermissionsResult中再做其他处理；
     * false表示本函数放弃处理本次事件，留给Activity的onRequestPermissionsResult处理。
     */
    public synchronized boolean onRequestPermissionsResult(final Activity activity, final int requestCode, @NonNull final String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==BtLibConstants.REQUEST_CODE_ACCESS_COARSE_LOCATION){
            int length = permissions.length;
            for(int i=0;i<length;i++){
                if(PackageManager.PERMISSION_DENIED==grantResults[i]){
                    String permission = permissions[i];
                    if(mAlertDialog!=null&&mAlertDialog.isShowing()){
                        mAlertDialog.dismiss();
                        mAlertDialog=null;
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if(activity.shouldShowRequestPermissionRationale(permission)){
                            mAlertDialog = new AlertDialog.Builder(activity)
                                    .setTitle("Vital Permission Denied")
                                    .setMessage("The permission :" + permission + " is vital for this app, you need to grant it in order to use this function." +
                                            "Click Confirm to grant permission.")
                                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                        @RequiresApi(api = Build.VERSION_CODES.M)
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            mAlertDialog.dismiss();
                                            activity.requestPermissions(permissions, requestCode);
                                        }
                                    })
                                    .create();
                            mAlertDialog.show();

                        }else {
                            stopBecauseMissingPermission(activity,permission );
                        }
                    }else {
                        stopBecauseMissingPermission(activity,permission );
                    }
                    break;
                }
            }
            return true;
        }
        return false;
    }

    private void stopBecauseMissingPermission(Activity activity,String permission){
        if(mAlertDialog!=null&&mAlertDialog.isShowing()){
            mAlertDialog.dismiss();
            mAlertDialog=null;
        }
        mAlertDialog = new AlertDialog.Builder(activity)
                .setTitle("Vital Permission Denied")
                .setMessage("The permission :" + permission + " is vital for this app, you need to grant it in order to use this function." +
                        "Please go to the app management window and grant it for this app.")
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mAlertDialog.dismiss();
                    }
                })
                .create();
        mAlertDialog.show();
    }

    /**
     * 6.0后蓝牙必须要获取定位权限方可正常运行。这个函数可以向系统申请定位权限。
     * 这个函数必须和{@link BleClientManager#onRequestPermissionsResult(Activity, int, String[], int[])}一起用。
     * @param activity
     */
    public void requestLocationPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.requestPermissions(
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    BtLibConstants.REQUEST_CODE_ACCESS_COARSE_LOCATION);
        }
    }

    /**
     * 请求后台服务读取Descriptor的值
     * @param context
     * @param serviceUUID
     * @param charUUID
     * @param descUUID
     */
    public void readDescriptorValue(Context context, String serviceUUID, String charUUID, String descUUID) {
        sendBroadcast(
                context,
                BtLibConstants.REQUEST_READ_DESCRIPTOR_VALUE,
                new Pair<String, String>(KEY_BLE_SERVICE_UUID,serviceUUID),
                new Pair<String, String>(KEY_BLE_CHAR_UUID,charUUID),
                new Pair<String, String>(KEY_BLE_DESC_UUID,descUUID)
        );
    }


    /**
     * 这个广播接受者专门用来接收后台服务返回的各种执行结果，通过{@link BleClientEventHandler}这个回调返回给调用者。
     */
    private class BleClientManagerBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String event = intent.getStringExtra(KEY_BLE_EVENT);
            switch (event) {
                case BtLibConstants.EVENT_LOCATION_PERMISSION_NOT_GRANTED:
                    mEventHandler.onLocationPermissionNotGranted();
                    break;
                case BtLibConstants.EVENT_BT_NOT_SUPPORTED:
                    mEventHandler.onBtNotSupported();
                    break;
                case BtLibConstants.EVENT_BLE_NOT_SUPPORTED:
                    mEventHandler.onBleNotSupported();
                    break;
                case BtLibConstants.EVENT_BT_NOT_ENABLE:
                    mEventHandler.onBtNotEnabled();
                    break;
                case EVENT_DEVICES_SCANNING_STARTS:
                    mEventHandler.onStartScanningDevice();
                    break;
                case EVENT_DEVICES_SCANNING_STOPS:
                    mEventHandler.onStopScanningDevice();
                    break;
                case EVENT_A_DEVICE_IS_SCANNED: {
                    BluetoothDevice device=intent.getParcelableExtra(KEY_BT_DEVICE);
                    int rssi=intent.getIntExtra(KEY_BT_RSSI,-1);
                    byte[] scanRecord=intent.getByteArrayExtra(KEY_SCAN_RECORD);
                    mEventHandler.onDeviceScanned(device,rssi,scanRecord);
                    break;
                }
                case EVENT_DEVICE_CONNECTED:
                {
                    BluetoothDevice device = intent.getParcelableExtra(KEY_BT_DEVICE);
                    mEventHandler.onDeviceConnected(device);
                }
                    break;
                case EVENT_DEVICE_DISCONNECT:
                {
                    BluetoothDevice device = intent.getParcelableExtra(KEY_BT_DEVICE);
                    mEventHandler.onDeviceDisconnected(device);
                }
                    break;
                case EVENT_SERVICES_DISCOVERED:
                {
                    BluetoothDevice device = intent.getParcelableExtra(KEY_BT_DEVICE);
                    ArrayList<TgiBtGattService> services = intent.getParcelableArrayListExtra(KEY_GATT_SERVICES);
                    mEventHandler.onServiceDiscover(device,services);
                }
                    break;
                case EVENT_BT_FAIL_TO_DISCOVER_SERVICE:
                {
                    BluetoothDevice device = intent.getParcelableExtra(KEY_BT_DEVICE);
                    mEventHandler.onFailToDiscoverService(device);
                }

                    break;
                case EVENT_BLE_READ_CHAR_FAILS:
                {
                    String charUUID = intent.getStringExtra(KEY_BLE_CHAR_UUID);
                    String serviceUUID = intent.getStringExtra(KEY_BLE_SERVICE_UUID);
                    mEventHandler.onFailToReadChar(serviceUUID,charUUID);
                }

                    break;
                case EVENT_CHAR_READ: {//ok
                    String uuid = intent.getStringExtra(KEY_BLE_CHAR_UUID);
                    byte[] bytes = intent.getByteArrayExtra(KEY_BLE_CHAR_VALUE);
                    mEventHandler.onCharRead(uuid, bytes);
                    break;
                }
                case EVENT_BLE_CHAR_WRITTEN: {//ok
                    String charUUID = intent.getStringExtra(KEY_BLE_CHAR_UUID);
                    String serviceUUID = intent.getStringExtra(KEY_BLE_SERVICE_UUID);
                    byte[] bytes = intent.getByteArrayExtra(KEY_BLE_CHAR_VALUE);
                    mEventHandler.onCharWritten(serviceUUID,charUUID, bytes);
                    break;
                }
                case EVENT_BLE_WRITE_CHAR_FAILS:
                {
                    String charUUID = intent.getStringExtra(KEY_BLE_CHAR_UUID);
                    String serviceUUID = intent.getStringExtra(KEY_BLE_SERVICE_UUID);
                    byte[] bytes = intent.getByteArrayExtra(KEY_BLE_CHAR_VALUE);
                    mEventHandler.onFailToWriteChar(serviceUUID,charUUID, bytes);
                }
                    break;
                case EVENT_BLE_NOTIFICATION_IS_REGISTERED:
                case EVENT_REGISTER_NOTIFICATION_FAILS:
                {
                    String descUUID = intent.getStringExtra(KEY_BLE_DESC_UUID);
                    String charUUID = intent.getStringExtra(KEY_BLE_CHAR_UUID);
                    String serviceUUID = intent.getStringExtra(KEY_BLE_SERVICE_UUID);
                    if(event.equals(EVENT_BLE_NOTIFICATION_IS_REGISTERED)){
                        mEventHandler.onNotificationRegisterSuccess(serviceUUID,charUUID,descUUID);
                    }else {
                        mEventHandler.onNotificationRegisterFails(serviceUUID,charUUID,descUUID);
                    }
                    break;
                }
                case EVENT_BLE_NOTIFICATION_IS_UNREGISTERED:
                case EVENT_UNREGISTER_NOTIFICATION_FAILS:
                {
                    String descUUID = intent.getStringExtra(KEY_BLE_DESC_UUID);
                    String charUUID = intent.getStringExtra(KEY_BLE_CHAR_UUID);
                    String serviceUUID = intent.getStringExtra(KEY_BLE_SERVICE_UUID);
                    if(event.equals(EVENT_BLE_NOTIFICATION_IS_UNREGISTERED)){
                        mEventHandler.onUnregisterNotificationSuccess(serviceUUID,charUUID,descUUID);
                    }else {
                        mEventHandler.onUnregisterNotificationFails(serviceUUID,charUUID,descUUID);
                    }
                }
                break;
                //this is when a notification is triggered
                case EVENT_NOTIFICATION_TRIGGERED:
                {
                    String serviceUUID = intent.getStringExtra(KEY_BLE_SERVICE_UUID);
                    String charUUID = intent.getStringExtra(KEY_BLE_CHAR_UUID);
                    byte[] value = intent.getByteArrayExtra(KEY_BLE_CHAR_VALUE);
                    mEventHandler.onReceiveNotification(serviceUUID,charUUID,value);
                }
                    break;
                case EVENT_BLE_DESCRIPTOR_IS_READ:
                {
                    String descUUID = intent.getStringExtra(KEY_BLE_DESC_UUID);
                    String charUUID = intent.getStringExtra(KEY_BLE_CHAR_UUID);
                    String serviceUUID = intent.getStringExtra(KEY_BLE_SERVICE_UUID);
                    byte[] data = intent.getByteArrayExtra(KEY_BLE_DESC_VALUE);
                    mEventHandler.onDescriptorRead(serviceUUID,charUUID,descUUID,data);
                }
                    break;
                case EVENT_READ_DESCRIPTOR_FAILS:
                {
                    String descUUID = intent.getStringExtra(KEY_BLE_DESC_UUID);
                    String charUUID = intent.getStringExtra(KEY_BLE_CHAR_UUID);
                    String serviceUUID = intent.getStringExtra(KEY_BLE_SERVICE_UUID);
                    mEventHandler.onReadDescriptorFails(serviceUUID,charUUID,descUUID);
                }
                default:
                    break;


            }


        }
    }

    private void showLog(String msg){
        Log.e(getClass().getSimpleName(),msg);
    }
}
