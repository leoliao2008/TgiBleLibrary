package tgi.com.libraryble.base;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.util.Log;

import java.lang.ref.WeakReference;

import tgi.com.libraryble.LibraryBtConstants;

import static android.app.Activity.RESULT_OK;

public abstract class BaseBtManager<MODEL extends BaseBtModel, HANDLER extends BaseBtEventHandler> {
    public MODEL mBtModel;
    public HANDLER mEventHandler;
    public BluetoothAdapter mBluetoothAdapter;
    private String TAG;
    private WeakReference<Activity> mWeakReference;

    public BaseBtManager(Activity context, HANDLER handler) {
        TAG = getClass().getSimpleName();
        mWeakReference = new WeakReference<>(context);
        mEventHandler = handler;
        mBtModel = setBtModel();
        if (!mBtModel.isBtSupported(context)) {
            mEventHandler.onBtNotSupported();
        } else {
            BluetoothAdapter btAdapter = mBtModel.getBtAdapter(context);
            if (!mBtModel.isBtEnable(btAdapter)) {
                mBtModel.enableBt(context);
            } else {
                mBluetoothAdapter = btAdapter;
            }
        }
    }

    protected abstract MODEL setBtModel();

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == LibraryBtConstants.REQUEST_ENABLE_BT) {
            if (resultCode != RESULT_OK) {
                mEventHandler.onUserRefusesToEnableBt();
            } else {
                if (mWeakReference.get() != null) {
                    mBluetoothAdapter = mBtModel.getBtAdapter(mWeakReference.get());
                }
            }
        }
    }

    protected void showLog(String msg) {
        Log.e(TAG, msg);
    }
}
