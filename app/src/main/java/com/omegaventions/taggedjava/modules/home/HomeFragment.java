package com.omegaventions.taggedjava.modules.home;


import android.bluetooth.BluetoothGatt;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.omegaventions.taggedjava.R;
import com.omegaventions.taggedjava.databinding.FragmentHomeBinding;
import com.omegaventions.taggedjava.modules.search.SearchFragment;
import com.omegaventions.taggedjava.utils.BleHelper;


public class HomeFragment extends Fragment {

    private String TAG = HomeFragment.class.getSimpleName();

    private FragmentHomeBinding binding;
    private HomeViewModel viewModel;
    private BleHelper bleHelper;

    public HomeFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(
                LayoutInflater.from(requireActivity()),
                container,
                false
        );
        bleHelper = BleHelper.getInstance(requireActivity());
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(getViewLifecycleOwner());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        bindSelectedGatt();
        setClickListener();
        observeViewModel();
    }

    private void bindSelectedGatt() {
        NavController navController = NavHostFragment.findNavController(this);
        // We use a String here, but any type that can be put in a Bundle is supported
        NavBackStackEntry backStackEntry = navController.getCurrentBackStackEntry();
        if (backStackEntry != null) {
            MutableLiveData<BleHelper.GattHolder> gattHolderLiveData = backStackEntry.getSavedStateHandle().getLiveData(SearchFragment.SELECTED_DEVICE_GATT);
            BleHelper.GattHolder gattHolder = gattHolderLiveData.getValue();
            if (gattHolder != null) {
                viewModel.selectedGatt.setValue(gattHolder.gatt);
            }
        }
    }

    private void setClickListener() {
        binding.searchBtn.setOnClickListener(v -> {
            Navigation.findNavController(binding.getRoot()).navigate(R.id.action_home_fragment_to_search_fragment);
        });
    }

    private void observeViewModel() {
        viewModel.selectedGatt.observe(getViewLifecycleOwner(),
                bluetoothGatt -> bleHelper.readBatteryLevel(bluetoothGatt, new BleHelper.GattReadListener() {
                    @Override
                    public void onSuccess(String value) {
                        Log.i(TAG, "Battery level " + value);
                        viewModel.deviceBinder.setValue(new HomeViewModel.DeviceBinder(
                                bluetoothGatt.getDevice(),
                                value
                        ));
                    }

                    @Override
                    public void onFail(String error) {
                        Log.i(TAG, error);
                    }
                }));
    }

    @Override
    public void onStop() {
        super.onStop();
        BluetoothGatt gatt = viewModel.selectedGatt.getValue();
        if (gatt != null) {
            gatt.disconnect();
        }
    }
}