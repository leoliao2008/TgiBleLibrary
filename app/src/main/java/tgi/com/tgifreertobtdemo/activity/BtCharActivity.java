package tgi.com.tgifreertobtdemo.activity;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.UUID;

import tgi.com.libraryble.callbacks.BleClientEventHandler;
import tgi.com.libraryble.manager.BleClientManagerBeta;
import tgi.com.tgifreertobtdemo.R;

public class BtCharActivity extends AppCompatActivity {

    private static final String BT_CHAR="BLUETOOTH_GATT_CHARACTERISTIC";
    private static final String BT_SERVICE="BLUETOOTH_GATT_SERVICE";
    private static final String BT_DESCRIPTOR="BLUETOOTH_GATT_DESCRIPTOR";
    private BleClientManagerBeta mBleClientManagerBeta;
    private String mBtCharUUID;
    private String mBtServiceUUID;
    private String mBtDescriptorUUID;
    private BluetoothGattCharacteristic mGattCharacteristic;
    private TextView mTvUUID;
    private Switch mSwtNotification;
    private ListView mLvConsole;
    private ArrayAdapter mAdapter;
    private ArrayList<String> mLogs=new ArrayList<>();



    public static void start(Context context, String serviceUUID,String btCharUUID,String btDescriptorUUID) {
        Intent starter = new Intent(context, BtCharActivity.class);
        starter.putExtra(BT_SERVICE,serviceUUID);
        starter.putExtra(BT_DESCRIPTOR,btDescriptorUUID);
        starter.putExtra(BT_CHAR,btCharUUID);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bt_char);
        mTvUUID=findViewById(R.id.activity_bt_char_tv_uuid);
        mSwtNotification=findViewById(R.id.activity_bt_char_swt_notification);
        mLvConsole=findViewById(R.id.activity_bt_char_lv_console);

        mBtCharUUID=getIntent().getStringExtra(BT_CHAR);
        mBtDescriptorUUID=getIntent().getStringExtra(BT_DESCRIPTOR);
        mBtServiceUUID=getIntent().getStringExtra(BT_SERVICE);

        mTvUUID.setText(mBtCharUUID);


        initBleManager();
        mGattCharacteristic= mBleClientManagerBeta.getBluetoothGattCharacteristic(mBtServiceUUID,mBtCharUUID);
        if(mGattCharacteristic==null){
            showToast("Characteristic not exists.");
            finish();
        }
        BluetoothGattDescriptor descriptor = mGattCharacteristic.getDescriptor(UUID.fromString(mBtDescriptorUUID));
        if(descriptor==null){
            showToast("BluetoothGattDescriptor not exists.");
            finish();
        }

        initConsole();
        initListeners();
    }

    private void initListeners() {
        mSwtNotification.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mBleClientManagerBeta.toggleNotification(
                        isChecked,
                        mBtServiceUUID,
                        mBtCharUUID,
                        mBtDescriptorUUID
                );
            }
        });
    }

    private void initConsole() {
        mAdapter=new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                mLogs
        );
        mLvConsole.setAdapter(mAdapter);
    }

    private void updateConsole(String log){
        mLogs.add(log);
        mAdapter.notifyDataSetChanged();
        mLvConsole.smoothScrollToPosition(Integer.MAX_VALUE);
    }

    private void initBleManager() {
        mBleClientManagerBeta =new BleClientManagerBeta(this,new BleClientEventHandler(){
            @Override
            public void onError(String msg) {
                super.onError(msg);
            }

            @Override
            public void onCharacteristicRead(BluetoothGattCharacteristic characteristic) {
                super.onCharacteristicRead(characteristic);
                byte[] value = characteristic.getValue();
                if(value!=null){
                    updateConsole(new String(value));
                }else {
                    updateConsole("Char value is null.");
                }
            }

            @Override
            public void onReceiveNotification(BluetoothGattCharacteristic characteristic) {
                super.onReceiveNotification(characteristic);
                byte[] value = characteristic.getValue();
                if(value!=null){
                    updateConsole("Echo: "+new String(value));
                }else {
                    updateConsole("Echo: Char value is null.");
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        mBleClientManagerBeta.disconnectDeviceIfAny();
        super.onDestroy();
    }

    private void showToast(String msg){
        Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();
    }

    public void writeData(View view) {
        mBleClientManagerBeta.writeCharacteristic(
                mGattCharacteristic,
                "hello world".getBytes()
        );
    }

    public void readData(View view) {
        mBleClientManagerBeta.readCharacteristic(mGattCharacteristic);
    }
}
