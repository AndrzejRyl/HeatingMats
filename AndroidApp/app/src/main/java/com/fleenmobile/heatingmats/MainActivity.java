package com.fleenmobile.heatingmats;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private static final String PIN = "854345";

    @BindView(R.id.paired_devices_list)
    ListView pairedDevicesRecycler;

    @BindView(R.id.new_devices_list)
    ListView newDevicesRecycler;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private BluetoothAdapter bluetoothAdapter;

    private ArrayAdapter<String> pairedDevicesArrayAdapter;
    private ArrayAdapter<String> newDevicesArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar(toolbar);

        ButterKnife.bind(this);

        initRecyclers();
        initBT();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(btReceiver);
    }

    private void initRecyclers() {
        pairedDevicesArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        newDevicesArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);

        pairedDevicesRecycler.setAdapter(pairedDevicesArrayAdapter);

        newDevicesRecycler.setAdapter(newDevicesArrayAdapter);
        newDevicesRecycler.setOnItemClickListener((adapterView, view, i, l) -> {
            String info = ((TextView) view).getText().toString();
            String address = info.substring(info.length() - 17);
            pairDevice(address);
        });
    }

    private void initBT() {
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN},
                1);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(btReceiver, filter);

        refreshPairedDevices();
        refreshNewDevices();
    }

    private void pairDevice(String address) {
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        try {
            device.setPin(PIN.getBytes("UTF8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        device.createBond();
    }

    @OnClick(R.id.refresh_button)
    public void onRefreshButtonClick() {
        pairedDevicesArrayAdapter.clear();
        newDevicesArrayAdapter.clear();

        refreshPairedDevices();
        refreshNewDevices();
    }

    private void refreshNewDevices() {
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }

        // Request discover from BluetoothAdapter
        bluetoothAdapter.startDiscovery();
    }

    private void refreshPairedDevices() {
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                pairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        }
    }

    private final BroadcastReceiver btReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                newDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        }
    };
}
