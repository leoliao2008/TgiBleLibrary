package tgi.com.bluetooth.service;

import android.Manifest;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import tgi.com.bluetooth.BtLibConstants;
import tgi.com.bluetooth.bean.TgiBtGattService;
import tgi.com.bluetooth.callbacks.BleDeviceScanCallback;
import tgi.com.bluetooth.models.BleClientModel;

import static android.bluetooth.BluetoothGatt.GATT_FAILURE;
import static android.bluetooth.BluetoothGatt.GATT_SUCCESS;
import static android.bluetooth.BluetoothProfile.STATE_CONNECTED;
import static android.bluetooth.BluetoothProfile.STATE_DISCONNECTED;
import static tgi.com.bluetooth.BtLibConstants.*;

public class BleBackgroundService extends Service {
    private BleClientModel mBleClientModel;
    private BluetoothAdapter mBtAdapter;
    private BleServiceBroadcastReceiver mReceiver;
    private BluetoothGatt mBluetoothGatt;
    private Handler mHandler;
    private Runnable mRunnableStopScanning = new Runnable() {
        @Override
        public void run() {
            mBleClientModel.stopScanningLeDevice(
                    mBtAdapter,
                    mScanCallback
            );
        }
    };
    private volatile boolean isScanning = false;
    //BleDeviceScanCallback is responsible for scanning device
    private BleDeviceScanCallback mScanCallback = new BleDeviceScanCallback() {
        @Override
        public void onScanStart() {
            sendBroadcast(genSimpleIntent(EVENT_DEVICES_SCANNING_STARTS));
            mHandler.postDelayed(mRunnableStopScanning, 5000);
            isScanning=true;
        }

        @Override
        public void onScanStop() {
            sendBroadcast(genSimpleIntent(EVENT_DEVICES_SCANNING_STOPS));
            mHandler.removeCallbacks(mRunnableStopScanning);
            isScanning=false;
        }

        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            Intent intent = genSimpleIntent(EVENT_A_DEVICE_IS_SCANNED);
            intent.putExtra(BtLibConstants.KEY_BT_DEVICE,device);
            intent.putExtra(BtLibConstants.KEY_BT_RSSI,rssi);
            intent.putExtra(BtLibConstants.KEY_SCAN_RECORD,scanRecord);
            sendBroadcast(intent);
        }
    };
    //BluetoothGattCallback is responsible for bt service, bt char and bt descriptor operation
    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (status == GATT_SUCCESS && newState == STATE_CONNECTED) {
                broadcastConnectDeviceSuccess(gatt.getDevice());
            } else if (status == GATT_SUCCESS && newState == STATE_DISCONNECTED) {
                mDevice=null;
                Intent intent = genSimpleIntent(EVENT_DEVICE_DISCONNECT);
                intent.putExtra(KEY_BT_DEVICE,gatt.getDevice());
                sendBroadcast(intent);
                gatt.close();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if (status == GATT_SUCCESS) {
                BluetoothDevice device = gatt.getDevice();
                Intent intent = genSimpleIntent(EVENT_SERVICES_DISCOVERED);
                intent.putExtra(KEY_BT_DEVICE,device);
                List<BluetoothGattService> services = gatt.getServices();
                ArrayList<TgiBtGattService> tgiBtGattServices=new ArrayList<>();
                for(BluetoothGattService temp:services){
                    tgiBtGattServices.add(new TgiBtGattService(temp));
                }
                intent.putParcelableArrayListExtra(BtLibConstants.KEY_GATT_SERVICES,tgiBtGattServices);
                sendBroadcast(intent);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            byte[] value = characteristic.getValue();
            if (value == null) {
                value = new byte[]{};
            }
            if (status==GATT_SUCCESS){
                String uuid = characteristic.getUuid().toString();
                Intent intent = genSimpleIntent(EVENT_CHAR_READ);
                intent.putExtra(KEY_BLE_CHAR_UUID, uuid);
                intent.putExtra(KEY_BLE_CHAR_VALUE, value);
                sendBroadcast(intent);
            }

        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            byte[] value = characteristic.getValue();
            if (value == null) {
                value = new byte[]{};
            }
            String charUUID = characteristic.getUuid().toString();
            String serviceUUID = characteristic.getService().getUuid().toString();
            Intent intent = genSimpleIntent(EVENT_BLE_CHAR_WRITTEN);
            intent.putExtra(KEY_BLE_CHAR_UUID, charUUID);
            intent.putExtra(KEY_BLE_SERVICE_UUID,serviceUUID);
            intent.putExtra(KEY_BLE_CHAR_VALUE, value);
            sendBroadcast(intent);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            byte[] value = characteristic.getValue();
            if (value == null) {
                value = new byte[]{};
            }
            String charUUID = characteristic.getUuid().toString();
            String serviceUUID = characteristic.getService().getUuid().toString();
            Intent intent = genSimpleIntent(EVENT_NOTIFICATION_TRIGGERED);
            intent.putExtra(KEY_BLE_CHAR_UUID, charUUID);
            intent.putExtra(KEY_BLE_SERVICE_UUID,serviceUUID);
            intent.putExtra(KEY_BLE_CHAR_VALUE, value);
            sendBroadcast(intent);
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
            BluetoothGattCharacteristic btChar = descriptor.getCharacteristic();
            String serviceUUID = btChar.getService().getUuid().toString();
            String charUUID = btChar.getUuid().toString();
            String descUUID = descriptor.getUuid().toString();
            if(status==GATT_SUCCESS){
                byte[] value = descriptor.getValue();
                if(value==null||value.length==0){
                    value=new byte[]{};
                }
                Intent simpleIntent = genSimpleIntent(BtLibConstants.EVENT_BLE_DESCRIPTOR_IS_READ);
                simpleIntent.putExtra(KEY_BLE_SERVICE_UUID,serviceUUID);
                simpleIntent.putExtra(KEY_BLE_CHAR_UUID,charUUID);
                simpleIntent.putExtra(KEY_BLE_DESC_UUID,descUUID);
                simpleIntent.putExtra(BtLibConstants.KEY_BLE_DESC_VALUE,value);
                sendBroadcast(simpleIntent);
            }else {
                broadcastReadDescriptorFails(serviceUUID,charUUID,descUUID);
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            String serviceUUID = descriptor.getCharacteristic().getService().getUuid().toString();
            String charUUID = descriptor.getCharacteristic().getUuid().toString();
            String descUUID = descriptor.getUuid().toString();
            byte[] value = descriptor.getValue();
            if (value == null) {
                value = new byte[]{};
            }
            Intent intent = null;
            //set up notification
            if (Arrays.equals(value, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)) {
                if(status==GATT_SUCCESS){
                    intent = genSimpleIntent(BtLibConstants.EVENT_BLE_NOTIFICATION_IS_REGISTERED);
                    intent.putExtra(KEY_BLE_SERVICE_UUID,serviceUUID);
                    intent.putExtra(KEY_BLE_CHAR_UUID,charUUID);
                    intent.putExtra(KEY_BLE_DESC_UUID,descUUID);
                    sendBroadcast(intent);
                }else if(status==GATT_FAILURE) {
                    broadcastSetNotificationFails(
                            true,
                            serviceUUID,
                            charUUID,
                            descUUID);
                }
            } else if (Arrays.equals(value, BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE)) {
                if(status==GATT_SUCCESS){
                    intent = genSimpleIntent(BtLibConstants.EVENT_BLE_NOTIFICATION_IS_UNREGISTERED);
                    intent.putExtra(KEY_BLE_SERVICE_UUID,serviceUUID);
                    intent.putExtra(KEY_BLE_CHAR_UUID,charUUID);
                    intent.putExtra(KEY_BLE_DESC_UUID,descUUID);
                    sendBroadcast(intent);
                }else if(status==GATT_FAILURE){
                    broadcastSetNotificationFails(
                            false,
                            serviceUUID,
                            charUUID,
                            descUUID);
                }
            }
        }
    };


    private BluetoothDevice mDevice;

    public static void start(Context context) {
        Intent starter = new Intent(context, BleBackgroundService.class);
        context.startService(starter);
    }


    @Override
    public void onCreate() {
        super.onCreate();
        mHandler = new Handler(Looper.getMainLooper());
        mBleClientModel = new BleClientModel();
        if(isBleSupported()){
            mReceiver = new BleServiceBroadcastReceiver();
            mBtAdapter=mBleClientModel.getBtAdapter(this);
            registerReceiver(mReceiver, new IntentFilter(ACTION_REQUEST_BLE_SERVICE));
        }else {
            stopSelf();
        }
    }

    private boolean isBtEnable() {
        return mBleClientModel.isBtEnable(mBtAdapter);
    }

    private boolean isBleSupported() {
        if(!mBleClientModel.isBtSupported(this)){
            sendBroadcast(genSimpleIntent(BtLibConstants.EVENT_BT_NOT_SUPPORTED));
            return false;
        }else if(!mBleClientModel.hasBleFeature(this)){
            sendBroadcast(genSimpleIntent(BtLibConstants.EVENT_BLE_NOT_SUPPORTED));
            return false;
        }
        return true;
    }

    @Override
    public void onDestroy() {
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
            mReceiver = null;
        }
        if(mBluetoothGatt!=null){
            mBleClientModel.disconnectLeDevice(mBluetoothGatt);
            mBluetoothGatt=null;
        }
        super.onDestroy();
    }

    public BleBackgroundService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    private Intent genSimpleIntent(String event) {
        Intent intent = new Intent(ACTION_BLE_ACTIVITY_UPDATE);
        intent.putExtra(KEY_BLE_EVENT, event);
        return intent;
    }


    private class BleServiceBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            //if location permission is not granted, need to grant first.
            if(getPackageManager().checkPermission(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    getPackageName())!=PackageManager.PERMISSION_GRANTED){
                sendBroadcast(genSimpleIntent(BtLibConstants.EVENT_LOCATION_PERMISSION_NOT_GRANTED));
                return;
            }
            //if bt not yet enabled, need to enable first.
            if(!isBtEnable()){
                sendBroadcast(genSimpleIntent(BtLibConstants.EVENT_BT_NOT_ENABLE));
                return;
            }
            String request = intent.getStringExtra(KEY_BLE_REQUEST);
            switch (request) {
                case REQUEST_SCAN_DEVICE:
                    if (!isScanning) {
                        //first display bonded devices because sometimes you can't find bonded devices
                        //via scanning.
                        ArrayList<BluetoothDevice> bondedDevices = mBleClientModel.getBondedDevices(mBtAdapter);
                        for(BluetoothDevice temp:bondedDevices){
                            Intent simpleIntent = genSimpleIntent(EVENT_A_DEVICE_IS_SCANNED);
                            simpleIntent.putExtra(BtLibConstants.KEY_BT_DEVICE,temp);
                            sendBroadcast(intent);
                        }
                        mBleClientModel.startScanningLeDevice(mBtAdapter, mScanCallback);
                    }
                    break;
                case REQUEST_STOP_SCANNING_DEVICE:
                    if (isScanning) {
                        mBleClientModel.stopScanningLeDevice(mBtAdapter, mScanCallback);
                    }
                    break;
                case REQUEST_CONNECT_DEVICE:
                {
                    BluetoothDevice device = intent.getParcelableExtra(KEY_BT_DEVICE);
                    //if the device is already connected, return success.
                    if (mDevice != null && mDevice.getAddress().equals(device.getAddress())) {
                        broadcastConnectDeviceSuccess(device);
                        return;
                    }
                    //else disconnect previous device.
                    if (mBluetoothGatt!=null) {
                        mBleClientModel.disconnectLeDevice(mBluetoothGatt);
                    }
                    //build a new connection with the new device.
                    mDevice = device;
                    mBluetoothGatt = mBleClientModel.connectToLeDevice(
                            getApplicationContext(),
                            mDevice,
                            mGattCallback
                    );
                }
                break;
                case REQUEST_DISCONNECT_DEVICE:
                    if (mBleClientModel != null) {
                        mBleClientModel.disconnectLeDevice(mBluetoothGatt);
                    }
                    break;
                case BtLibConstants.REQUEST_START_DISCOVER_SERVICES:
                {
                    boolean isSuccess = mBleClientModel.startDiscoveringServices(mBluetoothGatt);
                    if(!isSuccess){
                        Intent simpleIntent = genSimpleIntent(BtLibConstants.EVENT_BT_FAIL_TO_DISCOVER_SERVICE);
                        simpleIntent.putExtra(KEY_BT_DEVICE,mBluetoothGatt.getDevice());
                        sendBroadcast(simpleIntent);
                    }
                }
                    break;
                case REQUEST_READ_CHAR:
                {
                    boolean isSuccess = false;
                    String svUUID = intent.getStringExtra(KEY_BLE_SERVICE_UUID);
                    String btCharUUID = intent.getStringExtra(KEY_BLE_CHAR_UUID);
                    BluetoothGattService service = mBluetoothGatt.getService(UUID.fromString(svUUID));
                    if (service != null) {
                        BluetoothGattCharacteristic btChar = service.getCharacteristic(UUID.fromString(btCharUUID));
                        if (btChar != null) {
                            isSuccess = mBleClientModel.readCharacteristic(mBluetoothGatt, btChar);
                        }
                    }
                    if (!isSuccess) {
                        Intent simpleIntent = genSimpleIntent(EVENT_BLE_READ_CHAR_FAILS);
                        simpleIntent.putExtra(KEY_BLE_SERVICE_UUID,svUUID);
                        simpleIntent.putExtra(KEY_BLE_CHAR_UUID,btCharUUID);
                        sendBroadcast(simpleIntent);
                    }
                }
                break;
                case REQUEST_WRITE_CHAR:
                {
                    boolean isSuccess = false;
                    String svUUID = intent.getStringExtra(KEY_BLE_SERVICE_UUID);
                    String btCharUUID = intent.getStringExtra(KEY_BLE_CHAR_UUID);
                    byte[] value = intent.getByteArrayExtra(KEY_BLE_CHAR_VALUE);
                    BluetoothGattService service = mBluetoothGatt.getService(UUID.fromString(svUUID));
                    if (service != null) {
                        BluetoothGattCharacteristic btChar = service.getCharacteristic(UUID.fromString(btCharUUID));
                        if (btChar != null) {
                            isSuccess = mBleClientModel.writeCharacteristic(
                                    mBluetoothGatt,
                                    btChar,
                                    value
                            );
                        }
                    }
                    if (!isSuccess) {
                        Intent simpleIntent = genSimpleIntent(EVENT_BLE_WRITE_CHAR_FAILS);
                        simpleIntent.putExtra(KEY_BLE_SERVICE_UUID,svUUID);
                        simpleIntent.putExtra(KEY_BLE_CHAR_UUID,btCharUUID);
                        simpleIntent.putExtra(KEY_BLE_CHAR_VALUE,value);
                        sendBroadcast(simpleIntent);
                    }
                }
                break;
                case REQUEST_REGISTER_NOTIFICATION:
                case REQUEST_UNREGISTER_NOTIFICATION:
                {
                    {
                        boolean isSuccess = false;
                        boolean isToEnable = request.equals(REQUEST_REGISTER_NOTIFICATION);
                        String svUUID = intent.getStringExtra(KEY_BLE_SERVICE_UUID);
                        String btCharUUID = intent.getStringExtra(KEY_BLE_CHAR_UUID);
                        String descUUID = intent.getStringExtra(KEY_BLE_DESC_UUID);
                        BluetoothGattService service = mBluetoothGatt.getService(UUID.fromString(svUUID));
                        if (service != null) {
                            BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(btCharUUID));
                            if (characteristic != null) {
                                BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(descUUID));
                                if (descriptor != null) {
                                    isSuccess = mBleClientModel.toggleNotification(
                                            mBluetoothGatt,
                                            isToEnable,
                                            mBluetoothGatt.getService(UUID.fromString(svUUID))
                                                    .getCharacteristic(UUID.fromString(btCharUUID)),
                                            descriptor
                                    );
                                }
                            }
                        }
                        if(!isSuccess){
                            broadcastSetNotificationFails(isToEnable,svUUID,btCharUUID,descUUID);
                        }
                    }
                }
                    break;
                case REQUEST_READ_DESCRIPTOR_VALUE:
                {
                    String svUUID = intent.getStringExtra(KEY_BLE_SERVICE_UUID);
                    String btCharUUID = intent.getStringExtra(KEY_BLE_CHAR_UUID);
                    String descUUID = intent.getStringExtra(KEY_BLE_DESC_UUID);
                    boolean isSuccess = mBleClientModel.readDescriptor(
                            mBluetoothGatt,
                            mBluetoothGatt.getService(UUID.fromString(svUUID))
                                    .getCharacteristic(UUID.fromString(btCharUUID))
                                    .getDescriptor(UUID.fromString(descUUID))
                    );
                    if(!isSuccess){
                        broadcastReadDescriptorFails(svUUID,btCharUUID,descUUID);
                    }
                }
                    break;
                case REQUEST_KILL_BLE_BG_SERVICE:
                    stopSelf();
                    break;
                default:
                    break;
            }

        }
    }

    private void broadcastReadDescriptorFails(String svUUID, String btCharUUID, String descUUID) {
        Intent simpleIntent = genSimpleIntent(BtLibConstants.EVENT_READ_DESCRIPTOR_FAILS);
        simpleIntent.putExtra(KEY_BLE_SERVICE_UUID,svUUID);
        simpleIntent.putExtra(KEY_BLE_CHAR_UUID,btCharUUID);
        simpleIntent.putExtra(KEY_BLE_DESC_UUID,descUUID);
        sendBroadcast(simpleIntent);
    }

    private void broadcastSetNotificationFails(
            boolean isToEnable,String svUUID, String btCharUUID, String descUUID) {
        Intent simpleIntent;
        if(isToEnable){
            simpleIntent= genSimpleIntent(BtLibConstants.EVENT_REGISTER_NOTIFICATION_FAILS);
        }else {
            simpleIntent= genSimpleIntent(BtLibConstants.EVENT_UNREGISTER_NOTIFICATION_FAILS);
        }
        simpleIntent.putExtra(KEY_BLE_SERVICE_UUID,svUUID);
        simpleIntent.putExtra(KEY_BLE_CHAR_UUID,btCharUUID);
        simpleIntent.putExtra(KEY_BLE_DESC_UUID,descUUID);
        sendBroadcast(simpleIntent);
    }

    private void broadcastConnectDeviceSuccess(BluetoothDevice device) {
        Intent simpleIntent = genSimpleIntent(EVENT_DEVICE_CONNECTED);
        simpleIntent.putExtra(BtLibConstants.KEY_BT_DEVICE,device);
        sendBroadcast(simpleIntent);
    }

    private void showLog(String msg) {
        Log.e(getClass().getSimpleName(), msg);
    }
}
