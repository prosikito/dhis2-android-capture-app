package org.dhis2.Bindings;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputLayout;

import org.dhis2.R;
import org.dhis2.data.metadata.MetadataRepository;
import org.dhis2.data.tuples.Pair;
import org.dhis2.utils.CatComboAdapter;
import org.dhis2.utils.DateUtils;
import org.hisp.dhis.android.core.category.CategoryOptionComboModel;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.enrollment.EnrollmentModel;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.option.OptionModel;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.program.ProgramStageModel;
import org.hisp.dhis.android.core.resource.ResourceModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.databinding.BindingAdapter;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * QUADRAM. Created by ppajuelo on 28/09/2017.
 */

public class Bindings {

    private static MetadataRepository metadataRepository;

    @BindingAdapter("date")
    public static void setDate(TextView textView, String date) {
        SimpleDateFormat formatIn = DateUtils.databaseDateFormat();
        SimpleDateFormat formatOut = DateUtils.uiDateFormat();
        try {
            Date dateIn = formatIn.parse(date);
            String dateOut = formatOut.format(dateIn);
            textView.setText(dateOut);
        } catch (ParseException e) {
            Timber.e(e);
        }

    }

    @BindingAdapter("currentFragment")
    public static void setCurrentFragment(TextView textView, int currentFragmentId) {
        TypedValue typedValue = new TypedValue();
        TypedArray a = textView.getContext().obtainStyledAttributes(typedValue.data, new int[]{R.attr.colorPrimary});
        int colorPrimary = a.getColor(0, 0);
        a.recycle();
        int colorAccent = ContextCompat.getColor(textView.getContext(), R.color.colorAccent);
        if (currentFragmentId == textView.getId()) {
            textView.setTextColor(colorPrimary);
            textView.setBackgroundColor(colorAccent);
        } else {
            textView.setTextColor(colorAccent);
            textView.setBackgroundColor(colorPrimary);
        }
    }

    @BindingAdapter("date")
    public static void parseDate(TextView textView, Date date) {
        if (date != null) {
            SimpleDateFormat formatOut = DateUtils.uiDateFormat();
            String dateOut = formatOut.format(date);
            textView.setText(dateOut);
        }

    }

