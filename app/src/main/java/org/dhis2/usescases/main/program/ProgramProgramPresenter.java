package org.dhis2.usescases.main.program;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.unnamed.b.atv.model.TreeNode;

import org.dhis2.R;
import org.dhis2.data.tuples.Pair;
import org.dhis2.data.tuples.Trio;
import org.dhis2.usescases.datasets.datasetDetail.DataSetDetailActivity;
import org.dhis2.usescases.programEventDetail.ProgramEventDetailActivity;
import org.dhis2.usescases.searchTrackEntity.SearchTEActivity;
import org.dhis2.utils.ColorUtils;
import org.dhis2.utils.Constants;
import org.dhis2.utils.OrgUnitUtils;
import org.dhis2.utils.Period;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.program.ProgramType;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.BackpressureStrategy;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import static android.text.TextUtils.isEmpty;

/**
 * Created by ppajuelo on 18/10/2017.f
 */

public class ProgramProgramPresenter implements ProgramContract.ProgramPresenter {

    private static final String CURRENT_PERIOD = "CURRENT_PERIOD";
    private static final String CHOOSEN_DATE = "CHOOSEN_DATE";
    private ProgramContract.ProgramView programView;
    private final HomeRepository homeRepository;
    private CompositeDisposable compositeDisposable;

    private List<OrganisationUnitModel> myOrgs = new ArrayList<>();
    private FlowableProcessor<Trio> programQueries;

    private FlowableProcessor<Pair<TreeNode, String>> parentOrgUnit;

    ProgramProgramPresenter(HomeRepository homeRepository) {
        this.homeRepository = homeRepository;
    }

    @Override
    public void init(ProgramContract.ProgramView programView) {
        this.programView = programView;
        this.compositeDisposable = new CompositeDisposable();
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        programQueries = PublishProcessor.create();
        parentOrgUnit = PublishProcessor.create();

        compositeDisposable.add(
                programQueries
                        .startWith(Trio.create(null, null, null))
                        .flatMap(datePeriodOrgs -> homeRepository.programModels(
                                (List<Date>) datePeriodOrgs.val0(),
                                (Period) datePeriodOrgs.val1(),
                                (String) datePeriodOrgs.val2(),
                                myOrgs.size()))
                        .subscribeOn(Schedulers.from(executorService))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                programView.swapProgramModelData(),
                                throwable -> programView.renderError(throwable.getMessage())
                        ));

