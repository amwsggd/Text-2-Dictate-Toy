package com.example.testtest;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SentenceViewModel extends ViewModel {
    private final MutableLiveData<String[]> inputs = new MutableLiveData<>();

    public MutableLiveData<String[]> getInputs() {
        return inputs;
    }

    public void setInputs(String[] inputs) {
        this.inputs.setValue(inputs);
    }
}
