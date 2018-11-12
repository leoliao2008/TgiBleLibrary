package tgi.com.bluetooth.bean;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class TgiBtGattService implements Parcelable{
    private String mUuid;
    private int mInstanceId;
    private int mType;
    private ArrayList<TgiBtGattChar> mChars=new ArrayList<>();

    public TgiBtGattService(BluetoothGattService service){
        mUuid=service.getUuid().toString();
        mInstanceId = service.getInstanceId();
        mType = service.getType();
        List<BluetoothGattCharacteristic> btChars = service.getCharacteristics();
        for(BluetoothGattCharacteristic btChar:btChars){
            mChars.add(new TgiBtGattChar(btChar));
        }
    }


    protected TgiBtGattService(Parcel in) {
        mUuid = in.readString();
        mInstanceId = in.readInt();
        mType = in.readInt();
        mChars = in.createTypedArrayList(TgiBtGattChar.CREATOR);
    }

    public static final Creator<TgiBtGattService> CREATOR = new Creator<TgiBtGattService>() {
        @Override
        public TgiBtGattService createFromParcel(Parcel in) {
            return new TgiBtGattService(in);
        }

        @Override
        public TgiBtGattService[] newArray(int size) {
            return new TgiBtGattService[size];
        }
    };

    public String getUuid() {
        return mUuid;
    }

    public int getInstanceId() {
        return mInstanceId;
    }

    public int getType() {
        return mType;
    }

    public ArrayList<TgiBtGattChar> getChars() {
        return mChars;
    }



    @Override
    public String toString() {
        return "TgiBtGattService{" +
                "mUuid='" + mUuid + '\'' +
                ", mInstanceId=" + mInstanceId +
                ", mType=" + mType +
                ", mChars=" + mChars +
                '}';
    }

    public void setUuid(String uuid) {
        mUuid = uuid;
    }

    public void setChars(ArrayList<TgiBtGattChar> chars) {
        mChars = chars;
    }

    public void setInstanceId(int instanceId) {
        mInstanceId = instanceId;
    }

    public void setType(int type) {
        mType = type;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mUuid);
        dest.writeInt(mInstanceId);
        dest.writeInt(mType);
        dest.writeTypedList(mChars);
    }
}
