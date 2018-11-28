package tgi.com.tgifreertobtdemo.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import tgi.com.bluetooth.service.BleBackgroundService;
import tgi.com.tgifreertobtdemo.R;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void toDevicesList(View view) {
        DeviceListActivity.start(this);
    }

    public void startMcSimulation(View view) {
        MCSimulationActivity.start(this);

    }
}
