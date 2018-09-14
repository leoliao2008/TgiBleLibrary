package tgi.com.tgifreertobtdemo.activity;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

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
    private ArrayList<BluetoothGattService> mServicesList =new ArrayList<>();
    private ServicesListAdapter mAdapter;
    private ExpandableListView mExplvServicesList;
    private String mDevAddress;

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

        mTvDevName=findViewById(R.id.activity_service_list_tv_device_name);
        mTvDevAddress=findViewById(R.id.activity_service_list_tv_device_address);
        mTvData=findViewById(R.id.activity_service_list_tv_char_data);
        mExplvServicesList=findViewById(R.id.activity_service_list_explv_service_list);

        mDevAddress = getIntent().getStringExtra(DEVICE_ADDRESS);
        mTvDevName.setText(getIntent().getStringExtra(DEVICE_NAME));
        mTvDevAddress.setText(mDevAddress);

        initBtManager();
        initServicesList();

        mBleClientManager.connectToDevice(
                this,
                mDevAddress
        );

    }

    private void initServicesList() {
        mAdapter=new ServicesListAdapter(this, mServicesList);
        mExplvServicesList.setAdapter(mAdapter);
    }

    private void initBtManager() {
        mBleClientManager=new BleClientManager(
                this,
                new BleClientEventHandler(){
                    @Override
                    public void onServiceListUpdate(List<BluetoothGattService> services) {
                        super.onServiceListUpdate(services);

                    }

                    @Override
                    public void onCharacteristicRead(BluetoothGattCharacteristic characteristic) {
                        super.onCharacteristicRead(characteristic);
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
    public void updateExpListView(BluetoothDevice device) {

    }

    @Override
    public void updateData(byte[] data) {

    }

    @Override
    public void showToast(String msg) {
        Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();
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
}
