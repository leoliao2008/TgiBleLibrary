package tgi.com.bluetooth.callbacks;

import java.util.ArrayList;

import tgi.com.bluetooth.bean.TgiBtGattService;

/**
 *
 */
public class BleClientEventCallback {


    public void onBleNotSupported() {

    }


    public void onCharRead(String uuid, byte[] value) {

    }


    public void onCharWritten(String serviceUUID, String charUUID, byte[] writtenValue) {

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

    public void onDeviceConnected(String deviceName, String deviceAddress, ArrayList<TgiBtGattService> services) {

    }

    public void onDeviceDisconnected(String name, String address) {

    }

    public void onBtNotEnabled() {

    }


    public void onLocationPermissionNotGranted() {

    }

    public void onFailToReadChar(String serviceUUID, String charUUID) {

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

    public void onNotificationReceived(String serviceUUID, String charUUID, byte[] value) {

    }

    public void onReadDescriptorFails(String serviceUUID, String charUUID, String descUUID) {

    }

    public void onDescriptorRead(String serviceUUID, String charUUID, String descUUID, byte[] value) {

    }

    public void onDeviceScanned(String name, String address, int rssi, byte[] scanRecord) {

    }

    public void onDeviceConnectFails(String deviceName, String deviceAddress) {

    }
}
