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

package syncthing.android.api.model;

import android.databinding.BindingAdapter;
import android.support.v4.content.ContextCompat;
import android.widget.TextView;

import syncthing.android.R;
import syncthing.api.model.Compression;
import syncthing.api.model.ModelState;
import syncthing.api.model.PullOrder;
import syncthing.api.model.VersioningType;

/**
 * Created by drew on 10/11/15.
 */
public class EnumTextBinding {
    @BindingAdapter("compressionText")
    public static void setCompressionText(TextView view, Compression compression) {
        if (compression == null) compression = Compression.UNKNOWN;
        switch (compression) {
            case ALWAYS:
                view.setText(R.string.all_data);
                break;
            case METADATA:
                view.setText(R.string.metadata_only);
                break;
            case NEVER:
                view.setText(R.string.off);
                break;
            default:
                view.setText(R.string.unknown);
                break;
        }
    }
    @BindingAdapter("pullOrderText")
    public static void pullOrderText(TextView view, PullOrder order) {
        if (order == null) order = PullOrder.UNKNOWN;
        switch (order) {
            case RANDOM:
                view.setText(R.string.random);
                break;
            case ALPHABETIC:
                view.setText(R.string.alphabetic);
                break;
            case SMALLESTFIRST:
                view.setText(R.string.smallest_first);
                break;
            case LARGESTFIRST:
                view.setText(R.string.largest_first);
                break;
            case OLDESTFIRST:
                view.setText(R.string.oldest_first);
                break;
            case NEWESTFIRST:
                view.setText(R.string.newest_first);
                break;
            case UNKNOWN:
            default:
                view.setText(R.string.unknown);
                break;
        }
    }
    @BindingAdapter("versioningTypeText")
    public static void versioningTypeText(TextView view, VersioningType type) {
        if (type == null) type = VersioningType.NONE;
        switch (type) {
            case TRASHCAN:
                view.setText(R.string.trash_can_file_versioning);
                break;
            case SIMPLE:
                view.setText(R.string.simple_file_versioning);
                break;
            case STAGGERED:
                view.setText(R.string.staggered_file_versioning);
                break;
            case EXTERNAL:
                view.setText(R.string.external_file_versioning);
                break;
            case NONE:
            default:
                view.setText(R.string.no_file_versioning);
                break;
        }
    }
    @BindingAdapter("modelStateText")
    public static void modelStateText(TextView view, ModelState state) {
        if (state == null) state = ModelState.UNKNOWN;
        switch (state) {
            case IDLE:
                view.setText(R.string.up_to_date);
                break;
            case SCANNING:
                view.setText(R.string.scanning);
                break;
            case ERROR:
                view.setText(R.string.error);
                break;
            case SYNCING:
                view.setText(R.string.syncing);
                break;
            case UNKNOWN:
            default:
                view.setText(R.string.unknown);
                break;
        }
    }
    @BindingAdapter("modelStateTextColor")
    public static void modelStateTextColor(TextView view, ModelState state) {
        if (state == null) state = ModelState.UNKNOWN;
        switch (state) {
            case IDLE:
                view.setTextColor(ContextCompat.getColor(view.getContext(), R.color.folder_idle));
                break;
            case SCANNING:
            case SYNCING:
                view.setTextColor(ContextCompat.getColor(view.getContext(), R.color.folder_scanning));
                break;
            case ERROR:
                view.setTextColor(ContextCompat.getColor(view.getContext(), R.color.folder_error));
                break;
            default:
                view.setTextColor(ContextCompat.getColor(view.getContext(), R.color.folder_default));
                break;
        }
    }
}
