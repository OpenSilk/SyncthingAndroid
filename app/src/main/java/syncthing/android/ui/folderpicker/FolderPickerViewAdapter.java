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

package syncthing.android.ui.folderpicker;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.opensilk.common.ui.recycler.ItemClickSupport;
import org.opensilk.common.ui.recycler.RecyclerListAdapter;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import syncthing.android.R;

/**
 * Created by drew on 11/3/15.
 */
public class FolderPickerViewAdapter extends RecyclerListAdapter<String, FolderPickerViewAdapter.ViewHolder>
        implements ItemClickSupport.OnItemClickListener, ItemClickSupport.OnItemLongClickListener {

    final FolderPickerPresenter presenter;

    @Inject
    public FolderPickerViewAdapter(FolderPickerPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        ItemClickSupport.addTo(recyclerView)
                .setOnItemClickListener(this)
                .setOnItemLongClickListener(this);
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        ItemClickSupport.removeFrom(recyclerView);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(inflate(parent, R.layout.mtrl_list_item_oneline_text));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.title.setText(getBaseName(getItem(position)));
    }

    @Override
    public void onItemClicked(RecyclerView recyclerView, int position, View v) {
        presenter.onOpenFolder(recyclerView.getContext(), getItem(position));
    }

    @Override
    public boolean onItemLongClicked(RecyclerView recyclerView, int position, View v) {
        presenter.onFolderSelected(getItem(position));
        return true;
    }

    private static String getBaseName(String path) {
        if (StringUtils.endsWith(path, "/")) {
            path = path.substring(0, path.length()-1);
        }
        int lastSlash = path.lastIndexOf("/");
        if (lastSlash >= 0 && lastSlash < path.length()) {
            return path.substring(lastSlash+1);
        }
        return path;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @InjectView(android.R.id.text1) TextView title;
        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
        }
    }
}
