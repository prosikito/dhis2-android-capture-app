package org.dhis2.usescases.teiDashboard.teiProgramList;

import org.dhis2.R;
import org.dhis2.usescases.main.program.ProgramViewModel;

/**
 * QUADRAM. Created by Cristian on 06/03/2018.
 */

public class TeiProgramListPresenterImpl implements TeiProgramListContract.TeiProgramListPresenter {

    private TeiProgramListContract.TeiProgramListView teiProgramListView;
    private final TeiProgramListContract.TeiProgramListInteractor teiProgramListInteractor;
    private String teiUid;

    TeiProgramListPresenterImpl(TeiProgramListContract.TeiProgramListInteractor teiProgramListInteractor, String trackedEntityId) {
        this.teiProgramListInteractor = teiProgramListInteractor;
        this.teiUid = trackedEntityId;

    }

    @Override
    public void init(TeiProgramListContract.TeiProgramListView teiProgramListView) {
        this.teiProgramListView = teiProgramListView;
        teiProgramListInteractor.init(teiProgramListView, teiUid);
    }

    @Override
    public void onBackClick() {
        teiProgramListView.back();
    }

    @Override
    public void onEnrollClick(ProgramViewModel program) {
        if (program.accessDataWrite())
            teiProgramListInteractor.enroll(program.id(), teiUid);
        else
            teiProgramListView.displayMessage(teiProgramListView.getContext().getString(R.string.search_access_error));
    }

    @Override
    public void onActiveEnrollClick(EnrollmentViewModel enrollmentModel) {
        teiProgramListView.changeCurrentProgram(enrollmentModel.programUid());
    }

    @Override
    public String getProgramColor(String programUid) {
        return teiProgramListInteractor.getProgramColor(programUid);
    }

    @Override
    public void onUnselectEnrollment() {
        teiProgramListView.changeCurrentProgram(null);
    }

    @Override
    public void onDettach() {
        teiProgramListInteractor.onDettach();
    }

    @Override
    public void displayMessage(String message) {
        teiProgramListView.displayMessage(message);
    }
}
