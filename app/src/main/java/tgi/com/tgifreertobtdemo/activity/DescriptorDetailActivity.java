package tgi.com.tgifreertobtdemo.activity;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;

import tgi.com.bluetooth.callbacks.BleClientEventCallback;
import tgi.com.bluetooth.manager.BleClientManager;
import tgi.com.tgifreertobtdemo.R;

import static tgi.com.bluetooth.BtLibConstants.KEY_BLE_CHAR_UUID;
import static tgi.com.bluetooth.BtLibConstants.KEY_BLE_DESC_UUID;
import static tgi.com.bluetooth.BtLibConstants.KEY_BLE_SERVICE_UUID;
import static tgi.com.bluetooth.BtLibConstants.KEY_BT_DEVICE;

public class DescriptorDetailActivity extends AppCompatActivity {
    private TextView mTvInfo;
    private ToggleButton mTgBtnNotification;
    private ListView mListView;
    private TextView mTvValue;
    private ArrayAdapter<String> mAdapter;
    private ArrayList<String> mValues=new ArrayList<>();
    private BluetoothDevice mDevice;
    private String mServiceUUID;
    private String mCharUUID;
    private String mDescUUID;
    private BleClientManager mManager;
    private BleClientEventCallback mEventHandler=new BleClientEventCallback(){
        @Override
        public void onDescriptorRead(String serviceUUID, String charUUID, String descUUID, final byte[] value) {
            super.onDescriptorRead(serviceUUID, charUUID, descUUID, value);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTvValue.setText(genHexString(value));
                }
            });
        }

        @Override
        public void onReadDescriptorFails(String serviceUUID, String charUUID, String descUUID) {
            super.onReadDescriptorFails(serviceUUID, charUUID, descUUID);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTvValue.setText("Read fails.");
                }
            });
        }

        @Override
        public void onNotificationRegisterSuccess(String serviceUUID, String charUUID, String descUUID) {
            super.onNotificationRegisterSuccess(serviceUUID, charUUID, descUUID);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showToast("NotificationRegisterSuccess");
                }
            });
        }

        @Override
        public void onNotificationRegisterFails(String serviceUUID, String charUUID, String descUUID) {
            super.onNotificationRegisterFails(serviceUUID, charUUID, descUUID);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTgBtnNotification.setChecked(false);
                    showToast("NotificationRegisterFails");
                }
            });
        }

        @Override
        public void onUnregisterNotificationSuccess(String serviceUUID, String charUUID, String descUUID) {
            super.onUnregisterNotificationSuccess(serviceUUID, charUUID, descUUID);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showToast("UnregisterNotificationSuccess");
                }
            });
        }

        @Override
        public void onUnregisterNotificationFails(String serviceUUID, String charUUID, String descUUID) {
            super.onUnregisterNotificationFails(serviceUUID, charUUID, descUUID);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTgBtnNotification.setChecked(true);
                    showToast("UnregisterNotificationFails");
                }
            });
        }

        @Override
        public void onNotificationReceived(String serviceUUID, String charUUID, final byte[] value) {
            super.onNotificationReceived(serviceUUID, charUUID, value);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateListView(value);
                }
            });
        }

    };

    public static void start(Context context,BluetoothDevice device,String serviceUUID,String charUUID,String descUUID) {
        Intent starter = new Intent(context, DescriptorDetailActivity.class);
        starter.putExtra(KEY_BT_DEVICE,device);
        starter.putExtra(KEY_BLE_SERVICE_UUID,serviceUUID);
        starter.putExtra(KEY_BLE_CHAR_UUID,charUUID);
        starter.putExtra(KEY_BLE_DESC_UUID,descUUID);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_descriptor_detail);
        mManager=BleClientManager.getInstance();
        mManager.setupEventCallback(mEventHandler);
        Intent intent = getIntent();
        mDevice=intent.getParcelableExtra(KEY_BT_DEVICE);
        mServiceUUID=intent.getStringExtra(KEY_BLE_SERVICE_UUID);
        mCharUUID=intent.getStringExtra(KEY_BLE_CHAR_UUID);
        mDescUUID=intent.getStringExtra(KEY_BLE_DESC_UUID);
        mTvInfo=findViewById(R.id.activity_descriptor_detail_tv_info);
        mTgBtnNotification=findViewById(R.id.activity_descriptor_detail_tgbtn_notification);
        mTvValue=findViewById(R.id.activity_descriptor_detail_tv_desc_value);
        mListView=findViewById(R.id.activity_descriptor_detail_list_view);

        getSupportActionBar().setTitle("Descriptor Detail");

        initInfo();
        initListView();
        initListeners();
    }

    private void initListeners() {

        mTgBtnNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mTgBtnNotification.isChecked()){
                    mManager.registerNotification(
                            DescriptorDetailActivity.this,
                            mServiceUUID,
                            mCharUUID,
                            mDescUUID
                    );
                }else {
                    mManager.unRegisterNotification(
                            DescriptorDetailActivity.this,
                            mServiceUUID,
                            mCharUUID,
                            mDescUUID
                    );
                }
            }
        });
    }

    private void initListView() {
        mAdapter=new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                mValues
        );
        mListView.setAdapter(mAdapter);
    }

    private void updateListView(final byte[] value){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String string = genHexString(value);
                mValues.add(string);
                mAdapter.notifyDataSetChanged();
                mListView.smoothScrollToPosition(Integer.MAX_VALUE);
            }
        });
    }

    private String genHexString(byte[] value) {
        StringBuilder sb=new StringBuilder();
        for(byte b:value){
            String string = Integer.toHexString(b);
            if(string.length()<0){
                sb.append("0x0");
            }else {
                sb.append("0x");
            }
            sb.append(string).append(" ");
        }
        return sb.toString();
    }

    private void initInfo() {
        StringBuilder sb=new StringBuilder();
        sb.append("Device: \r\n").append(mDevice.getName()).append("\r\n")
                .append("Service:\r\n").append(mServiceUUID).append("\r\n")
                .append("Char:\r\n").append(mCharUUID).append("\r\n")
                .append("Descriptor:\r\n").append(mDescUUID);
        mTvInfo.setText(sb.toString());
    }

    @Override
    protected void onResume() {
        super.onResume();
        mManager.registerReceiver(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mManager.unRegisterReceiver(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ProgressDialog.dismiss();
    }

    public void getDescriptorValue(View view) {
        mManager.readDescriptorValue(this,mServiceUUID,mCharUUID,mDescUUID);

    }

    private void showToast(String msg){
        Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();
    }
}
