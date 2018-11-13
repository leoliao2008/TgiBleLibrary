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

public class BleClientManager {
    private BleClientEventHandler mEventHandler;
    private BleClientManagerBroadcastReceiver mReceiver;
    private static BleClientManager bleClientManager;
    private AlertDialog mAlertDialog;

    private BleClientManager() {
    }

    public synchronized static BleClientManager getInstance() {
        if (bleClientManager == null) {
            bleClientManager = new BleClientManager();
        }
        return bleClientManager;
    }

    public void enableBt(Activity activity){
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        activity.startActivityForResult(intent,REQUEST_ENABLE_BT);
    }

    public void onResume(Activity activity, BleClientEventHandler eventHandler) {
        BleBackgroundService.start(activity);//start the service if not already started.
        mEventHandler = eventHandler;
        mReceiver = new BleClientManagerBroadcastReceiver();
        activity.registerReceiver(mReceiver, new IntentFilter(ACTION_BLE_ACTIVITY_UPDATE));
    }

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

    public void startScanningDevices(Context context) {
        sendBroadcast(context, REQUEST_SCAN_DEVICE);
    }

    public void stopScanningDevices(Context context){
        sendBroadcast(context, REQUEST_STOP_SCANNING_DEVICE);
    }

    public void connectDevice(Context context,BluetoothDevice device){
//        sendBroadcast(
//                context,
//                REQUEST_CONNECT_DEVICE,
//                new Pair<String, String>(KEY_BLE_DEVICE_ADDRESS,devAddress));
        Intent intent=new Intent(ACTION_REQUEST_BLE_SERVICE);
        intent.putExtra(KEY_BLE_REQUEST,REQUEST_CONNECT_DEVICE);
        intent.putExtra(BtLibConstants.KEY_BT_DEVICE,device);
        context.sendBroadcast(intent);
    }

    public void disconnectDevice(Context context){
        sendBroadcast(context, REQUEST_DISCONNECT_DEVICE);
    }

    public void scanServices(Context activity){
        sendBroadcast(activity, REQUEST_START_DISCOVER_SERVICES);
    }

    public void readBtChar(Context activity,String serviceUUID,String charUUID){
        sendBroadcast(
                activity,
                REQUEST_READ_CHAR,
                new Pair<String, String>(KEY_BLE_SERVICE_UUID,serviceUUID),
                new Pair<String, String>(KEY_BLE_CHAR_UUID,charUUID)
        );
    }

    public void writeBtChar(Context context,String serviceUUID,String charUUID,byte[] value){
        Intent intent=new Intent(ACTION_REQUEST_BLE_SERVICE);
        intent.putExtra(KEY_BLE_REQUEST, REQUEST_WRITE_CHAR);
        intent.putExtra(KEY_BLE_SERVICE_UUID,serviceUUID);
        intent.putExtra(KEY_BLE_CHAR_UUID,charUUID);
        intent.putExtra(KEY_BLE_CHAR_VALUE,value);
        context.sendBroadcast(intent);
    }

    public void registerNotification(Context context,String serviceUUID,String charUUID,String descUUID){
        toggleNotification(
                context,
                serviceUUID,
                charUUID,
                descUUID,
                true
        );
    }

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

    public void killBleBgService(Context context){
        sendBroadcast(context, REQUEST_KILL_BLE_BG_SERVICE);
    }

    /**
     *
     * @param requestCode
     * @param resultCode
     * @param data
     * @return true will handle this result, false otherwise.
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

    public void requestLocationPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.requestPermissions(
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    BtLibConstants.REQUEST_CODE_ACCESS_COARSE_LOCATION);
        }
    }

    public void readDescriptorValue(Context context, String serviceUUID, String charUUID, String descUUID) {
        sendBroadcast(
                context,
                BtLibConstants.REQUEST_READ_DESCRIPTOR_VALUE,
                new Pair<String, String>(KEY_BLE_SERVICE_UUID,serviceUUID),
                new Pair<String, String>(KEY_BLE_CHAR_UUID,charUUID),
                new Pair<String, String>(KEY_BLE_DESC_UUID,descUUID)
        );
    }


    private class BleClientManagerBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String event = intent.getStringExtra(KEY_BLE_EVENT);
            switch (event) {
                case BtLibConstants.EVENT_LOCATION_PERMISSION_NOT_GRANTED:
                    mEventHandler.onLocationPermissionNotGranted();
                    break;
                case BtLibConstants.EVENT_BT_NOT_SUPPORTED://ok
                    mEventHandler.onBtNotSupported();
                    break;
                case BtLibConstants.EVENT_BLE_NOT_SUPPORTED://ok
                    mEventHandler.onBleNotSupported();
                    break;
                case BtLibConstants.EVENT_BT_NOT_ENABLE://ok
                    mEventHandler.onBtNotEnabled();
                    break;
                case EVENT_DEVICES_SCANNING_STARTS://ok
                    mEventHandler.onStartScanningDevice();
                    break;
                case EVENT_DEVICES_SCANNING_STOPS://ok
                    mEventHandler.onStopScanningDevice();
                    break;
                case EVENT_A_DEVICE_IS_SCANNED: {//ok
                    BluetoothDevice device=intent.getParcelableExtra(KEY_BT_DEVICE);
                    int rssi=intent.getIntExtra(KEY_BT_RSSI,-1);
                    byte[] scanRecord=intent.getByteArrayExtra(KEY_SCAN_RECORD);
                    mEventHandler.onDeviceScanned(device,rssi,scanRecord);
                    break;
                }
                case EVENT_DEVICE_CONNECTED://ok
                {
                    BluetoothDevice device = intent.getParcelableExtra(KEY_BT_DEVICE);
                    mEventHandler.onDeviceConnected(device);
                }
                    break;
                case EVENT_DEVICE_DISCONNECT://ok
                {
                    BluetoothDevice device = intent.getParcelableExtra(KEY_BT_DEVICE);
                    mEventHandler.onDeviceDisconnected(device);
                }
                    break;
                case EVENT_SERVICES_DISCOVERED://ok
                {
                    showLog("EVENT_SERVICES_DISCOVERED");
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
