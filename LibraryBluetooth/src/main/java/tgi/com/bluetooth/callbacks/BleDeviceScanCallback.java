package tgi.com.bluetooth.callbacks;

import android.bluetooth.BluetoothAdapter;

public interface BleDeviceScanCallback extends BluetoothAdapter.LeScanCallback {
    void onScanStart();
    void onScanStop();
}
