package com.omniconverter.app.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.omniconverter.app.R;
import com.omniconverter.app.ui.adapter.ConverterListAdapter;

public class ConverterListFragment extends Fragment {
    private RecyclerView recyclerView;
    private OnConverterSelectedListener listener;

    public interface OnConverterSelectedListener {
        void onConverterSelected(ConverterListAdapter.ConverterItem item);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_converter_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.converter_list_recycler);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        ConverterListAdapter adapter = new ConverterListAdapter(item -> {
            if (listener != null) {
                listener.onConverterSelected(item);
            }
        });
        recyclerView.setAdapter(adapter);
    }

    public void setListener(OnConverterSelectedListener listener) {
        this.listener = listener;
    }
}
