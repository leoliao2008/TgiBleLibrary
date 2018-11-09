package tgi.com.tgifreertobtdemo.activity;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import tgi.com.bluetooth.callbacks.BleClientEventHandler;
import tgi.com.bluetooth.manager.BleClientManager;
import tgi.com.tgifreertobtdemo.R;

public class DeviceListActivity extends AppCompatActivity {
    private ListView mListView;
    private ArrayList<BluetoothDevice> mDevices=new ArrayList<>();
    private ArrayAdapter<BluetoothDevice> mAdapter;
    private BleClientManager mManager;

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
        mManager.onResume(this,new BleClientEventHandler(){
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
            }

            @Override
            public void onUserEnableBt() {
                super.onUserEnableBt();
            }

            @Override
            public void onStartScanningDevice() {
                super.onStartScanningDevice();
            }

            @Override
            public void onStopScanningDevice() {
                super.onStopScanningDevice();
            }

            @Override
            public void onDeviceScanned(BluetoothDevice device, int rssi, byte[] scanRecord) {
                super.onDeviceScanned(device, rssi, scanRecord);
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        mManager.onStop(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(!mManager.onActivityResult(requestCode,resultCode,data)){
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void initListView() {
        mListView=findViewById(R.id.activity_devices_list_list_view);
        mAdapter = new ArrayAdapter<BluetoothDevice>(
                this,
                android.R.layout.simple_list_item_1,
                mDevices
        ){
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                BluetoothDevice device = mDevices.get(position);
                String name = device.getName();
                if(TextUtils.isEmpty(name)){
                    name="Unknown Device";
                }
                String address = device.getAddress();
                StringBuilder sb=new StringBuilder();
                sb.append(name)
                        .append("\r\n")
                        .append(address);
                view.setText(sb.toString());
                return view;
            }
        };
    }

    public void scanDevices(View view) {
    }
}
