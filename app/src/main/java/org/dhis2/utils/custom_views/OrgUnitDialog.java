package org.dhis2.utils.custom_views;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import org.dhis2.R;
import org.dhis2.databinding.DialogOrgunitBinding;
import org.dhis2.usescases.main.program.OrgUnitHolder;
import org.dhis2.utils.OrgUnitUtils;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;

/**
 * QUADRAM. Created by ppajuelo on 21/05/2018.
 */

public class OrgUnitDialog extends DialogFragment {
    DialogOrgunitBinding binding;
    private AndroidTreeView treeView;
    private boolean isMultiSelection;
    private static OrgUnitDialog instance;
    private View.OnClickListener possitiveListener;
    private View.OnClickListener negativeListener;
    private String title;
    private List<OrganisationUnitModel> myOrgs;
    private Context context;

    public static OrgUnitDialog getInstance() {
        if (instance == null) {
            instance = new OrgUnitDialog();
        }
        return instance;
    }

    public OrgUnitDialog() {
        instance = null;
        isMultiSelection = false;
        possitiveListener = null;
        negativeListener = null;
        title = null;
        myOrgs = null;


    }

    public OrgUnitDialog setPossitiveListener(View.OnClickListener listener) {
        this.possitiveListener = listener;
        return this;
    }

    public OrgUnitDialog setNegativeListener(View.OnClickListener listener) {
        this.negativeListener = listener;
        return this;
    }

    public OrgUnitDialog setMultiSelection(boolean multiSelection) {
        isMultiSelection = multiSelection;
        return this;
    }

    public OrgUnitDialog setTitle(String title) {
        this.title = title;
        return this;
    }

    public OrgUnitDialog setOrgUnits(List<OrganisationUnitModel> orgUnits) {
        this.myOrgs = orgUnits;
        return this;
    }

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (dialog.getWindow() != null) {
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_orgunit, container, false);

        binding.title.setText(title);
        binding.acceptButton.setOnClickListener(possitiveListener);
        binding.clearButton.setOnClickListener(negativeListener);
        renderTree(myOrgs);

        return binding.getRoot();
    }

    public boolean isMultiSelection() {
        return isMultiSelection;
    }

    private void renderTree(@NonNull List<OrganisationUnitModel> myOrgs) {

        binding.treeContainer.removeAllViews();
        treeView = new AndroidTreeView(getContext(), OrgUnitUtils.renderTree(context, myOrgs, isMultiSelection));
        treeView.deselectAll();
        treeView.setDefaultContainerStyle(R.style.TreeNodeStyle, false);
        treeView.setSelectionModeEnabled(true);
        binding.treeContainer.addView(treeView.getView());
        if (myOrgs.size() < 30)
            treeView.expandAll();

        treeView.setDefaultNodeClickListener((node, value) -> {
            for (TreeNode treeNode : node.getViewHolder().getTreeView().getSelected())
                ((OrgUnitHolder) treeNode.getViewHolder()).update();
            ((OrgUnitHolder) node.getViewHolder()).update();
        });
    }

    public String getSelectedOrgUnit() {
        return treeView.getSelected() != null && !treeView.getSelected().isEmpty() ? ((OrganisationUnitModel) treeView.getSelected().get(0).getValue()).uid() : "";
    }

    public String getSelectedOrgUnitName() {
        return treeView.getSelected() != null && !treeView.getSelected().isEmpty() ? ((OrganisationUnitModel) treeView.getSelected().get(0).getValue()).displayName() : "";
    }

    public OrganisationUnitModel getSelectedOrgUnitModel() {
        return ((OrganisationUnitModel) treeView.getSelected().get(0).getValue());
    }

    @Override
    public void dismiss() {
        destroyInstance();
        super.dismiss();
    }

    private static void destroyInstance() {
        instance = null;
    }
}
