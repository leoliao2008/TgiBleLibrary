package tgi.com.libraryble.bean;


import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class BleDevice implements Parcelable {
    private String mName;
    private String mAddress;
    private ArrayList<BleService> mBleServices;

    public BleDevice() {
    }

    protected BleDevice(Parcel in) {
        mName = in.readString();
        mAddress = in.readString();
        mBleServices = in.createTypedArrayList(BleService.CREATOR);
    }

    public static final Creator<BleDevice> CREATOR = new Creator<BleDevice>() {
        @Override
        public BleDevice createFromParcel(Parcel in) {
            return new BleDevice(in);
        }

        @Override
        public BleDevice[] newArray(int size) {
            return new BleDevice[size];
        }
    };

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getAddress() {
        return mAddress;
    }

    public void setAddress(String address) {
        mAddress = address;
    }

    public ArrayList<BleService> getBleServices() {
        return mBleServices;
    }

    public void setBleServices(ArrayList<BleService> bleServices) {
        mBleServices = bleServices;
    }

    @Override
    public String toString() {
        return "BleDevice{" +
                "mName='" + mName + '\'' +
                ", mAddress='" + mAddress + '\'' +
                ", mBleServices=" + mBleServices +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mName);
        dest.writeString(mAddress);
        dest.writeTypedList(mBleServices);
    }
}
