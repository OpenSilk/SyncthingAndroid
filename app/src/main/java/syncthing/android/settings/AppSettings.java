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

package syncthing.android.settings;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;
import org.opensilk.common.core.app.PreferencesWrapper;
import org.opensilk.common.core.dagger2.ForApplication;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import syncthing.api.Credentials;

/**
 * Created by drew on 3/8/15.
 */
@Singleton
public class AppSettings extends PreferencesWrapper {

    public static final String KEEP_SCREEN_ON = "keep_screen_on";

    final Gson gson;
    final Context appContext;
    final SharedPreferences prefs;
    final CredentialsDB db;

    @Inject
    public AppSettings(
            Gson gson,
            @ForApplication Context appContext,
            CredentialsDB db
    ) {
        this.gson = gson;
        this.appContext = appContext;
        prefs = PreferenceManager.getDefaultSharedPreferences(appContext);
        this.db = db;
        migrateToDb();
    }

    @Override
    public SharedPreferences getPrefs() {
        return prefs;
    }

    static final String[] credentialCols = new String[] {
            CredentialsDB.SCHEMA._ID,
            CredentialsDB.SCHEMA.ALIAS,
            CredentialsDB.SCHEMA.DEVICE_ID,
            CredentialsDB.SCHEMA.URL,
            CredentialsDB.SCHEMA.API_KEY,
            CredentialsDB.SCHEMA.CERT,
    };

    static final String[] idCols = new String[] {
            CredentialsDB.SCHEMA._ID,
    };

    static final String credentialsDeviceIdSel = CredentialsDB.SCHEMA.DEVICE_ID + "=?";
    static final String credentialsDefaultSel = CredentialsDB.SCHEMA.DEFAULT + "=?";
    static final String idSel = CredentialsDB.SCHEMA._ID + "=?";

    public Set<Credentials> getSavedCredentials() {
        return new HashSet<>(getSavedCredentialsSorted());
    }

    public List<Credentials> getSavedCredentialsSorted() {
        Cursor c = null;
        try {
            c = db.getReadableDatabase().query(CredentialsDB.SCHEMA.TABLE,
                    credentialCols, null, null, CredentialsDB.SCHEMA.ALIAS, null, null);
            List<Credentials> credentials = new ArrayList<>();
            if (c != null && c.moveToFirst()) {
                do {
                    credentials.add(new Credentials(c.getString(1), c.getString(2),
                            c.getString(3), c.getString(4), c.getString(5)));
                } while (c.moveToNext());
            }
            return credentials;
        } finally {
            if (c != null) c.close();
        }
    }

    public @Nullable Credentials getSavedCredentials(String deviceId) {
        Cursor c = null;
        try {
            c = db.getReadableDatabase().query(CredentialsDB.SCHEMA.TABLE,
                    credentialCols, credentialsDeviceIdSel, new String[]{deviceId}, null, null, null);
            if (c != null && c.moveToFirst()) {
                return new Credentials(c.getString(1), c.getString(2),
                        c.getString(3), c.getString(4), c.getString(5));
            } else {
                return null;
            }
        } finally {
            if (c != null) c.close();
        }
    }

    public void saveCredentials(Credentials creds) {
        SQLiteDatabase _db = db.getWritableDatabase();
        Cursor c = null;
        try {
            ContentValues cv = new ContentValues();
            cv.put(CredentialsDB.SCHEMA.ALIAS, creds.alias);
            cv.put(CredentialsDB.SCHEMA.URL, creds.url);
            cv.put(CredentialsDB.SCHEMA.API_KEY, creds.apiKey);
            cv.put(CredentialsDB.SCHEMA.CERT, creds.caCert);
            String[] sel = new String[]{creds.id};
            _db.beginTransaction();
            c = _db.query(CredentialsDB.SCHEMA.TABLE,
                    idCols, credentialsDeviceIdSel, sel, null, null, null);
            if (c != null && c.getCount() > 0) {
                _db.update(CredentialsDB.SCHEMA.TABLE, cv,
                        credentialsDeviceIdSel, sel);
            } else {
                cv.put(CredentialsDB.SCHEMA.DEVICE_ID, creds.id);
                _db.insert(CredentialsDB.SCHEMA.TABLE, null, cv);
            }
            _db.setTransactionSuccessful();
        } finally {
            _db.endTransaction();
            if (c != null) c.close();
        }
    }

