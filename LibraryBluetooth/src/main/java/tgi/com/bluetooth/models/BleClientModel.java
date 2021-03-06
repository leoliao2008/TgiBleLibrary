package tgi.com.bluetooth.models;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.pm.PackageManager;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import tgi.com.bluetooth.base.BaseBtModel;
import tgi.com.bluetooth.callbacks.BleDeviceScanCallback;

public class BleClientModel extends BaseBtModel {

    public boolean hasBleFeature(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    public boolean pairDevice(BluetoothDevice device) {
        try {
            Method createBondMethod = BluetoothDevice.class
                    .getMethod("createBond");
            Boolean result = (Boolean) createBondMethod.invoke(device);
            return result;
        } catch (Exception e) {
            // TODO: handle exception
            e.getStackTrace();
        }
        return false;
    }

    public boolean removeBond(BluetoothDevice btDevice){
        Method removeBondMethod = null;
        try {
            removeBondMethod = BluetoothDevice.class.getMethod("removeBond");
            Boolean returnValue = (Boolean) removeBondMethod.invoke(btDevice);
            return returnValue.booleanValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
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
        return device.connectGatt(context, false, callback);
    }

    public BluetoothDevice getBluetoothDevice(BluetoothAdapter adapter, String deviceAddress) {
        return adapter.getRemoteDevice(deviceAddress.toUpperCase());
    }


    public void disconnectLeDevice(BluetoothGatt btGatt) {
        btGatt.disconnect();
        btGatt.close();
    }

    public boolean startDiscoveringServices(BluetoothGatt btGatt) {
        return btGatt.discoverServices();
    }


    public BluetoothGattCharacteristic getBluetoothGattCharacteristic(BluetoothGattService service, String btCharUUID) {
        return service.getCharacteristic(UUID.fromString(btCharUUID));
    }

    public boolean readCharacteristic(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic btChar) {
        return bluetoothGatt.readCharacteristic(btChar);
    }

    public BluetoothGattDescriptor getDescriptor(BluetoothGattCharacteristic btChar, String descriptorUUID) {
        return btChar.getDescriptor(UUID.fromString(descriptorUUID));
    }

    public boolean readDescriptor(BluetoothGatt bluetoothGatt, BluetoothGattDescriptor descriptor) {
        return bluetoothGatt.readDescriptor(descriptor);
    }

    public boolean toggleNotification(
            BluetoothGatt gatt,
            boolean isToEnable,
            BluetoothGattCharacteristic btChar,
            BluetoothGattDescriptor descriptor) {

        gatt.setCharacteristicNotification(btChar, isToEnable);
        if (isToEnable) {
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        } else {
            descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        }
        return gatt.writeDescriptor(descriptor);
    }

    public boolean writeCharacteristic(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic btChar, byte[] value) {
        btChar.setValue(value);
        return bluetoothGatt.writeCharacteristic(btChar);
    }

    public ArrayList<BluetoothDevice> getBondedDevices(BluetoothAdapter adapter) {
        return new ArrayList<BluetoothDevice>(adapter.getBondedDevices());
    }
}
