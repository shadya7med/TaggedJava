package com.omegaventions.taggedjava;


import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.jetbrains.annotations.NotNull;

public class Extensions {
    public static void requestPermission(Activity activity, String permission, int requestCode) {
        ActivityCompat.requestPermissions(activity, new String[]{permission}, requestCode);
    }

    public static Boolean hasPermission(@NotNull Context context, String permissionType) {
        return ContextCompat.checkSelfPermission(context, permissionType) ==
                PackageManager.PERMISSION_GRANTED;
    }
}
