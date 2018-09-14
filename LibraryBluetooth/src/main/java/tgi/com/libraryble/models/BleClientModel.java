package tgi.com.libraryble.models;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.Context;
import android.content.pm.PackageManager;

import tgi.com.libraryble.base.BaseBtModel;
import tgi.com.libraryble.callbacks.BleDeviceScanCallback;

public class BleClientModel extends BaseBtModel {

    public boolean hasBleFeature(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    public void startScanningLeDevice(BluetoothAdapter adapter, BleDeviceScanCallback callback) {
        callback.onScanStart();
        adapter.startLeScan(callback);
    }


    public void stopScanningLeDevice(BluetoothAdapter adapter, BleDeviceScanCallback callback) {
        adapter.stopLeScan(callback);
        callback.onScanStop();
    }

    public BluetoothGatt connectToLeDevice(Context context, BluetoothDevice device, BluetoothGattCallback callback) {
        return device.connectGatt(context, true, callback);
    }

    public BluetoothDevice getBluetoothDevice(BluetoothAdapter adapter,String deviceAddress){
        return adapter.getRemoteDevice(deviceAddress);
    }


    public void disconnectLeDevice(BluetoothGatt btGatt){
        btGatt.disconnect();
        btGatt.close();
    }

    public void readCharacteristic(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic btChar) {
        bluetoothGatt.readCharacteristic(btChar);
    }

    public void readDescriptor(BluetoothGatt bluetoothGatt, BluetoothGattDescriptor descriptor) {
        bluetoothGatt.readDescriptor(descriptor);
    }
}
