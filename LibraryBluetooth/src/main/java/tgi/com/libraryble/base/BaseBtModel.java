package tgi.com.libraryble.base;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import tgi.com.libraryble.LibraryBtConstants;


public class BaseBtModel {
    public boolean isBtSupported(Context context){
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH);
    }

    public BluetoothAdapter getBtAdapter(Context context){
        BluetoothAdapter adapter=null;
        BluetoothManager manager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        try {
            adapter=manager.getAdapter();
        }catch (NullPointerException e){
            e.printStackTrace();
        }
        return adapter;
    }

    public boolean isBtEnable(BluetoothAdapter adapter) {
        return adapter == null || !adapter.isEnabled();
    }

    public void enableBt(Activity activity){
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        activity.startActivityForResult(enableBtIntent, LibraryBtConstants.REQUEST_ENABLE_BT);
    }




}
