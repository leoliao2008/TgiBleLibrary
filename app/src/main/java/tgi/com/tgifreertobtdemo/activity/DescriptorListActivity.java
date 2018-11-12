package tgi.com.tgifreertobtdemo.activity;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
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
import java.util.Locale;

import tgi.com.bluetooth.BtLibConstants;
import tgi.com.bluetooth.bean.TgiBtGattChar;
import tgi.com.bluetooth.bean.TgiBtGattDescriptor;
import tgi.com.bluetooth.callbacks.BleClientEventHandler;
import tgi.com.bluetooth.manager.BleClientManager;
import tgi.com.tgifreertobtdemo.R;

public class DescriptorListActivity extends AppCompatActivity {

    private BluetoothDevice mDevice;
    private String mServiceUUID;
    private String mCharUUID;
    private ArrayList<TgiBtGattDescriptor> mDescriptors;
    private BleClientManager mManager;
    private BleClientEventHandler mHandler=new BleClientEventHandler(){
        @Override
        public void onCharRead(String uuid, final byte[] value) {
            super.onCharRead(uuid, value);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ProgressDialog.dismiss();
                    initInfo(value);
                }
            });
        }

        @Override
        public void onFailToReadChar(String serviceUUID, String charUUID) {
            super.onFailToReadChar(serviceUUID, charUUID);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ProgressDialog.dismiss();
                    initInfo(null);
                }
            });
        }
    };
    private TextView mTvInfo;
    private ListView mListView;

    public static void start(
            Context context,
            BluetoothDevice device,
            String serviceUUID,
            TgiBtGattChar btChar) {
        Intent starter = new Intent(context, DescriptorListActivity.class);
        starter.putExtra(BtLibConstants.KEY_BT_DEVICE,device);
        starter.putExtra(BtLibConstants.KEY_BLE_SERVICE_UUID,serviceUUID);
        starter.putExtra(BtLibConstants.KEY_BLE_CHAR_UUID,btChar.getUuid());
        starter.putParcelableArrayListExtra(BtLibConstants.KEY_BLE_DESCRIPTORS,new ArrayList<Parcelable>(btChar.getDescriptors()));
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_descriptor_list);
        getSupportActionBar().setTitle("Descriptor List");
        mTvInfo = findViewById(R.id.activity_descriptor_list_tv_info);
        Intent intent = getIntent();
        mDevice = intent.getParcelableExtra(BtLibConstants.KEY_BT_DEVICE);
        mServiceUUID = intent.getStringExtra(BtLibConstants.KEY_BLE_SERVICE_UUID);
        mCharUUID = intent.getStringExtra(BtLibConstants.KEY_BLE_CHAR_UUID);
        mDescriptors=intent.getParcelableArrayListExtra(BtLibConstants.KEY_BLE_DESCRIPTORS);
        initInfo(null);

        initListView();

        mManager=BleClientManager.getInstance();

    }

    private void initListView() {
        mListView=findViewById(R.id.activity_descriptor_list_list_view);
        ArrayAdapter<TgiBtGattDescriptor> adapter=new ArrayAdapter<TgiBtGattDescriptor>(
                this,
                android.R.layout.simple_list_item_1,
                mDescriptors
        ){
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                TextView textView= (TextView) super.getView(position, convertView, parent);
                TgiBtGattDescriptor descriptor = mDescriptors.get(position);
                String uuid = descriptor.getUuid();
                textView.setText("Descriptor UUID: "+uuid);
                return textView;
            }
        };
        mListView.setAdapter(adapter);

    }

    private void initInfo(@Nullable byte[] charValue) {
        StringBuilder sb=new StringBuilder();
        sb.append("Device: \r\n")
                .append(TextUtils.isEmpty(mDevice.getName())?"Unknown Device":mDevice.getName())
                .append("\r\n")
                .append("Device Address:\r\n")
                .append(mDevice.getAddress())
                .append("\r\n")
                .append("Current Char:\r\n")
                .append(mCharUUID)
                .append("\r\n")
                .append("Char value:\r\n");
        if(charValue==null){
            sb.append("null");
        }else {
            for(byte temp:charValue){
                sb.append(String.format(Locale.US,"%#2x",temp)).append(" ");
            }
        }
        mTvInfo.setText(sb.toString());
    }

    @Override
    protected void onResume() {
        super.onResume();
        mManager.onResume(this,mHandler);
        mManager.readBtChar(this,mServiceUUID,mCharUUID);
        ProgressDialog.show(this,
                "Reading...",
                "Reading char value, please wait.",
                true,
                null);
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
}
