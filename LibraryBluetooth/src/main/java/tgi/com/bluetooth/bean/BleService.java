package tgi.com.bluetooth.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class BleService implements Parcelable {
    private String mName;
    private String mUUID;
    private ArrayList<BleCharacteristic> mBleCharacteristics;

    public BleService() {
    }

    protected BleService(Parcel in) {
        mName = in.readString();
        mUUID = in.readString();
        mBleCharacteristics = in.createTypedArrayList(BleCharacteristic.CREATOR);
    }

    public static final Creator<BleService> CREATOR = new Creator<BleService>() {
        @Override
        public BleService createFromParcel(Parcel in) {
            return new BleService(in);
        }

        @Override
        public BleService[] newArray(int size) {
            return new BleService[size];
        }
    };

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getUUID() {
        return mUUID;
    }

    public void setUUID(String UUID) {
        mUUID = UUID;
    }

    public ArrayList<BleCharacteristic> getBleCharacteristics() {
        return mBleCharacteristics;
    }

    public void setBleCharacteristics(ArrayList<BleCharacteristic> bleCharacteristics) {
        mBleCharacteristics = bleCharacteristics;
    }

    @Override
    public String toString() {
        return "BleService{" +
                "mName='" + mName + '\'' +
                ", mUUID='" + mUUID + '\'' +
                ", mBleCharacteristics=" + mBleCharacteristics +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mName);
        dest.writeString(mUUID);
        dest.writeTypedList(mBleCharacteristics);
    }
}
