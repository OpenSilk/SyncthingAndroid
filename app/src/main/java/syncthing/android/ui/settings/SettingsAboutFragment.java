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

import android.app.AlertDialog;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.webkit.WebView;

import syncthing.android.BuildConfig;
import syncthing.android.R;

/**
 * Created by drew on 4/7/15.
 */
public class SettingsAboutFragment extends PreferenceFragment implements
        Preference.OnPreferenceClickListener {

    static final String PREF_LICENSES   = "open_source_licenses";
    static final String PREF_VERSION    = "app_version";

    Preference licenses;
    Preference version;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs_about);

        licenses = getPreferenceScreen().findPreference(PREF_LICENSES);
        licenses.setOnPreferenceClickListener(this);

        version = getPreferenceScreen().findPreference(PREF_VERSION);
        version.setSummary(BuildConfig.VERSION_NAME);
        version.setOnPreferenceClickListener(this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (licenses == preference) {
            createOpenSourceDialog().show();
        } else if (version == preference) {
            //TODO
        }
        return true;
    }

    AlertDialog createOpenSourceDialog() {
        final WebView webView = new WebView(getActivity());
        webView.loadUrl("file:///android_asset/licenses.html");
        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.open_source_licenses)
                .setView(webView)
                .setPositiveButton(android.R.string.ok, null)
                .create();
    }
}
