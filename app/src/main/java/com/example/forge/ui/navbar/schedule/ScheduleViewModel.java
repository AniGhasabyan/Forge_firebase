package com.example.forge.ui.navbar.schedule;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ScheduleViewModel  extends ViewModel {

    private final MutableLiveData<String> mText;

    public ScheduleViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is training schedule fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }

}