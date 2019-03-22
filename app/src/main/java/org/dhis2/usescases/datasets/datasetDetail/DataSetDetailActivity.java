package org.dhis2.usescases.datasets.datasetDetail;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import org.dhis2.App;
import org.dhis2.R;
import org.dhis2.databinding.ActivityDatasetDetailBinding;
import org.dhis2.usescases.general.ActivityGlobalAbstract;
import org.dhis2.usescases.main.program.OrgUnitHolder;
import org.dhis2.utils.CatComboAdapter;
import org.dhis2.utils.DateUtils;
import org.dhis2.utils.Period;
import org.dhis2.utils.custom_views.RxDateDialog;
import org.hisp.dhis.android.core.category.CategoryComboModel;
import org.hisp.dhis.android.core.category.CategoryOptionComboModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.GravityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DividerItemDecoration;
import io.reactivex.Flowable;
import io.reactivex.processors.PublishProcessor;
import timber.log.Timber;

import static org.dhis2.utils.Period.DAILY;
import static org.dhis2.utils.Period.MONTHLY;
import static org.dhis2.utils.Period.NONE;
import static org.dhis2.utils.Period.WEEKLY;
import static org.dhis2.utils.Period.YEARLY;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class DataSetDetailActivity extends ActivityGlobalAbstract implements DataSetDetailContract.DataSetDetailView {

    private ActivityDatasetDetailBinding binding;
    private ArrayList<Date> chosenDateWeek = new ArrayList<>();
    private ArrayList<Date> chosenDateMonth = new ArrayList<>();
    private ArrayList<Date> chosenDateYear = new ArrayList<>();
    private String dataSetUid;
    private Period currentPeriod = Period.NONE;
    private StringBuilder orgUnitFilter = new StringBuilder();
    private SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
    private SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", Locale.getDefault());
    private Date chosenDateDay = new Date();
    private TreeNode treeNode;
    private AndroidTreeView treeView;
    private boolean isFilteredByCatCombo = false;
    @Inject
    DataSetDetailContract.DataSetDetailPresenter dataSetDetailPresenter;

    private PublishProcessor<Integer> currentPage;
    DataSetDetailAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((App) getApplicationContext()).userComponent().plus(new DataSetDetailModule()).inject(this);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_dataset_detail);

        chosenDateWeek.add(new Date());
        chosenDateMonth.add(new Date());
        chosenDateYear.add(new Date());

        dataSetUid = getIntent().getStringExtra("DATASET_UID");
        binding.setPresenter(dataSetDetailPresenter);

        adapter = new DataSetDetailAdapter(dataSetDetailPresenter);

        currentPage = PublishProcessor.create();
    }

    @Override
    protected void onResume() {
        super.onResume();
        dataSetDetailPresenter.init(this);

        dataSetDetailPresenter.getDataSetWithDates(null, currentPeriod, orgUnitFilter.toString());
    }

    @Override
    protected void onPause() {
        dataSetDetailPresenter.onDettach();
        super.onPause();
        binding.treeViewContainer.removeAllViews();
    }

    @Override
    public void setData(List<DataSetDetailModel> datasets) {
        if (binding.recycler.getAdapter() == null) {
            binding.recycler.setAdapter(adapter);
            binding.recycler.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        }
        adapter.setDatasets(datasets);
    }

    @Override
    public void addTree(TreeNode treeNode) {
        this.treeNode = treeNode;
        binding.treeViewContainer.removeAllViews();
        binding.orgUnitApply.setOnClickListener(view -> apply());
        treeView = new AndroidTreeView(getContext(), treeNode);

        treeView.setDefaultContainerStyle(R.style.TreeNodeStyle, false);
        treeView.setSelectionModeEnabled(true);

        binding.treeViewContainer.addView(treeView.getView());
        if (dataSetDetailPresenter.getOrgUnits().size() < 25)
            treeView.expandAll();

        treeView.setDefaultNodeClickListener((node, value) -> {
            if ((treeView.getSelected().size() == 1 && !node.isSelected()) || treeView.getSelected().size() > 1) {
                ((OrgUnitHolder) node.getViewHolder()).update();
                binding.buttonOrgUnit.setText(String.format(getString(R.string.org_unit_filter), treeView.getSelected().size()));
            }
        });

        binding.buttonOrgUnit.setText(String.format(getString(R.string.org_unit_filter), treeView.getSelected().size()));
    }

    @Override
    public void openDrawer() {
        if (!binding.drawerLayout.isDrawerOpen(GravityCompat.END))
            binding.drawerLayout.openDrawer(GravityCompat.END);
        else
            binding.drawerLayout.closeDrawer(GravityCompat.END);
    }

    private void setDrawable() {
        Drawable drawable = null;
        switch (currentPeriod) {
            case NONE:
                currentPeriod = DAILY;
                drawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_view_day);
                break;
            case DAILY:
                currentPeriod = WEEKLY;
                drawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_view_week);
                break;
            case WEEKLY:
                currentPeriod = MONTHLY;
                drawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_view_month);
                break;
            case MONTHLY:
                currentPeriod = YEARLY;
                drawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_view_year);
                break;
            case YEARLY:
                currentPeriod = NONE;
                drawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_view_none);
                break;
        }
        binding.buttonTime.setImageDrawable(drawable);
    }

    private String getDailyText() {
        String textToShow = "";
        ArrayList<Date> datesD = new ArrayList<>();
        datesD.add(chosenDateDay);
        if (!datesD.isEmpty())
            textToShow = DateUtils.getInstance().formatDate(datesD.get(0));
        if (!datesD.isEmpty() && datesD.size() > 1) {
            textToShow += "... ";
        }
        // TODO
        dataSetDetailPresenter.getDataSetWithDates(datesD, currentPeriod, orgUnitFilter.toString());
        return textToShow;
    }

    private String getWeeklyText() {
        String textToShow = "";
        if (!chosenDateWeek.isEmpty()) {
            String week = getString(R.string.week);
            SimpleDateFormat weeklyFormat = new SimpleDateFormat("'" + week + "' w", Locale.getDefault());
            textToShow = weeklyFormat.format(chosenDateWeek.get(0)) + ", " + yearFormat.format(chosenDateWeek.get(0));
        }
        if (!chosenDateWeek.isEmpty() && chosenDateWeek.size() > 1) textToShow += "... ";
        // TODO
        dataSetDetailPresenter.getDataSetWithDates(chosenDateWeek, currentPeriod, orgUnitFilter.toString());
        return textToShow;
    }

    private String getMonthlyText() {
        String textToShow = "";
        if (!chosenDateMonth.isEmpty()) {
            String dateFormatted = monthFormat.format(chosenDateMonth.get(0));
            textToShow = dateFormatted.substring(0, 1).toUpperCase() + dateFormatted.substring(1);
        }
        if (!chosenDateMonth.isEmpty() && chosenDateMonth.size() > 1) textToShow += "... ";
        // TODO
        dataSetDetailPresenter.getDataSetWithDates(chosenDateMonth, currentPeriod, orgUnitFilter.toString());
        return textToShow;
    }

    private String getYearlyText() {
        String textToShow = "";
        if (!chosenDateYear.isEmpty())
            textToShow = yearFormat.format(chosenDateYear.get(0));
        if (!chosenDateYear.isEmpty() && chosenDateYear.size() > 1) textToShow += "... ";
        // TODO
        dataSetDetailPresenter.getDataSetWithDates(chosenDateYear, currentPeriod, orgUnitFilter.toString());
        return textToShow;
    }

    private void setText() {
        String textToShow = "";


        switch (currentPeriod) {
            case NONE:
                // TODO
                dataSetDetailPresenter.getDataSetWithDates(null, currentPeriod, orgUnitFilter.toString());
                textToShow = getString(R.string.period);
                break;
            case DAILY:
                textToShow = getDailyText();
                break;
            case WEEKLY:
                textToShow = getWeeklyText();
                break;
            case MONTHLY:
                textToShow = getMonthlyText();
                break;
            case YEARLY:
                textToShow = getYearlyText();
                break;
        }

        binding.buttonPeriodText.setText(textToShow);
    }

    @Override
    public void showTimeUnitPicker() {
        setDrawable();
        setText();
    }

    @SuppressLint({"RxLeakedSubscription", "CheckResult"})
    @Override
    public void showRangeDatePicker() {
        Calendar calendar = Calendar.getInstance();
        calendar.setMinimalDaysInFirstWeek(7);

        String week = getString(R.string.week);
        SimpleDateFormat weeklyFormat = new SimpleDateFormat("'" + week + "' w", Locale.getDefault());

        if (currentPeriod != DAILY && currentPeriod != NONE) {
            new RxDateDialog(getAbstractActivity(), currentPeriod).create().show().subscribe(selectedDates -> {
                        if (!selectedDates.isEmpty()) {
                            showSelectedDatesPicker(weeklyFormat, selectedDates);

                        } else {
                            showNoSelectedDatesPicker(weeklyFormat);
                        }
                    },
                    Timber::d);
        } else if (currentPeriod == DAILY) {
            showDailyPicker(calendar);
        }
    }

    private void showNoSelectedDatesPicker(SimpleDateFormat weeklyFormat) {
        ArrayList<Date> date = new ArrayList<>();
        date.add(new Date());

        String text = "";

        switch (currentPeriod) {
            case WEEKLY:
                text = weeklyFormat.format(date.get(0)) + ", " + yearFormat.format(date.get(0));
                chosenDateWeek = date;
                break;
            case MONTHLY:
                text = monthFormat.format(date.get(0));
                chosenDateMonth = date;
                break;
            case YEARLY:
                text = yearFormat.format(date.get(0));
                chosenDateYear = date;
                break;
            default:
                break;
        }
        binding.buttonPeriodText.setText(text);

        // TODO
        dataSetDetailPresenter.getDataSetWithDates(date, currentPeriod, orgUnitFilter.toString());
    }

    private void showSelectedDatesPicker(SimpleDateFormat weeklyFormat, List<Date> selectedDates) {
        String textToShow;
        if (currentPeriod == WEEKLY) {
            textToShow = weeklyFormat.format(selectedDates.get(0)) + ", " + yearFormat.format(selectedDates.get(0));
            chosenDateWeek = (ArrayList<Date>) selectedDates;
            if (selectedDates.size() > 1)
                textToShow += "... " /*+ weeklyFormat.format(selectedDates.get(1))*/;
        } else if (currentPeriod == MONTHLY) {
            textToShow = monthFormat.format(selectedDates.get(0));
            chosenDateMonth = (ArrayList<Date>) selectedDates;
            if (selectedDates.size() > 1)
                textToShow += "... " /*+ monthFormat.format(selectedDates.get(1))*/;
        } else {
            textToShow = yearFormat.format(selectedDates.get(0));
            chosenDateYear = (ArrayList<Date>) selectedDates;
            if (selectedDates.size() > 1)
                textToShow += "... " /*+ yearFormat.format(selectedDates.get(1))*/;

        }
        binding.buttonPeriodText.setText(textToShow);

        // TODO
        dataSetDetailPresenter.getDataSetWithDates(selectedDates, currentPeriod, orgUnitFilter.toString());
    }

    private void showDailyPicker(Calendar calendar) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(chosenDateDay);
        DatePickerDialog pickerDialog;
        pickerDialog = new DatePickerDialog(getContext(), (datePicker, year, monthOfYear, dayOfMonth) -> {
            calendar.set(year, monthOfYear, dayOfMonth);
            Date[] dates = DateUtils.getInstance().getDateFromDateAndPeriod(calendar.getTime(), currentPeriod);
            ArrayList<Date> day = new ArrayList<>();
            day.add(dates[0]);
            // TODO
            dataSetDetailPresenter.getDataSetWithDates(day, currentPeriod, orgUnitFilter.toString());
            binding.buttonPeriodText.setText(DateUtils.getInstance().formatDate(dates[0]));
            chosenDateDay = dates[0];
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        pickerDialog.show();
    }

    @Override
    public void setCatComboOptions(CategoryComboModel catCombo, List<CategoryOptionComboModel> catComboList) {
        if (catCombo.uid().equals(CategoryComboModel.DEFAULT_UID) || catComboList == null || catComboList.isEmpty()) {
            binding.catCombo.setVisibility(View.GONE);
            binding.catCombo.setVisibility(View.GONE);
        } else {
            binding.catCombo.setVisibility(View.VISIBLE);
            CatComboAdapter adapterAux = new CatComboAdapter(this,
                    R.layout.spinner_layout,
                    R.id.spinner_text,
                    catComboList,
                    catCombo.displayName(),
                    R.color.white_faf);

            binding.catCombo.setVisibility(View.VISIBLE);
            binding.catCombo.setAdapter(adapterAux);

            binding.catCombo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (position == 0) {
                        isFilteredByCatCombo = false;
                        dataSetDetailPresenter.clearCatComboFilters(orgUnitFilter.toString());
                    } else {
                        isFilteredByCatCombo = true;
                        dataSetDetailPresenter.onCatComboSelected(adapterAux.getItem(position - 1), orgUnitFilter.toString());
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    isFilteredByCatCombo = false;
                    dataSetDetailPresenter.clearCatComboFilters(orgUnitFilter.toString());
                }
            });
        }
    }

    @Override
    public void setOrgUnitFilter(StringBuilder orgUnitFilter) {
        this.orgUnitFilter = orgUnitFilter;
    }

    @Override
    public void showHideFilter() {
        binding.filterLayout.setVisibility(binding.filterLayout.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        checkFilterEnabled();
    }

    @Override
    public void apply() {
        binding.drawerLayout.closeDrawers();
        orgUnitFilter = new StringBuilder();
        for (int i = 0; i < treeView.getSelected().size(); i++) {
            orgUnitFilter.append("'");
            orgUnitFilter.append(((OrganisationUnitModel) treeView.getSelected().get(i).getValue()).uid());
            orgUnitFilter.append("'");
            if (i < treeView.getSelected().size() - 1)
                orgUnitFilter.append(", ");
        }


        switch (currentPeriod) {
            case NONE:
                // TODO
                dataSetDetailPresenter.getDataSetWithDates(null, currentPeriod, orgUnitFilter.toString());
                break;
            case DAILY:
                ArrayList<Date> datesD = new ArrayList<>();
                datesD.add(chosenDateDay);
                // TODO
                dataSetDetailPresenter.getDataSetWithDates(datesD, currentPeriod, orgUnitFilter.toString());
                break;
            case WEEKLY:
                // TODO
                dataSetDetailPresenter.getDataSetWithDates(chosenDateWeek, currentPeriod, orgUnitFilter.toString());
                break;
            case MONTHLY:
                // TODO
                dataSetDetailPresenter.getDataSetWithDates(chosenDateMonth, currentPeriod, orgUnitFilter.toString());
                break;
            case YEARLY:
                // TODO
                dataSetDetailPresenter.getDataSetWithDates(chosenDateYear, currentPeriod, orgUnitFilter.toString());
                break;
        }
    }

    @Override
    public void setWritePermission(Boolean canWrite) {
        binding.addDatasetButton.setVisibility(canWrite ? View.VISIBLE : View.GONE);
    }

    @Override
    public Flowable<Integer> dataSetPage() {
        return currentPage;
    }

    @Override
    public String dataSetUid() {
        return dataSetUid;
    }

    private void checkFilterEnabled() {
        if (binding.filterLayout.getVisibility() == View.VISIBLE) {
            binding.filter.setBackgroundColor(getPrimaryColor());
            binding.filter.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_IN);
            binding.filter.setBackgroundResource(0);
        }
        // when filter layout is hidden
        else {
            // not applied period filter
            if (currentPeriod == Period.NONE && areAllOrgUnitsSelected() && !isFilteredByCatCombo) {
                binding.filter.setBackgroundColor(getPrimaryColor());
                binding.filter.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_IN);
                binding.filter.setBackgroundResource(0);
            }
            // applied period filter
            else {
                binding.filter.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.white, getTheme()));
                binding.filter.setColorFilter(getPrimaryColor(), PorterDuff.Mode.SRC_IN);
                binding.filter.setBackgroundResource(R.drawable.white_circle);
            }
        }
    }

    private boolean areAllOrgUnitsSelected() {
        return treeNode != null && treeNode.getChildren().size() == treeView.getSelected().size();
    }

}
