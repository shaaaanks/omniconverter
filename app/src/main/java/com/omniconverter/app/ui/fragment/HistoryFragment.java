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
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.history_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new HistoryAdapter();
        recyclerView.setAdapter(adapter);

        loadHistory();
    }

    private void loadHistory() {
        new Thread(() -> {
            var items = DatabaseManager.getInstance(getContext()).getAllConversions();
            getActivity().runOnUiThread(() -> adapter.setItems(items));
        }).start();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadHistory();
    }
}


