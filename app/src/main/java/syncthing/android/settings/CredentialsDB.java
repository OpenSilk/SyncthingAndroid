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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import org.opensilk.common.core.dagger2.ForApplication;

import javax.inject.Inject;

/**
 * Created by drew on 11/5/15.
 */
public class CredentialsDB extends SQLiteOpenHelper {
    static final String DBNAME = "credentials.db";
    static final int VERSION = 1;

    @Inject
    public CredentialsDB(@ForApplication Context context) {
        super(context, DBNAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        onUpgrade(db, 0, VERSION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion == 0) {
            db.execSQL("CREATE TABLE IF NOT EXISTS credentials (" +
                    "_id INTEGER PRIMARY KEY, " +
                    "alias TEXT," +
                    "device_id TEXT NOT NULL UNIQUE COLLATE NOCASE, " +
                    "url TEXT NOT NULL, " +
                    "api_key TEXT NOT NULL, " +
                    "cert TEXT, " +
                    "is_def INTEGER DEFAULT 0" +
                    ");");
        }
    }

    public interface SCHEMA extends BaseColumns {
        String TABLE = "credentials";
        String ALIAS = "alias";
        String DEVICE_ID = "device_id";
        String URL = "url";
        String API_KEY = "api_key";
        String CERT = "cert";
        String DEFAULT = "is_def"; //1 == true
    }
}
