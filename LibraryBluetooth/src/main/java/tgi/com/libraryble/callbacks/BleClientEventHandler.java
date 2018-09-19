package tgi.com.libraryble.callbacks;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import java.util.List;

import tgi.com.libraryble.base.BaseBtEventHandler;

public class BleClientEventHandler extends BaseBtEventHandler {


    public void onBleNotSupported() {

    }

    public void onServiceListUpdate(List<BluetoothGattService> services) {

    }

    public void onCharacteristicRead(BluetoothGattCharacteristic characteristic) {

    }


    public void onReceiveNotification(BluetoothGattCharacteristic characteristic) {

    }


}
