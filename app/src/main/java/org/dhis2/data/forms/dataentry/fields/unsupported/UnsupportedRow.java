package org.dhis2.data.forms.dataentry.fields.unsupported;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.fields.Row;
import org.dhis2.databinding.FormUnsupportedBinding;
import org.jetbrains.annotations.NotNull;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

public class UnsupportedRow implements Row<UnsupportedHolder, UnsupportedViewModel> {

    @NonNull
    private final LayoutInflater inflater;


    public UnsupportedRow(@NotNull LayoutInflater layoutInflater) {
        this.inflater = layoutInflater;
    }

    @NonNull
    @Override
    public UnsupportedHolder onCreate(@NonNull ViewGroup parent) {
        FormUnsupportedBinding binding = DataBindingUtil.inflate(inflater, R.layout.form_unsupported, parent, false);
        return new UnsupportedHolder(binding);
    }

    @Override
    public void onBind(@NonNull UnsupportedHolder viewHolder, @NonNull UnsupportedViewModel viewModel) {
        viewHolder.update(viewModel);
    }

    @Override
    public void deAttach(@NonNull UnsupportedHolder viewHolder) {
        viewHolder.dispose();
    }
}
