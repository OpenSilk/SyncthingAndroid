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

package syncthing.android.ui.sessionsettings;

import android.os.Bundle;
import android.view.View;

import org.opensilk.common.core.mortar.DaggerService;
import org.opensilk.common.ui.mortarfragment.MortarFragment;

import mortar.MortarScope;
import syncthing.api.Credentials;

/**
 * Created by drew on 10/11/15.
 */
public abstract class EditFragment2 extends MortarFragment {

    protected Credentials mCredentials;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        EditPresenterBindingComponent cmp = DaggerService.getDaggerComponent(getScope());
        cmp.presenterBinding().bindView(view);
    }

    protected static Bundle putCredentials(Credentials credentials) {
        Bundle b = new Bundle();
        b.putParcelable("creds", credentials);
        return b;
    }

    protected void ensureCredentials() {
        if (mCredentials == null) {
            getArguments().setClassLoader(getClass().getClassLoader());
            mCredentials = getArguments().getParcelable("creds");
        }
        if (mCredentials == null) {
            throw new NullPointerException("You forgot to supply credentials to the session");
        }
    }

    @Override
    protected Object[] getAdditionalServices() {
        return new Object[] {
                TitleService.TITLE_SERVICE,
                getArguments().getInt("title")
        };
    }

    //Hack cause im lazy and didn't want to add the title to all the screens
    public static class TitleService {
        public static final String TITLE_SERVICE = TitleService.class.getName();

        public static int getTitle(MortarScope scope) {
            return (int) scope.getService(TITLE_SERVICE);
        }
    }
}
