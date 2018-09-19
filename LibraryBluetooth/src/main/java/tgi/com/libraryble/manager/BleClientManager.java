package tgi.com.libraryble.manager;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.LongBinaryOperator;

import tgi.com.libraryble.LibraryBtConstants;
import tgi.com.libraryble.bean.BleDevice;
import tgi.com.libraryble.callbacks.BleClientEventHandler;
import tgi.com.libraryble.service.BleBackgroundService;

public class BleClientManager {
    private BleClientEventHandler mEventHandler;
    private BleClientManagerBroadcastReceiver mReceiver;

    public BleClientManager(Activity activity, BleClientEventHandler eventHandler) {
        mEventHandler = eventHandler;
        Intent intent = new Intent(activity, BleBackgroundService.class);
        activity.startService(intent);
    }

    public void onResume(Activity activity) {
        mReceiver = new BleClientManagerBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter(LibraryBtConstants.ACTION_BLE_ACTIVITY_UPDATE);
        activity.registerReceiver(mReceiver, intentFilter);
    }

    public void scannDevices(Activity activity) {
        Intent intent = new Intent(LibraryBtConstants.REQUEST_SCAN_DEVICE);
        activity.sendBroadcast(intent);
    }

    public void onStop(Activity activity) {
        if (mReceiver != null) {
            activity.unregisterReceiver(mReceiver);
            mReceiver = null;
        }
    }

    private class BleClientManagerBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String event = intent.getStringExtra(LibraryBtConstants.KEY_BLE_EVENT);
            switch (event) {
                case LibraryBtConstants.EVENT_SCAN_STARTS:
                    mEventHandler.onStartScanningDevice();
                    break;
                case LibraryBtConstants.EVENT_SCAN_STOPS:
                    mEventHandler.onStopScanningDevice();
                    break;
                case LibraryBtConstants.EVENT_DEVICE_SCAN: {
                    String name = intent.getStringExtra(LibraryBtConstants.KEY_BLE_DEVICE_NAME);
                    String address = intent.getStringExtra(LibraryBtConstants.KEY_BLE_DEVICE_ADDRESS);
                    mEventHandler.onDeviceScanned(name, address);
                }
                break;
                case LibraryBtConstants.EVENT_DEVICE_CONNECTED:
                    mEventHandler.onDeviceConnected();
                    break;
                case LibraryBtConstants.EVENT_DEVICE_DISCONNECT:
                    mEventHandler.onDeviceDisconnected();
                    break;
            }


        }
    }


}
