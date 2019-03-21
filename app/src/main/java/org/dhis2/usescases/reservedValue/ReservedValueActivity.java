package org.dhis2.usescases.reservedValue;

import android.os.Bundle;

import org.dhis2.App;
import org.dhis2.BR;
import org.dhis2.R;
import org.dhis2.databinding.ActivityReservedValueBinding;
import org.dhis2.usescases.general.ActivityGlobalAbstract;

import java.util.List;

import javax.inject.Inject;

import androidx.databinding.DataBindingUtil;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class ReservedValueActivity extends ActivityGlobalAbstract implements ReservedValueContracts.ReservedValueView {

    private ActivityReservedValueBinding reservedBinding;
    private ReservedValueAdapter adapter;
    @Inject
    ReservedValueContracts.ReservedValuePresenter reservedValuePresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((App) getApplicationContext()).userComponent().plus(new ReservedValueModule()).inject(this);
        super.onCreate(savedInstanceState);

        reservedBinding = DataBindingUtil.setContentView(this, R.layout.activity_reserved_value);
        reservedBinding.setVariable(BR.presenter, reservedValuePresenter);
        adapter = new ReservedValueAdapter(reservedValuePresenter);
    }

    @Override
    public void setDataElements(List<ReservedValueModel> reservedValueModels) {
        if (reservedBinding.recycler.getAdapter() == null) {
            reservedBinding.recycler.setAdapter(adapter);
        }
        adapter.setDataElements(reservedValueModels);
    }

    @Override
    protected void onResume() {
        super.onResume();
        reservedValuePresenter.init(this);
    }

    @Override
    public void onBackClick() {
        super.onBackPressed();
    }

    @Override
    public void refreshAdapter() {
        adapter.notifyDataSetChanged();
    }
}
