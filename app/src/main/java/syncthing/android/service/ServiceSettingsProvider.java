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

package syncthing.android.service;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.opensilk.common.core.mortar.DaggerService;
import org.opensilk.common.core.util.BundleHelper;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.inject.Inject;

/**
 * Created by drew on 10/13/15.
 */
public class ServiceSettingsProvider extends ContentProvider {

    @Inject ServiceSettingsDB mDB;
    private final ReentrantReadWriteLock mLock = new ReentrantReadWriteLock(true);

    @Override
    public boolean onCreate() {
        ServiceComponent cmp = DaggerService.getDaggerComponent(getContext());
        cmp.inject(this);
        return true;
    }

    @Nullable
    @Override
    public Bundle call(String method, String arg, Bundle extras) {
        if (StringUtils.equals("get_settings", method)) {
            if (arg == null) arg = "_NONE_";
            switch (arg) {
                case ServiceSettings.ENABLED:
                case ServiceSettings.INITIALISED:
                case ServiceSettings.ONLY_CHARGING:
                case ServiceSettings.ONLY_WIFI: {
                    Cursor c = null;
                    try {
                        c = getIntSetting(arg);
                        if (c != null && c.moveToNext()) {
                            return BundleHelper.b().putInt(c.getInt(0)).get();
                        } else {
                            return extras;
                        }
                    } finally {
                        if (c != null) c.close();
                    }
                }
                case ServiceSettings.RUN_WHEN:
                case ServiceSettings.RANGE_START:
                case ServiceSettings.RANGE_END:
                case ServiceSettings.WIFI_NETWORKS: {
                    Cursor c = null;
                    try {
                        c = getTextSetting(arg);
                        if (c != null && c.moveToNext()) {
                            return BundleHelper.b().putString(c.getString(0)).get();
                        } else {
                            return extras;
                        }
                    } finally {
                        if (c != null) c.close();
                    }
                }
                default:
                    return null;
            }
        } else if (StringUtils.equals("put_settings", method)) {
            if (arg == null) arg = "_NONE_";
            switch (arg) {
                case ServiceSettings.ENABLED:
                case ServiceSettings.INITIALISED:
                case ServiceSettings.ONLY_CHARGING:
                case ServiceSettings.ONLY_WIFI: {
                    long id = putIntSetting(arg, BundleHelper.getInt(extras));
                    return BundleHelper.b().putString(id > 0 ? "ok" : "err").get();
                }
                case ServiceSettings.RUN_WHEN:
                case ServiceSettings.RANGE_START:
                case ServiceSettings.RANGE_END:
                case ServiceSettings.WIFI_NETWORKS: {
                    long id = putTextSetting(arg, BundleHelper.getString(extras));
                    return BundleHelper.b().putString(id > 0 ? "ok" : "err").get();
                }
                default:
                    return null;
            }
        } else {
            return null;
        }
    }

    private static final String[] intCols = new String[] {
            ServiceSettingsDB.SCHEMA.INT_VAL
    };
    private static final String[] textCols = new String[] {
            ServiceSettingsDB.SCHEMA.TEXT_VAL
    };
    private static final String sel = ServiceSettingsDB.SCHEMA.KEY + "=?";

    private Cursor getTextSetting(String key) {
        ReentrantReadWriteLock.ReadLock lock = mLock.readLock();
        try {
            lock.lock();
            return mDB.getReadableDatabase().query(
                    ServiceSettingsDB.SCHEMA.TABLE,
                    textCols, sel, new String[]{key}, null, null, null);
        } finally {
            lock.unlock();
        }
    }

    private Cursor getIntSetting(String key) {
        ReentrantReadWriteLock.ReadLock lock = mLock.readLock();
        try {
            lock.lock();
            return mDB.getReadableDatabase().query(
                    ServiceSettingsDB.SCHEMA.TABLE,
                    intCols, sel, new String[]{key}, null, null, null);
        } finally {
            lock.unlock();
        }
    }

    private long putTextSetting(String key, String val) {
        ReentrantReadWriteLock.WriteLock lock = mLock.writeLock();
        try {
            lock.lock();
            ContentValues cv = new ContentValues(2);
            cv.put(ServiceSettingsDB.SCHEMA.KEY, key);
            cv.put(ServiceSettingsDB.SCHEMA.TEXT_VAL, val);
            return mDB.getWritableDatabase().insertWithOnConflict(
                    ServiceSettingsDB.SCHEMA.TABLE,
                    null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        } finally {
            lock.unlock();
        }
    }

    private long putIntSetting(String key, int val) {
        ReentrantReadWriteLock.WriteLock lock = mLock.writeLock();
        try {
            lock.lock();
            ContentValues cv = new ContentValues(2);
            cv.put(ServiceSettingsDB.SCHEMA.KEY, key);
            cv.put(ServiceSettingsDB.SCHEMA.INT_VAL, val);
            return mDB.getWritableDatabase().insertWithOnConflict(
                    ServiceSettingsDB.SCHEMA.TABLE,
                    null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        } finally {
            lock.unlock();
        }
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        throw new UnsupportedOperationException();
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }
}
