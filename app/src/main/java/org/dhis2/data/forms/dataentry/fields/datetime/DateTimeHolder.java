package org.dhis2.data.forms.dataentry.fields.datetime;

import org.dhis2.BR;
import org.dhis2.data.forms.dataentry.fields.FormViewHolder;
import org.dhis2.data.forms.dataentry.fields.RowAction;
import org.dhis2.databinding.FormDateTextBinding;
import org.dhis2.databinding.FormDateTimeTextBinding;
import org.dhis2.databinding.FormTimeTextBinding;
import org.dhis2.utils.DateUtils;
import org.hisp.dhis.android.core.common.ValueType;

import java.util.Date;

import androidx.databinding.ViewDataBinding;
import io.reactivex.processors.FlowableProcessor;

import static android.text.TextUtils.isEmpty;


/**
 * QUADRAM. Created by frodriguez on 16/01/2018.
 */

public class DateTimeHolder extends FormViewHolder implements OnDateSelected {

    private final FlowableProcessor<RowAction> processor;
    private final FlowableProcessor<Integer> currentPosition;

    private DateTimeViewModel dateTimeViewModel;

    DateTimeHolder(ViewDataBinding binding, FlowableProcessor<RowAction> processor, FlowableProcessor<Integer> currentPosition) {
        super(binding);
        this.processor = processor;
        this.currentPosition = currentPosition;

        if (binding instanceof FormTimeTextBinding) {
            ((FormTimeTextBinding) binding).timeView.setDateListener(this);
        }

        if (binding instanceof FormDateTextBinding) {
            ((FormDateTextBinding) binding).dateView.setDateListener(this);
        }

        if (binding instanceof FormDateTimeTextBinding) {
            ((FormDateTimeTextBinding) binding).dateTimeView.setDateListener(this);
        }

    }

    private void setInitData() {
        if (!isEmpty(dateTimeViewModel.value())) {
            binding.setVariable(BR.initData, dateTimeViewModel.value());
        } else {
            binding.setVariable(BR.initData, null);
        }
    }

    private void setWarnings() {
        if (binding instanceof FormTimeTextBinding)
            ((FormTimeTextBinding) binding).timeView.setWarningOrError(dateTimeViewModel.warning());
        if (binding instanceof FormDateTextBinding)
            ((FormDateTextBinding) binding).dateView.setWarningOrError(dateTimeViewModel.warning());
        if (binding instanceof FormDateTimeTextBinding)
            ((FormDateTimeTextBinding) binding).dateTimeView.setWarningOrError(dateTimeViewModel.warning());
    }

    private void setErrors() {
        if (binding instanceof FormTimeTextBinding)
            ((FormTimeTextBinding) binding).timeView.setWarningOrError(dateTimeViewModel.error());
        if (binding instanceof FormDateTextBinding)
            ((FormDateTextBinding) binding).dateView.setWarningOrError(dateTimeViewModel.error());
        if (binding instanceof FormDateTimeTextBinding)
            ((FormDateTimeTextBinding) binding).dateTimeView.setWarningOrError(dateTimeViewModel.error());
    }

    private void setNoWarningsOrErrors() {
        if (binding instanceof FormTimeTextBinding)
            ((FormTimeTextBinding) binding).timeView.setWarningOrError(null);
        if (binding instanceof FormDateTextBinding)
            ((FormDateTextBinding) binding).dateView.setWarningOrError(null);
        if (binding instanceof FormDateTimeTextBinding)
            ((FormDateTimeTextBinding) binding).dateTimeView.setWarningOrError(null);
    }

    private void setEditable() {
        if (binding instanceof FormTimeTextBinding)
            ((FormTimeTextBinding) binding).timeView.setEditable(dateTimeViewModel.editable());
        if (binding instanceof FormDateTextBinding)
            ((FormDateTextBinding) binding).dateView.setEditable(dateTimeViewModel.editable());
        if (binding instanceof FormDateTimeTextBinding)
            ((FormDateTimeTextBinding) binding).dateTimeView.setEditable(dateTimeViewModel.editable());
    }

    private void setAllowFutureDates() {
        if (binding instanceof FormDateTextBinding)
            ((FormDateTextBinding) binding).dateView.setAllowFutureDates(dateTimeViewModel.allowFutureDate());
        if (binding instanceof FormDateTimeTextBinding)
            ((FormDateTimeTextBinding) binding).dateTimeView.setAllowFutureDates(dateTimeViewModel.allowFutureDate());
    }

    private void setErrorsAndWarnings() {
        if (dateTimeViewModel.warning() != null) {
            setWarnings();

        } else if (dateTimeViewModel.error() != null) {
            setErrors();

        } else {
            setNoWarningsOrErrors();
        }
    }

    public void update(DateTimeViewModel viewModel) {
        this.dateTimeViewModel = viewModel;
        descriptionText = viewModel.description();
        label = new StringBuilder(dateTimeViewModel.label());
        if (dateTimeViewModel.mandatory())
            label.append("*");

        binding.setVariable(BR.label, label.toString());
        binding.setVariable(BR.description, descriptionText);

        setInitData();

        setAllowFutureDates();

        setErrorsAndWarnings();

        setEditable();

        binding.executePendingBindings();
    }

    @Override
    public void onDateSelected(Date date) {
        String dateFormatted = "";
        if (date != null) {
            if (dateTimeViewModel.valueType() == ValueType.DATE)
                dateFormatted = DateUtils.uiDateFormat().format(date);
            else if (dateTimeViewModel.valueType() == ValueType.TIME)
                dateFormatted = DateUtils.timeFormat().format(date);
            else {
                dateFormatted = DateUtils.databaseDateFormatNoMillis().format(date);
            }
        }
        RowAction rowAction = RowAction.create(dateTimeViewModel.uid(), date != null ? dateFormatted : null);
        if (processor != null) {
            processor.onNext(rowAction);
        }

        if (currentPosition != null) {
            currentPosition.onNext(getAdapterPosition());
        }

    }

    @Override
    public void dispose() {
        // unused
    }
}