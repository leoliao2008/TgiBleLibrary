package tgi.com.tgifreertobtdemo.iViews;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;

import java.util.List;

public interface ShowServicesListView {
    void updateExpListView(List<BluetoothGattService> services);
    void updateData(byte[] data);
    void showToast(String msg);
    void showProgressDialog();
    void dismissProgressDialog();
}
