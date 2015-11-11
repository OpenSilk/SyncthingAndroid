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

import android.databinding.BindingAdapter;
import android.widget.RadioGroup;

import com.jakewharton.rxbinding.widget.RxRadioGroup;

import rx.functions.Action1;

/**
 * Created by drew on 11/11/15.
 */
public class RadioGroupEventBinding {

    @BindingAdapter("checkedChanges")
    public static void subscribeCheckedChanges(BindingSubscriptionsHolder bsh, RadioGroup radioGroup, Action1<Integer> onNext) {
        bsh.bindingSubscriptions().add(RxRadioGroup.checkedChanges(radioGroup).subscribe(onNext));
    }
}
