package com.omegaventions.taggedjava.modules.home;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HomeViewModel extends ViewModel {

    public HomeViewModel() {

    }

    MutableLiveData<BluetoothGatt> selectedGatt = new MutableLiveData<BluetoothGatt>();
    public MutableLiveData<DeviceBinder> deviceBinder = new MutableLiveData<>();


    public static class DeviceBinder {

        public BluetoothDevice device;//setters and getters should be created for better encapsulation
        public String batteryLevel;

        DeviceBinder(BluetoothDevice device, String batteryLevel) {
            this.device = device;
            this.batteryLevel = batteryLevel;
        }

    }
}