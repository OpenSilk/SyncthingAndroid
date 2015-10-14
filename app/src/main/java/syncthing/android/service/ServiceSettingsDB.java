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

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.opensilk.common.core.dagger2.ForApplication;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by drew on 10/13/15.
 */
@Singleton
public class ServiceSettingsDB extends SQLiteOpenHelper {

    static final String DBNAME = "settings.db";
    static final int VERSION = 1;

    @Inject
    public ServiceSettingsDB(@ForApplication Context context) {
        super(context, DBNAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        onUpgrade(db, 0, VERSION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion == 0) {
            db.execSQL("CREATE TABLE IF NOT EXISTS service_settings (" +
                    "key VARCHAR(32) NOT NULL UNIQUE ON CONFLICT REPLACE, " +
                    "intVal INTEGER, " +
                    "textVal TEXT " +
                    ");");
            db.execSQL("CREATE INDEX IF NOT EXISTS service_settings_key_idx on service_settings(key);");
        }
    }

    public interface SCHEMA {
            String TABLE = "service_settings";
            String KEY = "key";
            String INT_VAL = "intVal";
            String TEXT_VAL = "textVal";
    }
}
