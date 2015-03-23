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

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.CompoundButton;
import android.widget.Switch;

import java.util.List;

import butterknife.ButterKnife;
import syncthing.android.R;
import syncthing.android.service.ServiceSettings;
import syncthing.android.service.SyncthingInstance;

/**
 * Created by drew on 3/21/15.
 */
public class ServiceSettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {

    ListPreference runWhen;
    MultiSelectListPreference wifiNetwork;

    PreferenceCategory catInterval;
    PreferenceCategory catBetween;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(ServiceSettings.FILE_NAME);
        getPreferenceManager().setSharedPreferencesMode(Context.MODE_MULTI_PROCESS);

        addPreferencesFromResource(R.xml.prefs_service);

        runWhen = (ListPreference) findPreference(ServiceSettings.RUN_WHEN);
        runWhen.setOnPreferenceChangeListener(this);

        wifiNetwork = (MultiSelectListPreference) findPreference(ServiceSettings.WIFI_NETWORKS);
        String[] ssids = getWifiNetworks();
        wifiNetwork.setEntries(ssids);
        wifiNetwork.setEntryValues(ssids);

        catInterval = (PreferenceCategory) findPreference("cat_interval");
        catBetween = (PreferenceCategory) findPreference("cat_between");
        hideShowRunWhenCategories(getPreferenceManager().getSharedPreferences()
                .getString(ServiceSettings.RUN_WHEN, ServiceSettings.WHEN_OPEN));

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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
        boolean enabled = getPreferenceManager().getSharedPreferences().getBoolean(ServiceSettings.ENABLED, false);
        getPreferenceScreen().setEnabled(enabled);
        Switch enableSwitch = ButterKnife.findById(
                menu.findItem(R.id.menu_enable_switch).getActionView(), R.id.action_widget_switch);
        enableSwitch.setChecked(enabled);
        enableSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                getPreferenceManager().getSharedPreferences().edit().putBoolean(ServiceSettings.ENABLED, isChecked).commit();
                getPreferenceScreen().setEnabled(isChecked);
            }
        });
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == runWhen) {
            hideShowRunWhenCategories((String) newValue);
        }
        return true;
    }


    void hideShowRunWhenCategories(String runwhen) {
        switch (runwhen) {
            case ServiceSettings.PERIODIC:
                catInterval.setEnabled(true);
                catBetween.setEnabled(false);
                break;
            case ServiceSettings.SCHEDULED:
                catInterval.setEnabled(false);
                catBetween.setEnabled(true);
                break;
            default:
                catInterval.setEnabled(false);
                catBetween.setEnabled(false);
                break;
        }
    }

    String[] getWifiNetworks() {
        List<WifiConfiguration> networks = ((WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE)).getConfiguredNetworks();
        String[] ssids = new String[networks.size()];
        for (int ii=0 ; ii<networks.size(); ii++) {
            ssids[ii] = networks.get(ii).SSID;
        }
        return ssids;
    }
}
