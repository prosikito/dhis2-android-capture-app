package org.dhis2.utils.custom_views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import org.dhis2.BR;
import org.dhis2.R;
import org.hisp.dhis.android.core.common.ValueType;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;


/**
 * QUADRAM. Created by frodriguez on 1/24/2018.
 */

public class YesNoView extends FieldLayout implements RadioGroup.OnCheckedChangeListener {

    private ViewDataBinding binding;

    private RadioGroup radioGroup;
    private RadioButton no;
    private TextView labelView;
    private View clearButton;

    public YesNoView(Context context) {
        super(context);
        init(context);
    }

    public YesNoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public YesNoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @Override
    public void performOnFocusAction() {
        //no action to perform
    }

    public void setValueType(ValueType valueType) {

        if (valueType == ValueType.TRUE_ONLY)
            no.setVisibility(View.GONE);
        else
            no.setVisibility(View.VISIBLE);

    }

    public void setLabel(String label) {
        binding.setVariable(BR.label, label);
        binding.executePendingBindings();
    }

    public void setDescription(String description) {
        binding.setVariable(BR.description, description);
        binding.executePendingBindings();
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {
        // unused
    }

    public void setIsBgTransparent(boolean isBgTransparent) {
        this.isBgTransparent = isBgTransparent;
        setLayout();
    }

    private void setLayout() {
        if (isBgTransparent)
            binding = DataBindingUtil.inflate(inflater, R.layout.yes_no_view_primary, this, true);
        else
            binding = DataBindingUtil.inflate(inflater, R.layout.yes_no_view, this, true);

        radioGroup = findViewById(R.id.radiogroup);
        clearButton = findViewById(R.id.clearSelection);
        no = findViewById(R.id.no);
        labelView = findViewById(R.id.label);
        radioGroup.setOnCheckedChangeListener(this);

    }

    public String getLabel() {
        if (labelView != null)
            return labelView.getText().toString();
        else
            return null;
    }

    public RadioGroup getRadioGroup() {
        return radioGroup;
    }

    public View getClearButton() {
        return clearButton;
    }
}
