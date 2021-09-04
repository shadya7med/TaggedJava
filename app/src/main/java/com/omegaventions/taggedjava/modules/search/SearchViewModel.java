package com.omegaventions.taggedjava.modules.search;

import android.bluetooth.BluetoothClass;
import android.bluetooth.le.ScanResult;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

public class SearchViewModel extends ViewModel {


    public MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    public MutableLiveData<Boolean> connecting = new MutableLiveData<>(false);

    public MutableLiveData<List<ScanResult>> scanResults = new MutableLiveData<>();
    public MutableLiveData<List<BluetoothClass.Device>> devicesList = new MutableLiveData<>();

    public SearchViewModel(){

    }

    void resetScan() {
        loading.setValue(false);
        scanResults.setValue(null);
    }
}