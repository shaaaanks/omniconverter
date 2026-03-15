package com.omniconverter.app.ui.adapter;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.omniconverter.app.R;

import java.io.File;
import java.util.List;

public class SelectedFilesAdapter extends RecyclerView.Adapter<SelectedFilesAdapter.ViewHolder> {
    private List<Uri> fileList;
    private OnFileRemoveListener removeListener;

    public interface OnFileRemoveListener {
        void onFileRemove(int position);
    }

    public SelectedFilesAdapter(List<Uri> fileList, OnFileRemoveListener removeListener) {
        this.fileList = fileList;
        this.removeListener = removeListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_selected_file_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Uri fileUri = fileList.get(position);
        String fileName = getFileNameFromUri(fileUri);

        holder.fileNameView.setText((position + 1) + ". " + fileName);
        holder.fileSizeView.setText("URI: " + fileUri.toString().substring(0, Math.min(50, fileUri.toString().length())) + "...");

        holder.removeButton.setOnClickListener(v -> {
            if (removeListener != null) {
                removeListener.onFileRemove(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return fileList.size();
    }

    private String getFileNameFromUri(Uri uri) {
        String fileName = uri.getLastPathSegment();
        if (fileName != null && fileName.contains(":")) {
            fileName = fileName.substring(fileName.lastIndexOf(":") + 1);
        }
        return fileName != null ? fileName : "unknown_file";
    }

    public void updateList(List<Uri> newList) {
        this.fileList = newList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView fileNameView;
        TextView fileSizeView;
        Button removeButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            fileNameView = itemView.findViewById(R.id.file_name);
            fileSizeView = itemView.findViewById(R.id.file_size);
            removeButton = itemView.findViewById(R.id.remove_file_btn);
        }
    }
}
