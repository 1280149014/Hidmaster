package com.lq.hid1.adapter;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lq.hid1.R;

import java.util.List;

public class DeviceListAdapter extends RecyclerView.Adapter<DeviceListAdapter.DeviceViewHolder> {

    private final Context mContext;
    private final List<BluetoothDevice> mDeviceList;
    private final OnDeviceClickListener mListener;
    // 记录选中的设备位置
    private int mSelectedPosition = -1;

    // 点击事件回调接口
    public interface OnDeviceClickListener {
        void onDeviceClick(BluetoothDevice deviceName, int position);
    }

    public DeviceListAdapter(Context context, List<BluetoothDevice> deviceList, OnDeviceClickListener listener) {
        this.mContext = context;
        this.mDeviceList = deviceList;
        this.mListener = listener;
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_device, parent, false);
        return new DeviceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, @SuppressLint("RecyclerView") int position) {
        BluetoothDevice device = mDeviceList.get(position);
        if (device != null && device.getName() != null) {
            holder.tvDeviceName.setText(device.getName());
        }
        // 列表项点击事件
        holder.itemView.setOnClickListener(v -> {
            mSelectedPosition = position;
            notifyDataSetChanged(); // 刷新列表更新选中状态
            mListener.onDeviceClick(device, position);
        });
    }

    @Override
    public int getItemCount() {
        return mDeviceList == null ? 0 : mDeviceList.size();
    }

    // 视图持有者
    static class DeviceViewHolder extends RecyclerView.ViewHolder {
        ImageView typeIcon;
        TextView tvDeviceName;

        public DeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            typeIcon = itemView.findViewById(R.id.cb_device);
            tvDeviceName = itemView.findViewById(R.id.tv_device_name);
        }
    }
}