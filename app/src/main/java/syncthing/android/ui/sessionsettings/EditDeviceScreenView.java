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

import android.annotation.SuppressLint;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.widget.CheckBox;
import android.widget.RadioGroup;

import com.jakewharton.rxbinding.widget.RxRadioGroup;
import com.jakewharton.rxbinding.widget.RxTextView;

import org.apache.commons.lang3.StringUtils;
import org.opensilk.common.core.mortar.DaggerService;
import org.opensilk.common.ui.mortar.ToolbarOwner;

import java.util.Collection;

import javax.inject.Inject;

import rx.subscriptions.CompositeSubscription;
import syncthing.android.R;
import syncthing.android.service.SyncthingUtils;
import syncthing.api.model.Compression;
import syncthing.api.model.DeviceConfig;
import syncthing.api.model.FolderConfig;
import syncthing.api.model.FolderDeviceConfig;

/**
 * Created by drew on 3/16/15.
 */
public class EditDeviceScreenView extends CoordinatorLayout {

    @Inject ToolbarOwner mToolbarOwner;
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
            binding.setPresenter(mPresenter);
            mPresenter.takeView(this);
        }
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isInEditMode()) {
            subscribeChanges();
            mPresenter.takeView(this);
            mToolbarOwner.attachToolbar(binding.toolbar);
            mToolbarOwner.setConfig(mPresenter.getToolbarConfig());
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mPresenter.dropView(this);
        mToolbarOwner.detachToolbar(binding.toolbar);
        if (subscriptons != null) subscriptons.unsubscribe();
    }

    void subscribeChanges() {
        subscriptons = new CompositeSubscription(
                RxTextView.textChanges(binding.editDeviceId)
                        .subscribe(this::onDeviceIdChange),
                RxRadioGroup.checkedChanges(binding.radioGroupCompression)
                        .subscribe(this::onCompressionCheckedChanged)
        );
    }

    void initialize(Collection<FolderConfig> folders, boolean fromsavedstate) {
        if (fromsavedstate) return;
        boolean isAdd = mPresenter.isAdd;
        DeviceConfig device = mPresenter.originalDevice;
        if (!isAdd) {
            binding.editDeviceId.setText(device.deviceID);
            binding.editDeviceId.setEnabled(false);
            binding.btnScanqr.setVisibility(GONE);
            binding.descDeviceId.setVisibility(GONE);
            binding.descDeviceId2.setVisibility(GONE);
            binding.editDeviceName.setText(device.name);
            binding.editAddresses.setText(SyncthingUtils.unrollArray(device.addresses));
            setCompression(device.compression);
            binding.checkIntroducer.setChecked(device.introducer);
        } else {
            //set nice defaults
            binding.editAddresses.setText(SyncthingUtils.unrollArray(device.addresses));
            setCompression(device.compression);
            binding.checkIntroducer.setChecked(device.introducer);
        }

        binding.shareFoldersContainer.removeAllViews();
        for (FolderConfig f : folders) {
            FolderCheckBox checkBox = new FolderCheckBox(getContext(), f);
            checkBox.setText(f.id);
            if (!isAdd) {
                for (FolderDeviceConfig d : f.devices) {
                    if (StringUtils.equals(d.deviceID, device.deviceID)) {
                        checkBox.setChecked(true);
                        break;
                    }
                }
            }
            binding.shareFoldersContainer.addView(checkBox);
        }

        binding.btnDelete.setVisibility(isAdd ? GONE : VISIBLE);

    }

    void setCompression(Compression compression) {
        switch (compression) {
            case ALWAYS:
                binding.radioAllCompression.setChecked(true);
                break;
            case METADATA:
                binding.radioMetaCompression.setChecked(true);
                break;
            case NEVER:
            default:
                binding.radioNoCompression.setChecked(true);
                break;

        }
    }

    void onDeviceIdChange(CharSequence text) {
        boolean isAdd = mPresenter.isAdd;
        if (isAdd && !StringUtils.isEmpty(text)) {
            if (mPresenter.validateDeviceId(text.toString(), true)) {
                binding.descDeviceId.setVisibility(VISIBLE);
                binding.descDeviceId2.setVisibility(VISIBLE);
                binding.errorDeviceIdBlank.setVisibility(GONE);
                binding.errorDeviceIdInvalid.setVisibility(GONE);
            }
        }
    }

    void notifyDeviceIdEmpty() {
        binding.descDeviceId.setVisibility(GONE);
        binding.descDeviceId2.setVisibility(GONE);
        binding.errorDeviceIdBlank.setVisibility(VISIBLE);
        binding.errorDeviceIdInvalid.setVisibility(GONE);
    }

    void notifyDeviceIdInvalid() {
        binding.descDeviceId.setVisibility(GONE);
        binding.descDeviceId2.setVisibility(GONE);
        binding.errorDeviceIdBlank.setVisibility(GONE);
        binding.errorDeviceIdInvalid.setVisibility(VISIBLE);
    }

    void notifyInvalidAddresses() {
        //TODO
    }

    void onCompressionCheckedChanged(int checkedId) {
        switch (checkedId) {
            case R.id.radio_all_compression:
                break;
            case R.id.radio_meta_compression:
                break;
            case R.id.radio_no_compression:
            default:
                break;
        }
    }

    @SuppressLint("ViewConstructor")
    static class FolderCheckBox extends CheckBox {
        final FolderConfig folder;
        FolderCheckBox(Context context, FolderConfig folder) {
            super(context);
            this.folder = folder;
        }
    }

}
