package com.bluetooth.test.bluetoothtest;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.bluetooth.le.*;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.UUID;
import java.util.ArrayList;
import java.util.List;

import com.bluetooth.test.bluetoothtest.BluetoothUtility;


public class MyActivity extends Activity {
    private static final String TAG = "MyActivity";
    BluetoothUtility ble;
    private String serviceOneCharUuid;
    private static final String SERVICE_UUID_1 = "00001802-0000-1000-8000-00805f9b34fb";
    private static final String SERVICE_DEVICE_INFORMATION = "0000180a-0000-1000-8000-00805f9b34fb";

    Button startAdvButton;
    Button stopAdvButton;
    Button scanButton;
    Button stopScanButton;
    TextView bluetoothState;
    ArrayAdapter<String> btArrayAdapter;
    ListView listDevicesFound;
    private ArrayList<String> foundDevices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        scanButton = (Button)findViewById(R.id.scan_button);
        stopScanButton = (Button)findViewById(R.id.scan_stop_button);
        startAdvButton = (Button)findViewById(R.id.adv_start_button);
        stopAdvButton = (Button)findViewById(R.id.adv_stop_button);
        bluetoothState = (TextView)findViewById(R.id.bluetooth_state_text);
        listDevicesFound = (ListView)findViewById(R.id.devicesfound);
        btArrayAdapter = new ArrayAdapter<String>(MyActivity.this, android.R.layout.simple_list_item_1);
        listDevicesFound.setAdapter(btArrayAdapter);

        ble = new BluetoothUtility(this);

        //ble.setAdvertiseCallback(advertiseCallback);
        ble.setGattServerCallback(gattServerCallback);
        //ble.setLeScanCallback(leScanCallback);
        //ble.setScanCallback(scanCallback);

        foundDevices = new ArrayList<String>();

        addServiceToGattServer();
        //IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        //filter.addAction(BluetoothDevice.ACTION_UUID);
        //registerReceiver(ActionFoundReceiver, filter); // Don't forget to unregister during onDestroy

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                handleStartClick(null);
            }
        }, 5000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ble.cleanUp();
        //unregisterReceiver(ActionFoundReceiver);
    }

    public void handleStartClick(View view) {
        ble.startAdvertise();
        startAdvButton.setEnabled(false);
        stopAdvButton.setEnabled(true);
    }

    public void handleStopClick(View view) {
        ble.stopAdvertise();
        startAdvButton.setEnabled(true);
        stopAdvButton.setEnabled(false);
    }

    private void addServiceToGattServer() {
        serviceOneCharUuid = UUID.randomUUID().toString();

        BluetoothGattService firstService = new BluetoothGattService(
                UUID.fromString(SERVICE_UUID_1),
                BluetoothGattService.SERVICE_TYPE_PRIMARY);
        // alert level char.
        BluetoothGattCharacteristic firstServiceChar = new BluetoothGattCharacteristic(
                UUID.fromString(serviceOneCharUuid),
                BluetoothGattCharacteristic.PROPERTY_READ |
                        BluetoothGattCharacteristic.PROPERTY_WRITE,
                BluetoothGattCharacteristic.PERMISSION_READ |
                        BluetoothGattCharacteristic.PERMISSION_WRITE);
        firstService.addCharacteristic(firstServiceChar);
        ble.addService(firstService);
    }

    public BluetoothGattServerCallback gattServerCallback = new BluetoothGattServerCallback() {
        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            super.onConnectionStateChange(device, status, newState);
            Log.d(TAG, "onConnectionStateChange status=" + status + "->" + newState);
        }

        @Override
        public void onServiceAdded(int status, BluetoothGattService service) {
            super.onServiceAdded(status, service);
        }

        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
            Log.d(TAG, "onCharacteristicReadRequest requestId=" + requestId + " offset=" + offset);

            if (characteristic.getUuid().equals(UUID.fromString(serviceOneCharUuid))) {
                Log.d(TAG, "SERVICE_UUID_1");
                characteristic.setValue("Text:This is a test characteristic");
                ble.getGattServer().sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset,
                        characteristic.getValue());
            }

        }

        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);
            Log.d(TAG, "onCharacteristicWriteRequest requestId=" + requestId + " preparedWrite="
                    + Boolean.toString(preparedWrite) + " responseNeeded="
                    + Boolean.toString(responseNeeded) + " offset=" + offset);
        }
    };

}
