package tgi.com.tgifreertobtdemo.iViews;

import android.bluetooth.BluetoothDevice;

public interface ShowServicesListView {
    void updateExpListView(BluetoothDevice device);
    void updateData(byte[] data);
    void showToast(String msg);
    void showProgressDialog();
    void dismissProgressDialog();
}
