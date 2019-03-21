package org.dhis2.usescases.splash;

import androidx.annotation.UiThread;

import org.dhis2.usescases.general.AbstractActivityContracts;

import io.reactivex.functions.Consumer;

public class SplashContracts {

    interface SplashView extends AbstractActivityContracts.View {

        Consumer<Integer> renderFlag();
    }

    interface SplashPresenter {
        void destroy();

        void init(SplashView splashView);

        @UiThread
        void isUserLoggedIn();

        @UiThread
        void navigateToLoginView();

        @UiThread
        void navigateToHomeView();
    }
}