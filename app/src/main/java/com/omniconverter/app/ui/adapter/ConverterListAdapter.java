package com.omniconverter.app.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.omniconverter.app.R;

import java.util.Arrays;
import java.util.List;

public class ConverterListAdapter extends RecyclerView.Adapter<ConverterListAdapter.ConverterViewHolder> {
    private List<ConverterItem> converters;
    private OnConverterClickListener listener;

    public interface OnConverterClickListener {
        void onConverterClick(ConverterItem item);
    }

    public static class ConverterItem {
        public String name;
        public String description;
        public String type;

        public ConverterItem(String name, String description, String type) {
            this.name = name;
            this.description = description;
            this.type = type;
        }
    }

    public ConverterListAdapter(OnConverterClickListener listener) {
        this.listener = listener;
        this.converters = Arrays.asList(
            new ConverterItem("Image Converter", "Convert between image formats (JPG, PNG, WebP, BMP)", "IMAGE"),
            new ConverterItem("MP4 to MP3", "Extract audio from MP4 videos", "MP4_TO_MP3"),
            new ConverterItem("PDF Merge", "Combine multiple PDFs", "PDF_MERGE"),
            new ConverterItem("PDF Split", "Split PDF files", "PDF_SPLIT"),
            new ConverterItem("OCR", "Extract text from images", "OCR"),
            new ConverterItem("Word to PDF", "Convert DOCX to PDF", "DOCX_TO_PDF"),
            new ConverterItem("Video to Frames", "Extract high-quality frames from video", "VIDEO_TO_FRAMES")
        );
    }

    @NonNull
    @Override
    public ConverterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_converter_item, parent, false);
        return new ConverterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConverterViewHolder holder, int position) {
        ConverterItem item = converters.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return converters.size();
    }

    public static class ConverterViewHolder extends RecyclerView.ViewHolder {
        private TextView titleView;
        private TextView descView;

        public ConverterViewHolder(@NonNull View itemView) {
            super(itemView);
            titleView = itemView.findViewById(R.id.converter_title);
            descView = itemView.findViewById(R.id.converter_desc);
        }

        public void bind(ConverterItem item, OnConverterClickListener listener) {
            titleView.setText(item.name);
            descView.setText(item.description);
            itemView.setOnClickListener(v -> listener.onConverterClick(item));
        }
    }
}
