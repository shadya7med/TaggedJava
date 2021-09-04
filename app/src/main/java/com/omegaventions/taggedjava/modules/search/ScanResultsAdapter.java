package com.omegaventions.taggedjava.modules.search;


import android.bluetooth.le.ScanResult;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.omegaventions.taggedjava.databinding.ListItemScanResultBinding;
import com.omegaventions.taggedjava.modules.search.ScanResultsAdapter.ScanResultViewHolder;

public class ScanResultsAdapter extends ListAdapter<ScanResult, ScanResultViewHolder> {

    ScanResultClickListener scanResultClickListener;

    protected ScanResultsAdapter(ScanResultClickListener scanResultClickListener) {
        super(new ScanResultDiffCallback());
        this.scanResultClickListener = scanResultClickListener;
    }


    @NonNull
    @Override
    public ScanResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return ScanResultViewHolder.from(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull ScanResultViewHolder holder, int position) {
        holder.bind(getItem(position), scanResultClickListener);
    }


    static class ScanResultViewHolder extends RecyclerView.ViewHolder {

        ListItemScanResultBinding binding;

        public ScanResultViewHolder(ListItemScanResultBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        static ScanResultViewHolder from(ViewGroup parent) {
            ListItemScanResultBinding binding = ListItemScanResultBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new ScanResultViewHolder(binding);
        }

        void bind(ScanResult item, ScanResultClickListener clickListener) {
            binding.setScanResult(item);
            binding.setClickListener(clickListener);
            binding.executePendingBindings();
        }
    }


    static class ScanResultDiffCallback extends DiffUtil.ItemCallback<ScanResult> {

        @Override
        public boolean areItemsTheSame(@NonNull ScanResult oldItem, @NonNull ScanResult newItem) {
            return oldItem.getDevice().getAddress().equals(newItem.getDevice().getAddress());
        }

        @Override
        public boolean areContentsTheSame(@NonNull ScanResult oldItem, @NonNull ScanResult newItem) {
            return oldItem.equals(newItem);
        }
    }

    public static class ScanResultClickListener {
        ClickListener clickListener;

        ScanResultClickListener(ClickListener clickListener) {
            this.clickListener = clickListener;
        }

        public void onClick(ScanResult scanResult) {
            clickListener.onCLick(scanResult);
        }
    }

     interface ClickListener {
        void onCLick(ScanResult scanResult);
    }


}