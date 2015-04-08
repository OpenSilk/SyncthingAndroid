/*
 * Copyright (c) 2015 OpenSilk Productions LLC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package syncthing.android;

import android.app.Application;
import android.os.StrictMode;
import android.text.TextUtils;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;
import org.acra.log.ACRALog;
import org.acra.sender.HttpSender;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

import mortar.MortarScope;
import mortar.dagger2support.DaggerService;
import syncthing.android.service.ServiceComponent;
import timber.log.Timber;

import static org.acra.ReportField.*;

/**
 * Created by drew on 3/4/15.
 */
@ReportsCrashes(
        /* yes you can find the login by decompiling the apk, please dont :) */
        formUri = BuildConfig.ACRA_REPORTING_URL,
        formUriBasicAuthLogin = BuildConfig.ACRA_REPORTING_USR,
        formUriBasicAuthPassword = BuildConfig.ACRA_REPORTING_PASS,
        httpMethod = HttpSender.Method.PUT,
        reportType = HttpSender.Type.JSON,
        customReportContent = {
                REPORT_ID,
                APP_VERSION_CODE,
                APP_VERSION_NAME,
                PACKAGE_NAME,
                PHONE_MODEL,
                BRAND,
                PRODUCT,
                ANDROID_VERSION,
                BUILD,
                TOTAL_MEM_SIZE,
                AVAILABLE_MEM_SIZE,
                BUILD_CONFIG,
                IS_SILENT,
                STACK_TRACE,
                INITIAL_CONFIGURATION,
                CRASH_CONFIGURATION,
                DISPLAY,
                USER_APP_START_DATE,
                USER_CRASH_DATE,
                INSTALLATION_ID,
                DEVICE_FEATURES,
                ENVIRONMENT,
                SHARED_PREFERENCES,
                THREAD_DETAILS,
        },
        excludeMatchingSharedPreferencesKeys = {
                "TRANSIENT.*", //Private stuff
        }
)
public class App extends Application {

    MortarScope rootScope;

    @Override
    public void onCreate() {
        Timber.d("onCreate()");
        super.onCreate();
        Timber.plant(new Timber.DebugTree());
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll().penaltyLog().build());
    }

    @Override
    public Object getSystemService(String name) {
        Timber.d("getSystemService(%s)", name);
        if (rootScope == null) {
            rootScope = MortarScope.buildRootScope()
                    .withService(DaggerService.SERVICE_NAME, makeComponent())
                    .build("ROOT");
        }
        if (rootScope.hasService(name)) {
            return rootScope.getService(name);
        }
        return super.getSystemService(name);

    }

    Object makeComponent() {
        if (isServiceProcess()) {
            return DaggerService.createComponent(ServiceComponent.class, new AppModule(this));
        } else {
            setupReporting();
            return DaggerService.createComponent(AppComponent.class, new AppModule(this));
        }
    }

    protected void setupReporting() {
        if (TextUtils.isEmpty(BuildConfig.ACRA_REPORTING_URL)) {
            return;
        }
        //ACRA.setLog(new AcraLogStub());
        ACRA.init(this);
    }

    boolean isServiceProcess() {
        try {
            final File comm = new File("/proc/self/comm");
            if (comm.exists() && comm.canRead()) {
                final List<String> commLines = FileUtils.readLines(comm);
                if (commLines.size() > 0) {
                    final String procName = commLines.get(0).trim();
                    Timber.i("%s >> %s ", comm.getAbsolutePath(), procName);
                    return procName.endsWith(":service");
                }
            }
        } catch (IOException ignored) { }
        return false;
    }
}