    @BindingAdapter("drawableEnd")
    public static void setDrawableEnd(TextView textView, Drawable drawable) {
        textView.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (drawable instanceof AnimatedVectorDrawable)
                ((AnimatedVectorDrawable) drawable).start();
        }
    }

    @SuppressLint({"CheckResult", "RxLeakedSubscription"})
    @BindingAdapter("lastEventDate")
    public static void setLastEventDate(TextView textView, Observable<List<EventModel>> listObservable) {
        listObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        data -> textView.setText(DateUtils.uiDateFormat().format(data.get(0).eventDate())),
                        Timber::d);
    }

    @SuppressLint({"CheckResult", "RxLeakedSubscription"})
    @BindingAdapter("numberOfRecords")
    public static void setNumberOfRecords(TextView textView, Observable<Pair<Integer, String>> listObservable) {
        CompositeDisposable disposable = new CompositeDisposable();
        disposable.add(listObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        data -> {
                            textView.setText(recordsPlusType(data.val0(), data.val1()));
                        },
                        Timber::d,
                        disposable::dispose));
    }

    private static String recordsPlusType(int numberOfRecords, String recordType) {
        String finalText = String.format(Locale.getDefault(), "%d %s", numberOfRecords, recordType);
        SpannableStringBuilder sp = new SpannableStringBuilder(finalText);
        sp.setSpan(new AbsoluteSizeSpan(20), 0, String.valueOf(numberOfRecords).length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        return sp.toString();
    }

    @SuppressLint({"CheckResult", "RxLeakedSubscription"})
    @BindingAdapter("programSyncState")
    public static void setProgramSyncState(ImageView imageView, ProgramModel program) {
        if (metadataRepository != null)
            metadataRepository
                    .syncState(program)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            resourceModelList -> {
                                for (ResourceModel resourceModel : resourceModelList) {
                                    if (resourceModel.resourceType().equals(ResourceModel.Type.PROGRAM.name())) {
                                        if (resourceModel.lastSynced().before(program.lastUpdated()))
                                            imageView.setImageResource(R.drawable.ic_sync_problem_grey);
                                        else
                                            imageView.setImageResource(R.drawable.ic_sync);
                                    }
                                }
                            },
                            Timber::d);
    }

    @BindingAdapter(value = {"initGrid", "spanCount"}, requireAll = false)
    public static void setLayoutManager(RecyclerView recyclerView, boolean horizontal, int spanCount) {
        RecyclerView.LayoutManager recyclerLayout;
        if (spanCount == -1)
            spanCount = 1;

        recyclerLayout = new GridLayoutManager(recyclerView.getContext(), spanCount, RecyclerView.VERTICAL, false);

        recyclerView.setLayoutManager(recyclerLayout);

    }

    @BindingAdapter("spanSize")
    public static void setSpanSize(RecyclerView recyclerView, boolean setSpanSize) {
        if (recyclerView.getLayoutManager() instanceof GridLayoutManager) {
            ((GridLayoutManager) recyclerView.getLayoutManager()).setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    int itemViewType = recyclerView.getAdapter().getItemViewType(position);
                    return (itemViewType == 4 || itemViewType == 8) ? 2 : 1;
                }
            });
        }
    }

    @BindingAdapter("progressColor")
    public static void setProgressColor(ProgressBar progressBar, int color) {
        TypedValue typedValue = new TypedValue();
        TypedArray a = progressBar.getContext().obtainStyledAttributes(typedValue.data, new int[]{R.attr.colorPrimary});
        color = a.getColor(0, 0);
        a.recycle();
        progressBar.getIndeterminateDrawable().setColorFilter(color, PorterDuff.Mode.SRC_IN);
    }

    @SuppressLint({"CheckResult", "RxLeakedSubscription"})
    @BindingAdapter("programStage")
    public static void getStageName(TextView textView, String stageId) {
        if (metadataRepository != null)
            metadataRepository.programStage(stageId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            programStageModel -> textView.setText(programStageModel.displayName()),
                            Timber::d
                    );
    }

    @SuppressLint({"CheckResult", "RxLeakedSubscription"})
    @BindingAdapter("programStageDescription")
    public static void getStageDescription(TextView textView, String stageId) {
        if (metadataRepository != null)
            metadataRepository.programStage(stageId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            programStageModel -> textView.setText(programStageModel.description()),
                            Timber::d
                    );
    }

    @BindingAdapter("enrolmentIcon")
    public static void setEnrolmentIcon(ImageView view, EnrollmentStatus status) {
        Drawable lock;
        if (status == null)
            status = EnrollmentStatus.ACTIVE;
        switch (status) {
            case ACTIVE:
                lock = ContextCompat.getDrawable(view.getContext(), R.drawable.ic_lock_open_green);
                break;
            case COMPLETED:
                lock = ContextCompat.getDrawable(view.getContext(), R.drawable.ic_lock_completed);
                break;
            case CANCELLED:
                lock = ContextCompat.getDrawable(view.getContext(), R.drawable.ic_lock_inactive);
                break;
            default:
                lock = ContextCompat.getDrawable(view.getContext(), R.drawable.ic_lock_read_only);
                break;
        }

        view.setImageDrawable(lock);

    }

    @BindingAdapter("enrolmentText")
    public static void setEnrolmentText(TextView view, EnrollmentStatus status) {
        String text;
        if (status == null)
            status = EnrollmentStatus.ACTIVE;
        switch (status) {
            case ACTIVE:
                text = view.getContext().getString(R.string.event_open);
                break;
            case COMPLETED:
                text = view.getContext().getString(R.string.completed);
                break;
            case CANCELLED:
                text = view.getContext().getString(R.string.cancelled);
                break;
            default:
                text = view.getContext().getString(R.string.read_only);
                break;
        }

        view.setText(text);
    }

    @BindingAdapter(value = {"eventStatusIcon", "enrollmentStatusIcon", "eventProgramStage"}, requireAll = false)
    public static void setEventIcon(ImageView view, EventModel event, EnrollmentModel enrollmentModel, ProgramStageModel eventProgramStage) {
        EventStatus status = event.status();
        EnrollmentStatus enrollmentStatus = enrollmentModel.enrollmentStatus();
        if (status == null)
            status = EventStatus.ACTIVE;
        if (enrollmentStatus == null)
            enrollmentStatus = EnrollmentStatus.ACTIVE;

        if (enrollmentStatus == EnrollmentStatus.ACTIVE) {
            switch (status) {
                case ACTIVE:
                    if (metadataRepository != null)
                        metadataRepository.getExpiryDateFromEvent(event.uid())
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(
                                        program -> {
                                            if (DateUtils.getInstance().hasExpired(event, program.expiryDays(), program.completeEventsExpiryDays(), eventProgramStage.periodType() != null ? eventProgramStage.periodType() : program.expiryPeriodType())) {
                                                view.setImageDrawable(ContextCompat.getDrawable(view.getContext(), R.drawable.ic_eye_red));
                                            } else {
                                                view.setImageDrawable(ContextCompat.getDrawable(view.getContext(), R.drawable.ic_edit));
                                            }
                                        },
                                        Timber::d
                                );
                    break;
                case OVERDUE:
                case COMPLETED:
                case SKIPPED:
                    view.setImageDrawable(ContextCompat.getDrawable(view.getContext(), R.drawable.ic_visibility));
                    break;
                case SCHEDULE:
                    view.setImageDrawable(ContextCompat.getDrawable(view.getContext(), R.drawable.ic_edit));
                    break;
                case VISITED:
                    view.setImageDrawable(ContextCompat.getDrawable(view.getContext(), R.drawable.ic_edit));
                    break;
                default:
                    view.setImageDrawable(ContextCompat.getDrawable(view.getContext(), R.drawable.ic_edit));
                    break;
            }
        } else if (enrollmentStatus == EnrollmentStatus.COMPLETED) {
            view.setImageDrawable(ContextCompat.getDrawable(view.getContext(), R.drawable.ic_visibility));
        } else { //EnrollmentStatus = CANCELLED
            view.setImageDrawable(ContextCompat.getDrawable(view.getContext(), R.drawable.ic_visibility));
        }
    }

    @BindingAdapter(value = {"eventStatusText", "enrollmentStatus", "eventProgramStage"})
    public static void setEventText(TextView view, EventModel event, EnrollmentModel enrollmentModel, ProgramStageModel eventProgramStage) {
        EventStatus status = event.status();
        EnrollmentStatus enrollmentStatus = enrollmentModel.enrollmentStatus();
        if (status == null)
            status = EventStatus.ACTIVE;
        if (enrollmentStatus == null)
            enrollmentStatus = EnrollmentStatus.ACTIVE;


        if (enrollmentStatus == EnrollmentStatus.ACTIVE) {
            switch (status) {
                case ACTIVE:
                    if (metadataRepository != null)
                        metadataRepository.getExpiryDateFromEvent(event.uid())
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(
                                        program -> {
                                            if (DateUtils.getInstance().hasExpired(event, program.expiryDays(), program.completeEventsExpiryDays(), eventProgramStage.periodType() != null ? eventProgramStage.periodType() : program.expiryPeriodType())) {
                                                view.setText(view.getContext().getString(R.string.event_expired));
                                            } else {
                                                view.setText(view.getContext().getString(R.string.event_open));
                                            }
                                        },
                                        Timber::d
                                );
                    break;
                case COMPLETED:
                    if (metadataRepository != null)
                        metadataRepository.getExpiryDateFromEvent(event.uid())
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(
                                        program -> {
                                            if (DateUtils.getInstance().isEventExpired(null, event.completedDate(), program.completeEventsExpiryDays())) {
                                                view.setText(view.getContext().getString(R.string.event_expired));
                                            } else {
                                                view.setText(view.getContext().getString(R.string.event_completed));
                                            }
                                        },
                                        Timber::d
                                );
                    break;
                case SCHEDULE:
                    if (metadataRepository != null)
                        metadataRepository.getExpiryDateFromEvent(event.uid())
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(
                                        program -> {
                                            if (DateUtils.getInstance().hasExpired(event, program.expiryDays(), program.completeEventsExpiryDays(), eventProgramStage.periodType() != null ? eventProgramStage.periodType() : program.expiryPeriodType())) {
                                                view.setText(view.getContext().getString(R.string.event_expired));
                                            } else {
                                                view.setText(view.getContext().getString(R.string.event_schedule));
                                            }
                                        },
                                        Timber::d
                                );
                    break;
                case SKIPPED:
                    view.setText(view.getContext().getString(R.string.event_skipped));
                    break;
                case OVERDUE:
                    view.setText(R.string.event_overdue);
                    break;
                default:
                    view.setText(view.getContext().getString(R.string.read_only));
                    break;
            }
        } else if (enrollmentStatus == EnrollmentStatus.COMPLETED) {
            view.setText(view.getContext().getString(R.string.program_completed));
        } else { //EnrollmentStatus = CANCELLED
            view.setText(view.getContext().getString(R.string.program_inactive));
        }


    }

    @BindingAdapter(value = {"eventColor", "eventProgramStage"})
    public static void setEventColor(View view, EventModel event, ProgramStageModel programStage) {

        if (metadataRepository != null)
            metadataRepository.getExpiryDateFromEvent(event.uid())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            program -> {
                                int eventColor;
                                int bgColor;
                                if (DateUtils.getInstance().isEventExpired(null, event.completedDate(), program.completeEventsExpiryDays())) {
                                    eventColor = R.color.event_red;
                                    bgColor = R.drawable.item_event_red_ripple;
                                } else {
                                    switch (event.status()) {
                                        case ACTIVE:
                                            if (DateUtils.getInstance().hasExpired(event, program.expiryDays(), program.completeEventsExpiryDays(), programStage.periodType() != null ? programStage.periodType() : program.expiryPeriodType())) {
                                                eventColor = R.color.event_red;
                                                bgColor = R.drawable.item_event_red_ripple;
                                            } else {
                                                eventColor = R.color.event_yellow;
                                                bgColor = R.drawable.item_event_yellow_ripple;
                                            }
                                            break;
                                        case COMPLETED:
                                            if (DateUtils.getInstance().isEventExpired(null, event.completedDate(), program.completeEventsExpiryDays())) {
                                                eventColor = R.color.event_red;
                                                bgColor = R.drawable.item_event_red_ripple;

                                            } else {
                                                eventColor = R.color.event_gray;
                                                bgColor = R.drawable.item_event_gray_ripple;

                                            }
                                            break;
                                        case SCHEDULE:
                                            if (DateUtils.getInstance().hasExpired(event, program.expiryDays(), program.completeEventsExpiryDays(), programStage.periodType() != null ? programStage.periodType() : program.expiryPeriodType())) {
                                                eventColor = R.color.event_red;
                                                bgColor = R.drawable.item_event_red_ripple;
                                            } else {
                                                eventColor = R.color.event_green;
                                                bgColor = R.drawable.item_event_green_ripple;
                                            }
                                            break;
                                        case VISITED:
                                        case SKIPPED:
                                        default:
                                            eventColor = R.color.event_red;
                                            bgColor = R.drawable.item_event_red_ripple;
                                            break;
                                    }
                                }

                                view.setBackground(ContextCompat.getDrawable(view.getContext(), bgColor));
//                                view.setBackgroundColor(ContextCompat.getColor(view.getContext(), eventColor));

                            },
                            Timber::d
                    );

    }


    @BindingAdapter("scheduleColor")
    public static void setScheduleColor(ImageView view, EventStatus status) {
        int drawable;
        if (status == null)
            status = EventStatus.ACTIVE;
        switch (status) {
            case SCHEDULE:
                drawable = R.drawable.schedule_circle_green;
                break;
            default:
                drawable = R.drawable.schedule_circle_red;
                break;
        }

        view.setImageDrawable(ContextCompat.getDrawable(view.getContext(), drawable));
    }

    @SuppressLint({"CheckResult", "RxLeakedSubscription"})
    @BindingAdapter("programName")
    public static void setProgramName(TextView textView, String programUid) {
        if (metadataRepository != null)
            metadataRepository.getProgramWithId(programUid)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            programModel -> textView.setText(programModel.displayName()),
                            Timber::d
                    );
    }

    @SuppressLint({"CheckResult", "RxLeakedSubscription"})
    @BindingAdapter("organisationUnitName")
    public static void setOrganisationUnitName(TextView textView, String organisationUnitUid) {
        if (metadataRepository != null)
            metadataRepository.getOrganisationUnit(organisationUnitUid)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            organisationUnitModel -> textView.setText(organisationUnitModel.displayName()),
                            Timber::d
                    );
    }

    @SuppressLint({"CheckResult", "RxLeakedSubscription"})
    @BindingAdapter("categoryOptionComboName")
    public static void setCategoryOptionComboName(TextView textView, String categoryOptionComboId) {
        if (metadataRepository != null)
            metadataRepository.getCategoryOptionComboWithId(categoryOptionComboId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            categoryOptionComboModel -> metadataRepository.getCategoryComboWithId(categoryOptionComboModel.categoryCombo())
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(
                                            categoryOptionModel -> {
                                                if (!categoryOptionModel.isDefault()) {
                                                    textView.setVisibility(View.VISIBLE);
                                                    textView.setText(categoryOptionComboModel.displayName());
                                                } else {
                                                    textView.setVisibility(View.GONE);
                                                    textView.setText("");
                                                }
                                            },
                                            Timber::d
                                    ),
                            Timber::d
                    );
    }

    @BindingAdapter("eventWithoutRegistrationStatusText")
    public static void setEventWithoutRegistrationStatusText(TextView textView, EventModel eventModel) {
        switch (eventModel.status()) {
            case ACTIVE:
                metadataRepository.getExpiryDateFromEvent(eventModel.uid())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                program -> {
                                    if (DateUtils.getInstance().hasExpired(eventModel, program.expiryDays(), program.completeEventsExpiryDays(), program.expiryPeriodType())) {
                                        textView.setText(textView.getContext().getString(R.string.event_editing_expired));
                                        textView.setTextColor(ContextCompat.getColor(textView.getContext(), R.color.red_060));
                                    } else {
                                        textView.setText(textView.getContext().getString(R.string.event_open));
                                        textView.setTextColor(ContextCompat.getColor(textView.getContext(), R.color.yellow_fdd));
                                    }
                                },
                                Timber::d
                        );
                break;
            case COMPLETED:
                metadataRepository.getExpiryDateFromEvent(eventModel.uid())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                program -> {
                                    if (DateUtils.getInstance().isEventExpired(null, eventModel.completedDate(), program.completeEventsExpiryDays())) {
                                        textView.setText(textView.getContext().getString(R.string.event_editing_expired));
                                        textView.setTextColor(ContextCompat.getColor(textView.getContext(), R.color.red_060));
                                    } else {
                                        textView.setText(textView.getContext().getString(R.string.event_completed));
                                        textView.setTextColor(ContextCompat.getColor(textView.getContext(), R.color.gray_b2b));
                                    }
                                },
                                Timber::d
                        );
                break;
            case SKIPPED:
                textView.setText(textView.getContext().getString(R.string.event_editing_expired));
                textView.setTextColor(ContextCompat.getColor(textView.getContext(), R.color.red_060));
                break;
            default:
                // TODO CRIS: HERE CHECK THE EVENT APPROVAL
                textView.setText(textView.getContext().getString(R.string.read_only));
                textView.setTextColor(ContextCompat.getColor(textView.getContext(), R.color.green_7ed));
                break;
        }
    }

    @BindingAdapter("eventWithoutRegistrationStatusIcon")
    public static void setEventWithoutRegistrationStatusIcon(ImageView imageView, EventModel eventModel) {
        switch (eventModel.status()) {
            case ACTIVE:
                if (metadataRepository != null)
                    metadataRepository.getExpiryDateFromEvent(eventModel.uid())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    program -> {
                                        if (DateUtils.getInstance().hasExpired(eventModel, program.expiryDays(), program.completeEventsExpiryDays(), program.expiryPeriodType())) {
                                            imageView.setImageResource(R.drawable.ic_eye_red);
                                        } else {
                                            imageView.setImageResource(R.drawable.ic_edit_yellow);
                                        }
                                    },
                                    Timber::d
                            );
                break;
            case COMPLETED:
                if (metadataRepository != null)
                    metadataRepository.getExpiryDateFromEvent(eventModel.uid())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    program -> {
                                        if (DateUtils.getInstance().isEventExpired(null, eventModel.completedDate(), program.completeEventsExpiryDays())) {
                                            imageView.setImageResource(R.drawable.ic_eye_red);
                                        } else {
                                            imageView.setImageResource(R.drawable.ic_eye_grey);
                                        }
                                    },
                                    Timber::d
                            );
                break;
            default:
                // TODO CRIS: HERE CHECK THE EVENT APPROVAL
                imageView.setImageResource(R.drawable.ic_eye_green);
                break;
        }
    }

    @BindingAdapter("stateText")
    public static void setStateText(TextView textView, State state) {
        switch (state) {
            case TO_POST:
                textView.setText(textView.getContext().getString(R.string.state_to_post));
                break;
            case TO_UPDATE:
                textView.setText(textView.getContext().getString(R.string.state_to_update));
                break;
            case TO_DELETE:
                textView.setText(textView.getContext().getString(R.string.state_to_delete));
                break;
            case ERROR:
                textView.setText(textView.getContext().getString(R.string.state_error));
                break;
            case SYNCED:
                textView.setText(textView.getContext().getString(R.string.state_synced));
                break;
            default:
                break;
        }
    }

    @BindingAdapter("stateIcon")
    public static void setStateIcon(ImageView imageView, State state) {
        if (state != null) {
            switch (state) {
                case TO_POST:
                    imageView.setImageResource(R.drawable.ic_sync_problem_grey);
                    break;
                case TO_UPDATE:
                    imageView.setImageResource(R.drawable.ic_sync_problem_grey);
                    break;
                case TO_DELETE:
                    imageView.setImageResource(R.drawable.ic_sync_problem_grey);
                    break;
                case ERROR:
                    imageView.setImageResource(R.drawable.ic_sync_problem_red);
                    break;
                case SYNCED:
                    imageView.setImageResource(R.drawable.ic_sync);
                    break;
                default:
                    break;
            }
        }
    }

    public static void setMetadataRepository(MetadataRepository metadata) {
        metadataRepository = metadata;
    }

    @SuppressLint({"CheckResult", "RxLeakedSubscription"})
    @BindingAdapter("dataElementHint")
    public static void setDataElementName(TextInputLayout view, String dataElementUid) {
        if (metadataRepository != null)
            metadataRepository.getDataElement(dataElementUid)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            dataModel -> view.setHint(dataModel.displayName()),
                            Timber::d
                    );
    }

    @SuppressLint({"CheckResult", "RxLeakedSubscription"})
    @BindingAdapter("attrHint")
    public static void setAttrName(TextInputLayout view, String teAttribute) {
        if (metadataRepository != null)
            metadataRepository.getTrackedEntityAttribute(teAttribute)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            dataModel -> view.setHint(dataModel.displayName()),
                            Timber::d
                    );
    }

    @BindingAdapter("spinnerOptions")
    public static void setSpinnerOptions(Spinner spinner, List<CategoryOptionComboModel> options) {
        CatComboAdapter adapter = new CatComboAdapter(spinner.getContext(),
                R.layout.spinner_layout,
                R.id.spinner_text,
                options,
                "",
                R.color.white_faf);
        spinner.setAdapter(adapter);
    }

    @SuppressLint({"CheckResult", "RxLeakedSubscription"})
    public static List<OptionModel> setOptionSet(@NonNull String optionSet) {
        return metadataRepository.optionSet(optionSet);
    }

    public static int optionSetItemSize(@Nonnull String optionSet) {
        return metadataRepository.optionSetSize(optionSet);
    }

    @BindingAdapter("fromResBgColor")
    public static void setFromResBgColor(View view, int color) {
        String tintedColor;

        ArrayList<Double> rgb = new ArrayList<>();
        rgb.add(Color.red(color) / 255.0d);
        rgb.add(Color.green(color) / 255.0d);
        rgb.add(Color.blue(color) / 255.0d);

        Double r = null;
        Double g = null;
        Double b = null;
        for (Double c : rgb) {
            if (c <= 0.03928d)
                c = c / 12.92d;
            else
                c = Math.pow(((c + 0.055d) / 1.055d), 2.4d);

            if (r == null)
                r = c;
            else if (g == null)
                g = c;
            else
                b = c;
        }

        double L = 0.2126d * r + 0.7152d * g + 0.0722d * b;


        if (L > 0.179d)
            tintedColor = "#000000"; // bright colors - black font
        else
            tintedColor = "#FFFFFF"; // dark colors - white font

        if (view instanceof TextView) {
            ((TextView) view).setTextColor(Color.parseColor(tintedColor));
        }
        if (view instanceof ImageView) {
            Drawable drawable = ((ImageView) view).getDrawable();
            if (drawable != null)
                drawable.setColorFilter(Color.parseColor(tintedColor), PorterDuff.Mode.SRC_IN);
            ((ImageView) view).setImageDrawable(drawable);
        }
    }

    @SuppressLint("RxLeakedSubscription")
    @BindingAdapter({"objectStyle", "itemView"})
    public static void setObjectStyle(View view, View itemView, String uid) {
        if (metadataRepository != null)
            metadataRepository.getObjectStyle(uid)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            data -> {
                                if (data.icon() != null) {
                                    Resources resources = view.getContext().getResources();
                                    String iconName = data.icon().startsWith("ic_") ? data.icon() : "ic_" + data.icon();
                                    int icon = resources.getIdentifier(iconName, "drawable", view.getContext().getPackageName());
                                    if (view instanceof ImageView)
                                        ((ImageView) view).setImageResource(icon);
                                }

                                if (data.color() != null) {
                                    String color = data.color().startsWith("#") ? data.color() : "#" + data.color();
                                    int colorRes = Color.parseColor(color);
                                    itemView.setBackgroundColor(colorRes);
                                    setFromResBgColor(view, colorRes);
                                }
                            },
                            Timber::d
                    );


    }

    @SuppressLint("RxLeakedSubscription")
    @BindingAdapter({"objectStyle", "itemView"})
    public static void setObjectStyleAndTint(View view, View itemView, String uid) {
        if (metadataRepository != null)
            metadataRepository.getObjectStyle(uid)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            data -> {
                                if (data.icon() != null) {
                                    Resources resources = view.getContext().getResources();
                                    String iconName = data.icon().startsWith("ic_") ? data.icon() : "ic_" + data.icon();
                                    int icon = resources.getIdentifier(iconName, "drawable", view.getContext().getPackageName());
                                    if (view instanceof ImageView)
                                        ((ImageView) view).setImageResource(icon);
                                }

                                if (data.color() != null) {
                                    String color = data.color().startsWith("#") ? data.color() : "#" + data.color();
                                    int colorRes = Color.parseColor(color);
                                    ColorStateList colorStateList = ColorStateList.valueOf(colorRes);
                                    ViewCompat.setBackgroundTintList(view, colorStateList);
                                    setFromResBgColor(view, colorRes);
                                }
                            },
                            Timber::d
                    );


    }

    @BindingAdapter("imageBackground")
    public static void setImageBackground(ImageView imageView, Drawable drawable) {

        TypedValue typedValue = new TypedValue();
        TypedArray a = imageView.getContext().obtainStyledAttributes(typedValue.data, new int[]{R.attr.colorPrimaryDark});
        TypedArray b = imageView.getContext().obtainStyledAttributes(typedValue.data, new int[]{R.attr.colorPrimaryLight});
        int colorPrimaryDark = a.getColor(0, 0);
        int colorPrimaryLight = b.getColor(0, 0);

        int px = (int) (1 * Resources.getSystem().getDisplayMetrics().density);
        ((GradientDrawable) drawable.mutate()).setStroke(px, colorPrimaryDark);
        //((GradientDrawable) drawable.mutate()).setColor(colorPrimaryLight);

        imageView.setBackground(drawable);

    }
}