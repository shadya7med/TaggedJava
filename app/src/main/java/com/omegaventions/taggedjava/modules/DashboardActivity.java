package com.omegaventions.taggedjava.modules ;

import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.omegaventions.taggedjava.R;
import com.omegaventions.taggedjava.databinding.ActivityDashboardBinding;

public class DashboardActivity extends AppCompatActivity {


    private ActivityDashboardBinding binding;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardBinding.inflate(LayoutInflater.from(this));
        NavHostFragment navHostFragment = (NavHostFragment)
                getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        navController = NavHostFragment.findNavController(navHostFragment);
        setupBottomNav();

        setContentView(binding.getRoot());
    }

    private void setupBottomNav() {
        NavigationUI.setupWithNavController(binding.bottomNavigation, navController);
    }


}