    public void removeCredentials(Credentials creds) {
        SQLiteDatabase _db = db.getWritableDatabase();
        Cursor c = null;
        try {
            _db.beginTransaction();
            _db.delete(CredentialsDB.SCHEMA.TABLE, credentialsDeviceIdSel, new String[]{creds.id});
            c = _db.query(CredentialsDB.SCHEMA.TABLE, idCols,
                    credentialsDefaultSel, null, null, null, null);
            if (c != null && c.getCount() == 0) {
                c.close();
                //no default set a new one
                c = _db.query(CredentialsDB.SCHEMA.TABLE, idCols, null, null, null, null, null);
                if (c != null && c.moveToFirst()) {
                    ContentValues cv = new ContentValues();
                    cv.put(CredentialsDB.SCHEMA.DEFAULT, 1);
                    _db.update(CredentialsDB.SCHEMA.TABLE, cv, idSel, new String[]{c.getString(0)});
                }
            }
            _db.setTransactionSuccessful();
        } finally {
            _db.endTransaction();
            if (c != null) c.close();
        }
    }

    public @Nullable Credentials getDefaultCredentials() {
        Cursor c = null;
        try {
            c = db.getReadableDatabase().query(CredentialsDB.SCHEMA.TABLE,
                    credentialCols, credentialsDefaultSel, new String[]{"1"}, null, null, null);
            if (c != null && c.moveToFirst()) {
                return new Credentials(c.getString(1), c.getString(2),
                        c.getString(3), c.getString(4), c.getString(5));
            }
        } finally {
            if (c != null) c.close();
        }
        return null;
    }

    public void setDefaultCredentials(Credentials creds) {
        SQLiteDatabase _db = db.getWritableDatabase();
        try {
            _db.beginTransaction();
            ContentValues cv = new ContentValues();
            cv.put(CredentialsDB.SCHEMA.DEFAULT, 0);
            //first unset default for everyone
            _db.update(CredentialsDB.SCHEMA.TABLE, cv, null, null);
            cv.put(CredentialsDB.SCHEMA.DEFAULT, 1);
            String[] sel = new String[]{creds.id};
            //set default for specified device
            _db.update(CredentialsDB.SCHEMA.TABLE, cv,
                    credentialsDeviceIdSel, sel);
            _db.setTransactionSuccessful();
        } finally {
            _db.endTransaction();
        }
    }

    public boolean keepScreenOn() {
        return getBoolean(KEEP_SCREEN_ON, false);
    }

    @SuppressWarnings("unchecked")
    private void migrateToDb() {
        if (getPrefs().contains("TRANSIENT_saved_credentials")) {
            String str = getPrefs().getString("TRANSIENT_saved_credentials", null);
            if (!StringUtils.isEmpty(str)) {
                Set<Credentials> credentialses = (Set<Credentials>)gson.fromJson(str,
                        new TypeToken<Set<Credentials>>(){}.getType());
                for (Credentials c : credentialses) {
                    saveCredentials(c);
                }
            }
            getPrefs().edit().remove("TRANSIENT_saved_credentials").commit();
        }
        if (getPrefs().contains("TRANSIENT_default_credentials")) {
            String str = getPrefs().getString("TRANSIENT_default_credentials", null);
            Credentials def = gson.fromJson(str, Credentials.class);
            if (def != null) {
                setDefaultCredentials(def);
            }
            getPrefs().edit().remove("TRANSIENT_default_credentials").commit();
        }
    }

}
