package tgi.com.tgifreertobtdemo.iViews;

import android.bluetooth.BluetoothDevice;

public interface ShowDevicesListView {
    void showProgressDialogScanning();
    void dismissProgressDialogScanning();
    void updateDeviceList(BluetoothDevice device);
    void toConnectDeviceActivity(String deviceName,String deviceAddress);
    void showToast(String msg);
}
