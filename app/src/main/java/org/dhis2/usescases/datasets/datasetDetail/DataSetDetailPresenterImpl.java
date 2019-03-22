package org.dhis2.usescases.datasets.datasetDetail;

import android.annotation.SuppressLint;
import android.os.Bundle;

import org.dhis2.usescases.datasets.datasetInitial.DataSetInitialActivity;
import org.dhis2.utils.Constants;
import org.dhis2.utils.OrgUnitUtils;
import org.dhis2.utils.Period;
import org.hisp.dhis.android.core.category.CategoryComboModel;
import org.hisp.dhis.android.core.category.CategoryOptionComboModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.period.PeriodType;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Date;
import java.util.List;

import androidx.annotation.IntDef;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;


public class DataSetDetailPresenterImpl implements DataSetDetailContract.DataSetDetailPresenter {

    private DataSetDetailRepository dataSetDetailRepository;
    private DataSetDetailContract.DataSetDetailView dataSetDetailView;
    private int lastSearchType;
    private Date fromDate;
    private Date toDate;
    private Period period;
    private List<Date> dates;
    private CompositeDisposable compositeDisposable;
    private List<OrganisationUnitModel> orgUnits;
    private CategoryComboModel mCatCombo;
    private List<String> selectedOrgUnits;
    private PeriodType selectedPeriodType;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({LastSearchType.DATES, LastSearchType.DATE_RANGES})
    public @interface LastSearchType {
        int DATES = 1;
        int DATE_RANGES = 32;
    }

    public DataSetDetailPresenterImpl(DataSetDetailRepository dataSetDetailRepository) {
        this.dataSetDetailRepository = dataSetDetailRepository;
        compositeDisposable = new CompositeDisposable();
    }

    @Override
    public void init(DataSetDetailContract.DataSetDetailView dataSetDetailView) {
        this.dataSetDetailView = dataSetDetailView;
        getOrgUnits(null);
        compositeDisposable.add(
                dataSetDetailView.dataSetPage()
                        .startWith(0)
                        .flatMap(page -> dataSetDetailRepository.dataSetGroups(dataSetDetailView.dataSetUid(), selectedOrgUnits, selectedPeriodType, page))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                dataSetDetailView::setData,
                                Timber::d
                        )
        );
    }

    @Override
    public void onTimeButtonClick() {
        dataSetDetailView.showTimeUnitPicker();
    }

    @Override
    public void onDateRangeButtonClick() {
        dataSetDetailView.showRangeDatePicker();
    }

    @Override
    public void onOrgUnitButtonClick() {
        dataSetDetailView.openDrawer();
    }


    @Override
    public void addDataSet() {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.DATA_SET_UID, dataSetDetailView.dataSetUid());

        dataSetDetailView.startActivity(DataSetInitialActivity.class, bundle, false, false, null);
    }

    @Override
    public void onBackClick() {
        if (dataSetDetailView != null)
            dataSetDetailView.back();
    }

    @Override
    public void onCatComboSelected(CategoryOptionComboModel categoryOptionComboModel, String
            orgUnitQuery) {
        updateFilters(categoryOptionComboModel, orgUnitQuery);
    }

    @Override
    public void clearCatComboFilters(String orgUnitQuery) {
        updateFilters(null, orgUnitQuery);
    }

    @Override
    public void onDataSetClick(String eventId, String orgUnit) {
        // unused
    }

    @Override
    public List<OrganisationUnitModel> getOrgUnits() {
        return this.orgUnits;
    }

    @Override
    public void showFilter() {
        dataSetDetailView.showHideFilter();
    }

    @SuppressLint("CheckResult")
    @Override
    public void getDataSets(Date fromDate, Date toDate, String orgUnitQuery) {
        this.fromDate = fromDate;
        this.toDate = toDate;
        lastSearchType = LastSearchType.DATES;
    }

    @Override
    public void getOrgUnits(Date date) {
        compositeDisposable.add(dataSetDetailRepository.orgUnits()
                .map(orgUnitsResult -> {
                    orgUnits = orgUnitsResult;
                    return OrgUnitUtils.renderTree(dataSetDetailView.getContext(), orgUnits, true);
                })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        treeNode -> dataSetDetailView.addTree(treeNode),
                        throwable -> dataSetDetailView.renderError(throwable.getMessage())
                ));
    }

    private void updateFilters(CategoryOptionComboModel categoryOptionComboModel, String
            orgUnitQuery) {
        CategoryOptionComboModel categoryOptionComboModel1 = categoryOptionComboModel;
        switch (lastSearchType) {
            case LastSearchType.DATES:
                getDataSets(this.fromDate, this.toDate, orgUnitQuery);
                break;
            case LastSearchType.DATE_RANGES:
                getDataSetWithDates(this.dates, this.period, orgUnitQuery);
                break;
            default:
                getDataSetWithDates(null, this.period, orgUnitQuery);
                break;
        }
    }

    @Override
    public void getDataSetWithDates(List<Date> dates, Period period, String orgUnitQuery) {
        this.dates = dates;
        this.period = period;
        lastSearchType = LastSearchType.DATE_RANGES;
        //FIXME cuando haya datos para dataset hay que cambiarlo
        //ahora falla por que se va a hacer la select y no puede
       /* compositeDisposable.add(dataSetDetailRepository.filteredDataSet(programId,"","", categoryOptionComboModel)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        list ->dataSetDetailView.setData(getPeriodFromType(list)),
                        throwable -> dataSetDetailView.renderError(throwable.getMessage())));*/
    }

    @Override
    public void onDettach() {
        compositeDisposable.clear();
    }

    @Override
    public void displayMessage(String message) {
        dataSetDetailView.displayMessage(message);
    }
}
