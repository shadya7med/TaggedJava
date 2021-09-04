package com.omegaventions.taggedjava.utils;


import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;

import com.omegaventions.taggedjava.Extensions;
import com.omegaventions.taggedjava.R;

import java.io.Serializable;
import java.util.UUID;

public class BleHelper {

    private final String TAG = BleHelper.class.getSimpleName();

    public static final int GATT_MAX_MTU_SIZE = 517;
    private static final int CURRENT_MTU_SIZE = 23;
    public static final int ENABLE_BLUETOOTH_REQUEST_CODE = 1;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 2;
    private static final UUID BATTERY_SERVICE_UUID = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb");
    private static final UUID SERVICE_1_UUID = UUID.fromString("00001800-0000-1000-8000-00805f9b34fb");
    private static final UUID SERVICE_2_UUID = UUID.fromString("00001802-0000-1000-8000-00805f9b34fb");
    private static final UUID SERVICE_3_UUID = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
    private static final UUID BATTERY_LEVEL_CHAR_UUID = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb");

    private FragmentActivity activity;

    private final Boolean isLocationPermissionGranted;

    private final BluetoothAdapter bluetoothAdapter;

    private final BluetoothLeScanner bleScanner;

    private final ScanSettings scanSettings;

    private GattReadListener gattReadListener;

    private static BleHelper SHARED;

    private BleHelper(FragmentActivity activity) {
        this.activity = activity;
        isLocationPermissionGranted = Extensions.hasPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION);
        bluetoothAdapter = ((BluetoothManager) activity.getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
        bleScanner = bluetoothAdapter.getBluetoothLeScanner();
        scanSettings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();
    }

    public static synchronized BleHelper getInstance(FragmentActivity activity) {
        if (SHARED == null) {
            SHARED = new BleHelper(activity);
        }
        return SHARED;
    }

