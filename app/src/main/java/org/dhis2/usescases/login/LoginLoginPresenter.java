package org.dhis2.usescases.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.view.View;

import com.github.pwittchen.rxbiometric.library.RxBiometric;
import com.github.pwittchen.rxbiometric.library.validation.RxPreconditions;

import org.dhis2.App;
import org.dhis2.R;
import org.dhis2.data.server.ConfigurationRepository;
import org.dhis2.data.server.UserManager;
import org.dhis2.usescases.main.MainActivity;
import org.dhis2.usescases.qrScanner.QRActivity;
import org.dhis2.utils.Constants;
import org.hisp.dhis.android.core.maintenance.D2Error;
import org.hisp.dhis.android.core.systeminfo.SystemInfo;

import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.databinding.ObservableField;
import de.adorsys.android.securestoragelibrary.SecurePreferences;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.Response;
import timber.log.Timber;

public class LoginLoginPresenter implements LoginContracts.LoginPresenter {

    private static final String SESSION_LOCKED = "SessionLocked";
    private final ConfigurationRepository configurationRepository;
    private LoginContracts.LoginView loginView;

    private UserManager userManager;
    private CompositeDisposable disposable;

    private ObservableField<Boolean> isServerUrlSet = new ObservableField<>(false);
    private ObservableField<Boolean> isUserNameSet = new ObservableField<>(false);
    private ObservableField<Boolean> isUserPassSet = new ObservableField<>(false);
    private Boolean canHandleBiometrics;

    LoginLoginPresenter(ConfigurationRepository configurationRepository) {
        this.configurationRepository = configurationRepository;
    }

    @SuppressWarnings("squid:S2583")
    @Override
    public void init(LoginContracts.LoginView loginView) {
        this.loginView = loginView;
        this.disposable = new CompositeDisposable();

        userManager = null;
        if (((App) loginView.getContext().getApplicationContext()).getServerComponent() != null)
            userManager = ((App) loginView.getContext().getApplicationContext()).getServerComponent().userManager();

        if (userManager != null) {
            disposable.add(userManager.isUserLoggedIn()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(isUserLoggedIn -> {
                        SharedPreferences prefs = loginView.getAbstracContext().getSharedPreferences(
                                Constants.SHARE_PREFS, Context.MODE_PRIVATE);
                        if (isUserLoggedIn && !prefs.getBoolean(SESSION_LOCKED, false)) {
                            loginView.startActivity(MainActivity.class, null, true, true, null);
                        } else if (prefs.getBoolean(SESSION_LOCKED, false)) {
                            loginView.getBinding().unlockLayout.setVisibility(View.VISIBLE);
                        }

                    }, Timber::e));

            disposable.add(
                    Observable.just(userManager.getD2().systemInfoModule().systemInfo.getWithAllChildren() != null ?
                            userManager.getD2().systemInfoModule().systemInfo.getWithAllChildren() : SystemInfo.builder().build())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    systemInfo -> {
                                        if (systemInfo.contextPath() != null)
                                            loginView.setUrl(systemInfo.contextPath());
                                        else
                                            loginView.setUrl(loginView.getContext().getString(R.string.login_https));
                                    },
                                    Timber::e));
        } else
            loginView.setUrl(loginView.getContext().getString(R.string.login_https));


        if (false && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) //TODO: REMOVE FALSE WHEN GREEN LIGHT
            disposable.add(RxPreconditions
                    .hasBiometricSupport(loginView.getContext())
                    .filter(canHandleBiometricsResult -> {
                        this.canHandleBiometrics = canHandleBiometrics;
                        return canHandleBiometrics && SecurePreferences.contains(Constants.SECURE_SERVER_URL);
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            canHandleBiometricsResult2 -> loginView.showBiometricButton(),
                            Timber::e));


    }

    @Override
    public void onServerChanged(CharSequence s, int start, int before, int count) {
        boolean testingSet = false;
        isServerUrlSet.set(!loginView.getBinding().serverUrl.getEditText().getText().toString().isEmpty());
        loginView.resetCredentials(false, true, true);

        if (isServerUrlSet.get() && !testingSet &&
                (loginView.getBinding().serverUrl.getEditText().getText().toString().equals(Constants.URL_TEST_229) ||
                        loginView.getBinding().serverUrl.getEditText().getText().toString().equals(Constants.URL_TEST_230))) {
            loginView.setTestingCredentials();
        }

        loginView.setLoginVisibility(isServerUrlSet.get() && isUserNameSet.get() && isUserPassSet.get());


    }

    @Override
    public void onUserChanged(CharSequence s, int start, int before, int count) {
        isUserNameSet.set(!loginView.getBinding().userName.getEditText().getText().toString().isEmpty());
        loginView.resetCredentials(false, false, true);

        loginView.setLoginVisibility(isServerUrlSet.get() && isUserNameSet.get() && isUserPassSet.get());

    }

    @Override
    public void onPassChanged(CharSequence s, int start, int before, int count) {
        isUserPassSet.set(!loginView.getBinding().userPass.getEditText().getText().toString().isEmpty());
        loginView.setLoginVisibility(isServerUrlSet.get() && isUserNameSet.get() && isUserPassSet.get());
    }

