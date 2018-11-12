package tgi.com.bluetooth.callbacks;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;

import java.util.ArrayList;

import tgi.com.bluetooth.bean.TgiBtGattService;

public class BleClientEventHandler {


    public void onBleNotSupported() {

    }


    public void onCharRead(String uuid, byte[] value) {

    }


    public void onCharWritten(String uuid, byte[] value) {

    }


    public void onEnableNotification(boolean isSuccess, String descUuid) {

    }

    public void onDisableNotification(boolean isSuccess, String descUuid) {

    }

    public void onDeviceScanned(BluetoothDevice device, int rssi, byte[] scanRecord) {

    }
    public void onBtNotSupported() {

    }

    public void onUserRefusesToEnableBt() {

    }

    public void onUserEnableBt() {

    }

    public void onError(String msg) {

    }

    public void onStartScanningDevice() {

    }

    public void onStopScanningDevice() {

    }

    public void onDeviceConnected(BluetoothDevice device) {

    }

    public void onDeviceDisconnected(BluetoothDevice device) {

    }

    public void onBtNotEnabled() {

    }

    public void onServiceDiscover(BluetoothDevice device, ArrayList<TgiBtGattService> services) {

    }

    public void onLocationPermissionNotGranted() {

    }

    public void onFailToReadChar(String serviceUUID, String charUUID) {

    }

    public void onFailToDiscoverService(BluetoothDevice deviceAddress) {

    }
}
