/* Copyright (C) 2013 David Li <li.davidm96@gmail.com>

   This file is part of Fiction Music.

   Fiction Music is free software: you can redistribute it and/or modify it
   under the terms of the GNU General Public License as published by the Free
   Software Foundation, either version 3 of the License, or (at your option)
   any later version.

   Fiction Music is distributed in the hope that it will be useful, but WITHOUT
   ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
   FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
   more details.

   You should have received a copy of the GNU General Public License along with
   Fiction Music.  If not, see <http://www.gnu.org/licenses/>. */


package com.lithiumli.fiction.fragments;

import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.TextView;

import de.keyboardsurfer.android.widget.crouton.Crouton;

import com.lithiumli.fiction.LibraryActivity;
import com.lithiumli.fiction.Playlist;
import com.lithiumli.fiction.R;
import com.lithiumli.fiction.ui.UiUtils;

public class PlaylistsListFragment
    extends FictionListFragment
    implements ActionMode.Callback {
    static final String[] PROJECTION = {
        MediaStore.Audio.Playlists._ID,
        MediaStore.Audio.Playlists.NAME,
    };
    ActionMode mActionMode;

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.playlist_context, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
        case R.id.delete:
            Long id = (Long) mode.getTag();
            Playlist p = new Playlist(id);
            p.delete(getActivity().getContentResolver());
            mode.finish();
            Crouton.makeText(getActivity(),
                             R.string.playlist_deleted,
                             UiUtils.STYLE_INFO).show();
            return true;
        default:
            return false;
        }
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        mActionMode = null;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setEmptyText("No playlists");

        if (getListAdapter() == null) {
            mAdapter = new PlaylistsCursorAdapter(getActivity(), null, 0);
            setListAdapter(mAdapter);
            mAdapter.setFilterQueryProvider(this);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.library_playlists, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        boolean drawerOpen = ((LibraryActivity) getActivity()).isDrawerOpen();
        menu.findItem(R.id.new_playlist).setVisible(!drawerOpen);
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.new_playlist:
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.new_playlist);
            EditText text = new EditText(getActivity());
            builder.setView(text);
            builder.setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
            AlertDialog dialog = builder.create();
            dialog.show();
            return true;
        default:
            break;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public Cursor runQuery(CharSequence constraint) {
        String query;
        String filter;
        String[] params;
        if (constraint == null) {
            query = "";
        }
        else {
            query = constraint.toString();
        }
        if (query.equals("")) {
            filter = "";
            params = null;
        }
        else {
            filter = MediaStore.Audio.Playlists.NAME + " LIKE '%' || ? || '%'";
            params = new String[] { query };
        }
        return getActivity().getContentResolver().query(
            MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
            PROJECTION,
            filter,
            params,
            MediaStore.Audio.Playlists.NAME);
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long
                            id) {
        Uri contentUri = MediaStore.Audio.Playlists.Members.getContentUri("external", id);
        ((LibraryActivity) getActivity()).onPlaylistSelected(contentUri);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view,
                                   int position, long id) {
        if (mActionMode != null) return false;

        mActionMode = getActivity().startActionMode(this);
        mActionMode.setTitle(((TextView) view.findViewById(R.id.title_text)).getText().toString());
        mActionMode.setTag((Long) id);
        view.setSelected(true);
        return true;
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
        return new CursorLoader(getActivity(), uri,
                                PROJECTION,
                                "", null,
                                MediaStore.Audio.Playlists.NAME);
    }

    class PlaylistsCursorAdapter extends FictionCursorAdapter {
        public PlaylistsCursorAdapter(Context context, Cursor c, int flags) {
            super(context, c, flags);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            TextView title = (TextView) view.findViewById(R.id.title_text);

            String name =
                cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Playlists.NAME));

            title.setText(name);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            final View view = mInflater.inflate(R.layout.list_item_single,
                                                parent, false);
            return view;
        }
    }
}