        compositeDisposable.add(
                parentOrgUnit
                        .flatMap(orgUnit -> homeRepository.orgUnits(orgUnit.val1()).toFlowable(BackpressureStrategy.LATEST)
                                .map(this::transformToNode)
                                .map(nodeList -> Pair.create(orgUnit.val0(), nodeList)))
                        .subscribeOn(Schedulers.from(executorService))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                programView.addNodeToTree(),
                                Timber::e
                        ));
    }


    @Override
    public void getProgramsWithDates(ArrayList<Date> dates, Period period) {

        programQueries.onNext(Trio.create(dates, period, orgUnitQuery()));

    }


    @Override
    public void getProgramsOrgUnit(List<Date> dates, Period period, String orgUnitQuery) {
        programQueries.onNext(Trio.create(dates, period, orgUnitQuery));
    }


    @Override
    public void getAllPrograms(String orgUnitQuery) {
        programQueries.onNext(Trio.create(null, null, orgUnitQuery));
    }

    @Override
    public void onExpandOrgUnitNode(TreeNode treeNode, String parentUid) {
        parentOrgUnit.onNext(Pair.create(treeNode, parentUid));
    }

    @Override
    public List<TreeNode> transformToNode(List<OrganisationUnitModel> orgUnits) {
        return OrgUnitUtils.createNode(programView.getContext(), orgUnits, true);
    }

    @Override
    public List<OrganisationUnitModel> getOrgUnits() {
        return myOrgs;
    }

    @Override
    public void dispose() {
        if (!myOrgs.isEmpty())
            myOrgs.clear();
        compositeDisposable.clear();
    }

    @Override
    public void onItemClick(ProgramViewModel programModel, Period currentPeriod) {

        Bundle bundle = new Bundle();
        String idTag;
        if (!isEmpty(programModel.type())) {
            bundle.putString("TRACKED_ENTITY_UID", programModel.type());
        }

        if (programModel.typeName().equals("DataSets"))
            idTag = "DATASET_UID";
        else
            idTag = "PROGRAM_UID";

        bundle.putString(idTag, programModel.id());

        switch (currentPeriod) {
            case NONE:
                bundle.putInt(CURRENT_PERIOD, R.string.period);
                bundle.putSerializable(CHOOSEN_DATE, null);
                break;
            case DAILY:
                bundle.putInt(CURRENT_PERIOD, R.string.DAILY);
                bundle.putSerializable(CHOOSEN_DATE, programView.getChosenDateDay());
                break;
            case WEEKLY:
                bundle.putInt(CURRENT_PERIOD, R.string.WEEKLY);
                bundle.putSerializable(CHOOSEN_DATE, programView.getChosenDateWeek());
                break;
            case MONTHLY:
                bundle.putInt(CURRENT_PERIOD, R.string.MONTHLY);
                bundle.putSerializable(CHOOSEN_DATE, programView.getChosenDateMonth());
                break;
            case YEARLY:
                bundle.putInt(CURRENT_PERIOD, R.string.YEARLY);
                bundle.putSerializable(CHOOSEN_DATE, programView.getChosenDateYear());
                break;
        }

        int programTheme = ColorUtils.getThemeFromColor(programModel.color());
        SharedPreferences prefs = programView.getAbstracContext().getSharedPreferences(
                Constants.SHARE_PREFS, Context.MODE_PRIVATE);
        if (programTheme != -1) {
            prefs.edit().putInt(Constants.PROGRAM_THEME, programTheme).apply();
        } else
            prefs.edit().remove(Constants.PROGRAM_THEME).apply();

        if (programModel.programType().equals(ProgramType.WITH_REGISTRATION.name())) {
            programView.startActivity(SearchTEActivity.class, bundle, false, false, null);
        } else if (programModel.programType().equals(ProgramType.WITHOUT_REGISTRATION.name())) {
            programView.startActivity(ProgramEventDetailActivity.class, bundle, false, false, null);
        } else {
            programView.startActivity(DataSetDetailActivity.class, bundle, false, false, null);
        }
    }

    @Override
    public void onOrgUnitButtonClick() {
        programView.openDrawer();
        if (myOrgs.isEmpty()) {
            programView.orgUnitProgress(true);
            compositeDisposable.add(
                    homeRepository.orgUnits()
                            .subscribeOn(Schedulers.computation())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    data -> {
                                        this.myOrgs = data;
                                        programView.orgUnitProgress(false);
                                        programView.addTree(OrgUnitUtils.renderTree(programView.getContext(), myOrgs, true));
                                    },
                                    throwable -> programView.renderError(throwable.getMessage())));
        }
    }

    @Override
    public void onDateRangeButtonClick() {
        programView.showRageDatePicker();
    }


    @Override
    public void onTimeButtonClick() {
        programView.showTimeUnitPicker();
    }

    @Override
    public void showDescription(String description) {
        programView.showDescription(description);
    }

    private String orgUnitQuery() {
        StringBuilder orgUnitFilter = new StringBuilder();
        for (int i = 0; i < myOrgs.size(); i++) {
            orgUnitFilter.append("'");
            orgUnitFilter.append(myOrgs.get(i).uid());
            orgUnitFilter.append("'");
            if (i < myOrgs.size() - 1)
                orgUnitFilter.append(", ");
        }
        programView.setOrgUnitFilter(orgUnitFilter);
        return orgUnitFilter.toString();
    }
}
