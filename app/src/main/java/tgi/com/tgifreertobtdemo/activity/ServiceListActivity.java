package tgi.com.tgifreertobtdemo.activity;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import tgi.com.libraryble.callbacks.BleClientEventHandler;
import tgi.com.libraryble.manager.BleClientManager;
import tgi.com.tgifreertobtdemo.R;
import tgi.com.tgifreertobtdemo.adapters.ServicesListAdapter;
import tgi.com.tgifreertobtdemo.iViews.ShowServicesListView;

public class ServiceListActivity extends AppCompatActivity implements ShowServicesListView {

    private static final String DEVICE_NAME = "DEVICE_NAME";
    private static final String DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private ProgressDialog mProgressDialog;
    private TextView mTvDevName;
    private TextView mTvDevAddress;
    private TextView mTvData;
    private BleClientManager mBleClientManager;
    private ArrayList<BluetoothGattService> mServicesList = new ArrayList<>();
    private ServicesListAdapter mAdapter;
    private ExpandableListView mExplvServicesList;
    private String mDevAddress;
    private ToggleButton mTgbtnDataFormat;
    private byte[] mCharData;

    public static void start(Context context, String deviceName, String deviceAddress) {
        Intent starter = new Intent(context, ServiceListActivity.class);
        starter.putExtra(DEVICE_NAME, deviceName);
        starter.putExtra(DEVICE_ADDRESS, deviceAddress);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_list);

        mTvDevName = findViewById(R.id.activity_service_list_tv_device_name);
        mTvDevAddress = findViewById(R.id.activity_service_list_tv_device_address);
        mTvData = findViewById(R.id.activity_service_list_tv_char_data);
        mExplvServicesList = findViewById(R.id.activity_service_list_explv_service_list);
        mTgbtnDataFormat =findViewById(R.id.activity_service_list_tgbtn_data_format);


        mDevAddress = getIntent().getStringExtra(DEVICE_ADDRESS);
        mTvDevName.setText("Device Name: "+getIntent().getStringExtra(DEVICE_NAME));
        mTvDevAddress.setText("Device Address: "+mDevAddress);

        initBtManager();
        initServicesList();
        initListeners();

        mBleClientManager.connectToDevice(
                this,
                mDevAddress
        );

    }

    private void initListeners() {
        mExplvServicesList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                BluetoothGattCharacteristic characteristic = mServicesList.get(groupPosition).getCharacteristics().get(childPosition);
                boolean b = mBleClientManager.readCharacteristic(characteristic);
                if(b){
                    Log.e("readCharacteristic", "success");
                }else {
                    showToast("This Characteristic is not readable.");
                }
                return true;
            }
        });

        mTgbtnDataFormat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(mCharData==null){
                    return;
                }
                updateData(mCharData);
            }
        });
    }

    private void initServicesList() {
        mAdapter = new ServicesListAdapter(this, mServicesList);
        mExplvServicesList.setAdapter(mAdapter);
    }

    private void initBtManager() {
        mBleClientManager = new BleClientManager(
                this,
                new BleClientEventHandler() {
                    @Override
                    public void onServiceListUpdate(List<BluetoothGattService> services) {
                        super.onServiceListUpdate(services);
                        updateExpListView(services);
                    }

                    @Override
                    public void onCharacteristicRead(BluetoothGattCharacteristic characteristic) {
                        super.onCharacteristicRead(characteristic);
                        byte[] value = characteristic.getValue();
                        if(value!=null){
                            updateData(value);
                        }else {
                            showToast("no data is available.");
                        }

                    }

                    @Override
                    public void onError(String msg) {
                        super.onError(msg);
                        showToast(msg);
                    }
                }
        );
    }

    @Override
    public void updateExpListView(final List<BluetoothGattService> services) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mExplvServicesList.setAdapter((ExpandableListAdapter) null);
                mServicesList.clear();
                mServicesList.addAll(services);
                mAdapter=new ServicesListAdapter(ServiceListActivity.this,mServicesList);
                mExplvServicesList.setAdapter(mAdapter);
            }
        });
    }

    @Override
    public void updateData(final byte[] data) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mCharData=data;
                String text;
                if(mTgbtnDataFormat.isChecked()){
                    text=toHexString(data);
                }else {
                    text=toAscString(data);
                }
                mTvData.setText("Data: "+text);
            }
        });

    }

    @Override
    public void showToast(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ServiceListActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void showProgressDialog() {
        mProgressDialog = ProgressDialog.show(
                this,
                null,
                null,
                true,
                false);

    }

    @Override
    public void dismissProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        mBleClientManager.onDestroy();
        super.onDestroy();
    }

    private void showLog(String msg){
        Log.e("ServiceListActivity",msg);
    }

    private String toHexString(byte[] data){
        StringBuilder sb=new StringBuilder();
        for(byte b:data){
            sb.append("0x").append(String.format(Locale.CHINA,"%02x",b)).append(" ");
        }
        return sb.toString();
    }

    private String toAscString(byte[] data){
        return new String(data);
    }
}
