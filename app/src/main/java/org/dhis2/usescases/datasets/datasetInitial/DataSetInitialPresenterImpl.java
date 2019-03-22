package org.dhis2.usescases.datasets.datasetInitial;

import android.os.Bundle;

import org.dhis2.usescases.datasets.dataSetTable.DataSetTableActivity;
import org.dhis2.utils.DateUtils;
import org.hisp.dhis.android.core.period.PeriodType;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class DataSetInitialPresenterImpl implements DataSetInitialContract.DataSetInitialPresenter {

    private CompositeDisposable compositeDisposable;
    private DataSetInitialRepository dataSetInitialRepository;
    private DataSetInitialContract.DataSetInitialView dataSetInitialView;

    public DataSetInitialPresenterImpl(DataSetInitialRepository dataSetInitialRepository) {
        this.dataSetInitialRepository = dataSetInitialRepository;
    }


    @Override
    public void init(DataSetInitialContract.DataSetInitialView dataSetInitialView) {
        this.dataSetInitialView = dataSetInitialView;
        compositeDisposable = new CompositeDisposable();
        compositeDisposable.add(
                dataSetInitialRepository.dataSet()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                dataSetInitialView::setData,
                                Timber::d
                        ));
    }

    @Override
    public void onBackClick() {
        dataSetInitialView.back();
    }

    @Override
    public void onOrgUnitSelectorClick() {
        compositeDisposable.add(
                dataSetInitialRepository.orgUnits()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                data -> dataSetInitialView.showOrgUnitDialog(data),
                                Timber::d
                        )
        );
    }

    @Override
    public void onReportPeriodClick(PeriodType periodType) {
        dataSetInitialView.showPeriodSelector(periodType);
    }

    @Override
    public void onCatOptionClick(String catOptionUid) {
        compositeDisposable.add(
                dataSetInitialRepository.catCombo(catOptionUid)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                data -> dataSetInitialView.showCatComboSelector(catOptionUid, data),
                                Timber::d
                        )
        );
    }

    @Override
    public void onActionButtonClick() {
        Bundle bundle = DataSetTableActivity.getBundle(
                dataSetInitialView.getDataSetUid(),
                dataSetInitialView.getSelectedOrgUnit(),
                dataSetInitialView.getPeriodType(),
                DateUtils.databaseDateFormat().format(dataSetInitialView.getSelectedPeriod()),
                dataSetInitialView.getSelectedCatOptions()
        );
        dataSetInitialView.startActivity(DataSetTableActivity.class, bundle, true, false, null);
    }


    @Override
    public void onDettach() {
        compositeDisposable.clear();
    }

    @Override
    public void displayMessage(String message) {
        dataSetInitialView.displayMessage(message);
    }
}
