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


    public void onCharWritten(String serviceUUID, String charUUID, byte[] writtenValue) {

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

    public void onFailToWriteChar(String serviceUUID, String charUUID, byte[] writeContent) {

    }

    public void onNotificationRegisterSuccess(String serviceUUID, String charUUID, String descUUID) {

    }

    public void onNotificationRegisterFails(String serviceUUID, String charUUID, String descUUID) {

    }

    public void onUnregisterNotificationSuccess(String serviceUUID, String charUUID, String descUUID) {

    }

    public void onUnregisterNotificationFails(String serviceUUID, String charUUID, String descUUID) {

    }

    public void onReceiveNotification(String serviceUUID, String charUUID, byte[] value) {

    }

    public void onReadDescriptorFails(String serviceUUID, String charUUID, String descUUID) {

    }

    public void onDescriptorRead(String serviceUUID, String charUUID, String descUUID, byte[] value) {

    }
}
