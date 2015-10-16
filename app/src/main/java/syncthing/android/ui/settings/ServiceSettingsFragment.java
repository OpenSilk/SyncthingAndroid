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

package syncthing.android.ui.settings;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import org.opensilk.common.core.mortar.DaggerService;
import org.opensilk.common.core.util.VersionUtils;

import java.io.File;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import butterknife.ButterKnife;
import syncthing.android.R;
import syncthing.android.service.ConfigXml;
import syncthing.android.service.ReceiverHelper;
import syncthing.android.service.ServiceSettings;
import syncthing.android.service.SyncthingInstance;
import syncthing.android.service.SyncthingUtils;
import syncthing.android.ui.common.ActivityRequestCodes;

/**
 * Created by drew on 3/21/15.
 */
public class ServiceSettingsFragment extends PreferenceFragment implements
        Preference.OnPreferenceClickListener,
        Preference.OnPreferenceChangeListener {

    ListPreference runWhen;
    CheckBoxPreference onlyOnWifi;
    MultiSelectListPreference wifiNetwork;
    CheckBoxPreference onlyCharging;
    TimePreference scheduleStart;
    TimePreference scheduleEnd;

    PreferenceCategory catBetween;

    Preference exportConfig;
    Preference importConfig;

    @Inject WifiManager mWifimanager;
    @Inject ServiceSettings mSettings;
    ReceiverHelper mReceiverHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SettingsActivityComponent cmp = DaggerService.getDaggerComponent(getActivity());
        cmp.inject(this);

        getPreferenceManager().setSharedPreferencesName(ServiceSettings.FILE_NAME);
        getPreferenceManager().setSharedPreferencesMode(Context.MODE_MULTI_PROCESS);

        addPreferencesFromResource(R.xml.prefs_service);

        mReceiverHelper = new ReceiverHelper(getActivity());

        runWhen = (ListPreference) findPreference(ServiceSettings.RUN_WHEN);
        runWhen.setPersistent(false);
        runWhen.setValue(mSettings.runWhen());
        runWhen.setOnPreferenceChangeListener(this);

        onlyOnWifi = (CheckBoxPreference) findPreference(ServiceSettings.ONLY_WIFI);
        onlyOnWifi.setPersistent(false);
        onlyOnWifi.setChecked(mSettings.onlyOnWifi());
        onlyOnWifi.setOnPreferenceChangeListener(this);

        wifiNetwork = (MultiSelectListPreference) findPreference(ServiceSettings.WIFI_NETWORKS);
        wifiNetwork.setPersistent(false);
        String[] ssids = getWifiNetworks();
        wifiNetwork.setEntries(ssids);
        wifiNetwork.setEntryValues(ssids);
        wifiNetwork.setValues(mSettings.allowedWifiNetworks());
        wifiNetwork.setOnPreferenceChangeListener(this);

        onlyCharging = (CheckBoxPreference) findPreference(ServiceSettings.ONLY_CHARGING);
        onlyCharging.setPersistent(false);
        onlyCharging.setChecked(mSettings.onlyWhenCharging());
        onlyCharging.setOnPreferenceChangeListener(this);

        catBetween = (PreferenceCategory) findPreference("cat_between");
        hideShowRunWhenCategories(mSettings.runWhen());

        scheduleStart = (TimePreference) findPreference(ServiceSettings.RANGE_START);
        scheduleStart.setPersistent(false);
        scheduleStart.setValue(mSettings.getScheduledStartTime());
        scheduleStart.setOnPreferenceChangeListener(this);

        scheduleEnd = (TimePreference) findPreference(ServiceSettings.RANGE_END);
        scheduleEnd.setPersistent(false);
        scheduleEnd.setValue(mSettings.getScheduledEndTime());
        scheduleEnd.setOnPreferenceChangeListener(this);

        exportConfig = findPreference("export");
        exportConfig.setOnPreferenceClickListener(this);
        importConfig = findPreference("import");
        importConfig.setOnPreferenceClickListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSettings.release();
        getActivity().startService(new Intent(getActivity(), SyncthingInstance.class).setAction(SyncthingInstance.REEVALUATE));
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.service_settings, menu);
        boolean enabled = isEnabled();
        getPreferenceScreen().setEnabled(enabled);
        Switch enableSwitch = ButterKnife.findById(
                menu.findItem(R.id.menu_enable_switch).getActionView(), R.id.action_widget_switch);
        enableSwitch.setChecked(enabled);
        enableSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setEnabled(isChecked);
                getPreferenceScreen().setEnabled(isChecked);
                updateReceievers();
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference == exportConfig) {
            SyncthingUtils.exportConfig(getActivity());
            return true;
        } else if (preference == importConfig) {
            final File[] availableExports = SyncthingUtils.listExportedConfigs(getActivity());
            if (availableExports == null || availableExports.length == 0) {
                showFilePicker();
            } else {
                String[] items = new String[availableExports.length];
                for (int ii=0; ii<items.length; ii++) {
                    items[ii] = availableExports[ii].getName();
                }
                AlertDialog.Builder bob = new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.import_config)
                        .setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                File file = availableExports[which];
                                if(file.exists()) {
                                    SyncthingUtils.importConfig(getActivity(), Uri.fromFile(file), false);
                                    mSettings.setInitialized(ConfigXml.get(getActivity()) != null);
                                }
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton(R.string.show_file_picker, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                showFilePicker();
                            }
                        });
                bob.show();
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == runWhen) {
            hideShowRunWhenCategories((String) newValue);
            mSettings.setRunWhen((String) newValue);
        } else if (preference == onlyOnWifi) {
            mSettings.setOnlyOnWifi((Boolean) newValue);
        } else if (preference == wifiNetwork) {
            mSettings.setAllowedWifiNetworks((Set<String>) newValue);
        } else if (preference == onlyCharging) {
            mSettings.setOnlyWhenCharging((Boolean) newValue);
        } else if (preference == scheduleStart) {
            mSettings.setScheduledStartTime((String) newValue);
        } else if (preference == scheduleEnd) {
            mSettings.setScheduledEndTime((String) newValue);
        }
        getView().post(new Runnable() {
            @Override
            public void run() {
                updateReceievers();
            }
        });
        return true;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ActivityRequestCodes.IMPORT_CONFIG && resultCode == Activity.RESULT_OK) {
            SyncthingUtils.importConfig(getActivity(), data.getData(), false);
            mSettings.setInitialized(ConfigXml.get(getActivity()) != null);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    void hideShowRunWhenCategories(String runwhen) {
        switch (runwhen) {
            case ServiceSettings.SCHEDULED:
                catBetween.setEnabled(true);
                break;
            default:
                catBetween.setEnabled(false);
                break;
        }
    }

    boolean isEnabled() {
        return !mSettings.isDisabled();
    }

    void setEnabled(boolean enabled) {
        mSettings.setEnabled(enabled);
        getActivity().startService(new Intent(getActivity(), SyncthingInstance.class).setAction(SyncthingInstance.REEVALUATE));
    }

    String[] getWifiNetworks() {
        List<WifiConfiguration> networks = mWifimanager.getConfiguredNetworks();
        if (networks == null) {
            return new String[0];
        }
        String[] ssids = new String[networks.size()];
        for (int ii=0 ; ii<networks.size(); ii++) {
            ssids[ii] = networks.get(ii).SSID;
        }
        return ssids;
    }

    void updateReceievers() {
        boolean enabled = isEnabled();
        if (!enabled) {
            mReceiverHelper.setBootReceiverEnabled(false);
            mReceiverHelper.setChargingReceiverEnabled(false);
            mReceiverHelper.setConnectivityReceiverEnabled(false);
        } else {
            //Dont care if not allowed to run in background
            mReceiverHelper.setBootReceiverEnabled(true);
            //dont care if we can run whenever
            mReceiverHelper.setChargingReceiverEnabled(onlyCharging.isChecked());
            mReceiverHelper.setConnectivityReceiverEnabled(true);
        }
    }

    void showFilePicker() {
        Intent intent = new Intent();
        intent.setType("application/zip");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        if (VersionUtils.hasKitkat()) {
            intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        } else {
            intent = Intent.createChooser(
                    intent.setAction(Intent.ACTION_GET_CONTENT), "");
        }
        try {
            startActivityForResult(intent, ActivityRequestCodes.IMPORT_CONFIG);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getActivity(), R.string.no_file_picker_found, Toast.LENGTH_LONG).show();
        }
    }
}
