package org.dhis2.usescases.programStageSelection;

import org.dhis2.data.tuples.Pair;
import org.dhis2.usescases.general.AbstractActivityContracts;
import org.hisp.dhis.android.core.common.ObjectStyleModel;
import org.hisp.dhis.android.core.period.PeriodType;
import org.hisp.dhis.android.core.program.ProgramStageModel;

import java.util.List;

import androidx.annotation.NonNull;

public class ProgramStageSelectionContract {

    public interface ProgramStageSelectionView extends AbstractActivityContracts.View {

        void setData(List<Pair<ProgramStageModel, ObjectStyleModel>> programStageModels);

        void setResult(String programStageUid, boolean repeatable, PeriodType periodType);
    }

    public interface ProgramStageSelectionPresenter extends AbstractActivityContracts.Presenter {

        void onBackClick();

        void getProgramStages(String programId, @NonNull String programUid, @NonNull ProgramStageSelectionView programStageSelectionView);

        void onProgramStageClick(ProgramStageModel programStage);
    }
}
