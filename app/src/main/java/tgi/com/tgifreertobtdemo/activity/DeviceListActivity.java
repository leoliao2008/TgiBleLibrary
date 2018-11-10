package tgi.com.tgifreertobtdemo.activity;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import tgi.com.bluetooth.callbacks.BleClientEventHandler;
import tgi.com.bluetooth.manager.BleClientManager;
import tgi.com.tgifreertobtdemo.R;

public class DeviceListActivity extends AppCompatActivity {
    private ListView mListView;
    private ArrayList<BluetoothDevice> mDevices = new ArrayList<>();
    private ArrayAdapter<BluetoothDevice> mAdapter;
    private BleClientManager mManager;

    public static void start(Context context) {
        Intent starter = new Intent(context, DeviceListActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);
        initListView();
        initBleManager();
    }

    private void initBleManager() {
        mManager = BleClientManager.getInstance();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mManager.onResume(this, new BleClientEventHandler() {
            @Override
            public void onLocationPermissionNotGranted() {
                super.onLocationPermissionNotGranted();
                mManager.requestLocationPermission(DeviceListActivity.this);
            }

            @Override
            public void onBtNotSupported() {
                super.onBtNotSupported();
                finish();
            }

            @Override
            public void onBleNotSupported() {
                super.onBleNotSupported();
                finish();
            }

            @Override
            public void onBtNotEnabled() {
                super.onBtNotEnabled();
                mManager.turnOnBt(DeviceListActivity.this);
            }

            @Override
            public void onUserRefusesToEnableBt() {
                super.onUserRefusesToEnableBt();
                finish();
            }

            @Override
            public void onUserEnableBt() {
                super.onUserEnableBt();
            }

            @Override
            public void onStartScanningDevice() {
                super.onStartScanningDevice();
                ProgressDialog.show(
                        DeviceListActivity.this,
                        "Scanning...",
                        "Scanning for devices, please wait...",
                        true,
                        new Runnable() {
                            @Override
                            public void run() {
                                mManager.stopScanningDevices(DeviceListActivity.this);
                            }
                        });
            }

            @Override
            public void onStopScanningDevice() {
                super.onStopScanningDevice();
                ProgressDialog.dismiss();
            }

            @Override
            public void onDeviceScanned(BluetoothDevice device, int rssi, byte[] scanRecord) {
                super.onDeviceScanned(device, rssi, scanRecord);
                updateList(device);
            }
        });
    }

    private void updateList(BluetoothDevice device) {
        boolean isAlreadyContained=false;
        for(BluetoothDevice temp:mDevices){
            if(temp.getAddress().equals(device.getAddress())){
                isAlreadyContained=true;
                break;
            }
        }
        if(!isAlreadyContained){
            showLog("new device:"+device.getAddress());
            mDevices.add(device);
            mAdapter.notifyDataSetChanged();
        }

    }

    private void showLog(String msg) {
        Log.e(getClass().getSimpleName(), msg);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mManager.onStop(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!mManager.onActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(!mManager.onRequestPermissionsResult(this, requestCode , permissions ,grantResults )){
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

    }

    private void initListView() {
        mListView = findViewById(R.id.activity_devices_list_list_view);
        mAdapter = new ArrayAdapter<BluetoothDevice>(
                this,
                android.R.layout.simple_list_item_1,
                mDevices
        ) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                BluetoothDevice device = mDevices.get(position);
                String name = device.getName();
                if (TextUtils.isEmpty(name)) {
                    name = "Unknown Device";
                }
                String address = device.getAddress();
                StringBuilder sb = new StringBuilder();
                sb.append(name)
                        .append("\r\n")
                        .append(address);
                view.setText(sb.toString());
                return view;
            }
        };
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                BluetoothDevice device = mDevices.get(i);

            }
        });
    }

    public void scanDevices(View view) {
        mManager.startScanningDevices(this);
    }
}
