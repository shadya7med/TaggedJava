package com.omegaventions.taggedjava.modules.search;


import android.bluetooth.BluetoothGatt;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.snackbar.Snackbar;
import com.omegaventions.taggedjava.databinding.FragmentSearchBinding;
import com.omegaventions.taggedjava.utils.BleHelper;

import java.util.ArrayList;

public class SearchFragment extends Fragment {

    private final String TAG = SearchFragment.class.getSimpleName();

    private static final long ONE_MINUTE_IN_MILLI_SEC = 60000L;
    private static final long ONE_SEC_IN_MILLI_SEC = 1000L;
    public static final String SELECTED_DEVICE_GATT = "selected_device_gatt";

    private BleHelper bleHelper;
    private final ActivityResultLauncher<Intent> enableBtLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> startActivityForResult(BleHelper.ENABLE_BLUETOOTH_REQUEST_CODE, result));

    private FragmentSearchBinding binding;
    private SearchViewModel viewModel;

    public SearchFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding =
                FragmentSearchBinding.inflate(LayoutInflater.from(requireActivity()), container, false);
        bleHelper = BleHelper.getInstance(requireActivity());
        viewModel = new ViewModelProvider(this).get(SearchViewModel.class);
        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(getViewLifecycleOwner());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupScanResultsRecyclerView();
        setClickListener();
    }

    @Override
    public void onResume() {
        super.onResume();
        bleHelper.promptEnableBluetooth(enableBtLauncher);
    }

    @Override
    public void onStop() {
        super.onStop();
        bleHelper.stopBleScan(scanCallback);
        viewModel.resetScan();
    }

    private void setupScanResultsRecyclerView() {
        binding.scanResultsRv.setAdapter(new ScanResultsAdapter(new ScanResultsAdapter.ScanResultClickListener(scanResult -> {
            selectDevice(scanResult);
        })));
    }

    private void setClickListener() {
        binding.searchBtn.setOnClickListener(v -> {
            if (bleHelper.startBleScan(scanCallback)) {
                viewModel.loading.setValue(true);
                setupCountdownTimer(ONE_MINUTE_IN_MILLI_SEC, ONE_SEC_IN_MILLI_SEC).start();
            }
        });
    }

    private final ScanCallback scanCallback = new ScanCallback() {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            ArrayList<ScanResult> currentList = new ArrayList<>(((ScanResultsAdapter) binding.scanResultsRv.getAdapter()).getCurrentList());

            if (currentList.stream().noneMatch(scanResult -> scanResult.getDevice().getAddress().equals(result.getDevice().getAddress()))) {
                currentList.add(result);
            }
            viewModel.scanResults.setValue(currentList);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.e(TAG, "onScanFailed: code " + errorCode);
        }

    };

    private CountDownTimer setupCountdownTimer(long countDownTime, long countDownInterval) {
        return new CountDownTimer(countDownTime, countDownInterval) {

            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {

                bleHelper.stopBleScan(scanCallback);
                viewModel.loading.setValue(false);
            }
        };

    }


    private void startActivityForResult(int requestCode, ActivityResult result) {
        if (requestCode == BleHelper.ENABLE_BLUETOOTH_REQUEST_CODE && result.getResultCode() == AppCompatActivity.RESULT_OK) {
            bleHelper.promptEnableBluetooth(enableBtLauncher);
        }
    }

    private void selectDevice(ScanResult scanResult) {
        bleHelper.stopBleScan(scanCallback);
        viewModel.loading.setValue(false);
        viewModel.connecting.setValue(true);
        bleHelper.connectAndDiscover(scanResult.getDevice(), new BleHelper.DeviceGattConnectListener() {
            @Override
            public void onSuccess(BluetoothGatt gatt) {
                viewModel.connecting.setValue(false);
                NavController navController = Navigation.findNavController(binding.getRoot());
                NavBackStackEntry backStackEntry = navController.getPreviousBackStackEntry();
                if (backStackEntry != null) {
                    backStackEntry.getSavedStateHandle().set(SearchFragment.SELECTED_DEVICE_GATT, new BleHelper.GattHolder(gatt));
                    navController.navigateUp();
                }
            }

            @Override
            public void onFail(String error) {
                Snackbar.make(requireActivity().findViewById(android.R.id.content), error, Snackbar.LENGTH_SHORT);
            }
        });

    }

}