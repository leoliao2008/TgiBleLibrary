package tgi.com.libraryble.service;

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
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import tgi.com.libraryble.LibraryBtConstants;
import tgi.com.libraryble.bean.BleCharacteristic;
import tgi.com.libraryble.bean.BleDescriptor;
import tgi.com.libraryble.bean.BleDevice;
import tgi.com.libraryble.bean.BleService;
import tgi.com.libraryble.callbacks.BleDeviceScanCallback;
import tgi.com.libraryble.models.BleClientModel;

import static android.bluetooth.BluetoothGatt.GATT_SUCCESS;
import static android.bluetooth.BluetoothProfile.STATE_CONNECTED;
import static android.bluetooth.BluetoothProfile.STATE_DISCONNECTED;
import static tgi.com.libraryble.LibraryBtConstants.*;

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
    private AtomicBoolean isScanning = new AtomicBoolean(false);
    private BleDeviceScanCallback mScanCallback = new BleDeviceScanCallback() {
        @Override
        public void onScanStart() {
            sendBroadcast(genSimpleIntent(EVENT_SCAN_STARTS));
            mHandler.postDelayed(mRunnableStopScanning, 5000);
            isScanning.set(true);
        }

        @Override
        public void onScanStop() {
            sendBroadcast(genSimpleIntent(EVENT_SCAN_STOPS));
            mHandler.removeCallbacks(mRunnableStopScanning);
            isScanning.set(false);
        }

        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            String deviceName = device.getName();
            if (TextUtils.isEmpty(deviceName)) {
                deviceName = "Unknown Device";
            }
            Intent intent = genSimpleIntent(EVENT_DEVICE_SCAN);
            intent.putExtra(KEY_BLE_DEVICE_NAME, deviceName);
            intent.putExtra(KEY_BLE_DEVICE_ADDRESS, device.getAddress());
            sendBroadcast(intent);

        }
    };
    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onPhyUpdate(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
            super.onPhyUpdate(gatt, txPhy, rxPhy, status);
        }

        @Override
        public void onPhyRead(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
            super.onPhyRead(gatt, txPhy, rxPhy, status);
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (status == GATT_SUCCESS && newState == STATE_CONNECTED) {
                sendBroadcast(genSimpleIntent(EVENT_DEVICE_CONNECTED));
                boolean isStartSuccess = mBluetoothGatt.discoverServices();
                Intent intent = genSimpleIntent(EVENT_START_DISCOVER_SERVICE);
                intent.putExtra(KEY_BLE_IS_DISCOVER_START_SUCCESS, isStartSuccess);
                sendBroadcast(intent);
            } else if (status == GATT_SUCCESS && newState == STATE_DISCONNECTED) {
                sendBroadcast(genSimpleIntent(EVENT_DEVICE_DISCONNECT));
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if (status == GATT_SUCCESS) {
                BleDevice device = genBleDevice(gatt);
                Intent intent = genSimpleIntent(EVENT_SERVICES_DISCOVERED);
                intent.putExtra(KEY_DEVICE_DETAILS, device);
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
            String uuid = characteristic.getUuid().toString();
            Intent intent = genSimpleIntent(EVENT_CHAR_READ);
            intent.putExtra(KEY_BLE_CHAR_UUID, uuid);
            intent.putExtra(KEY_BLE_CHAR_VALUE, value);
            intent.putExtra(KEY_BLE_OP_SUCCESS, status == GATT_SUCCESS);
            sendBroadcast(intent);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            byte[] value = characteristic.getValue();
            if (value == null) {
                value = new byte[]{};
            }
            String uuid = characteristic.getUuid().toString();
            Intent intent = genSimpleIntent(EVENT_BLE_WRITE_CHAR_RESULT);
            intent.putExtra(KEY_BLE_CHAR_UUID, uuid);
            intent.putExtra(KEY_BLE_CHAR_VALUE, value);
            intent.putExtra(KEY_BLE_WRITE_CHAR_RESULT, status == GATT_SUCCESS);
            sendBroadcast(intent);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            byte[] value = characteristic.getValue();
            if (value == null) {
                value = new byte[]{};
            }
            String uuid = characteristic.getUuid().toString();
            Intent intent = genSimpleIntent(EVENT_CHAR_CHANGED);
            intent.putExtra(KEY_BLE_CHAR_UUID, uuid);
            intent.putExtra(KEY_BLE_CHAR_VALUE, value);
            sendBroadcast(intent);
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            String uuid = descriptor.getUuid().toString();
            byte[] value = descriptor.getValue();
            Intent intent=null;
            if (status == GATT_SUCCESS) {
                if (Arrays.equals(value, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)) {
                    intent = genSimpleIntent(EVENT_BLE_ENABLE_NOTIFICATION_SUCCESS);
                } else if (Arrays.equals(value, BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE)) {
                    intent = genSimpleIntent(EVENT_BLE_DISABLE_NOTIFICATION_SUCCESS);
                }
            }else {
                intent=genSimpleIntent(EVENT_BLE_TOGGLE_NOTIFICATION_FAILS);
            }
            if(intent!=null){
                intent.putExtra(KEY_BLE_DESC_UUID,uuid);
                sendBroadcast(intent);
            }
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
        }
    };
    private BluetoothDevice mDevice;


    @Override
    public void onCreate() {
        super.onCreate();
        mHandler = new Handler(Looper.getMainLooper());
        mBleClientModel = new BleClientModel();
        mReceiver = new BleServiceBroadcastReceiver();
        mBtAdapter = mBleClientModel.getBtAdapter(this);
        mBleClientModel.startScanningLeDevice(mBtAdapter, mScanCallback);
        registerReceiver(mReceiver, new IntentFilter(ACTION_REQUEST_BLE_SERVICE));

    }

    @Override
    public void onDestroy() {
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
            mReceiver = null;
        }
        mBleClientModel.disconnectLeDevice(mBluetoothGatt);
        super.onDestroy();
    }

    public BleBackgroundService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
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

    private BleDevice genBleDevice(BluetoothGatt gatt) {
        BleDevice device = new BleDevice();
        List<BluetoothGattService> services = gatt.getServices();
        BluetoothDevice dv = gatt.getDevice();
        String dvName = dv.getName();
        if (TextUtils.isEmpty(dvName)) {
            dvName = "Unknown Device";
        }
        device.setName(dvName);
        device.setAddress(dv.getAddress());
        ArrayList<BleService> bleServices = new ArrayList<>();
        for (BluetoothGattService sv : services) {
            BleService bleService = new BleService();
            bleService.setName("Unknown Service");
            bleService.setUUID(sv.getUuid().toString());
            ArrayList<BleCharacteristic> characteristics = new ArrayList<>();
            List<BluetoothGattCharacteristic> btChars = sv.getCharacteristics();
            for (BluetoothGattCharacteristic ch : btChars) {
                BleCharacteristic characteristic = new BleCharacteristic();
                characteristic.setName("Unknown Characteristic");
                characteristic.setUUID(ch.getUuid().toString());
                ArrayList<BleDescriptor> bleDescriptors = new ArrayList<>();
                List<BluetoothGattDescriptor> btDescs = ch.getDescriptors();
                for (BluetoothGattDescriptor dsc : btDescs) {
                    BleDescriptor descriptor = new BleDescriptor();
                    descriptor.setName("Unknown Descriptor");
                    descriptor.setUUID(dsc.getUuid().toString());
                    bleDescriptors.add(descriptor);
                }
                characteristic.setDescriptors(bleDescriptors);
                characteristics.add(characteristic);
            }

            bleService.setBleCharacteristics(characteristics);
            bleServices.add(bleService);
        }
        device.setBleServices(bleServices);
        return device;
    }

    private class BleServiceBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String request = intent.getStringExtra(KEY_BLE_REQUEST);
            switch (request) {
                case REQUEST_SCAN_DEVICE:
                    if (!isScanning.get()) {
                        mBleClientModel.startScanningLeDevice(mBtAdapter, mScanCallback);
                    }
                    break;
                case REQUEST_STOP_SCANNING_DEVICE:
                    if (isScanning.get()) {
                        mBleClientModel.stopScanningLeDevice(mBtAdapter, mScanCallback);
                    }
                    break;
                case REQUEST_CONNECT_DEVICE: {
                    String bleDevAdd = intent.getStringExtra(KEY_BLE_DEVICE_ADDRESS);
                    if (mDevice != null && mDevice.getAddress().equals(bleDevAdd)) {
                        return;
                    }
                    if (mBleClientModel != null) {
                        mBleClientModel.disconnectLeDevice(mBluetoothGatt);
                    }
                    mDevice = mBleClientModel.getBluetoothDevice(mBtAdapter, bleDevAdd);
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
                case REQUEST_READ_CHAR: {
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
                    if (isSuccess) {
                        showLog("REQUEST_READ_CHAR success");
                        sendBroadcast(genSimpleIntent(EVENT_BLE_READ_CHAR_INIT_OK));
                    } else {
                        showLog("REQUEST_READ_CHAR fails");
                        sendBroadcast(genSimpleIntent(EVENT_BLE_READ_CHAR_INIT_FAILS));
                    }
                }
                break;
                case REQUEST_WRITE_CHAR: {
                    boolean isSuccess = false;
                    String svUUID = intent.getStringExtra(KEY_BLE_SERVICE_UUID);
                    String btCharUUID = intent.getStringExtra(KEY_BLE_CHAR_UUID);
                    byte[] data = intent.getByteArrayExtra(KEY_BLE_CHAR_DATA);
                    BluetoothGattService service = mBluetoothGatt.getService(UUID.fromString(svUUID));
                    if (service != null) {
                        BluetoothGattCharacteristic btChar = service.getCharacteristic(UUID.fromString(btCharUUID));
                        if (btChar != null) {
                            isSuccess = mBleClientModel.writeCharacteristic(
                                    mBluetoothGatt,
                                    btChar,
                                    data
                            );
                        }
                    }
                    if (isSuccess) {
                        showLog("REQUEST_WRITE_CHAR success");
                        sendBroadcast(genSimpleIntent(EVENT_BLE_WRITE_CHAR_INIT_OK));
                    } else {
                        showLog("REQUEST_WRIT_CHAR fails");
                        sendBroadcast(genSimpleIntent(EVENT_BLE_WRITE_CHAR_INIT_FAILS));
                    }
                }
                break;
                case REQUEST_TOGGLE_REG_NOTIFICATION: {
                    boolean isSuccess = false;
                    String svUUID = intent.getStringExtra(KEY_BLE_SERVICE_UUID);
                    String btCharUUID = intent.getStringExtra(KEY_BLE_CHAR_UUID);
                    String descUUID = intent.getStringExtra(KEY_BLE_DESC_UUID);
                    boolean isEnable = intent.getBooleanExtra(KEY_IS_ENABLE, false);
                    BluetoothGattService service = mBluetoothGatt.getService(UUID.fromString(svUUID));
                    if (service != null) {
                        BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(btCharUUID));
                        if (characteristic != null) {
                            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(descUUID));
                            if (descriptor != null) {
                                isSuccess = mBleClientModel.toggleNotification(
                                        mBluetoothGatt,
                                        isEnable,
                                        descriptor
                                );
                            }
                        }
                    }
                    Intent simpleIntent;
                    if (isEnable) {
                        simpleIntent = genSimpleIntent(EVENT_BEGIN_ENABLE_BLE_NOTIFICATION);
                    } else {
                        simpleIntent = genSimpleIntent(EVENT_BEGIN_DISABLE_BLE_NOTIFICATION);
                    }
                    simpleIntent.putExtra(KEY_BLE_DESC_UUID, descUUID);
                    simpleIntent.putExtra(KEY_BLE_OP_SUCCESS, isSuccess);
                    sendBroadcast(simpleIntent);
                }
                break;
                case REQUEST_STOP_BLE_BG_SERVICE:
                    stopSelf();
                    break;
                default:
                    break;
            }

        }
    }

    private void showLog(String msg) {
        Log.e(getClass().getSimpleName(), msg);
    }
}
