package org.dhis2.usescases.reservedValue;


import org.dhis2.BR;
import org.dhis2.databinding.ItemReservedValueBinding;

import androidx.recyclerview.widget.RecyclerView;

public class ReservedValueViewHolder extends RecyclerView.ViewHolder {

    private ItemReservedValueBinding binding;

    public ReservedValueViewHolder(ItemReservedValueBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(ReservedValueContracts.ReservedValuePresenter reservedValuePresenter, ReservedValueModel dataElement) {
        //TODO cambiarlo en el xml tambien
        binding.setVariable(BR.dataElement, dataElement);
        binding.setVariable(BR.presenter, reservedValuePresenter);
    }

}
