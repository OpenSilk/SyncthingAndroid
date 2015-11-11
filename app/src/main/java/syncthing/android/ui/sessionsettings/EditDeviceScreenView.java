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

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.widget.CheckBox;

import com.jakewharton.rxbinding.widget.RxCompoundButton;
import com.jakewharton.rxbinding.widget.RxRadioGroup;
import com.jakewharton.rxbinding.widget.RxTextView;

import org.opensilk.common.core.mortar.DaggerService;
import org.opensilk.common.ui.mortar.ToolbarOwner;

import java.util.Map;

import javax.inject.Inject;

import rx.subscriptions.CompositeSubscription;
import syncthing.android.R;
import syncthing.api.model.Compression;

/**
 * Created by drew on 3/16/15.
 */
public class EditDeviceScreenView extends CoordinatorLayout {

    @Inject EditDevicePresenter mPresenter;
    CompositeSubscription subscriptons;
    syncthing.android.ui.sessionsettings.EditDeviceScreenViewBinding binding;

    public EditDeviceScreenView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            EditDeviceComponent cmp = DaggerService.getDaggerComponent(getContext());
            cmp.inject(this);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (!isInEditMode()) {
            binding = DataBindingUtil.bind(this);
            mPresenter.takeView(this);
            binding.setPresenter(mPresenter);
            binding.executePendingBindings();
            subscribeChanges();
            initializeSharedFolders();
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mPresenter.dropView(this);
        if (subscriptons != null) subscriptons.unsubscribe();
    }

    void subscribeChanges() {
        subscriptons = new CompositeSubscription(
                RxTextView.textChanges(binding.editDeviceId)
                        .subscribe(mPresenter::setDeviceID),
                RxTextView.textChanges(binding.editDeviceName)
                        .subscribe(mPresenter::setDeviceName),
                RxTextView.textChanges(binding.editAddresses)
                        .subscribe(mPresenter::setAddresses),
                RxCompoundButton.checkedChanges(binding.checkIntroducer)
                        .subscribe(mPresenter::setIntroducer),
                RxRadioGroup.checkedChanges(binding.radioGroupCompression)
                        .subscribe(this::onCompressionCheckedChanged)
        );
    }

    void initializeSharedFolders() {
        //TODO how to do this with databinding?
        binding.shareFoldersContainer.removeAllViews();
        for (Map.Entry<String, Boolean> e : mPresenter.sharedFolders.entrySet()) {
            final String id = e.getKey();
            CheckBox checkBox = new CheckBox(getContext());
            checkBox.setText(id);
            checkBox.setChecked(e.getValue());
            subscriptons.add(RxCompoundButton.checkedChanges(checkBox)
                    .subscribe(checked -> {
                        mPresenter.setFolderShared(id, checked);
                    }));
            binding.shareFoldersContainer.addView(checkBox);
        }
    }

    void onCompressionCheckedChanged(int checkedId) {
        switch (checkedId) {
            case R.id.radio_all_compression:
                mPresenter.setCompression(Compression.ALWAYS);
                break;
            case R.id.radio_meta_compression:
                mPresenter.setCompression(Compression.METADATA);
                break;
            case R.id.radio_no_compression:
                mPresenter.setCompression(Compression.NEVER);
                break;
        }
    }

}
