package tgi.com.libraryble.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class BleCharacteristic implements Parcelable {
    private String mName;
    private String mUUID;
    private ArrayList<BleDescriptor> mDescriptors;

    public BleCharacteristic() {
    }

    protected BleCharacteristic(Parcel in) {
        mName = in.readString();
        mUUID = in.readString();
        mDescriptors = in.createTypedArrayList(BleDescriptor.CREATOR);
    }

    public static final Creator<BleCharacteristic> CREATOR = new Creator<BleCharacteristic>() {
        @Override
        public BleCharacteristic createFromParcel(Parcel in) {
            return new BleCharacteristic(in);
        }

        @Override
        public BleCharacteristic[] newArray(int size) {
            return new BleCharacteristic[size];
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

    public ArrayList<BleDescriptor> getDescriptors() {
        return mDescriptors;
    }

    public void setDescriptors(ArrayList<BleDescriptor> descriptors) {
        mDescriptors = descriptors;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mName);
        dest.writeString(mUUID);
        dest.writeTypedList(mDescriptors);
    }

    @Override
    public String toString() {
        return "BleCharacteristic{" +
                "mName='" + mName + '\'' +
                ", mUUID='" + mUUID + '\'' +
                ", mDescriptors=" + mDescriptors +
                '}';
    }
}
