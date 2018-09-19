package tgi.com.libraryble.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class BleDescriptor implements Parcelable {
    private String mName;
    private String mUUID;

    public BleDescriptor() {
    }

    protected BleDescriptor(Parcel in) {
        mName = in.readString();
        mUUID = in.readString();
    }

    public static final Creator<BleDescriptor> CREATOR = new Creator<BleDescriptor>() {
        @Override
        public BleDescriptor createFromParcel(Parcel in) {
            return new BleDescriptor(in);
        }

        @Override
        public BleDescriptor[] newArray(int size) {
            return new BleDescriptor[size];
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

    @Override
    public String toString() {
        return "BleDescriptor{" +
                "mName='" + mName + '\'' +
                ", mUUID='" + mUUID + '\'' +
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
    }
}
