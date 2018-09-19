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

import tgi.com.libraryble.callbacks.BleClientEventHandler;
import tgi.com.libraryble.manager.BleClientManagerBeta;
import tgi.com.tgifreertobtdemo.R;
import tgi.com.tgifreertobtdemo.adapters.DevicesListAdapter;
import tgi.com.tgifreertobtdemo.iViews.ShowDevicesListView;

public class DevicesListActivity extends AppCompatActivity implements ShowDevicesListView{
    private ListView mLvDevicesList;
    private ToggleButton mTgbtnScan;
    private DevicesListAdapter mAdapter;
    private ArrayList<BluetoothDevice> mDevices=new ArrayList<>();
    private ProgressDialog mProgressDialog;
    private BleClientManagerBeta mBleClientManagerBeta;

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
        mBleClientManagerBeta =new BleClientManagerBeta(
                this,
                new BleClientEventHandler(){
                    @Override
                    public void onError(String msg) {
                        super.onError(msg);
                        showToast(msg);
                        finish();
                    }

                    @Override
                    public void onUserRefusesToEnableBt() {
                        super.onUserRefusesToEnableBt();
                        showToast("Cannot run without bt enabled.");
                        finish();
                    }

                    @Override
                    public void onBtNotSupported() {
                        super.onBtNotSupported();
                        showToast("Bt is not supported in this devices.");
                        finish();
                    }

                    @Override
                    public void onBleNotSupported() {
                        super.onBleNotSupported();
                        showToast("Ble is not supported in this devices.");
                        finish();
                    }

                    @Override
                    public void onStartScanningDevice() {
                        super.onStartScanningDevice();
                        mTgbtnScan.setChecked(true);
                        showProgressDialogScanning();
                    }

                    @Override
                    public void onDeviceScanned(BluetoothDevice device, int rssi, byte[] scanRecord) {
                        super.onDeviceScanned(device, rssi, scanRecord);
                        updateDeviceList(device);
                    }

                    @Override
                    public void onStopScanningDevice() {
                        super.onStopScanningDevice();
                        mTgbtnScan.setChecked(false);
                        dismissProgressDialogScanning();
                        showToast("Scan Stops.");
                    }
                }
        );
    }

    private void initListeners() {
        mLvDevicesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BluetoothDevice device = mDevices.get(position);
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
                    mBleClientManagerBeta.startScanningDevice();
                }else {
                    mBleClientManagerBeta.stopScanningDevice();
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
                        mBleClientManagerBeta.stopScanningDevice();
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
    public void updateDeviceList(final BluetoothDevice device) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for(BluetoothDevice dv:mDevices){
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
        mBleClientManagerBeta.onActivityResult(requestCode,resultCode,data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        mBleClientManagerBeta.disconnectDeviceIfAny();
        dismissProgressDialogScanning();
        super.onDestroy();
    }
}
