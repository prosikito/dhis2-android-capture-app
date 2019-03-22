package org.dhis2.usescases.sync;

import org.dhis2.data.metadata.MetadataRepository;
import org.dhis2.data.service.ReservedValuesWorker;
import org.dhis2.data.service.SyncDataWorker;
import org.dhis2.data.service.SyncMetadataWorker;

import java.util.concurrent.TimeUnit;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class SyncPresenterImpl implements SyncContracts.SyncPresenter {

    private final MetadataRepository metadataRepository;
    private SyncContracts.SyncView syncView;

    private CompositeDisposable disposable;


    SyncPresenterImpl(MetadataRepository metadataRepository) {
        this.metadataRepository = metadataRepository;
    }

    @Override
    public void init(SyncContracts.SyncView syncView) {
        this.syncView = syncView;
        this.disposable = new CompositeDisposable();
    }

    @Override
    public void syncMeta(int seconds, String scheduleTag) {
        WorkManager.getInstance().cancelAllWorkByTag(scheduleTag);
        PeriodicWorkRequest.Builder syncDataBuilder = new PeriodicWorkRequest.Builder(SyncMetadataWorker.class, seconds, TimeUnit.SECONDS);
        syncDataBuilder.addTag(scheduleTag);
        syncDataBuilder.setConstraints(new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build());
        PeriodicWorkRequest request = syncDataBuilder.build();
        WorkManager.getInstance().enqueueUniquePeriodicWork(scheduleTag, ExistingPeriodicWorkPolicy.REPLACE, request);
    }

    @Override
    public void syncData(int seconds, String scheduleTag) {
        WorkManager.getInstance().cancelAllWorkByTag(scheduleTag);
        PeriodicWorkRequest.Builder syncDataBuilder = new PeriodicWorkRequest.Builder(SyncDataWorker.class, seconds, TimeUnit.SECONDS);
        syncDataBuilder.addTag(scheduleTag);
        syncDataBuilder.setConstraints(new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build());
        PeriodicWorkRequest request = syncDataBuilder.build();
        WorkManager.getInstance().enqueueUniquePeriodicWork(scheduleTag, ExistingPeriodicWorkPolicy.REPLACE, request);
    }

    public void getTheme() {
        disposable.add(metadataRepository.getTheme()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(flagTheme -> {
                            syncView.saveFlag(flagTheme.val0());
                            syncView.saveTheme(flagTheme.val1());
                        }, Timber::e
                ));

    }

/*
    @Override
    public void syncAggregatesData() {
        disposable.add(aggregatesData()
                .map(response -> SyncResult.success())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorReturn(throwable -> SyncResult.failure(
                        throwable.getMessage() == null ? "" : throwable.getMessage()))
                .startWith(SyncResult.progress())
                .subscribe(update(SyncActivity.SyncState.AGGREGATES),
                        throwable -> syncView.displayMessage(throwable.getMessage())
                ));
    }*/

    @Override
    public void syncReservedValues() {

        WorkManager.getInstance().cancelAllWorkByTag("TAG_RV");
        OneTimeWorkRequest.Builder syncDataBuilder = new OneTimeWorkRequest.Builder(ReservedValuesWorker.class);
        syncDataBuilder.addTag("TAG_RV");
        syncDataBuilder.setConstraints(new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build());
        OneTimeWorkRequest request = syncDataBuilder.build();
        WorkManager.getInstance().enqueue(request);

    }

    @Override
    public void onDettach() {
        disposable.clear();
    }

    @Override
    public void displayMessage(String message) {
        syncView.displayMessage(message);
    }
}