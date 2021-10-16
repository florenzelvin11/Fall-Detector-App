package org.elvincode.fall_detector_v2.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.elvincode.fall_detector_v2.R;
import org.elvincode.fall_detector_v2.models.HistoryLogDisplay;
import org.elvincode.fall_detector_v2.models.HistoryLogModel;
import org.elvincode.fall_detector_v2.repositories.HistoryLogRepositories;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class HistoryLogRecyclerView extends RecyclerView.Adapter<HistoryLogRecyclerView.ViewHolder>{

    private static final String TAG = "HistoryLogRecyclerView";

    private List<HistoryLogDisplay> mHistoryLogs = new ArrayList<>();
    private Context mContext;

    public HistoryLogRecyclerView(Context mContext) {
        this.mContext = mContext;
    }

    public void setmHistoryLogs(List<HistoryLogDisplay> mHistoryLogs) {
        this.mHistoryLogs = mHistoryLogs;
        notifyDataSetChanged();
    }

    public void addHistoryLogs(HistoryLogDisplay historyLogDisplay){
        mHistoryLogs.add(historyLogDisplay);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        private TableLayout tableLayout;
        private TextView dateTime,fall_state, location;

        public ViewHolder(View itemView) {
            super(itemView);
            tableLayout = itemView.findViewById(R.id.parent);
            dateTime = itemView.findViewById(R.id.txtDateTime);
            fall_state = itemView.findViewById(R.id.txtFallState);
            location = itemView.findViewById(R.id.txtLocation);
        }
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_log_layout_rv, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull HistoryLogRecyclerView.ViewHolder holder, int position) {

        // Set the text of each attributes
        holder.dateTime.setText(mHistoryLogs.get(position).getDateTime());
        holder.fall_state.setText(mHistoryLogs.get(position).getFallState());
        holder.location.setText(mHistoryLogs.get(position).getLocation());
    }

    @Override
    public int getItemCount() {
        return mHistoryLogs.size();
    }
}
