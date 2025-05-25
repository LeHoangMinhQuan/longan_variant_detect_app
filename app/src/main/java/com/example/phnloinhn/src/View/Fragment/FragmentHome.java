package com.example.phnloinhn.src.View.Fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.phnloinhn.R;
import com.example.phnloinhn.src.ViewModel.FragmentHomeViewModel;

public class FragmentHome extends Fragment {

    private FragmentHomeViewModel mViewModel;

    public static FragmentHome newInstance() {
        return new FragmentHome();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

}