package com.omniconverter.app.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.omniconverter.app.R;
import com.omniconverter.app.storage.DatabaseManager;
import com.omniconverter.app.ui.adapter.HistoryAdapter;

public class HistoryFragment extends Fragment {
    private RecyclerView recyclerView;
    private HistoryAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        try {
            return inflater.inflate(R.layout.fragment_history, container, false);
        } catch (Exception e) {
            android.util.Log.e("HistoryFragment", "Error inflating fragment_history layout: ", e);
            android.widget.LinearLayout fallback = new android.widget.LinearLayout(getContext());
            fallback.setOrientation(android.widget.LinearLayout.VERTICAL);
            android.widget.TextView errorText = new android.widget.TextView(getContext());
            errorText.setText("Error loading history: " + e.getMessage());
            errorText.setTextColor(android.graphics.Color.RED);
            fallback.addView(errorText);
            return fallback;
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            recyclerView = view.findViewById(R.id.history_recycler);
            if (recyclerView == null) {
                android.util.Log.e("HistoryFragment", "recyclerView is null - R.id.history_recycler not found");
                return;
            }
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

            adapter = new HistoryAdapter();
            recyclerView.setAdapter(adapter);

            loadHistory();
        } catch (Exception e) {
            android.util.Log.e("HistoryFragment", "Error in onViewCreated: ", e);
        }
    }

    private void loadHistory() {
        try {
            new Thread(() -> {
                try {
                    var items = DatabaseManager.getInstance(getContext()).getAllConversions();
                    getActivity().runOnUiThread(() -> {
                        try {
                            adapter.setItems(items);
                        } catch (Exception e) {
                            android.util.Log.e("HistoryFragment", "Error setting adapter items: ", e);
                        }
                    });
                } catch (Exception e) {
                    android.util.Log.e("HistoryFragment", "Error loading history from database: ", e);
                }
            }).start();
        } catch (Exception e) {
            android.util.Log.e("HistoryFragment", "Error in loadHistory: ", e);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            loadHistory();
        } catch (Exception e) {
            android.util.Log.e("HistoryFragment", "Error in onResume: ", e);
        }
    }
}


