package tgi.com.tgifreertobtdemo.activity;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import java.util.ArrayList;

import tgi.com.bluetooth.manager.BleClientManager;

public class BaseApplication extends Application {
    private ArrayList<String> mStack=new ArrayList<>();
    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                String tag = activity.toString();
                if(mStack.contains(tag)){
                    return;
                }
                mStack.add(tag);

            }

            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityResumed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                mStack.remove(activity.toString());
                if(mStack.isEmpty()){
                    BleClientManager.getInstance().killBleBgService(getApplicationContext());
                    unregisterActivityLifecycleCallbacks(this);
                }
            }
        });
    }


}

