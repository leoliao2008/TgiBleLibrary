package tgi.com.tgifreertobtdemo.activity;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;

import tgi.com.bluetooth.bean.TgiBtGattService;
import tgi.com.bluetooth.callbacks.BleClientEventCallback;
import tgi.com.bluetooth.manager.BleClientManager;
import tgi.com.tgifreertobtdemo.R;


public class MCSimulationActivity extends AppCompatActivity {
    private ArrayList<Pair<String, String>> mDevices = new ArrayList<>();
    private ArrayList<String> mLogs = new ArrayList<>();
    private ArrayAdapter<Pair<String, String>> mDevicesListAdapter;
    private ArrayAdapter<String> mLogsAdapter;
    private static final String MASTER_SERVICE_UUID = "0000180d-0000-1000-8000-00805f9b34fb";
    private static final String FUNCTION_CHAR_UUID = "00002a39-0000-1000-8000-00805f9b34fb";
    private static final String STATUS_CHAR_UUID = "00002a37-0000-1000-8000-00805f9b34fb";
    private static final String STATUS_DESCRIPTOR_UUID = "00002902-0000-1000-8000-00805f9b34fb";
    private static final String TEST_LINE="{\"time\":60000,\"message\":\"550FA10100010000000000000007AA\",\"timestamp\":\"04122018071159381\"}";
    private MCSimulationActivity mThis = this;
    private ListView mLsvDevicesList;


    private EditText mEdtInputCommand;
    private ListView mLsvLogs;
    private ToggleButton mTgBtnNotification;
    private Led mLed;
    private BleClientManager mManager;
    private BleClientEventCallback mEventCallback = new BleClientEventCallback() {
        @Override
        public void onStartScanningDevice() {
            super.onStartScanningDevice();
            ProgressDialog.show(
                    mThis,
                    "扫描",
                    "蓝牙设备扫描中...请稍候。",
                    true,
                    new Runnable() {
                        @Override
                        public void run() {
                            mManager.stopScanningDevices(mThis);
                        }
                    }
            );
            mDevices.clear();
            mDevicesListAdapter.notifyDataSetChanged();
        }

        @Override
        public void onDeviceScanned(String name, String address, int rssi, byte[] scanRecord) {
            super.onDeviceScanned(name, address, rssi, scanRecord);
            Pair<String, String> device = new Pair<>(name, address);
            if (!mDevices.contains(device)) {
                mDevices.add(device);
                mDevicesListAdapter.notifyDataSetChanged();
                mLsvDevicesList.smoothScrollToPosition(Integer.MAX_VALUE);
            }
        }

        @Override
        public void onStopScanningDevice() {
            super.onStopScanningDevice();
            ProgressDialog.dismiss();
        }

        @Override
        public void onDeviceConnected(String deviceName, String deviceAddress, ArrayList<TgiBtGattService> services) {
            super.onDeviceConnected(deviceName, deviceAddress, services);
            ProgressDialog.dismiss();
            updateLogs("连接设备成功");
        }

        @Override
        public void onDeviceConnectFails(String deviceName, String deviceAddress) {
            super.onDeviceConnectFails(deviceName, deviceAddress);
            ProgressDialog.dismiss();
            updateLogs("连接设备失败");
        }

        @Override
        public void onDeviceDisconnected(String name, String address) {
            super.onDeviceDisconnected(name, address);
            ProgressDialog.dismiss();
            updateLogs("连接断开");
        }

        @Override
        public void onCharWritten(String serviceUUID, String charUUID, byte[] writtenValue) {
            super.onCharWritten(serviceUUID, charUUID, writtenValue);
            ProgressDialog.dismiss();
            updateLogs("写入成功:" + new String(writtenValue));
        }

        @Override
        public void onFailToWriteChar(String serviceUUID, String charUUID, byte[] writeContent) {
            super.onFailToWriteChar(serviceUUID, charUUID, writeContent);
            ProgressDialog.dismiss();
            updateLogs("写入失败: " + new String(writeContent));
        }

        @Override
        public void onNotificationRegisterSuccess(String serviceUUID, String charUUID, String descUUID) {
            super.onNotificationRegisterSuccess(serviceUUID, charUUID, descUUID);
            ProgressDialog.dismiss();
            updateLogs("订阅通知成功");
        }

        @Override
        public void onNotificationRegisterFails(String serviceUUID, String charUUID, String descUUID) {
            super.onNotificationRegisterFails(serviceUUID, charUUID, descUUID);
            ProgressDialog.dismiss();
            updateLogs("订阅通知失败");
        }

        @Override
        public void onNotificationReceived(String serviceUUID, String charUUID, byte[] value) {
            super.onNotificationReceived(serviceUUID, charUUID, value);
            //每次更新就闪一下灯
            mLed.animateColorChanging();
        }

        @Override
        public void onError(String msg) {
            super.onError(msg);
            ProgressDialog.dismiss();
            updateLogs("Error: " + msg);
        }
    };


