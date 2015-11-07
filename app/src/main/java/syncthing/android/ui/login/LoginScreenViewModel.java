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

package syncthing.android.ui.login;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

/**
 * Created by drew on 11/6/15.
 */
public class LoginScreenViewModel extends BaseObservable {
    private String alias;
    private String host;
    private String port;
    private String user;
    private String pass;
    private boolean tls;

    @Bindable
    public String getAlias() {
        return alias;
    }

    public void setAlias(CharSequence alias) {
        this.alias = alias == null ? "" : alias.toString();
    }

    @Bindable
    public String getHost() {
        return host;
    }

    public void setHost(CharSequence host) {
        this.host = host == null ? "" : host.toString();
    }

    @Bindable
    public String getPort() {
        return port;
    }

    public void setPort(CharSequence port) {
        this.port = port == null ? "" : port.toString();
    }

    @Bindable
    public String getUser() {
        return user;
    }

    public void setUser(CharSequence user) {
        this.user = user == null ? "" : user.toString();
    }

    @Bindable
    public String getPass() {
        return pass;
    }

    public void setPass(CharSequence pass) {
        this.pass = pass == null ? "" : pass.toString();
    }

    @Bindable
    public boolean isTls() {
        return tls;
    }

    public void setTls(boolean tls) {
        this.tls = tls;
    }
}
