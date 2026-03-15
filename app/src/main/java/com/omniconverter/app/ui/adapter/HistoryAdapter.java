package com.omniconverter.app.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.omniconverter.app.R;
import com.omniconverter.app.storage.ConversionEntity;
import com.omniconverter.app.ui.helper.FileDownloadHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {
    private List<ConversionEntity> items = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

    public void setItems(List<ConversionEntity> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_history_item, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        ConversionEntity item = items.get(position);
        holder.bind(item, dateFormat);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class HistoryViewHolder extends RecyclerView.ViewHolder {
        private final TextView typeView;
        private final TextView statusView;
        private final TextView dateView;
        private final TextView messageView;
        private final Button openButton;
        private final Button shareButton;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            typeView = itemView.findViewById(R.id.history_type);
            statusView = itemView.findViewById(R.id.history_status);
            dateView = itemView.findViewById(R.id.history_date);
            messageView = itemView.findViewById(R.id.history_message);
            openButton = itemView.findViewById(R.id.history_open_btn);
            shareButton = itemView.findViewById(R.id.history_share_btn);
        }

        public void bind(ConversionEntity item, SimpleDateFormat dateFormat) {
            typeView.setText(itemView.getContext().getString(R.string.history_type, item.type));
            statusView.setText(itemView.getContext().getString(R.string.history_status, item.status));
            dateView.setText(itemView.getContext().getString(R.string.history_date, dateFormat.format(new Date(item.timestamp))));
            if (item.message != null) {
                messageView.setText(itemView.getContext().getString(R.string.history_message, item.message));
            } else {
                messageView.setText(itemView.getContext().getString(R.string.history_message, itemView.getContext().getString(R.string.history_na)));
            }

            // Show/hide buttons based on status
            if ("SUCCESS".equals(item.status) && item.outputUri != null) {
                openButton.setVisibility(View.VISIBLE);
                shareButton.setVisibility(View.VISIBLE);

                openButton.setOnClickListener(v ->
                        FileDownloadHelper.saveToDownloads(itemView.getContext(), item.outputUri, "converted_" + item.id + ".file")
                );

                shareButton.setOnClickListener(v ->
                        FileDownloadHelper.shareFile(itemView.getContext(), item.outputUri)
                );
            } else {
                openButton.setVisibility(View.GONE);
                shareButton.setVisibility(View.GONE);
            }
        }
    }
}