    public static void start(Context context) {
        Intent starter = new Intent(context, MCSimulationActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mcsimulation);

        initViews();
        initData();
        initDeviceList();
        initLogList();
        initBtManager();
        initListeners();

    }

    private void initData() {
        mEdtInputCommand.setText(TEST_LINE);
    }

    private void initListeners() {
        mTgBtnNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTgBtnNotification.isChecked()) {
                    mManager.registerNotification(
                            mThis,
                            MASTER_SERVICE_UUID,
                            STATUS_CHAR_UUID,
                            STATUS_DESCRIPTOR_UUID
                    );
                } else {
                    mManager.unRegisterNotification(
                            mThis,
                            MASTER_SERVICE_UUID,
                            STATUS_CHAR_UUID,
                            STATUS_DESCRIPTOR_UUID
                    );
                }

            }
        });
    }

    private void initBtManager() {
        mManager = BleClientManager.getInstance();
        mManager.setupEventCallback(mEventCallback);
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
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus){
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION|
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY|View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            );
        }
    }

    private void initViews() {
        mLsvDevicesList = findViewById(R.id.activity_mc_simulation_lst_view_devices_list);
        mLsvLogs = findViewById(R.id.activity_mc_simulation_lst_view_log);
        mTgBtnNotification = findViewById(R.id.activity_mc_simulation_tg_btn_notification);
        mEdtInputCommand=findViewById(R.id.activity_mc_simulation_edt_input_command);
        mLed = findViewById(R.id.activity_mc_simulation_led);
    }

    private void initLogList() {
        mLogsAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                mLogs) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                view.setText(mLogs.get(position));
                return view;
            }
        };
        mLsvLogs.setAdapter(mLogsAdapter);

    }


    private void initDeviceList() {
        mDevicesListAdapter = new ArrayAdapter<Pair<String, String>>(
                this,
                android.R.layout.simple_list_item_1,
                mDevices
        ) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                view.setText(new StringBuilder()
                        .append(mDevices.get(position).first)
                        .append("\r\n")
                        .append(mDevices.get(position).second)
                        .toString()
                );
                return view;
            }
        };
        mLsvDevicesList.setAdapter(mDevicesListAdapter);
        mLsvDevicesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Pair<String, String> device = mDevices.get(position);
                ProgressDialog.show(mThis, "连接", "连接蓝牙设备中，请稍候...", true, new Runnable() {
                    @Override
                    public void run() {
                        mManager.disconnectDevice(mThis);
                    }
                });
                mManager.connectDevice(mThis, device.second);
            }
        });

    }

    private void updateLogs(String log) {
        mLogs.add(log);
        if (mLogs.size() > 50) {
            mLogs.remove(0);
        }
        mLogsAdapter.notifyDataSetChanged();
        mLsvLogs.smoothScrollToPosition(Integer.MAX_VALUE);
    }

    private String toHexString(byte[] value) {
        StringBuilder sb = new StringBuilder();
        for (byte temp : value) {
            String hexString = Integer.toHexString(temp);
            sb.append("0x");
            if (hexString.length() < 2) {
                sb.append("0");
            }
            sb.append(hexString).append(" ");
        }
        return sb.toString();
    }

    public void sendStopCommand(View view) {
        //        String cmd="550FA10000000000000000010006AA";
        String cmd = "Stop MC Device. 5 4 3 2 1";
        mManager.writeBtChar(
                mThis,
                MASTER_SERVICE_UUID,
                FUNCTION_CHAR_UUID,
                cmd.getBytes()
        );

    }

    public void sendActivateCommand(View view) {
        //        String cmd="550FA10100010000000000000007AA";
        String cmd = "Start MC Device. 1 2 3 4 5";
        mManager.writeBtChar(
                mThis,
                MASTER_SERVICE_UUID,
                FUNCTION_CHAR_UUID,
                cmd.getBytes()
        );

    }

    public void scanDevices(View view) {
        mManager.startScanningDevices(mThis);
    }

    public void sendCommand(View view) {
        String s = mEdtInputCommand.getText().toString().trim();
        if(TextUtils.isEmpty(s)){
            return;
        }
        mManager.writeBtChar(
                mThis,
                MASTER_SERVICE_UUID,
                FUNCTION_CHAR_UUID,
                s.getBytes()
        );

    }

    public void resetCommand(View view) {
        mEdtInputCommand.setText(TEST_LINE);
    }
}
