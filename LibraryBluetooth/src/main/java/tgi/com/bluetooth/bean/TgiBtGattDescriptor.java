package tgi.com.bluetooth.bean;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Arrays;

public class TgiBtGattDescriptor implements Parcelable{

    private  String mUuid;
    private  byte[] mValue;
    private  int mPermissions;
    private  String mBtGattCharUUID;

    public TgiBtGattDescriptor(BluetoothGattDescriptor descriptor){
        mUuid = descriptor.getUuid().toString();
        mValue = descriptor.getValue();
        if(mValue==null){
            mValue=new byte[]{};
        }
        mPermissions = descriptor.getPermissions();
        BluetoothGattCharacteristic btChar = descriptor.getCharacteristic();
        mBtGattCharUUID =btChar.getUuid().toString();
    }


    protected TgiBtGattDescriptor(Parcel in) {
        mUuid = in.readString();
        mValue = in.createByteArray();
        mPermissions = in.readInt();
        mBtGattCharUUID = in.readString();
    }

    public static final Creator<TgiBtGattDescriptor> CREATOR = new Creator<TgiBtGattDescriptor>() {
        @Override
        public TgiBtGattDescriptor createFromParcel(Parcel in) {
            return new TgiBtGattDescriptor(in);
        }

        @Override
        public TgiBtGattDescriptor[] newArray(int size) {
            return new TgiBtGattDescriptor[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(mUuid);
        dest.writeByteArray(mValue);
        dest.writeInt(mPermissions);
        dest.writeString(mBtGattCharUUID);
    }


    public String getUuid() {
        return mUuid;
    }

    public void setUuid(String uuid) {
        mUuid = uuid;
    }

    public byte[] getValue() {
        return mValue;
    }

    public void setValue(byte[] value) {
        mValue = value;
    }

    public int getPermissions() {
        return mPermissions;
    }

    public void setPermissions(int permissions) {
        mPermissions = permissions;
    }

    public String getBtGattCharUUID() {
        return mBtGattCharUUID;
    }

    public void setBtGattCharUUID(String btGattCharUUID) {
        mBtGattCharUUID = btGattCharUUID;
    }

    @Override
    public String toString() {
        return "TgiBtGattDescriptor{" +
                "mUuid='" + mUuid + '\'' +
                ", mValue=" + Arrays.toString(mValue) +
                ", mPermissions=" + mPermissions +
                ", mBtGattCharUUID='" + mBtGattCharUUID + '\'' +
                '}';
    }
}
