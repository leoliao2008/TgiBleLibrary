package tgi.com.tgifreertobtdemo.activity;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
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

import java.util.ArrayList;
import java.util.Locale;

import tgi.com.bluetooth.BtLibConstants;
import tgi.com.bluetooth.bean.TgiBtGattChar;
import tgi.com.bluetooth.bean.TgiBtGattService;
import tgi.com.tgifreertobtdemo.R;

public class CharsListActivity extends AppCompatActivity {
    private BluetoothDevice mDevice;
    private String mServiceUUID;
    private ArrayList<TgiBtGattChar> mChars;
    private TextView mTvInfo;
    private ListView mListView;

    public static void start(Context context,BluetoothDevice device,TgiBtGattService service) {
        Intent starter = new Intent(context, CharsListActivity.class);
        starter.putExtra(BtLibConstants.KEY_BT_DEVICE,device);
        starter.putExtra(BtLibConstants.KEY_BT_GATT_SERVICE_UUID,service.getUuid().toString());
        starter.putParcelableArrayListExtra(
                BtLibConstants.KEY_BT_SERVICE_CHARS_LIST,
                new ArrayList<Parcelable>(service.getChars()));
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chars_list);
        getSupportActionBar().setTitle("BtChar List");
        Intent intent = getIntent();
        mDevice=intent.getParcelableExtra(BtLibConstants.KEY_BT_DEVICE);
        mServiceUUID=intent.getStringExtra(BtLibConstants.KEY_BT_GATT_SERVICE_UUID);
        mChars=intent.getParcelableArrayListExtra(BtLibConstants.KEY_BT_SERVICE_CHARS_LIST);
        Log.e(getClass().getSimpleName(),mChars.toString());

        mTvInfo=findViewById(R.id.activity_chars_list_tv_info);
        mTvInfo.setText(
                new StringBuilder().append("Device Name: ")
                .append(TextUtils.isEmpty(mDevice.getName())?"Unknown Device":mDevice.getName())
                        .append("\r\n")
                        .append("Address: ")
                        .append(mDevice.getAddress())
                        .append("\r\n")
                        .append("Service: ")
                        .append(mServiceUUID)
                        .toString()
        );
        initListView();

    }

    private void initListView() {
        mListView=findViewById(R.id.activity_chars_list_list_view);
        ArrayAdapter<TgiBtGattChar> adapter=new ArrayAdapter<TgiBtGattChar>(
                this,
                android.R.layout.simple_list_item_1,
                mChars
        ){
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                TgiBtGattChar temp = mChars.get(position);
                String uuid = temp.getUuid().toString();
                StringBuilder sb=new StringBuilder();
                sb.append("Characteristic")
                        .append("\r\n")
                        .append("UUID: ")
                        .append(uuid);
                view.setText(sb.toString());
                return view;
            }
        };
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TgiBtGattChar btChar = mChars.get(position);
                DescriptorListActivity.start(
                        CharsListActivity.this,
                        mDevice,
                        mServiceUUID,
                        btChar
                );


            }
        });
    }
}
