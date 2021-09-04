package com.omegaventions.taggedjava.utils ;

import android.view.View;

import androidx.databinding.BindingConversion;

import kotlin.jvm.JvmStatic;


public class BindingConverters {
    @BindingConversion
    @JvmStatic
    public static int booleanToVisibility(Boolean isVisible) {
        if (isVisible) return View.VISIBLE;
        else return View.GONE;
    }
}
