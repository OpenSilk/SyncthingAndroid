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

import android.text.TextUtils;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;
import org.acra.sender.HttpSender;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.opensilk.common.core.app.BaseApp;
import org.opensilk.common.core.mortar.DaggerService;

import java.io.File;
import java.io.IOException;
import java.util.List;

import syncthing.android.service.ServiceComponent;
import syncthing.android.service.ServiceSettings;
import timber.log.Timber;

import static org.acra.ReportField.ANDROID_VERSION;
import static org.acra.ReportField.APP_VERSION_CODE;
import static org.acra.ReportField.APP_VERSION_NAME;
import static org.acra.ReportField.AVAILABLE_MEM_SIZE;
import static org.acra.ReportField.BRAND;
import static org.acra.ReportField.BUILD;
import static org.acra.ReportField.BUILD_CONFIG;
import static org.acra.ReportField.CRASH_CONFIGURATION;
import static org.acra.ReportField.DEVICE_FEATURES;
import static org.acra.ReportField.DISPLAY;
import static org.acra.ReportField.ENVIRONMENT;
import static org.acra.ReportField.INITIAL_CONFIGURATION;
import static org.acra.ReportField.INSTALLATION_ID;
import static org.acra.ReportField.IS_SILENT;
import static org.acra.ReportField.PACKAGE_NAME;
import static org.acra.ReportField.PHONE_MODEL;
import static org.acra.ReportField.PRODUCT;
import static org.acra.ReportField.REPORT_ID;
import static org.acra.ReportField.SHARED_PREFERENCES;
import static org.acra.ReportField.STACK_TRACE;
import static org.acra.ReportField.THREAD_DETAILS;
import static org.acra.ReportField.TOTAL_MEM_SIZE;
import static org.acra.ReportField.USER_APP_START_DATE;
import static org.acra.ReportField.USER_CRASH_DATE;

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
                THREAD_DETAILS,
        }
)
public class App extends BaseApp {

    private String mProcess;

    @Override
    public void onCreate() {
        Timber.d("onCreate()");
        super.onCreate();
        setupTimber(true, null);
        enableStrictMode();
    }

    @Override
    protected Object getRootComponent() {
        if (isServiceProcess()) {
            return ServiceComponent.FACTORY.call(this);
        } else if (isEmulator()) {
            return EmulatorComponent.FACTORY.call(this);
        } else {
            setupReporting();
            return AppComponent.FACTORY.call(this);
        }
    }

    protected void setupReporting() {
        if (TextUtils.isEmpty(BuildConfig.ACRA_REPORTING_URL)) {
            return;
        }
        //ACRA.setLog(new AcraLogStub());
        ACRA.init(this);
    }

    boolean isEmulator() {
        readProcess();
        return StringUtils.isEmpty(mProcess);
    }

    boolean isServiceProcess() {
        readProcess();
        return StringUtils.endsWith(mProcess, ":service");
    }

    void readProcess() {
        if (mProcess == null) {
            try {
                final File comm = new File("/proc/self/comm");
                if (comm.exists() && comm.canRead()) {
                    final List<String> commLines = FileUtils.readLines(comm);
                    if (commLines.size() > 0) {
                        final String procName = commLines.get(0).trim();
                        Timber.i("%s >> %s ", comm.getAbsolutePath(), procName);
                        mProcess = procName;
                    }
                }
            } catch (IOException ignored) {
                mProcess = "";
            }
        }
    }
}
