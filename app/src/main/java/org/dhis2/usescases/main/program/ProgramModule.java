package org.dhis2.usescases.main.program;

import com.squareup.sqlbrite2.BriteDatabase;

import org.dhis2.data.dagger.PerFragment;

import dagger.Module;
import dagger.Provides;

/**
 * QUADRAM. Created by ppajuelo on 07/02/2018.
 */
@Module
@PerFragment
public class ProgramModule {

    @Provides
    @PerFragment
    ProgramContract.ProgramPresenter programPresenter(HomeRepository homeRepository) {
        return new ProgramProgramPresenter(homeRepository);
    }

    @Provides
    @PerFragment
    HomeRepository homeRepository(BriteDatabase briteDatabase) {
        return new HomeRepositoryImpl(briteDatabase);
    }


}
