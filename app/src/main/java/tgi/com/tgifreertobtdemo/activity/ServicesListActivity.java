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
import android.widget.Toast;

import java.util.ArrayList;

import tgi.com.bluetooth.BtLibConstants;
import tgi.com.bluetooth.bean.TgiBtGattService;
import tgi.com.bluetooth.callbacks.BleClientEventHandler;
import tgi.com.bluetooth.manager.BleClientManager;
import tgi.com.tgifreertobtdemo.R;

public class ServicesListActivity extends AppCompatActivity {

    private BluetoothDevice mDevice;
    private BleClientManager mManager;
    private ArrayList<TgiBtGattService> mServices=new ArrayList<>();
    private ListView mListView;
    private BleClientEventHandler mHandler=new BleClientEventHandler(){
        @Override
        public void onDeviceConnected(BluetoothDevice device) {
            super.onDeviceConnected(device);
            ProgressDialog.show(
                    ServicesListActivity.this,
                    "Discovering",
                    "Discovering services, please wait...",
                    true,
                    null
            );
            mManager.scanServices(ServicesListActivity.this);
        }

        @Override
        public void onServiceDiscover(BluetoothDevice device, final ArrayList<TgiBtGattService> services) {
            super.onServiceDiscover(device, services);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ProgressDialog.dismiss();
                    mServices.clear();
                    mServices.addAll(services);
                    mAdapter.notifyDataSetChanged();
                }
            });
        }

        @Override
        public void onFailToDiscoverService(BluetoothDevice device) {
            super.onFailToDiscoverService(device);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ProgressDialog.dismiss();
                    Toast.makeText(ServicesListActivity.this,"Fail To Discover Service",Toast.LENGTH_SHORT).show();
                }
            });
        }
    };
    private ArrayAdapter mAdapter;
    private TextView mTvDeviceInfo;

    public static void start(Context context, BluetoothDevice device) {
        Intent starter = new Intent(context, ServicesListActivity.class);
        starter.putExtra(BtLibConstants.KEY_BT_DEVICE,device);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_services_list);
        getSupportActionBar().setTitle("Services List");
        initListView();
        mManager=BleClientManager.getInstance();
        mDevice = getIntent().getParcelableExtra(BtLibConstants.KEY_BT_DEVICE);
        String name = mDevice.getName();
        String address = mDevice.getAddress();
        if(TextUtils.isEmpty(name)){
            name="Unknown Device";
        }
        mTvDeviceInfo=findViewById(R.id.activity_services_list_tv_device_info);
        mTvDeviceInfo.setText(
                new StringBuffer()
                        .append("Device Name: ")
                        .append(name)
                        .append("\r\n")
                        .append("Address: ")
                        .append(address).toString()
        );
        mManager.connectDevice(this,mDevice);
        ProgressDialog.show(
                this,
                "Connecting",
                "Connecting devices, please wait...",
                true,
                null
        );
    }

    private void initListView() {
        mListView=findViewById(R.id.activity_services_list_list_view);
        mAdapter = new ArrayAdapter<TgiBtGattService>(
                this,
                android.R.layout.simple_list_item_1,
                mServices
        ){
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                TextView tv= (TextView) super.getView(position, convertView, parent);
                TgiBtGattService service = mServices.get(position);
                tv.setText(service.getUuid().toString());
                return tv;
            }
        };
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TgiBtGattService service = mServices.get(position);
                CharsListActivity.start(
                        ServicesListActivity.this,
                        mDevice,
                        service);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mManager.onResume(this,mHandler);
    }


    @Override
    protected void onPause() {
        super.onPause();
        mManager.onPause(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ProgressDialog.dismiss();
    }

    private void showLog(String msg){
        Log.e(getClass().getSimpleName(),msg);
    }
}
