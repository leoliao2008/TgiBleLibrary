package tgi.com.tgifreertobtdemo.adapters;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import tgi.com.bluetooth.bean.BleDevice;
import tgi.com.tgifreertobtdemo.R;

public class DevicesListAdapter extends BaseAdapter {
    private ArrayList<BleDevice> mList;
    private Context mContext;

    public DevicesListAdapter(ArrayList<BleDevice> list, Context context) {
        mList = list;
        mContext = context;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder vh;
        if(convertView==null){
            convertView= LayoutInflater.from(mContext).inflate(R.layout.adatper_device_list,parent,false);
            vh=new ViewHolder(convertView);
            convertView.setTag(vh);
        }else {
            vh= (ViewHolder) convertView.getTag();
        }
        BleDevice device = mList.get(position);
        String name = device.getName();
        if(TextUtils.isEmpty(name)){
            name="Unknown Device";
        }
        vh.mTvName.setText(name);
        vh.mTvAddress.setText(device.getAddress());
        return convertView;
    }

    class ViewHolder{
        private TextView mTvName;
        private TextView mTvAddress;

        public ViewHolder(View contentView){
            mTvName=contentView.findViewById(R.id.adapter_device_list_name);
            mTvAddress =contentView.findViewById(R.id.adapter_device_list_address);
        }
    }
}
