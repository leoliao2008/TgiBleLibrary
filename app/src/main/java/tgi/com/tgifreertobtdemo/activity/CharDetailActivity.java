package tgi.com.tgifreertobtdemo.activity;

import android.bluetooth.BluetoothDevice;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import tgi.com.bluetooth.BtLibConstants;
import tgi.com.bluetooth.bean.TgiBtGattChar;
import tgi.com.bluetooth.bean.TgiBtGattDescriptor;
import tgi.com.bluetooth.callbacks.BleClientEventCallback;
import tgi.com.bluetooth.manager.BleClientManager;
import tgi.com.tgifreertobtdemo.R;

public class CharDetailActivity extends AppCompatActivity {

    private BluetoothDevice mDevice;
    private String mServiceUUID;
    private String mCharUUID;
    private ArrayList<TgiBtGattDescriptor> mDescriptors;
    private BleClientManager mManager;
    private BleClientEventCallback mEventCallback =new BleClientEventCallback(){

        @Override
        public void onCharRead(String uuid, final byte[] value) {
            super.onCharRead(uuid, value);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ProgressDialog.dismiss();

                    if(value==null||value.length==0){
                        mTvCharValue.setText("Empty Value");
                    }else {
                        mTvCharValue.setText(genHexString(value));
                    }
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
                    mTvCharValue.setText("Fail to get value.");
                }
            });
        }

        @Override
        public void onCharWritten(String serviceUUID, String charUUID, final byte[] writtenValue) {
            super.onCharWritten(serviceUUID, charUUID, writtenValue);
            if(serviceUUID.equals(mServiceUUID)&&charUUID.equals(mCharUUID)){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTvWriteResult.setText(genHexString(writtenValue));
                    }
                });
            }
        }

        @Override
        public void onFailToWriteChar(String serviceUUID, String charUUID, byte[] writeContent) {
            super.onFailToWriteChar(serviceUUID, charUUID, writeContent);
            if(serviceUUID.equals(mServiceUUID)&&charUUID.equals(mCharUUID)){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ProgressDialog.dismiss();
                        mTvWriteResult.setText("Fail to write content...");
                    }
                });
            }
        }

        @Override
        public void onDeviceDisconnected(String name, String address) {
            super.onDeviceDisconnected(name, address);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ProgressDialog.dismiss();
                    Toast.makeText(CharDetailActivity.this,"Device disconnected.",Toast.LENGTH_SHORT).show();
                }
            });
        }
    };

    private String genHexString(byte[] bytes) {
        StringBuilder sb=new StringBuilder();
        for(byte b:bytes){
            String string = Integer.toHexString(b);
            if(string.length()<2){
                sb.append("0x0");
            }else {
                sb.append("0x");
            }
            sb.append(string).append(" ");
        }
        return sb.toString();
    }

    private TextView mTvInfo;
    private ListView mListView;
    private TextView mTvCharValue;
    private TextView mTvWriteResult;

    public static void start(
            Context context,
            BluetoothDevice device,
            String serviceUUID,
            TgiBtGattChar btChar) {
        Intent starter = new Intent(context, CharDetailActivity.class);
        starter.putExtra(BtLibConstants.KEY_BT_DEVICE,device);
        starter.putExtra(BtLibConstants.KEY_BLE_SERVICE_UUID,serviceUUID);
        starter.putExtra(BtLibConstants.KEY_BLE_CHAR_UUID,btChar.getUuid());
        starter.putParcelableArrayListExtra(BtLibConstants.KEY_BLE_DESCRIPTORS,new ArrayList<Parcelable>(btChar.getDescriptors()));
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_char_detail);
        getSupportActionBar().setTitle("Bt Char Detail");
        mTvInfo = findViewById(R.id.activity_char_detail_tv_info);
        mTvCharValue=findViewById(R.id.activity_char_detail_tv_char_value);
        mTvWriteResult=findViewById(R.id.activity_char_detail_tv_write_result);
        Intent intent = getIntent();
        mDevice = intent.getParcelableExtra(BtLibConstants.KEY_BT_DEVICE);
        mServiceUUID = intent.getStringExtra(BtLibConstants.KEY_BLE_SERVICE_UUID);
        mCharUUID = intent.getStringExtra(BtLibConstants.KEY_BLE_CHAR_UUID);
        mDescriptors=intent.getParcelableArrayListExtra(BtLibConstants.KEY_BLE_DESCRIPTORS);
        initInfo();

        initListView();

        mManager=BleClientManager.getInstance();
        mManager.setupEventCallback(mEventCallback);

    }

    private void initListView() {
        mListView=findViewById(R.id.activity_char_detail_list_view);
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
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DescriptorDetailActivity.start(
                        CharDetailActivity.this,
                        mDevice,
                        mServiceUUID,
                        mCharUUID,
                        mDescriptors.get(position).getUuid()
                );
            }
        });

    }

    private void initInfo() {
        StringBuilder sb=new StringBuilder();
        sb.append("Device: \r\n")
                .append(TextUtils.isEmpty(mDevice.getName())?"Unknown Device":mDevice.getName())
                .append("\r\n")
                .append("Device Address:\r\n")
                .append(mDevice.getAddress())
                .append("\r\n")
                .append("Service:\r\n")
                .append(mServiceUUID)
                .append("\r\n")
                .append("Current Char:\r\n")
                .append(mCharUUID);
        mTvInfo.setText(sb.toString());
    }

    @Override
    protected void onResume() {
        super.onResume();
        mManager.registerReceiver(this);
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
        mManager.unRegisterReceiver(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ProgressDialog.dismiss();
    }

    public void getCharValue(View view) {
        mManager.readBtChar(this,mServiceUUID,mCharUUID);
    }

    public void writeTestContent(View view) {
        byte[] bytes=new byte[]{0x01,0x02,0x03,0x04,0x05,0x06,0x07,0x08};
        mManager.writeBtChar(
                this,
                mServiceUUID,
                mCharUUID,
                bytes
        );
    }
}
