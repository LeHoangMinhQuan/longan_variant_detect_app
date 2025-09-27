package com.example.phnloinhn.src.View.Fragment;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.phnloinhn.R;
import com.example.phnloinhn.src.View.HistoryAdapter;
import com.example.phnloinhn.src.ViewModel.FragmentHistoryViewModel;

import java.util.Arrays;

public class FragmentHistory extends Fragment {

    private FragmentHistoryViewModel mViewModel;

    public static FragmentHistory newInstance() {
        return new FragmentHistory();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));


        // Sample data
        HistoryAdapter adapter = new HistoryAdapter(Arrays.asList(
                "Item 1", "Item 2", "Item 3", "Item 4", "Item 5"
        ));
        recyclerView.setAdapter(adapter);

        return view;
    }


}