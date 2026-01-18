package com.lq.hid1.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lq.hid1.R;
import com.lq.hid1.bean.BluetoothDeviceItem;

import java.util.List;

public class BluetoothDeviceAdapter extends RecyclerView.Adapter<BluetoothDeviceAdapter.ViewHolder> {

    private List<BluetoothDeviceItem> deviceList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public BluetoothDeviceAdapter(List<BluetoothDeviceItem> deviceList) {
        this.deviceList = deviceList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_bluetooth_device, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BluetoothDeviceItem device = deviceList.get(position);

        holder.deviceName.setText(device.getDeviceName());
        holder.deviceStatus.setText(device.getStatus());
        holder.deviceIcon.setImageResource(device.getImageResId());

        if (!device.isSupported()) {
            holder.deviceStatus.setTextColor(Color.parseColor("#FF5252")); // 红色提示
        } else {
            holder.deviceStatus.setTextColor(Color.parseColor("#757575")); // 灰色提示
        }

        // 点击事件
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return deviceList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView deviceIcon;
        TextView deviceName;
        TextView deviceStatus;

        ViewHolder(View itemView) {
            super(itemView);
            deviceIcon = itemView.findViewById(R.id.device_icon);
            deviceName = itemView.findViewById(R.id.device_name);
            deviceStatus = itemView.findViewById(R.id.device_status);
        }
    }
}