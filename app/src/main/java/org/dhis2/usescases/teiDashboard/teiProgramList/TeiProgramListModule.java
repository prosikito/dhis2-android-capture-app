package org.dhis2.usescases.teiDashboard.teiProgramList;

import com.squareup.sqlbrite2.BriteDatabase;

import org.dhis2.data.dagger.PerActivity;
import org.dhis2.utils.CodeGenerator;

import androidx.annotation.NonNull;
import dagger.Module;
import dagger.Provides;

/**
 * QUADRAM. Created by Cristian on 13/02/2018.
 */
@PerActivity
@Module
public class TeiProgramListModule {


    private final String teiUid;

    TeiProgramListModule(String teiUid) {
        this.teiUid = teiUid;
    }

    @Provides
    @PerActivity
    TeiProgramListContract.TeiProgramListView provideView(TeiProgramListActivity activity) {
        return activity;
    }

    @Provides
    @PerActivity
    TeiProgramListContract.TeiProgramListPresenter providesPresenter(TeiProgramListContract.TeiProgramListInteractor teiProgramListInteractor) {
        return new TeiProgramListPresenterImpl(teiProgramListInteractor, teiUid);
    }

    @Provides
    @PerActivity
    TeiProgramListContract.TeiProgramListInteractor provideInteractor(@NonNull TeiProgramListRepository teiProgramListRepository) {
        return new TeiProgramListInteractorImpl(teiProgramListRepository);
    }

    @Provides
    @PerActivity
    TeiProgramListAdapter provideProgramEventDetailAdapter(TeiProgramListContract.TeiProgramListPresenter teiProgramListPresenter) {
        return new TeiProgramListAdapter(teiProgramListPresenter);
    }

    @Provides
    @PerActivity
    TeiProgramListRepository eventDetailRepository(@NonNull CodeGenerator codeGenerator, @NonNull BriteDatabase briteDatabase) {
        return new TeiProgramListRepositoryImpl(codeGenerator, briteDatabase);
    }
}
