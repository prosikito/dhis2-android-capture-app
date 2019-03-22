package org.dhis2.usescases.sync;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.util.TypedValue;

import com.airbnb.lottie.LottieDrawable;

import org.dhis2.App;
import org.dhis2.Bindings.Bindings;
import org.dhis2.R;
import org.dhis2.databinding.ActivitySynchronizationBinding;
import org.dhis2.usescases.general.ActivityGlobalAbstract;
import org.dhis2.usescases.main.MainActivity;
import org.dhis2.utils.ColorUtils;
import org.dhis2.utils.Constants;
import org.dhis2.utils.SyncUtils;

import javax.inject.Inject;

import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;


public class SyncActivity extends ActivityGlobalAbstract implements SyncContracts.SyncView {

    ActivitySynchronizationBinding binding;

    @Inject
    SyncContracts.SyncPresenter syncPresenter;

    private BroadcastReceiver syncReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null && intent.getAction().equals("action_sync")) {
                if (SyncUtils.isSyncRunning()) {
                    handleSync(intent);
                } else {
                    handleEndSync(intent);
                }
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SyncComponent syncComponent = ((App) getApplicationContext()).syncComponent();
        if (syncComponent == null) {
            // in case if we don't have cached syncPresenter
            syncComponent = ((App) getApplicationContext()).createSyncComponent();
        }
        syncComponent.inject(this);
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_synchronization);
        binding.setPresenter(syncPresenter);
        syncPresenter.init(this);
        syncPresenter.syncMeta(getSharedPreferences().getInt(Constants.TIME_META, Constants.TIME_DAILY), Constants.META);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (binding.lottieView != null) {
            binding.lottieView.setRepeatCount(LottieDrawable.INFINITE);
            binding.lottieView.setRepeatMode(LottieDrawable.RESTART);
            binding.lottieView.useHardwareAcceleration(true);
            binding.lottieView.enableMergePathsForKitKatAndAbove(true);
            binding.lottieView.playAnimation();
        }
        Bindings.setDrawableEnd(binding.metadataText, ContextCompat.getDrawable(this, R.drawable.animator_sync));

    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(syncReceiver, new IntentFilter("action_sync"));
        handleSyncStatus();
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(syncReceiver);
        super.onPause();
    }

    public void handleSyncStatus() {

        if (SyncUtils.isSyncRunning(Constants.DATA)) {
            binding.metadataText.setText(getString(R.string.configuration_ready));
            Bindings.setDrawableEnd(binding.metadataText, ContextCompat.getDrawable(this, R.drawable.animator_done));
            syncPresenter.getTheme();
            binding.eventsText.setText(getString(R.string.syncing_data));
            Bindings.setDrawableEnd(binding.eventsText, ContextCompat.getDrawable(this, R.drawable.animator_sync));
            binding.eventsText.setAlpha(1.0f);
        }
    }

    public void handleEndSync(Intent intent) {
        if (!intent.getBooleanExtra("metaSyncInProgress", true)) {

            binding.metadataText.setText(getString(R.string.configuration_ready));
            Bindings.setDrawableEnd(binding.metadataText, ContextCompat.getDrawable(this, R.drawable.animator_done));
            syncPresenter.getTheme();
            syncPresenter.syncData(getSharedPreferences().getInt(Constants.TIME_DATA, Constants.TIME_DAILY), Constants.DATA);

        } else if (!intent.getBooleanExtra("dataSyncInProgress", true)) {

            binding.eventsText.setText(getString(R.string.data_ready));
            Bindings.setDrawableEnd(binding.eventsText, ContextCompat.getDrawable(this, R.drawable.animator_done));
            syncPresenter.syncReservedValues();
            startMain();
        }
    }

    @Override
    protected void onStop() {
        ((App) getApplicationContext()).releaseSyncComponent();
        if (binding.lottieView != null) {
            binding.lottieView.cancelAnimation();
        }
        syncPresenter.onDettach();
        super.onStop();
    }

    public void handleSync(Intent intent) {
        if (intent.getBooleanExtra("metaSyncInProgress", false)) {

            binding.metadataText.setText(getString(R.string.syncing_configuration));

        } else if (intent.getBooleanExtra("dataSyncInProgress", false)) {

            binding.eventsText.setText(getString(R.string.syncing_data));
            Bindings.setDrawableEnd(binding.eventsText, ContextCompat.getDrawable(this, R.drawable.animator_sync));
            binding.eventsText.setAlpha(1.0f);
        }
    }

    @Override
    public void saveTheme(Integer themeId) {
        SharedPreferences prefs = getAbstracContext().getSharedPreferences(
                Constants.SHARE_PREFS, Context.MODE_PRIVATE);
        prefs.edit().putInt(Constants.THEME, themeId).apply();
        setTheme(themeId);

        int startColor = ColorUtils.getPrimaryColor(this, ColorUtils.ColorType.PRIMARY);
        TypedValue typedValue = new TypedValue();
        TypedArray a = obtainStyledAttributes(typedValue.data, new int[]{R.attr.colorPrimary});
        int endColor = a.getColor(0, 0);
        a.recycle();

        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), startColor, endColor);
        colorAnimation.setDuration(2000); // milliseconds
        colorAnimation.addUpdateListener(animator -> binding.logo.setBackgroundColor((int) animator.getAnimatedValue()));
        colorAnimation.start();

    }

    @Override
    public void saveFlag(String s) {
        SharedPreferences prefs = getAbstracContext().getSharedPreferences(
                Constants.SHARE_PREFS, Context.MODE_PRIVATE);
        prefs.edit().putString("FLAG", s).apply();

        binding.logoFlag.setImageResource(getResources().getIdentifier(s, "drawable", getPackageName()));
        ValueAnimator alphaAnimator = ValueAnimator.ofFloat(0f, 1f);
        alphaAnimator.setDuration(2000);
        alphaAnimator.addUpdateListener(animation -> {
            binding.logoFlag.setAlpha((float) animation.getAnimatedValue());
            binding.dhisLogo.setAlpha((float) 0);
        });
        alphaAnimator.start();

    }


    public void startMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

}
