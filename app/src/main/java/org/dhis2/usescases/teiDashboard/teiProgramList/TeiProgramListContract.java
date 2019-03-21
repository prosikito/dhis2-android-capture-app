package org.dhis2.usescases.teiDashboard.teiProgramList;

import org.dhis2.usescases.general.AbstractActivityContracts;
import org.dhis2.usescases.main.program.ProgramViewModel;

import java.util.List;

import androidx.annotation.NonNull;

/**
 * QUADRAM. Created by Cristian on 13/02/2017.
 */

public class TeiProgramListContract {

    public interface TeiProgramListView extends AbstractActivityContracts.View {
        void setActiveEnrollments(List<EnrollmentViewModel> enrollments);

        void setOtherEnrollments(List<EnrollmentViewModel> enrollments);

        void setPrograms(List<ProgramViewModel> programs);

        void goToEnrollmentScreen(String enrollmentUid, String programUid);

        void changeCurrentProgram(String program);
    }

    public interface TeiProgramListPresenter extends AbstractActivityContracts.Presenter {
        void init(TeiProgramListView teiProgramListView);

        void onBackClick();

        void onEnrollClick(ProgramViewModel program);

        void onActiveEnrollClick(EnrollmentViewModel enrollmentModel);

        void onUnselectEnrollment();

        String getProgramColor(String uid);
    }

    public interface TeiProgramListInteractor extends AbstractActivityContracts.Interactor {
        void init(TeiProgramListView mview, String trackedEntityId);

        void enroll(String programUid, String uid);

        String getProgramColor(@NonNull String programUid);
    }
}
