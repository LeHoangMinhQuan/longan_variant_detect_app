package com.example.phnloinhn.src.ViewModel;

import android.app.Application;
import android.graphics.Bitmap;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.phnloinhn.src.ml.MobilenetClassifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;

public class FragmentHomeViewModel extends AndroidViewModel {

    private boolean isClassified = false;

    public FragmentHomeViewModel(@NonNull Application application) {
        super(application);

    }


    @Override
    protected void onCleared() {
        super.onCleared();
    }
}
