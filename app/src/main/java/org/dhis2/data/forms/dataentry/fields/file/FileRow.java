package org.dhis2.data.forms.dataentry.fields.file;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.fields.Row;
import org.dhis2.databinding.FormButtonBinding;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

/**
 * QUADRAM. Created by ppajuelo on 19/03/2018.
 */

public class FileRow implements Row<FileHolder, FileViewModel> {
    private FormButtonBinding formButtonBinding;
    @NonNull
    private final LayoutInflater inflater;
    private final boolean isBgTransparent;

    public FileRow(@NonNull LayoutInflater layoutInflater, boolean isBgTransparent) {
        this.inflater = layoutInflater;
        this.isBgTransparent = isBgTransparent;
    }

    @NonNull
    @Override
    public FileHolder onCreate(@NonNull ViewGroup parent) {
        formButtonBinding = DataBindingUtil.inflate(inflater, R.layout.form_button, parent, false);
        if (isBgTransparent)
            formButtonBinding.formButton.setTextColor(ContextCompat.getColor(parent.getContext(), R.color.colorPrimary));
        else
            formButtonBinding.formButton.setTextColor(ContextCompat.getColor(parent.getContext(), R.color.colorAccent));

        return new FileHolder(formButtonBinding);
    }

    @Override
    public void onBind(@NonNull FileHolder viewHolder, @NonNull FileViewModel viewModel) {
        formButtonBinding.setLabel(viewModel.label());
    }

    @Override
    public void deAttach(@NonNull FileHolder viewHolder) {
        // unused
    }
}
