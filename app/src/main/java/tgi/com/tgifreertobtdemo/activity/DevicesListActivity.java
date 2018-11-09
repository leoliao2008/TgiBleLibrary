package tgi.com.tgifreertobtdemo.activity;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;

import tgi.com.bluetooth.bean.BleDevice;
import tgi.com.bluetooth.callbacks.BleClientEventHandler;
import tgi.com.bluetooth.manager.BleClientManager;
import tgi.com.tgifreertobtdemo.R;
import tgi.com.tgifreertobtdemo.adapters.DevicesListAdapter;
import tgi.com.tgifreertobtdemo.iViews.ShowDevicesListView;

public class DevicesListActivity extends AppCompatActivity implements ShowDevicesListView{
    private ListView mLvDevicesList;
    private ToggleButton mTgbtnScan;
    private DevicesListAdapter mAdapter;
    private ArrayList<BleDevice> mDevices=new ArrayList<>();
    private ProgressDialog mProgressDialog;
    private BleClientManager mBleClientManager;
    private BleClientEventHandler mBleClientEventHandler;

    public static void start(Context context) {
        Intent starter = new Intent(context, DevicesListActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices_list);
        mLvDevicesList=findViewById(R.id.activity_devices_list_lv);
        mTgbtnScan=findViewById(R.id.activity_devices_list_tgbtn_scan);

        initBtManager();
        initListView();
        initListeners();

    }

    private void initBtManager() {
        mBleClientManager=BleClientManager.getInstance();
        mBleClientEventHandler = new BleClientEventHandler(){
            @Override
            public void onStartScanningDevice() {
                super.onStartScanningDevice();
                showProgressDialogScanning();
            }

            @Override
            public void onDeviceScanned(BluetoothDevice device, int rssi, byte[] scanRecord) {
                super.onDeviceScanned(device, rssi, scanRecord);
                BleDevice bleDevice=new BleDevice();
                String name = device.getName();
                if(TextUtils.isEmpty(name)){
                    name="Unknown Device";
                }
                String address = device.getAddress();
                bleDevice.setName(name);
                bleDevice.setAddress(address);
                updateDeviceList(bleDevice);
            }

            @Override
            public void onStopScanningDevice() {
                super.onStopScanningDevice();
                mTgbtnScan.setChecked(false);
                dismissProgressDialogScanning();
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
            public void onUserRefusesToEnableBt() {
                super.onUserRefusesToEnableBt();
                finish();
            }

            @Override
            public void onError(String msg) {
                super.onError(msg);
                showToast(msg);
            }
        };
    }

    private void initListeners() {
        mLvDevicesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BleDevice device = mDevices.get(position);
                String name = device.getName();
                if(TextUtils.isEmpty(name)){
                    name="Unknown Device";
                }
                toConnectDeviceActivity(name,device.getAddress());
            }
        });

        mTgbtnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mTgbtnScan.isChecked()){
                    mBleClientManager.startScanningDevices(DevicesListActivity.this);
                }else {
                    mBleClientManager.stopScanningDevices(DevicesListActivity.this);
                }
            }
        });
    }

    private void initListView() {
        mAdapter=new DevicesListAdapter(mDevices,this);
        mLvDevicesList.setAdapter(mAdapter);
    }

    @Override
    public void showProgressDialogScanning() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProgressDialog = ProgressDialog.show(
                        DevicesListActivity.this,
                        null,
                        "Scanning for Bt devices...",
                        true,
                        true);
                mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        mBleClientManager.stopScanningDevices(DevicesListActivity.this);
                    }
                });
            }
        });

    }

    @Override
    public void dismissProgressDialogScanning() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(mProgressDialog!=null&&mProgressDialog.isShowing()){
                    mProgressDialog.dismiss();
                }
            }
        });
    }

    @Override
    public void updateDeviceList(final BleDevice device) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for(BleDevice dv:mDevices){
                    if(dv.getAddress().equals(device.getAddress())){
                        return;
                    }
                }
                mDevices.add(device);
                mAdapter.notifyDataSetChanged();
            }
        });

    }

    @Override
    public void toConnectDeviceActivity(String deviceName, String deviceAddress) {
        ServiceListActivity.start(this,deviceName,deviceAddress);
    }

    @Override
    public void showToast(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(DevicesListActivity.this,msg,Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(!mBleClientManager.onActivityResult(requestCode,resultCode,data)){
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onDestroy() {
        dismissProgressDialogScanning();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBleClientManager.onResume(this,mBleClientEventHandler);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mBleClientManager.onStop(this);
    }
}
