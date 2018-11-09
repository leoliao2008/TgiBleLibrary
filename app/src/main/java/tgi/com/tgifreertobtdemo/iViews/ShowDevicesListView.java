package tgi.com.tgifreertobtdemo.iViews;

import android.bluetooth.BluetoothDevice;

import tgi.com.bluetooth.bean.BleDevice;

public interface ShowDevicesListView {
    void showProgressDialogScanning();
    void dismissProgressDialogScanning();
    void updateDeviceList(BleDevice device);
    void toConnectDeviceActivity(String deviceName,String deviceAddress);
    void showToast(String msg);
}
