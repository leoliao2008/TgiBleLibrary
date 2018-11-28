package tgi.com.bluetooth.base;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;


public class BaseBtModel {
    public boolean isBtSupported(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH);
    }

    public BluetoothAdapter getBtAdapter(Context context) {
        BluetoothAdapter adapter = null;
        BluetoothManager manager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        try {
            adapter = manager.getAdapter();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return adapter;
    }

    public boolean isBtEnable(BluetoothAdapter adapter) {
        return adapter != null && adapter.isEnabled();
    }


    public BluetoothDevice getDeviceByAddress(BluetoothAdapter adapter, String address) {
        return adapter.getRemoteDevice(address);
    }
}
