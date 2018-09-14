package tgi.com.libraryble.manager;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.util.List;

import tgi.com.libraryble.base.BaseBtManager;
import tgi.com.libraryble.callbacks.BleClientEventHandler;
import tgi.com.libraryble.callbacks.BleDeviceScanCallback;
import tgi.com.libraryble.models.BleClientModel;

import static android.bluetooth.BluetoothGatt.GATT_SUCCESS;
import static android.bluetooth.BluetoothProfile.STATE_CONNECTED;

/**
 * Bluetooth low energy client manager.
 * This class is developed according to Google official API guide.
 * <a href="https://developer.android.google.cn/guide/topics/connectivity/bluetooth-le">Official Link.</a>
 * Use this class instead of the over-built Google APIs.
 */
public class BleClientManager extends BaseBtManager<BleClientModel,BleClientEventHandler> {
    private BleDeviceScanCallback mBleDeviceScanCallback = new BleDeviceScanCallback() {
        @Override
        public void onScanStart() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mEventHandler.onScanStart();
                }
            });
        }

        @Override
        public void onScanStop() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mEventHandler.onScanStop();
                }
            });
        }

        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
            if(device!=null&&device.getName()!=null){
                showLog(device.getName());
            }else {
                showLog("Unknown Device");
            }
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mEventHandler.onScanDevice(device, rssi, scanRecord);
                }
            });
        }
    };
    private BluetoothGatt mBluetoothGatt;
    private Handler mHandler;




    public BleClientManager(Activity context,BleClientEventHandler eventHandler) {
        super(context,eventHandler);
        if (Looper.myLooper() == null) {
            Looper.prepare();
            Looper.loop();
        }
        mHandler = new Handler(Looper.myLooper());
        if (!mBtModel.hasBleFeature(context)) {
            mEventHandler.onBtBleNotSupported();
        }
    }


    @Override
    protected BleClientModel setBtModel() {
        return new BleClientModel();
    }


    public void startScanningDevice() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        stopScanningDevice();
                    }
                },5000);
                mBtModel.startScanningLeDevice(mBluetoothAdapter, mBleDeviceScanCallback);
                showLog("Scanning...");
            }
        }).start();
    }

    public void stopScanningDevice() {
        showLog("Stop Scanning.");
        mBtModel.stopScanningLeDevice(mBluetoothAdapter, mBleDeviceScanCallback);
    }

    public void connectToDevice(Context context,String deviceAddress){
        BluetoothDevice device = mBtModel.getBluetoothDevice(mBluetoothAdapter, deviceAddress);
        connectToDevice(context,device);
    }

    public void connectToDevice(Context context, BluetoothDevice device) {
        disconnectDeviceIfAny();
        mBluetoothGatt = mBtModel.connectToLeDevice(context, device, new BluetoothGattCallback() {

            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                super.onConnectionStateChange(gatt, status, newState);
                if (status == GATT_SUCCESS && newState == STATE_CONNECTED) {
                    mBluetoothGatt.discoverServices();
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                super.onServicesDiscovered(gatt, status);
                List<BluetoothGattService> services = mBluetoothGatt.getServices();
                mEventHandler.onServiceListUpdate(services);
            }

            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicRead(gatt, characteristic, status);
                mEventHandler.onCharacteristicRead(characteristic);
            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicWrite(gatt, characteristic, status);
            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                super.onCharacteristicChanged(gatt, characteristic);
            }

            @Override
            public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                super.onDescriptorRead(gatt, descriptor, status);
            }

            @Override
            public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                super.onDescriptorWrite(gatt, descriptor, status);
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
        });
    }

    public void readCharacteristic(BluetoothGattCharacteristic btChar){
        mBtModel.readCharacteristic(mBluetoothGatt,btChar);
    }

    public void readDescriptor(BluetoothGattDescriptor descriptor){
        mBtModel.readDescriptor(mBluetoothGatt,descriptor);
    }


    public void onDestroy(){
        disconnectDeviceIfAny();
    }

    private void disconnectDeviceIfAny(){
        if(mBluetoothGatt!=null){
            mBtModel.disconnectLeDevice(mBluetoothGatt);
            mBluetoothGatt=null;
        }
    }

}
