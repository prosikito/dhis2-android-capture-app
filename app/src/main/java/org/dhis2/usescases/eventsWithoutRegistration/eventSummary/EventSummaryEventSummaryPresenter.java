package org.dhis2.usescases.eventsWithoutRegistration.eventSummary;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Created by Cristian on 01/03/2018.
 */

public class EventSummaryEventSummaryPresenter implements EventSummaryContract.EventSummaryPresenter {

    private EventSummaryContract.EventSummaryView eventSummaryView;
    private final EventSummaryContract.EventSummaryInteractor eventSummaryInteractor;


    EventSummaryEventSummaryPresenter(EventSummaryContract.EventSummaryInteractor eventSummaryInteractor) {
        this.eventSummaryInteractor = eventSummaryInteractor;
    }

    @Override
    public void init(@NonNull EventSummaryContract.EventSummaryView mview, @NonNull String programId, @NonNull String eventId) {
        eventSummaryView = mview;
        eventSummaryInteractor.init(eventSummaryView, programId, eventId);
    }

    @Override
    public void onBackClick() {
        eventSummaryView.back();
    }

    @Override
    public void getSectionCompletion(@Nullable String sectionUid) {
        eventSummaryInteractor.getSectionCompletion(sectionUid);
    }

    @Override
    public void onDoAction() {
        eventSummaryInteractor.onDoAction();
    }

    @Override
    public void doOnComple() {
        eventSummaryInteractor.doOnComple();
    }

    @Override
    public void onDettach() {
        eventSummaryInteractor.onDettach();
    }

    @Override
    public void displayMessage(String message) {
        eventSummaryView.displayMessage(message);
    }
}
