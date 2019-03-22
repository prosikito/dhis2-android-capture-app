package org.dhis2.usescases.programStageSelection;

import org.dhis2.utils.Result;
import org.dhis2.utils.RulesUtilsProvider;
import org.hisp.dhis.android.core.program.ProgramStageModel;
import org.hisp.dhis.rules.models.RuleEffect;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * QUADRAM. Created by ppajuelo on 31/10/2017.
 */

public class ProgramStageSelectionPresenterImpl implements ProgramStageSelectionContract.ProgramStageSelectionPresenter {

    private final RulesUtilsProvider ruleUtils;
    private ProgramStageSelectionContract.ProgramStageSelectionView programStageSelectionView;
    private CompositeDisposable compositeDisposable;
    private ProgramStageSelectionRepository programStageSelectionRepository;

    ProgramStageSelectionPresenterImpl(ProgramStageSelectionRepository programStageSelectionRepository, RulesUtilsProvider ruleUtils) {
        this.programStageSelectionRepository = programStageSelectionRepository;
        this.ruleUtils = ruleUtils;
        compositeDisposable = new CompositeDisposable();
    }

    @Override
    public void onBackClick() {
        if (programStageSelectionView != null)
            programStageSelectionView.back();
    }

    @Override
    public void getProgramStages(String programId, @NonNull String uid, @NonNull ProgramStageSelectionContract.ProgramStageSelectionView programStageSelectionView) {
        this.programStageSelectionView = programStageSelectionView;

        Flowable<List<ProgramStageModel>> stagesFlowable = programStageSelectionRepository.enrollmentProgramStages(programId, uid);

        Flowable<Result<RuleEffect>> ruleEffectFlowable = programStageSelectionRepository.calculate().subscribeOn(Schedulers.computation())
                .onErrorReturn(throwable -> Result.failure(new Exception(throwable)));

        // Combining results of two repositories into a single stream.
        Flowable<List<ProgramStageModel>> stageModelsFlowable = Flowable.zip(stagesFlowable, ruleEffectFlowable, this::applyEffects);

        compositeDisposable.add(stageModelsFlowable
                .map(data -> programStageSelectionRepository.objectStyle(data))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        programStageSelectionView::setData,
                        Timber::e));
    }

    private List<ProgramStageModel> applyEffects(List<ProgramStageModel> stageModels, Result<RuleEffect> calcResult) {
        if (calcResult.error() != null) {
            Timber.e(calcResult.error());
            return stageModels;
        }

        Map<String, ProgramStageModel> stageViewModels = toMap(stageModels);

        ruleUtils.applyRuleEffects(stageViewModels, calcResult);

        return new ArrayList<>(stageViewModels.values());
    }

    @NonNull
    private static Map<String, ProgramStageModel> toMap(@NonNull List<ProgramStageModel> stageViewModels) {
        Map<String, ProgramStageModel> map = new LinkedHashMap<>();
        for (ProgramStageModel stageModelModel : stageViewModels) {
            map.put(stageModelModel.uid(), stageModelModel);
        }
        return map;
    }

    @Override
    public void onDettach() {
        compositeDisposable.clear();
    }

    @Override
    public void displayMessage(String message) {
        programStageSelectionView.displayMessage(message);
    }

    @Override
    public void onProgramStageClick(ProgramStageModel programStage) {
        programStageSelectionView.setResult(programStage.uid(), programStage.repeatable(), programStage.periodType());
    }
}