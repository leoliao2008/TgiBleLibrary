package tgi.com.libraryble.manager;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import java.util.List;
import java.util.UUID;

import tgi.com.libraryble.base.BaseBtManager;
import tgi.com.libraryble.callbacks.BleClientEventHandler;
import tgi.com.libraryble.callbacks.BleDeviceScanCallback;
import tgi.com.libraryble.models.BleClientModel;
import tgi.com.libraryble.service.BleBackgroundService;

import static android.bluetooth.BluetoothGatt.GATT_SUCCESS;
import static android.bluetooth.BluetoothProfile.STATE_CONNECTED;

/**
 * Bluetooth low energy client manager.
 * This class is developed according to Google official API guide.
 * <a href="https://developer.android.google.cn/guide/topics/connectivity/bluetooth-le">Official Link.</a>
 * Use this class instead of the over-built Google APIs.
 */
public class BleClientManagerBeta extends BaseBtManager<BleClientModel, BleClientEventHandler> {
    private BleDeviceScanCallback mBleDeviceScanCallback = new BleDeviceScanCallback() {
        @Override
        public void onScanStart() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mEventHandler.onStartScanningDevice();
                }
            });
        }

        @Override
        public void onScanStop() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mEventHandler.onStopScanningDevice();
                }
            });
        }

        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
            if (device != null && device.getName() != null) {
                showLog(device.getName());
            } else {
                showLog("Unknown Device");
            }
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mEventHandler.onDeviceScanned(device, rssi, scanRecord);
                }
            });
        }
    };
    private BluetoothGatt mBluetoothGatt;
    private Handler mHandler;
    private Runnable mRunnableStopScanning = new Runnable() {
        @Override
        public void run() {
            stopScanningDevice();
        }
    };


    public BleClientManagerBeta(Activity context, BleClientEventHandler eventHandler) {
        super(context, eventHandler);
        if (Looper.myLooper() == null) {
            Looper.prepare();
            Looper.loop();
        }
        mHandler = new Handler(Looper.myLooper());
        if (!mBtModel.hasBleFeature(context)) {
            mEventHandler.onBleNotSupported();
        }
        Intent intent=new Intent(context,BleBackgroundService.class);
        context.startService(intent);
    }


    @Override
    protected BleClientModel setBtModel() {
        return new BleClientModel();
    }


    public void startScanningDevice() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                mHandler.postDelayed(mRunnableStopScanning, 5000);
                mBtModel.startScanningLeDevice(mBluetoothAdapter, mBleDeviceScanCallback);
                showLog("Scanning...");
            }
        }).start();
    }

    public void stopScanningDevice() {
        showLog("Stop Scanning.");
        mBtModel.stopScanningLeDevice(mBluetoothAdapter, mBleDeviceScanCallback);
        mHandler.removeCallbacks(mRunnableStopScanning);
    }

    public void connectToDevice(Context context, String deviceAddress) {
        BluetoothDevice device = mBtModel.getBluetoothDevice(mBluetoothAdapter, deviceAddress);
        connectToDevice(context, device);
    }

    public void connectToDevice(Context context, BluetoothDevice device) {
        disconnectDeviceIfAny();
        mBluetoothGatt = mBtModel.connectToLeDevice(context, device, new BluetoothGattCallback() {

            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                super.onConnectionStateChange(gatt, status, newState);
                if (status == GATT_SUCCESS && newState == STATE_CONNECTED) {
                    showLog("Device Connect Success!");
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mBluetoothGatt.discoverServices();
                        }
                    });
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                super.onServicesDiscovered(gatt, status);
                showLog("Services Discover:");
                final List<BluetoothGattService> services = mBluetoothGatt.getServices();
                for (BluetoothGattService s : services) {
                    showLog(s.getUuid().toString());
                }
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mEventHandler.onServiceListUpdate(services);
                    }
                });

            }

            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicRead(gatt, characteristic, status);
                showLog("Char read.");
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mEventHandler.onCharacteristicRead(characteristic);
                    }
                });

            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicWrite(gatt, characteristic, status);
                showLog("onCharacteristicWrite");
            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
                showLog("onCharacteristicChanged");
                super.onCharacteristicChanged(gatt, characteristic);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mEventHandler.onReceiveNotification(characteristic);
                    }
                });
            }

            @Override
            public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                super.onDescriptorRead(gatt, descriptor, status);
            }

            @Override
            public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                showLog("onDescriptorWrite");
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

    public BluetoothGattCharacteristic getBluetoothGattCharacteristic(String btServiceUUID, String btCharUUID) {
        List<BluetoothGattService> services = mBluetoothGatt.getServices();
        BluetoothGattService service = null;
        for (BluetoothGattService sv : services) {
            UUID uuid = sv.getUuid();
            if (btServiceUUID.equals(uuid.toString())) {
                service = sv;
                break;
            }
        }
        if (service != null) {
            return service.getCharacteristic(UUID.fromString(btCharUUID));
        }
        return null;
    }

    public boolean readCharacteristic(BluetoothGattCharacteristic btChar) {
        return mBtModel.readCharacteristic(mBluetoothGatt, btChar);
    }

    public boolean writeCharacteristic(BluetoothGattCharacteristic btChar, byte[] value) {
        return mBtModel.writeCharacteristic(mBluetoothGatt, btChar, value);
    }

    public boolean toggleNotification(BluetoothGattService service, boolean isToEnable, String btCharUUID, String descriptorUUID) {
        BluetoothGattCharacteristic btChar = mBtModel.getBluetoothGattCharacteristic(service, btCharUUID);
        if (btChar != null) {
            BluetoothGattDescriptor descriptor = mBtModel.getDescriptor(btChar, descriptorUUID);
            if (descriptor != null) {
                return mBtModel.toggleNotification(mBluetoothGatt, isToEnable, descriptor);
            }
        }
        return false;
    }

    public boolean toggleNotification(boolean isToEnable, String btServiceUUID, String btCharUUID, String descriptorUUID) {
        BluetoothGattCharacteristic characteristic = getBluetoothGattCharacteristic(btServiceUUID, btCharUUID);
        if (characteristic != null) {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(descriptorUUID));
            if (descriptor != null) {
                return mBtModel.toggleNotification(mBluetoothGatt, isToEnable, descriptor);
            }
        }
        return false;
    }

    public void disconnectDeviceIfAny() {
        if (mBluetoothGatt != null) {
            mBtModel.disconnectLeDevice(mBluetoothGatt);
            mBluetoothGatt = null;
        }
    }

}
