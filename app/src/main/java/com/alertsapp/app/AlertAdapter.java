package com.alertsapp.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import android.graphics.Color;
import android.util.Log;
public class AlertAdapter extends RecyclerView.Adapter<AlertAdapter.ViewHolder> {

    private List<Alert> alerts;

    public AlertAdapter(List<Alert> alerts) {
        this.alerts = alerts;
    }

    public void setAlerts(List<Alert> alerts) {
        this.alerts = alerts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_alert, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Alert alert = alerts.get(position);

        holder.tvCategory.setText(alert.getCategoryDesc());
        holder.tvTime.setText(alert.getTime());
        holder.tvCity.setText(alert.getCity());
        holder.tvDate.setText(alert.getAlertDate());
        Log.d("AlertAdapter", alert.getCategoryDesc() + " -> " + alert.getAlertType());
        if (alert.getAlertType() == Alert.AlertType.DANGER) {
            holder.itemView.setBackgroundColor(Color.parseColor("#2B0000"));
        } else {
            holder.itemView.setBackgroundColor(Color.parseColor("#002B00"));
        }
    }

    @Override
    public int getItemCount() {
        return alerts != null ? alerts.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategory, tvTime, tvCity, tvDate;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvCity = itemView.findViewById(R.id.tvCity);
            tvDate = itemView.findViewById(R.id.tvDate);
        }
    }
}
