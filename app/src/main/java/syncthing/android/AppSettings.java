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

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.opensilk.common.core.app.PreferencesWrapper;
import org.opensilk.common.core.dagger2.ForApplication;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import syncthing.android.model.Credentials;
import timber.log.Timber;

/**
 * Created by drew on 3/8/15.
 */
@Singleton
public class AppSettings extends PreferencesWrapper {

    public static final String DEFAULT_CREDENTIALS = "TRANSIENT_default_credentials";
    public static final String SAVED_CREDENTIALS = "TRANSIENT_saved_credentials";
    public static final String KEEP_SCREEN_ON = "keep_screen_on";

    final Gson gson;
    final Context appContext;
    final SharedPreferences prefs;

    @Inject
    public AppSettings(Gson gson, @ForApplication Context appContext) {
        this.gson = gson;
        this.appContext = appContext;
        prefs = PreferenceManager.getDefaultSharedPreferences(appContext);
    }

    @Override
    public SharedPreferences getPrefs() {
        return prefs;
    }

    public Set<Credentials> getSavedCredentials() {
        String str = getString(SAVED_CREDENTIALS, null);
        if (str == null) {
            return new HashSet<>();
        } else {
            return (Set<Credentials>)gson.fromJson(str, new TypeToken<Set<Credentials>>(){}.getType());
        }
    }

    public List<Credentials> getSavedCredentialsSorted() {
        Set<Credentials> set = getSavedCredentials();
        List<Credentials> sorted = new ArrayList<>(set);
        Collections.sort(sorted, (lhs, rhs) -> lhs.alias.compareTo(rhs.alias));
        //TODO remove
        if (BuildConfig.DEBUG) {
            for (Credentials creds : sorted) {
                Timber.d(ReflectionToStringBuilder.reflectionToString(creds));
            }
        }
        return sorted;
    }

    public Set<Credentials> saveCredentials(Credentials creds) {
        Set<Credentials> oldSet = getSavedCredentials();
        if (oldSet.contains(creds)) oldSet.remove(creds);
        oldSet.add(creds);
        putString(SAVED_CREDENTIALS, gson.toJson(oldSet,
                new TypeToken<Set<Credentials>>() {
                }.getType()));
        //update default
        Credentials def = getDefaultCredentials();
        if (def == null || creds.equals(def)) {
            setDefaultCredentials(creds);
        }
        return oldSet;
    }

    public Set<Credentials> removeCredentials(Credentials creds) {
        Set<Credentials> oldSet = getSavedCredentials();
        oldSet.remove(creds);
        putString(SAVED_CREDENTIALS, gson.toJson(oldSet,
                new TypeToken<Set<Credentials>>() {
                }.getType()));
        //update default
        if (creds.equals(getDefaultCredentials())) {
            Iterator<Credentials> ii = oldSet.iterator();
            if (ii.hasNext()) {
                //set a random device as default
                setDefaultCredentials(ii.next());
            } else {
                //no devices left
                remove(DEFAULT_CREDENTIALS);
            }
        }
        return oldSet;
    }

    @Nullable
    public Credentials getDefaultCredentials() {
        String str = getString(DEFAULT_CREDENTIALS, null);
        if (str != null) {
            return gson.fromJson(str, Credentials.class);
        }
        return null;
    }

    public void setDefaultCredentials(Credentials credentials) {
        putString(DEFAULT_CREDENTIALS, gson.toJson(credentials));
    }

    public boolean keepScreenOn() {
        return getBoolean(KEEP_SCREEN_ON, false);
    }

}
