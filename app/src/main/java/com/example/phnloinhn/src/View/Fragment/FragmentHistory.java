package com.example.phnloinhn.src.View.Fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.phnloinhn.R;
import com.example.phnloinhn.src.View.ActivityMain;
import com.example.phnloinhn.src.View.HistoryAdapter;
import com.example.phnloinhn.src.ViewModel.SharedViewModel;

public class FragmentHistory extends Fragment {
    private SharedViewModel viewModel;
    private HistoryAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new HistoryAdapter(getContext());
        recyclerView.setAdapter(adapter);

        // Get shared ViewModel
        viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        // Observe history list
        viewModel.getHistoryList().observe(getViewLifecycleOwner(), histories -> {
            if (histories != null) {
                adapter.submitList(histories); // DiffUtil handles changes
            }
        });

        return view;
    }
}