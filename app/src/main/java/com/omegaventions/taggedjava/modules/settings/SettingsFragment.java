package com.omegaventions.taggedjava.modules.settings;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.omegaventions.taggedjava.databinding.FragmentSettingsBinding;


public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private SettingsViewModel viewModel ;

    public SettingsFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentSettingsBinding.inflate(
                LayoutInflater.from(requireActivity()),
                container,
                false
        );
        viewModel = new ViewModelProvider(this).get(SettingsViewModel.class);
        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(getViewLifecycleOwner());
        return binding.getRoot();
    }


}