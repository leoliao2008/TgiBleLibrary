package tgi.com.bluetooth.bean;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TgiBtGattChar implements Parcelable {

    private String mUuid;
    private String mServiceUUID;
    private byte[] mValue;
    private int mInstanceId;
    private int mPermissions;
    private int mProperties;
    private int mWriteType;
    private ArrayList<TgiBtGattDescriptor> mDescriptors = new ArrayList<>();

    public TgiBtGattChar(BluetoothGattCharacteristic btChar) {
        mUuid = btChar.getUuid().toString();
        BluetoothGattService service = btChar.getService();
        mServiceUUID = service.getUuid().toString();
        mValue = btChar.getValue();
        if (mValue == null) {
            mValue = new byte[]{0};
        }
        mInstanceId = btChar.getInstanceId();
        mPermissions = btChar.getPermissions();
        mProperties = btChar.getProperties();
        mWriteType = btChar.getWriteType();
        List<BluetoothGattDescriptor> descriptors = btChar.getDescriptors();
        for (BluetoothGattDescriptor temp : descriptors) {
            mDescriptors.add(new TgiBtGattDescriptor(temp));
        }
    }


    protected TgiBtGattChar(Parcel in) {
        mUuid = in.readString();
        mServiceUUID = in.readString();
        mValue = in.createByteArray();
        mInstanceId = in.readInt();
        mPermissions = in.readInt();
        mProperties = in.readInt();
        mWriteType = in.readInt();
        mDescriptors = in.createTypedArrayList(TgiBtGattDescriptor.CREATOR);
    }

    public static final Creator<TgiBtGattChar> CREATOR = new Creator<TgiBtGattChar>() {
        @Override
        public TgiBtGattChar createFromParcel(Parcel in) {
            return new TgiBtGattChar(in);
        }

        @Override
        public TgiBtGattChar[] newArray(int size) {
            return new TgiBtGattChar[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(mUuid);
        dest.writeString(mServiceUUID);
        dest.writeByteArray(mValue);
        dest.writeInt(mInstanceId);
        dest.writeInt(mPermissions);
        dest.writeInt(mProperties);
        dest.writeInt(mWriteType);
        dest.writeTypedList(mDescriptors);
    }


    public String getUuid() {
        return mUuid;
    }


    public byte[] getValue() {
        return mValue;
    }

    public int getInstanceId() {
        return mInstanceId;
    }

    public int getPermissions() {
        return mPermissions;
    }

    public int getProperties() {
        return mProperties;
    }

    public int getWriteType() {
        return mWriteType;
    }

    public ArrayList<TgiBtGattDescriptor> getDescriptors() {
        return mDescriptors;
    }

    public void setUuid(String uuid) {
        mUuid = uuid;
    }


    public void setValue(byte[] value) {
        mValue = value;
    }

    public void setDescriptors(ArrayList<TgiBtGattDescriptor> descriptors) {
        mDescriptors = descriptors;
    }

    public void setInstanceId(int instanceId) {
        mInstanceId = instanceId;
    }

    public void setPermissions(int permissions) {
        mPermissions = permissions;
    }

    public void setProperties(int properties) {
        mProperties = properties;
    }

    public void setWriteType(int writeType) {
        mWriteType = writeType;
    }

    public String getServiceUUID() {
        return mServiceUUID;
    }

    public void setServiceUUID(String serviceUUID) {
        mServiceUUID = serviceUUID;
    }

    @Override
    public String toString() {
        return "TgiBtGattChar{" +
                "mUuid='" + mUuid + '\'' +
                ", mServiceUUID='" + mServiceUUID + '\'' +
                ", mValue=" + Arrays.toString(mValue) +
                ", mInstanceId=" + mInstanceId +
                ", mPermissions=" + mPermissions +
                ", mProperties=" + mProperties +
                ", mWriteType=" + mWriteType +
                ", mDescriptors=" + mDescriptors +
                '}';
    }
}
