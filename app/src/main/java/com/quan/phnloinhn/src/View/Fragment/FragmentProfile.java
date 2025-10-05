package com.quan.phnloinhn.src.View.Fragment;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.quan.phnloinhn.databinding.FragmentProfileBinding;
import com.google.firebase.auth.FirebaseAuth;

public class FragmentProfile extends Fragment {

    private FragmentProfileBinding binding;
    private FirebaseAuth mauth;

    public static FragmentProfile newInstance() {
        return new FragmentProfile();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        mauth = FirebaseAuth.getInstance();

        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String userName = mauth.getCurrentUser().getDisplayName();
        String userEmail = mauth.getCurrentUser().getEmail();

        binding.name.setText(userName);
        binding.email.setText(userEmail);
    }

}