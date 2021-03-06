package org.dhis2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.dhis2.data.forms.FormComponent;
import org.dhis2.data.forms.FormModule;
import org.dhis2.data.server.ServerComponent;
import org.dhis2.data.user.UserComponent;

import org.dhis2.usescases.login.LoginComponent;
import org.dhis2.usescases.sync.SyncComponent;
import org.hisp.dhis.android.core.configuration.ConfigurationModel;

public interface Components {

    @NonNull
    AppComponent appComponent();

    ///////////////////////////////////////////////////////////////////
    // Login component
    ///////////////////////////////////////////////////////////////////


    @NonNull
    LoginComponent createLoginComponent();

    @Nullable
    LoginComponent loginComponent();

    void releaseLoginComponent();


    ///////////////////////////////////////////////////////////////////
    // Synchronization component
    ///////////////////////////////////////////////////////////////////


    @NonNull
    SyncComponent createSyncComponent();

    @Nullable
    SyncComponent syncComponent();

    void releaseSyncComponent();


    ////////////////////////////////////////////////////////////////////
    // Server component
    ////////////////////////////////////////////////////////////////////

    @NonNull
    ServerComponent createServerComponent(@NonNull ConfigurationModel configuration);

    @Nullable
    ServerComponent serverComponent();

    void releaseServerComponent();

    ////////////////////////////////////////////////////////////////////
    // User component
    ////////////////////////////////////////////////////////////////////

    @NonNull
    UserComponent createUserComponent();

    @Nullable
    UserComponent userComponent();

    void releaseUserComponent();

    ////////////////////////////////////////////////////////////////////
    // Form component
    ////////////////////////////////////////////////////////////////////

    @NonNull
    FormComponent createFormComponent(@NonNull FormModule formModule);

    @Nullable
    FormComponent formComponent();

    void releaseFormComponent();
}
