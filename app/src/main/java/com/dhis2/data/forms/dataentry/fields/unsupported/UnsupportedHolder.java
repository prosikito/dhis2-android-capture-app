package com.dhis2.data.forms.dataentry.fields.unsupported;

import android.widget.Button;

import com.dhis2.data.forms.dataentry.fields.FormViewHolder;
import com.dhis2.databinding.FormUnsupportedBinding;


public class UnsupportedHolder extends FormViewHolder {

    private final Button button;

    public UnsupportedHolder(FormUnsupportedBinding binding) {
        super(binding);
        button = binding.formButton;
    }

    @Override
    public void dispose() {

    }


    public void update(UnsupportedViewModel viewModel) {
        button.setText(viewModel.label());
    }
}
