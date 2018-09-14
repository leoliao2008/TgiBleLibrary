package tgi.com.tgifreertobtdemo.adapters;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import tgi.com.tgifreertobtdemo.R;

public class ServicesListAdapter implements ExpandableListAdapter {
    private Context mContext;
    private ArrayList<BluetoothGattService> mList;

    public ServicesListAdapter(Context context, ArrayList<BluetoothGattService> list) {
        mContext = context;
        mList = list;
    }


    @Override
    public void registerDataSetObserver(DataSetObserver observer) {

    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {

    }

    @Override
    public int getGroupCount() {
        return mList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return mList.get(groupPosition).getCharacteristics().size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mList.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return mList.get(groupPosition).getCharacteristics().get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return groupPosition << 8 | childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        ParentViewHolder vh=null;
        if(convertView==null){
            convertView = LayoutInflater.from(mContext)
                    .inflate(R.layout.adapter_services_list_parent, parent, false);
            vh=new ParentViewHolder(convertView);
            convertView.setTag(vh);
        }else {
            vh= (ParentViewHolder) convertView.getTag();
        }
        BluetoothGattService service = mList.get(groupPosition);
        String name="Unknown Service";
        vh.mTvServiceName.setText(name);
        vh.mTvServiceUUID.setText(service.getUuid().toString());
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ChildViewHolder vh=null;
        if(convertView==null){
            convertView=LayoutInflater.from(mContext)
                    .inflate(R.layout.adapter_services_list_child,parent,false);
            vh=new ChildViewHolder(convertView);
            convertView.setTag(vh);
        }else {
            vh= (ChildViewHolder) convertView.getTag();
        }
        BluetoothGattCharacteristic characteristic = mList.get(groupPosition).getCharacteristics().get(childPosition);
        vh.mTvCharName.setText("Unknown Characteristic Name");
        vh.mTvCharUUID.setText(characteristic.getUuid().toString());
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public void onGroupExpanded(int groupPosition) {

    }

    @Override
    public void onGroupCollapsed(int groupPosition) {

    }

    @Override
    public long getCombinedChildId(long groupId, long childId) {
        return 0;
    }

    @Override
    public long getCombinedGroupId(long groupId) {
        return 0;
    }

    class ParentViewHolder {
        private TextView mTvServiceName;
        private TextView mTvServiceUUID;

        public ParentViewHolder(View convertView) {
            mTvServiceName = convertView.findViewById(R.id.adapter_services_list_parent_tv_service_name);
            mTvServiceUUID = convertView.findViewById(R.id.adapter_services_list_parent_tv_service_uuid);
        }
    }

    class ChildViewHolder {
        private TextView mTvCharName;
        private TextView mTvCharUUID;

        public ChildViewHolder(View convertView) {
            mTvCharName = convertView.findViewById(R.id.adapter_services_list_child_tv_char_name);
            mTvCharUUID = convertView.findViewById(R.id.adapter_services_list_child_tv_char_uuid);
        }
    }
}
