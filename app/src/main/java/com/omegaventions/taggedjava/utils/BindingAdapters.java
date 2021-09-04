package com.omegaventions.taggedjava.utils;


import androidx.databinding.BindingAdapter;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BindingAdapters {
    @BindingAdapter("listData")
    public static void setDataList(RecyclerView rv, List list){
        ((ListAdapter)rv.getAdapter()).submitList(list);
    }

}

