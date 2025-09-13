package com.example.phnloinhn.src.View.Fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.phnloinhn.R;
import com.example.phnloinhn.databinding.FragmentHomeBinding;
import com.example.phnloinhn.src.ViewModel.FragmentHomeViewModel;

public class FragmentHome extends Fragment {

    private FragmentHomeBinding binding;
    private FragmentHomeViewModel mViewModel;

    public static FragmentHome newInstance() {
        return new FragmentHome();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(FragmentHomeViewModel.class);

        // Observe kết quả AI
        mViewModel.getResult().observe(getViewLifecycleOwner(), result -> {
            // TODO: update UI, ví dụ hiển thị nhãn và độ tin cậy

        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}