    public void promptEnableBluetooth(ActivityResultLauncher<Intent> enableBtLauncher) {
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            enableBtLauncher.launch(enableBtIntent);
        }
    }

    public Boolean startBleScan(ScanCallback scanCallback) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !isLocationPermissionGranted) {
            requestLocationPermission();
            return false;
        } else {
            bleScanner.startScan(null, scanSettings, scanCallback);
            return true;
        }
    }

    public void stopBleScan(ScanCallback scanCallback) {
        bleScanner.stopScan(scanCallback);
    }

    private void requestLocationPermission() {
        if (isLocationPermissionGranted) {
            return;
        }
        activity.runOnUiThread(() -> new AlertDialog.Builder(activity)
                .setTitle("Location permission required")
                .setMessage(
                        R.string.request_location_for_ble
                ).setCancelable(false)
                .setPositiveButton(
                        R.string.ok,
                        (dialog, which) ->// User clicked OK button
                                Extensions.requestPermission(activity,
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        LOCATION_PERMISSION_REQUEST_CODE

                                )).show()
        );
    }


    public Boolean isReadable(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        return containsProperty(bluetoothGattCharacteristic, BluetoothGattCharacteristic.PROPERTY_READ);
    }

    public Boolean isWritable(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        return containsProperty(bluetoothGattCharacteristic, BluetoothGattCharacteristic.PROPERTY_WRITE);
    }

    public Boolean isWritableWithoutResponse(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        return containsProperty(bluetoothGattCharacteristic, BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE);
    }

    private Boolean containsProperty(BluetoothGattCharacteristic bluetoothGattCharacteristic, int property) {
        return (bluetoothGattCharacteristic.getProperties() | property) != 0;
    }

    public void connectAndDiscover(BluetoothDevice device, DeviceGattConnectListener connectListener) {
        device.connectGatt(activity,
                false,
                new BluetoothGattCallback() {
                    //all of the following methods are invoked on a background thread by default
                    @Override
                    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

                        String deviceAddress = gatt.getDevice().getAddress();

                        if (status == BluetoothGatt.GATT_SUCCESS) {
                            if (newState == BluetoothProfile.STATE_CONNECTED) {
                                Log.v(TAG, "Successfully connected to" + deviceAddress);
                                new Handler(Looper.getMainLooper()).post(gatt::discoverServices);
                            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                                Log.v(TAG, "Successfully disconnected from" + deviceAddress);
                                gatt.close();
                            }
                        } else {
                            Log.v(TAG, "Error $status encountered for " + deviceAddress + " Disconnecting...");
                            gatt.close();
                            connectListener.onFail("Error $status encountered for " + deviceAddress + " Disconnecting...");
                        }
                    }

                    @Override
                    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                        if (gatt.getServices().isEmpty()) {
                            Log.v(TAG, "No service and characteristic available, call discoverServices() first?");
                            return;
                        }
                        for (BluetoothGattService service : gatt.getServices()) {
                            Log.v(TAG, "\nService" + service.getUuid());
                        }
//            gatt.requestMtu(bleHelper.GATT_MAX_MTU_SIZE );
                        //device is connected and its services are discovered
                        new Handler(Looper.getMainLooper()).post(() -> connectListener.onSuccess(gatt));
                    }

                    @Override
                    public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
                        Log.v(TAG, "onMtuChanged: " + mtu + "success:" + ((status == BluetoothGatt.GATT_SUCCESS) ? "true" : "false"));
                    }

                    @Override
                    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                        switch (status) {
                            case BluetoothGatt.GATT_SUCCESS:
                                new Handler(Looper.getMainLooper()).post(() -> {
                                    int value = characteristic.getValue()[0];
                                    gattReadListener.onSuccess("" + value);
                                });
                            case BluetoothGatt.GATT_READ_NOT_PERMITTED:
                                Log.e(TAG, "Read not permitted for " + characteristic.getUuid());
                                gattReadListener.onFail("Read not permitted for " + characteristic.getUuid());
                            default:
                                Log.e(TAG, "Characteristic read failed for " + characteristic.getUuid() + ", error:" + status);
                                gattReadListener.onFail("Characteristic read failed for " + characteristic.getUuid() + ", error:" + status);
                        }
                    }

                    @Override
                    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {

                        switch (status) {
                            case BluetoothGatt.GATT_SUCCESS: {
                                Log.i(TAG, "Wrote to characteristic" + characteristic.getUuid());
                            }
                            case BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH: {
                                Log.e(TAG, "Write exceeded connection ATT MTU!");
                            }
                            case BluetoothGatt.GATT_WRITE_NOT_PERMITTED: {
                                Log.e(TAG, "Write not permitted for " + characteristic.getUuid());
                            }
                            default:
                                Log.e(TAG, "Characteristic write failed for" + characteristic.getUuid() + ", error: " + status);
                        }
                    }

                    @Override
                    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                        super.onDescriptorWrite(gatt, descriptor, status);
                    }

                    @Override
                    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                        Log.i(TAG, "Characteristic $uuid changed | value: " + characteristic.getValue());

                    }
                }

        );
    }


    public void readBatteryLevel(BluetoothGatt gatt, GattReadListener gattReadListener) {
        this.gattReadListener = gattReadListener;
        BluetoothGattCharacteristic batteryLevelChar = gatt.getService(BATTERY_SERVICE_UUID).getCharacteristic(BATTERY_LEVEL_CHAR_UUID);
        if (isReadable(batteryLevelChar)) {
            gatt.readCharacteristic(batteryLevelChar);
        } else {
            gattReadListener.onFail("Battery level is not readable");
        }
    }

    void writeCharacteristic(BluetoothGattCharacteristic characteristic, BluetoothGatt gatt, byte[] payload) {
        int writeType = 0;
        if (isWritable(characteristic)) {
            writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT;
        } else if (isWritableWithoutResponse(characteristic)) {
            writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE;
        } else {
            Log.i(TAG, "Characteristic ${characteristic.uuid} cannot be written to");
            return;
        }

        characteristic.setWriteType(writeType);
        characteristic.setValue(payload);
        gatt.writeCharacteristic(characteristic);
    }

    void writeDescriptor(BluetoothGattDescriptor descriptor, BluetoothGatt gatt, byte[] payload) {
        descriptor.setValue(payload);
        gatt.writeDescriptor(descriptor);
    }

    Boolean isReadable(BluetoothGattDescriptor bluetoothGattDescriptor) {
        return containsPermission(bluetoothGattDescriptor, BluetoothGattDescriptor.PERMISSION_READ) ||

                containsPermission(bluetoothGattDescriptor, BluetoothGattDescriptor.PERMISSION_READ_ENCRYPTED) ||

                containsPermission(bluetoothGattDescriptor, BluetoothGattDescriptor.PERMISSION_READ_ENCRYPTED_MITM);
    }

    Boolean isWritable(BluetoothGattDescriptor bluetoothGattDescriptor) {
        return containsPermission(bluetoothGattDescriptor, BluetoothGattDescriptor.PERMISSION_WRITE) ||

                containsPermission(bluetoothGattDescriptor, BluetoothGattDescriptor.PERMISSION_WRITE_ENCRYPTED) ||

                containsPermission(bluetoothGattDescriptor, BluetoothGattDescriptor.PERMISSION_WRITE_ENCRYPTED_MITM) ||

                containsPermission(bluetoothGattDescriptor, BluetoothGattDescriptor.PERMISSION_WRITE_SIGNED) ||

                containsPermission(bluetoothGattDescriptor, BluetoothGattDescriptor.PERMISSION_WRITE_SIGNED_MITM);
    }

    Boolean containsPermission(BluetoothGattDescriptor bluetoothGattDescriptor, int permission) {
        return (bluetoothGattDescriptor.getPermissions() & permission) != 0;
    }

    Boolean isIndicatable(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        return containsProperty(bluetoothGattCharacteristic, BluetoothGattCharacteristic.PROPERTY_INDICATE);
    }


    Boolean isNotifiable(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        return containsProperty(bluetoothGattCharacteristic, BluetoothGattCharacteristic.PROPERTY_NOTIFY);
    }

    void enableNotifications(BluetoothGattCharacteristic characteristic, BluetoothGatt gatt) {
        UUID cccdUuid = characteristic.getUuid();
        byte[] payload;
        if (isIndicatable(characteristic)) {
            payload = BluetoothGattDescriptor.ENABLE_INDICATION_VALUE;
        } else if (isNotifiable(characteristic)) {
            payload = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
        } else {
            Log.e(TAG, characteristic.getUuid() + "doesn't support notifications/indications");
            return;
        }
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(cccdUuid);
        if (descriptor != null) {
            if (!gatt.setCharacteristicNotification(characteristic, true)) {
                Log.e(TAG, "setCharacteristicNotification failed for " + characteristic.getUuid());
                return;
            }
            writeDescriptor(descriptor, gatt, payload);
        } else {
            Log.e(TAG, characteristic.getUuid() + "doesn't contain the CCC descriptor!");
        }
    }

    void disableNotifications(BluetoothGattCharacteristic characteristic, BluetoothGatt gatt) {
        UUID cccdUuid = characteristic.getUuid();
        if (!isNotifiable(characteristic) && !isIndicatable(characteristic)) {
            Log.e(TAG, characteristic.getUuid() + "doesn't support notifications/indications");
            return;
        }

        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(cccdUuid);
        if (descriptor != null) {
            if (!gatt.setCharacteristicNotification(characteristic, false)) {
                Log.e(TAG, "setCharacteristicNotification failed for " + characteristic.getUuid());
                return;
            }
            writeDescriptor(descriptor, gatt, BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        } else {
            Log.e(TAG, characteristic.getUuid() + "doesn't contain the CCC descriptor!");
        }
    }


    public interface DeviceGattConnectListener {
        void onSuccess(BluetoothGatt gatt);

        void onFail(String error);
    }

    public interface GattReadListener {
        void onSuccess(String value);

        void onFail(String error);
    }

    static public class GattHolder implements Serializable {
        public BluetoothGatt gatt;

        public GattHolder(BluetoothGatt gatt) {
            this.gatt = gatt;
        }

    }

}