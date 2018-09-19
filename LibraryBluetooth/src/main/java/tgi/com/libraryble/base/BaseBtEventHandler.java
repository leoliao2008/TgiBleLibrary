package tgi.com.libraryble.base;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import java.util.List;

import tgi.com.libraryble.bean.BleDevice;

public class BaseBtEventHandler {

    public void onUserRefusesToEnableBt() {

    }

    public void onDeviceScanned(BluetoothDevice device, int rssi, byte[] scanRecord) {

    }

    public void onDeviceScanned(String deviceName,String deviceAddress) {

    }


    public void onError(String msg) {

    }

    public void onBtNotSupported() {

    }

    public void onStartScanningDevice() {

    }

    public void onStopScanningDevice() {

    }

    public void onDeviceConnected() {

    }

    public void onDeviceDisconnected() {

    }
}
