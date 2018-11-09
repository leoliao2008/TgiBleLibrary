package tgi.com.bluetooth.manager;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.util.Pair;

import java.util.ArrayList;

import tgi.com.bluetooth.BtLibConstants;
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

    public synchronized static BleClientManager getInstance() {
        if (bleClientManager == null) {
            bleClientManager = new BleClientManager();
        }
        return bleClientManager;
    }

    public void onResume(Activity activity, BleClientEventHandler eventHandler) {
        BleBackgroundService.start(activity);//start the service if not already started.
        mEventHandler = eventHandler;
        mReceiver = new BleClientManagerBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter(ACTION_BLE_ACTIVITY_UPDATE);
        activity.registerReceiver(mReceiver, intentFilter);
    }

    public void onStop(Activity activity) {
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

    public void connectDevice(Context context,String devAddress){
        sendBroadcast(
                context,
                REQUEST_CONNECT_DEVICE,
                new Pair<String, String>(KEY_BLE_DEVICE_ADDRESS,devAddress));
    }

    public void disconnectDevice(Context context){
        sendBroadcast(context, REQUEST_DISCONNECT_DEVICE);
    }

    public void startScanningServices(Context activity){
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

    public void toggleNotification(Context context,boolean isToReg,String serviceUUID,String charUUID,String descUUID){
        Intent intent=new Intent(ACTION_REQUEST_BLE_SERVICE);
        intent.putExtra(KEY_BLE_REQUEST, REQUEST_TOGGLE_REG_NOTIFICATION);
        intent.putExtra(KEY_BLE_SERVICE_UUID,serviceUUID);
        intent.putExtra(KEY_BLE_CHAR_UUID,charUUID);
        intent.putExtra(KEY_BLE_DESC_UUID,descUUID);
        intent.putExtra(KEY_IS_TO_ENABLE,isToReg);
        context.sendBroadcast(intent);
    }

    public void killLibraryService(Context context){
        sendBroadcast(context, REQUEST_KILL_LIBRARY_SERVICE);
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


    private class BleClientManagerBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String event = intent.getStringExtra(KEY_BLE_EVENT);
            switch (event) {
                case BtLibConstants.EVENT_BT_NOT_SUPPORTED://ok
                    mEventHandler.onBtNotSupported();
                    break;
                case BtLibConstants.EVENT_BLE_NOT_SUPPORTED://ok
                    mEventHandler.onBleNotSupported();
                    break;
                case BtLibConstants.EVENT_BT_NOT_ENABLE://ok
                    mEventHandler.onBtNotEnabled();
                    break;
                case EVENT_SCAN_STARTS://ok
                    showLog("onStartScanningDevice");
                    mEventHandler.onStartScanningDevice();
                    break;
                case EVENT_SCAN_STOPS://ok
                    showLog("onStopScanningDevice");
                    mEventHandler.onStopScanningDevice();
                    break;
                case EVENT_DEVICE_IS_SCANNED: {//ok
                    showLog("onDeviceScanned");
                    BluetoothDevice device=intent.getParcelableExtra(KEY_BT_DEVICE);
                    int rssi=intent.getIntExtra(KEY_BT_RSSI,-1);
                    byte[] scanRecord=intent.getByteArrayExtra(KEY_SCAN_RECORD);
                    mEventHandler.onDeviceScanned(device,rssi, scanRecord);
                    break;
                }
                case EVENT_DEVICE_CONNECTED://ok
                    mEventHandler.onDeviceConnected();
                    break;
                case EVENT_DEVICE_DISCONNECT://ok
                    mEventHandler.onDeviceDisconnected();
                    break;
//                case EVENT_START_DISCOVER_SERVICE://ok
//                    boolean isStartSuccess = intent.getBooleanExtra(KEY_BLE_IS_DISCOVER_START_SUCCESS, false);
//                    mEventHandler.onStartDiscoveringServices(isStartSuccess);
//                    break;
                case EVENT_SERVICES_DISCOVERED://ok
                {
                    BluetoothDevice device = intent.getParcelableExtra(KEY_BT_DEVICE);
                    ArrayList<BluetoothGattService> services = intent.getParcelableArrayListExtra(KEY_GATT_SERVICES);
                    mEventHandler.onServiceDiscover(device,services);
                }
                    break;
//                case EVENT_BLE_READ_CHAR_INIT_OK:
//                    mEventHandler.onCharacteristicReadInitComplete(true);
//                    break;
//                case EVENT_BLE_READ_CHAR_INIT_FAILS:
//                    mEventHandler.onCharacteristicReadInitComplete(false);
//                    break;
                case EVENT_CHAR_READ: {//ok
//                    boolean isSuccess = intent.getBooleanExtra(KEY_BLE_OP_SUCCESS, false);
                    String uuid = intent.getStringExtra(KEY_BLE_CHAR_UUID);
                    byte[] bytes = intent.getByteArrayExtra(KEY_BLE_CHAR_VALUE);
                    mEventHandler.onCharRead(uuid, bytes);
                    break;
                }
//                case EVENT_BLE_WRITE_CHAR_INIT_OK:
//                    mEventHandler.onCharacteristicWriteInitComplete(true);
//                    break;
//                case EVENT_BLE_WRITE_CHAR_INIT_FAILS:
//                    mEventHandler.onCharacteristicWriteInitComplete(false);
//                    break;
                case EVENT_BLE_CHAR_WRITTEN: {//ok
//                    boolean isSuccess = intent.getBooleanExtra(KEY_BLE_OP_SUCCESS, false);
                    String uuid = intent.getStringExtra(KEY_BLE_CHAR_UUID);
                    byte[] bytes = intent.getByteArrayExtra(KEY_BLE_CHAR_VALUE);
                    mEventHandler.onCharWritten(uuid, bytes);
                    break;
                }
//                case EVENT_BEGIN_ENABLE_BLE_NOTIFICATION: {
//                    String uuid = intent.getStringExtra(KEY_BLE_DESC_UUID);
//                    boolean iSuccess = intent.getBooleanExtra(KEY_BLE_OP_SUCCESS, false);
//                    mEventHandler.onBeginEnableNotification(uuid, iSuccess);
//                    break;
//                }
//                case EVENT_BEGIN_DISABLE_BLE_NOTIFICATION: {
//                    String uuid = intent.getStringExtra(KEY_BLE_DESC_UUID);
//                    boolean iSuccess = intent.getBooleanExtra(KEY_BLE_OP_SUCCESS, false);
//                    mEventHandler.onBeginDisableNotification(uuid, iSuccess);
//                    break;
//                }
                case EVENT_BLE_ENABLE_NOTIFICATION:{
                    String uuid = intent.getStringExtra(KEY_BLE_DESC_UUID);
                    boolean isSuccess = intent.getBooleanExtra(KEY_BLE_OP_SUCCESS, false);
                    mEventHandler.onEnableNotification(isSuccess,uuid);
                    break;
                }
                case EVENT_BLE_DISABLE_NOTIFICATION:{
                    String uuid = intent.getStringExtra(KEY_BLE_DESC_UUID);
                    boolean isSuccess = intent.getBooleanExtra(KEY_BLE_OP_SUCCESS, false);
                    mEventHandler.onDisableNotification(isSuccess,uuid);
                    break;
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
