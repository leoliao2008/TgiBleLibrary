package tgi.com.libraryble.base;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import java.util.List;

public class BaseBtEventHandler {

    public void onUserRefusesToEnableBt() {

    }

    public void onDeviceScanned(BluetoothDevice device, int rssi, byte[] scanRecord) {

    }


    public void onError(String msg) {

    }

    public void onBtNotSupported() {

    }

    public void onStartScanningDevice() {

    }

    public void onStopScanningDevice() {

    }
}
