package com.bdtd.ccg.ui;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bdtd.ccg.R;
import com.bdtd.ccg.model.BluetoothDevModel;

import java.util.ArrayList;
import java.util.List;

public class BluetoothDevAdapter extends RecyclerView.Adapter<BluetoothDevAdapter.InnerHolder> {
    private OnBluetoothDevAdapterListener mListener = null;
    private List<BluetoothDevModel> mList = new ArrayList<>();

    @NonNull
    @Override
    public InnerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.bluetooth_dev_item_view, parent, false);
        return new InnerHolder(itemView);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onBindViewHolder(@NonNull InnerHolder holder, int position) {
        BluetoothDevModel bean = mList.get(position);
        if (bean != null) {
            holder.tvDevName.setText(bean.getDev().getName());
            holder.clDev.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onItemClick(bean);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        if (mList != null) {
            return mList.size();
        }
        return 0;
    }

    public void addData(BluetoothDevModel device) {
        boolean isContain = false; // mList中是否包含device
        for (int i = 0; i < mList.size(); i++) {
            if (mList.get(i).getDev().getAddress().equals(device.getDev().getAddress())) {
                isContain = true;
            }
        }
        if (!isContain) {
            mList.add(device);
            notifyDataSetChanged();
        }
    }

    public void addData(List<BluetoothDevModel> devices) {
        for (int i = 0; i < devices.size(); i++) {
            boolean isContain = false; // mList中是否包含device
            for (int j = 0; j < mList.size(); j++) {
                if (mList.get(j).getDev().getAddress().equals(devices.get(i).getDev().getAddress())) {
                    isContain = true;
                }
            }
            if (!isContain) {
                mList.add(devices.get(i));
            }
        }
        notifyDataSetChanged();
    }

    public void removeData(BluetoothDevModel device) {
        mList.remove(device);
        notifyDataSetChanged();
    }

    public void resetData() {
        for (int i = 0; i < mList.size(); i++) {
            mList.get(i).setConnected(false);
        }
        notifyDataSetChanged();
    }

    public void clear() {
        mList.clear();
        notifyDataSetChanged();
    }

    public void release() {
        mListener = null;
    }

    class InnerHolder extends RecyclerView.ViewHolder {
        private TextView tvDevName;
        private ConstraintLayout clDev;

        public InnerHolder(@NonNull View itemView) {
            super(itemView);
            tvDevName = itemView.findViewById(R.id.tv_dev_name);
            clDev = itemView.findViewById(R.id.cl_dev);
        }
    }

    void setListener(OnBluetoothDevAdapterListener listener) {
        mListener = listener;
    }

    interface OnBluetoothDevAdapterListener {

        void onItemClick(BluetoothDevModel device);

    }
}