    @Override
    public void onButtonClick() {
        loginView.hideKeyboard();
        SharedPreferences prefs = loginView.getAbstracContext().getSharedPreferences(
                Constants.SHARE_PREFS, Context.MODE_PRIVATE);
        if (!prefs.getBoolean(Constants.USER_ASKED_CRASHLYTICS, false))
            loginView.showCrashlyticsDialog();
        else
            loginView.showLoginProgress(true);
    }

    @Override
    public void logIn(String serverUrl, String userName, String pass) {
        HttpUrl baseUrl = HttpUrl.parse(canonizeUrl(serverUrl));
        if (baseUrl == null) {
            return;
        }
        disposable.add(
                configurationRepository.configure(baseUrl)
                        .map(config -> ((App) loginView.getAbstractActivity().getApplicationContext()).createServerComponent(config).userManager())
                        .switchMap(userManagerResult -> {
                            SharedPreferences prefs = loginView.getAbstractActivity().getSharedPreferences(
                                    Constants.SHARE_PREFS, Context.MODE_PRIVATE);
                            prefs.edit().putString(Constants.SERVER, serverUrl).apply();
                            this.userManager = userManagerResult;
                            return userManagerResult.logIn(userName.trim(), pass).map(user -> {
                                if (user == null)
                                    return Response.error(404, ResponseBody.create(MediaType.parse("text"), "NOT FOUND"));
                                else {
                                    prefs.edit().putString(Constants.USER, user.userCredentials().username());
                                    return Response.success(null);
                                }
                            });
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                this::handleResponse,
                                this::handleError));
    }

    private String canonizeUrl(@NonNull String serverUrl) {
        String urlToCanonized = serverUrl.trim();
        urlToCanonized = urlToCanonized.replace(" ", "");
        return urlToCanonized.endsWith("/") ? urlToCanonized : urlToCanonized + "/";
    }

    @Override
    public void onQRClick(View v) {
        Intent intent = new Intent(loginView.getContext(), QRActivity.class);
        loginView.getAbstractActivity().startActivityForResult(intent, Constants.RQ_QR_SCANNER);
    }

    @Override
    public void onVisibilityClick(View v) {
        loginView.switchPasswordVisibility();
    }

    @Override
    public ObservableField<Boolean> isServerUrlSet() {
        return isServerUrlSet;
    }

    @Override
    public ObservableField<Boolean> isUserNameSet() {
        return isUserNameSet;
    }

    @Override
    public ObservableField<Boolean> isUserPassSet() {
        return isServerUrlSet;
    }

    @Override
    public void unlockSession(String pin) {
        SharedPreferences prefs = loginView.getAbstracContext().getSharedPreferences(
                Constants.SHARE_PREFS, Context.MODE_PRIVATE);
        if (prefs.getString("pin", "").equals(pin)) {
            prefs.edit().putBoolean(SESSION_LOCKED, false).apply();
            loginView.startActivity(MainActivity.class, null, true, true, null);
        }
    }

    @Override
    public void onDestroy() {
        disposable.clear();
    }


    @Override
    public void logOut() {
        if (userManager != null)
            disposable.add(Observable.fromCallable(
                    userManager.getD2().userModule().logOut())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            data -> {
                                SharedPreferences prefs = loginView.getAbstracContext().getSharedPreferences();
                                prefs.edit().putBoolean(SESSION_LOCKED, false).apply();
                                prefs.edit().putString("pin", null).apply();
                                loginView.handleLogout();
                            },
                            t -> loginView.handleLogout()
                    )
            );
    }

    @Override
    public void handleResponse(@NonNull Response userResponse) {
        Timber.d("Authentication response url: %s", userResponse.raw().request().url().toString());
        Timber.d("Authentication response code: %s", userResponse.code());
        loginView.showLoginProgress(false);
        if (userResponse.isSuccessful()) {
            ((App) loginView.getContext().getApplicationContext()).createUserComponent();
            loginView.saveUsersData();
        }

    }

    @Override
    public void handleError(@NonNull Throwable throwable) {
        Timber.e(throwable);
        if (throwable instanceof IOException) {
            loginView.renderInvalidServerUrlError();
        } else if (throwable instanceof D2Error) {
            D2Error d2CallException = (D2Error) throwable;
            switch (d2CallException.errorCode()) {
                case ALREADY_AUTHENTICATED:
                    handleResponse(Response.success(null));
                    break;
                default:
                    loginView.renderError(d2CallException.errorCode(), d2CallException.errorDescription());
                    break;
            }
        } else {
            loginView.renderUnexpectedError();
        }

        loginView.showLoginProgress(false);
    }

    @Override
    public void onFingerprintClick() {
        disposable.add(
                RxBiometric
                        .title("Title")
                        .description("description")
                        .negativeButtonText("Cancel")
                        .negativeButtonListener((dialog, which) -> {
                        })
                        .executor(ActivityCompat.getMainExecutor(loginView.getAbstractActivity()))
                        .build()
                        .authenticate(loginView.getAbstractActivity())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                () -> loginView.checkSecuredCredentials(),
                                error -> loginView.displayMessage("AUTH ERROR")));
    }

    @Override
    public Boolean canHandleBiometrics() {
        return canHandleBiometrics;
    }


}