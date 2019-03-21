package org.dhis2.usescases.enrollment;

import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.usescases.general.AbstractActivityContracts;
import org.hisp.dhis.android.core.period.FeatureType;

import io.reactivex.functions.Consumer;

public class EnrollmentContracts {

    public interface EnrollmentView extends AbstractActivityContracts.View {
        void renderEnrollmentDate(String s, String date);

        void renderIncidentDate(String s, String date);

        void showCoordinates(FeatureType show);

        Consumer<FieldViewModel> showFields();
    }

    public interface EnrollmentPresenter extends AbstractActivityContracts.Presenter {
        void init(EnrollmentView enrollmentView);

        void onNextClick();
    }

}
