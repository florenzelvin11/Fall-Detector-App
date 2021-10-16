package org.elvincode.fall_detector_v2.adapters;


import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.elvincode.fall_detector_v2.MainActivity;
import org.elvincode.fall_detector_v2.R;
import org.elvincode.fall_detector_v2.models.DeviceInfoModel;
import org.jetbrains.annotations.NotNull;

import java.util.List;


public class DeviceListAdapter extends RecyclerView.Adapter<DeviceListAdapter.ViewHolder>{

    private Context context;
    private List<Object> deviceList;

    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView textName, textAddress;
        LinearLayout linearLayout;

        public ViewHolder(View v){
            super(v);
            textName = v.findViewById(R.id.txtViewDeviceName);
            textAddress = v.findViewById(R.id.txtViewDeviceAddress);
            linearLayout = v.findViewById(R.id.linearLayoutDeviceInfo);
        }
    }

    public DeviceListAdapter(Context context, List<Object> deviceList){
        this.context = context;
        this.deviceList = deviceList;
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.device_info_layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull DeviceListAdapter.ViewHolder holder, int position) {
        final DeviceInfoModel deviceInfoModel = (DeviceInfoModel) deviceList.get(position);
        holder.textName.setText(deviceInfoModel.getDeviceName());
        holder.textAddress.setText(deviceInfoModel.getDeviceHardwareAddress());

        // When a device is selected
        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent  = new Intent(context.getApplicationContext(), MainActivity.class);
                // Send device details to the MainActivity
                intent.putExtra("deviceName", deviceInfoModel.getDeviceName());
                intent.putExtra("deviceAddress", deviceInfoModel.getDeviceHardwareAddress());

                // Call MainActivity
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return deviceList.size();
    }
    
}