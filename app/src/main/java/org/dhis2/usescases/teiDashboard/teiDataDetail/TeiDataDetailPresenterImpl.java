package org.dhis2.usescases.teiDashboard.teiDataDetail;

import android.content.Intent;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.dhis2.data.metadata.MetadataRepository;
import org.dhis2.usescases.map.MapSelectorActivity;
import org.dhis2.usescases.teiDashboard.DashboardProgramModel;
import org.dhis2.usescases.teiDashboard.DashboardRepository;
import org.dhis2.utils.Constants;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * QUADRAM. Created by frodriguez on 12/13/2017.
 */

public class TeiDataDetailPresenterImpl implements TeiDataDetailContracts.TeiDataDetailPresenter {

    private final DashboardRepository dashboardRepository;
    private final MetadataRepository metadataRepository;
    private final CompositeDisposable disposable;
    private final EnrollmentStatusStore enrollmentStore;
    private TeiDataDetailContracts.TeiDataDetailView view;
    private FusedLocationProviderClient mFusedLocationClient;

    TeiDataDetailPresenterImpl(DashboardRepository dashboardRepository, MetadataRepository metadataRepository, EnrollmentStatusStore enrollmentStatusStore) {
        this.dashboardRepository = dashboardRepository;
        this.metadataRepository = metadataRepository;
        this.enrollmentStore = enrollmentStatusStore;
        disposable = new CompositeDisposable();
    }

    @Override
    public void init(TeiDataDetailContracts.TeiDataDetailView view, String uid, String programUid, String enrollmentUid) {
        this.view = view;
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(view.getContext());

        if (programUid != null) {
            disposable.add(Observable.zip(
                    metadataRepository.getTrackedEntityInstance(uid),
                    dashboardRepository.getEnrollment(programUid, uid),
                    dashboardRepository.getProgramStages(programUid),
                    dashboardRepository.getTEIEnrollmentEvents(programUid, uid),
                    metadataRepository.getProgramTrackedEntityAttributes(programUid),
                    dashboardRepository.getTEIAttributeValues(programUid, uid),
                    metadataRepository.getTeiOrgUnit(uid),
                    metadataRepository.getTeiActivePrograms(uid),
                    DashboardProgramModel::new)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            view::setData,
                            Timber::e)
            );

            disposable.add(
                    enrollmentStore.enrollmentStatus(enrollmentUid)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    view.handleStatus(),
                                    Timber::e)

            );

            disposable.add(
                    enrollmentStore.enrollmentCoordinates()
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    data -> view.setLocation(data.val0(), data.val1()),
                                    Timber::e
                            )
            );
        } else {
            //TODO: NO SE HA SELECCIONADO PROGRAMA
            disposable.add(Observable.zip(
                    metadataRepository.getTrackedEntityInstance(uid),
                    metadataRepository.getProgramTrackedEntityAttributes(null),
                    dashboardRepository.getTEIAttributeValues(null, uid),
                    metadataRepository.getTeiOrgUnit(uid),
                    metadataRepository.getTeiActivePrograms(uid),
                    metadataRepository.getTEIEnrollments(uid),
                    DashboardProgramModel::new)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(view::setData,
                            Timber::e)
            );
        }
    }

    @Override
    public void onBackPressed() {
        view.getAbstracContext().onBackPressed();
    }


    @Override
    public void onDeactivate(DashboardProgramModel dashboardProgramModel) {
        if (dashboardProgramModel.getCurrentProgram().accessDataWrite())
            disposable.add(enrollmentStore.save(dashboardProgramModel.getCurrentEnrollment().uid(), EnrollmentStatus.CANCELLED)
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .map(result -> EnrollmentStatus.CANCELLED)
                    .subscribe(
                            view.handleStatus(),
                            Timber::d)
            );
        else
            view.displayMessage(null);

    }

    @Override
    public void onReOpen(DashboardProgramModel dashboardProgramModel) {
        if (dashboardProgramModel.getCurrentProgram().accessDataWrite())
            disposable.add(enrollmentStore.save(dashboardProgramModel.getCurrentEnrollment().uid(), EnrollmentStatus.ACTIVE)
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .map(result -> EnrollmentStatus.ACTIVE)
                    .subscribe(
                            view.handleStatus(),
                            Timber::d)
            );
        else
            view.displayMessage(null);
    }

    @Override
    public void onComplete(DashboardProgramModel dashboardProgramModel) {
        if (dashboardProgramModel.getCurrentProgram().accessDataWrite())
            disposable.add(enrollmentStore.save(dashboardProgramModel.getCurrentEnrollment().uid(), EnrollmentStatus.COMPLETED)
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .map(result -> EnrollmentStatus.COMPLETED)
                    .subscribe(
                            view.handleStatus(),
                            Timber::d)
            );
        else
            view.displayMessage(null);
    }

    @Override
    public void onActivate(DashboardProgramModel dashboardProgramModel) {
        if (dashboardProgramModel.getCurrentProgram().accessDataWrite())
            disposable.add(enrollmentStore.save(dashboardProgramModel.getCurrentEnrollment().uid(), EnrollmentStatus.ACTIVE)
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .map(result -> EnrollmentStatus.ACTIVE)
                    .subscribe(
                            view.handleStatus(),
                            Timber::d)
            );
        else
            view.displayMessage(null);
    }

    @Override
    public void onLocationClick() {
        if (view.checkLocationPermission()) {
            mFusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    saveLocation(location.getLatitude(), location.getLongitude());
                }
            });
        }
    }

    @Override
    public void onLocation2Click() {
        Intent intent = new Intent(view.getContext(), MapSelectorActivity.class);
        view.getAbstractActivity().startActivityForResult(intent, Constants.RQ_MAP_LOCATION);
    }

    @Override
    public void saveLocation(double latitude, double longitude) {
        disposable.add(
                enrollmentStore.saveCoordinates(latitude, longitude)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                data -> {
                                },
                                Timber::e
                        )
        );
    }

    @Override
    public void onDettach() {
        disposable.clear();
    }

    @Override
    public void displayMessage(String message) {
        view.displayMessage(message);
    }
}
