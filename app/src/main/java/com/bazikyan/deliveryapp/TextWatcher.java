package com.bazikyan.deliveryapp;

import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class TextWatcher implements android.text.TextWatcher {

    View v;
    EditText[] edList;

    public TextWatcher(EditText[] edList, Button v) {
        this.v = v;
        this.edList = edList;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {}

    @Override
    public void afterTextChanged(Editable s) {
        for (EditText editText : edList) {
            if (editText.getText().toString().trim().length() <= 0) {
                v.setEnabled(false);
                break;
            }
            else v.setEnabled(true);
        }
    }
}