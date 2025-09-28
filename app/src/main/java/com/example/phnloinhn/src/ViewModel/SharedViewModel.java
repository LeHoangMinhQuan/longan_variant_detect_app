package com.example.phnloinhn.src.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.phnloinhn.src.Model.LonganVariant;

public class SharedViewModel extends ViewModel {
    private final MutableLiveData<LonganVariant> selectedVariant = new MutableLiveData<>(null);

    public LiveData<LonganVariant> getSelectedVariant() {
        return selectedVariant;
    }

    public void setSelectedVariant(LonganVariant variant) {
        selectedVariant.setValue(variant);
    }
}

