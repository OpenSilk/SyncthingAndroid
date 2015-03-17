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

package syncthing.android.ui.session.edit;

import org.opensilk.common.mortar.HasScope;

import javax.inject.Inject;

import mortar.Presenter;
import mortar.bundler.BundleService;

/**
 * Created by drew on 3/16/15.
 */
@EditScope
public class EditFragmentPresenter extends Presenter<EditFragmentPresenter.DialogOwner> {

    public interface DialogOwner extends HasScope {
        void dismiss();
    }

    @Inject
    public EditFragmentPresenter() {
    }

    @Override
    protected BundleService extractBundleService(DialogOwner view) {
        return BundleService.getBundleService(view.getScope());
    }

    public void dismissDialog() {
        if (hasView()) getView().dismiss();
    }
}
