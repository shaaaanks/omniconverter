package com.omniconverter.app.ui.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.text.InputType;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.LinearLayout;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.omniconverter.app.R;
import com.omniconverter.app.storage.ConversionEntity;
import com.omniconverter.app.storage.DatabaseManager;
import com.omniconverter.app.ui.adapter.ConverterListAdapter;
import com.omniconverter.app.ui.adapter.SelectedFilesAdapter;
import com.omniconverter.app.ui.helper.FileDownloadHelper;
import com.omniconverter.app.worker.ConversionWorker;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ConverterDetailFragment extends Fragment {
    private Uri selectedUri;
    private String converterType;
    private String selectedFormat;
    private ActivityResultLauncher<String> filePickerLauncher;
    private ActivityResultLauncher<String[]> multipleFilePickerLauncher;
    private TextView selectedFileView;
    private TextView selectedFileTypeView;
    private Spinner formatSpinner;
    private ProgressBar progressBar;
    private Button convertButton;
    private Button downloadButton;
    private UUID currentWorkId;
    private String outputUriString;
    private SelectedFilesAdapter selectedFilesAdapter;

    private List<Uri> selectedUris = new ArrayList<>();

    public static ConverterDetailFragment newInstance(ConverterListAdapter.ConverterItem item) {
        Bundle args = new Bundle();
        args.putString("type", item.type);
        args.putString("name", item.name);
        ConverterDetailFragment fragment = new ConverterDetailFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_converter_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            converterType = getArguments().getString("type");
            String name = getArguments().getString("name");
            view.findViewById(R.id.converter_title).setVisibility(View.VISIBLE);
            ((TextView) view.findViewById(R.id.converter_title)).setText(name);
        }

        selectedFileView = view.findViewById(R.id.selected_file_text);
        selectedFileTypeView = view.findViewById(R.id.selected_file_type_text);
        Button pickFileButton = view.findViewById(R.id.pick_file_button);
        convertButton = view.findViewById(R.id.convert_button);
        downloadButton = view.findViewById(R.id.download_button);
        progressBar = view.findViewById(R.id.conversion_progress);
        formatSpinner = view.findViewById(R.id.format_spinner);

        // Setup file list RecyclerView for PDF_MERGE
        RecyclerView selectedFilesList = view.findViewById(R.id.selected_files_list);
        TextView selectedFilesLabel = view.findViewById(R.id.selected_files_label);

        if ("PDF_MERGE".equalsIgnoreCase(converterType)) {
            selectedFilesList.setVisibility(View.VISIBLE);
            selectedFilesLabel.setVisibility(View.VISIBLE);
            selectedFilesList.setLayoutManager(new LinearLayoutManager(getContext()));

            selectedFilesAdapter = new SelectedFilesAdapter(selectedUris, position -> {
                selectedUris.remove(position);
                selectedFilesAdapter.updateList(selectedUris);
                if (selectedUris.isEmpty()) {
                    selectedFileView.setText("No files selected");
                    selectedFilesLabel.setVisibility(View.GONE);
                    selectedFilesList.setVisibility(View.GONE);
                } else {
                    String fileNames = selectedUris.stream()
                        .map(this::getFileNameFromUri)
                        .collect(Collectors.joining(", "));
                    selectedFileView.setText("Selected: " + fileNames);
                    selectedFilesAdapter.notifyDataSetChanged();
                }
            });
            selectedFilesList.setAdapter(selectedFilesAdapter);
        }

        // Setup format spinner based on converter type
        setupFormatSpinner();

        // Register file picker launcher
        filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedUri = uri;
                    try {
                        getActivity().getContentResolver().takePersistableUriPermission(
                            uri,
                            android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                        );
                    } catch (Exception e) {
                        // Permission already granted or device doesn't support it
                    }
                    String fileName = getFileNameFromUri(uri);
                    selectedFileView.setText("Selected: " + fileName);
                    selectedFileTypeView.setText("File type: " + getFileTypeFromUri(uri));
                    Toast.makeText(getContext(), "File selected: " + fileName, Toast.LENGTH_SHORT).show();
                }
            }
        );

        multipleFilePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.OpenMultipleDocuments(),
            uris -> {
                if (uris != null && !uris.isEmpty()) {
                    selectedUris = new ArrayList<>(uris);
                    selectedUri = uris.get(0);
                    String fileNames = uris.stream()
                        .map(this::getFileNameFromUri)
                        .collect(Collectors.joining(", "));
                    selectedFileView.setText("Selected: " + fileNames);
                    selectedFileTypeView.setText("File type: " + getFileTypeFromUri(uris.get(0)));

                    // Update RecyclerView for PDF_MERGE
                    if ("PDF_MERGE".equalsIgnoreCase(converterType) && selectedFilesAdapter != null) {
                        selectedFilesAdapter.updateList(selectedUris);
                    }

                    Toast.makeText(getContext(), "Files selected: " + fileNames, Toast.LENGTH_SHORT).show();
                }
            }
        );

        pickFileButton.setOnClickListener(v -> {
            if ("PDF_MERGE".equalsIgnoreCase(converterType)) {
                multipleFilePickerLauncher.launch(new String[]{"application/pdf"});
            } else {
                filePickerLauncher.launch("*/*");
            }
        });

        convertButton.setOnClickListener(v -> startConversion());

        downloadButton.setOnClickListener(v -> {
            if (outputUriString != null) {
                String fileName = "converted_" + System.currentTimeMillis() + "." + selectedFormat.toLowerCase();
                FileDownloadHelper.saveToDownloads(getContext(), outputUriString, fileName);
            } else {
                Toast.makeText(getContext(), "No converted file available", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupFormatSpinner() {
        String[] formats = new String[]{"Select format"};

        if ("IMAGE".equalsIgnoreCase(converterType)) {
            formats = new String[]{"PNG", "JPG", "WebP", "BMP"};
            selectedFormat = "PNG";
        } else if ("MP4_TO_MP3".equalsIgnoreCase(converterType)) {
            formats = new String[]{"MP3", "WAV", "AAC"};
            selectedFormat = "MP3";
        } else if ("PDF_MERGE".equalsIgnoreCase(converterType)) {
            formats = new String[]{"PDF"};
            selectedFormat = "PDF";
        } else if ("PDF_SPLIT".equalsIgnoreCase(converterType)) {
            formats = new String[]{"PDF"};
            selectedFormat = "PDF";
        } else if ("DOCX_TO_PDF".equalsIgnoreCase(converterType)) {
            formats = new String[]{"PDF"};
            selectedFormat = "PDF";
        } else if ("OCR".equalsIgnoreCase(converterType)) {
            formats = new String[]{"TXT"};
            selectedFormat = "TXT";
        } else if ("VIDEO_TO_FRAMES".equalsIgnoreCase(converterType)) {
            formats = new String[]{"PNG", "JPG", "WEBP"};
            selectedFormat = "JPG";
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            getContext(),
            android.R.layout.simple_spinner_item,
            formats
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        formatSpinner.setAdapter(adapter);

        // Set default selection
        if (selectedFormat != null) {
            for (int i = 0; i < formats.length; i++) {
                if (formats[i].equals(selectedFormat)) {
                    formatSpinner.setSelection(i);
                    break;
                }
            }
        }

        // Listener for spinner changes
        formatSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                selectedFormat = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });
    }

    private String getFileNameFromUri(Uri uri) {
        String fileName = uri.getLastPathSegment();
        if (fileName != null && fileName.contains(":")) {
            fileName = fileName.substring(fileName.lastIndexOf(":") + 1);
        }
        return fileName != null ? fileName : "selected_file";
    }

    private String getFileTypeFromUri(Uri uri) {
        String type = null;
        try {
            type = getActivity().getContentResolver().getType(uri);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return type != null ? type : "unknown";
    }

    private void startConversion() {
        if (selectedUri == null) {
            Toast.makeText(getContext(), "Please select a file first", Toast.LENGTH_SHORT).show();
            return;
        }

        if (converterType == null) {
            Toast.makeText(getContext(), "Converter type not set", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedFormat == null || selectedFormat.equals("Select format")) {
            Toast.makeText(getContext(), "Please select output format", Toast.LENGTH_SHORT).show();
            return;
        }

        // For video-to-frames, collect fps & quality before starting
        if ("VIDEO_TO_FRAMES".equalsIgnoreCase(converterType)) {
            showVideoFramesParamDialog();
            return;
        }

        enqueueWork(null, null);
    }

    private void showVideoFramesParamDialog() {
        final EditText fpsInput = new EditText(getContext());
        fpsInput.setHint("Frames per second (default 1)");
        fpsInput.setInputType(InputType.TYPE_CLASS_NUMBER);

        final EditText qualityInput = new EditText(getContext());
        qualityInput.setHint("Quality 1-100 (default 90)");
        qualityInput.setInputType(InputType.TYPE_CLASS_NUMBER);

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (getResources().getDisplayMetrics().density * 16);
        layout.setPadding(pad, pad, pad, pad);
        layout.addView(fpsInput);
        layout.addView(qualityInput);

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Video to Frames Options")
            .setMessage("Tip: stay on this screen to view progress. Switching tabs won’t stop the work but you won’t see updates.")
            .setView(layout)
            .setPositiveButton("Start", (d, which) -> {
                Integer fps = null;
                Integer quality = null;
                try {
                    String fpsStr = fpsInput.getText().toString().trim();
                    if (!TextUtils.isEmpty(fpsStr)) fps = Integer.parseInt(fpsStr);
                } catch (Exception ignored) {}
                try {
                    String qStr = qualityInput.getText().toString().trim();
                    if (!TextUtils.isEmpty(qStr)) quality = Integer.parseInt(qStr);
                } catch (Exception ignored) {}
                enqueueWork(fps, quality);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void enqueueWork(Integer fps, Integer quality) {
        try {
            Data.Builder dataBuilder = new Data.Builder()
                .putString(ConversionWorker.KEY_TYPE, converterType)
                .putString(ConversionWorker.KEY_FORMAT, selectedFormat);

            if ("PDF_MERGE".equalsIgnoreCase(converterType)) {
                if (selectedUris != null && !selectedUris.isEmpty()) {
                    String[] allUris = selectedUris.stream().map(Uri::toString).toArray(String[]::new);
                    dataBuilder.putStringArray("file_uris", allUris);
                    dataBuilder.putString(ConversionWorker.KEY_URI, selectedUris.get(0).toString());
                } else if (selectedUri != null) {
                    dataBuilder.putStringArray("file_uris", new String[]{selectedUri.toString()});
                    dataBuilder.putString(ConversionWorker.KEY_URI, selectedUri.toString());
                }
            } else {
                dataBuilder.putString(ConversionWorker.KEY_URI, selectedUri.toString());
            }

            if (fps != null) dataBuilder.putInt("fps", fps);
            if (quality != null) dataBuilder.putInt("quality", quality);

            Data inputData = dataBuilder.build();

            OneTimeWorkRequest conversionRequest = new OneTimeWorkRequest.Builder(ConversionWorker.class)
                .setInputData(inputData)
                .build();

            Toast.makeText(getContext(), "Conversion started. Stay on this tab to monitor progress.", Toast.LENGTH_SHORT).show();

            WorkManager.getInstance(getContext()).getWorkInfoByIdLiveData(conversionRequest.getId())
                .observe(getViewLifecycleOwner(), new Observer<WorkInfo>() {
                    @Override
                    public void onChanged(WorkInfo workInfo) {
                        if (workInfo != null) {
                            if (workInfo.getState() == WorkInfo.State.RUNNING) {
                                progressBar.setVisibility(View.VISIBLE);
                                progressBar.setIndeterminate(true);
                                convertButton.setEnabled(false);
                                downloadButton.setEnabled(false);
                            } else if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                                progressBar.setVisibility(View.GONE);
                                convertButton.setEnabled(true);
                                downloadButton.setEnabled(true);

                                outputUriString = workInfo.getOutputData().getString("outputUri");
                                if (outputUriString != null) {
                                    downloadButton.setVisibility(View.VISIBLE);
                                    Toast.makeText(getContext(), "Conversion successful", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getContext(), "No output file found", Toast.LENGTH_SHORT).show();
                                }
                            } else if (workInfo.getState() == WorkInfo.State.FAILED) {
                                progressBar.setVisibility(View.GONE);
                                convertButton.setEnabled(true);
                                downloadButton.setEnabled(false);
                                Toast.makeText(getContext(), "Conversion failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });

            WorkManager.getInstance(getContext()).enqueue(conversionRequest);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
