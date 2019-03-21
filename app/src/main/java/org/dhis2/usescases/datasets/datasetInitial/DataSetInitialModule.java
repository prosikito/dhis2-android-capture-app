package org.dhis2.usescases.datasets.datasetInitial;

import com.squareup.sqlbrite2.BriteDatabase;

import org.dhis2.data.dagger.PerActivity;

import dagger.Module;
import dagger.Provides;

/**
 * QUADRAM. Created by ppajuelo on 24/09/2018.
 */
@PerActivity
@Module
public class DataSetInitialModule {
    private final String dataSetUid;

    DataSetInitialModule(String dataSetUid) {
        this.dataSetUid = dataSetUid;
    }

    @Provides
    @PerActivity
    DataSetInitialContract.DataSetInitialView provideView(DataSetInitialActivity activity) {
        return activity;
    }

    @Provides
    @PerActivity
    DataSetInitialContract.DataSetInitialPresenter providesPresenter(DataSetInitialRepository dataSetInitialRepository) {
        return new DataSetInitialDataSetInitialPresenter(dataSetInitialRepository);
    }

    @Provides
    @PerActivity
    DataSetInitialRepository dataSetInitialRepository(BriteDatabase briteDatabase) {
        return new DataSetInitialRepositoryImpl(briteDatabase, dataSetUid);
    }
}
