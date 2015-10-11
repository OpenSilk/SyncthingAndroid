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

package syncthing.android.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by drew on 3/6/15.
 */
public class Credentials implements Parcelable {
    public static final Credentials NONE = new Credentials(null, null);

    public final String alias;
    public final String id;
    public final String url;
    public final String apiKey;
    public final String caCert;

    @Deprecated
    public Credentials(String url, String apiKey) {
        this(null, null, url, apiKey, null);
    }

    public Credentials(String alias, String id, String url, String apiKey, String caCert) {
        this.alias = alias;
        this.id = id;
        this.url = url;
        this.apiKey = apiKey;
        this.caCert = caCert;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Credentials that = (Credentials) o;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(alias);
        dest.writeString(id);
        dest.writeString(url);
        dest.writeString(apiKey);
        dest.writeString(caCert);
    }

    public static final Creator<Credentials> CREATOR = new Creator<Credentials>() {
        @Override
        public Credentials createFromParcel(Parcel source) {
            return new Credentials(
                    source.readString(),
                    source.readString(),
                    source.readString(),
                    source.readString(),
                    source.readString()
            );
        }

        @Override
        public Credentials[] newArray(int size) {
            return new Credentials[0];
        }
    };
}
