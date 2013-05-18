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

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AlphabetIndexer;
import android.widget.ListView;
import android.widget.CursorAdapter;
import android.widget.SectionIndexer;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.lithiumli.fiction.FictionActivity;
import com.lithiumli.fiction.R;
import com.lithiumli.fiction.PlaybackQueue;
import com.lithiumli.fiction.PlaybackService;

abstract public class FictionListFragment
    extends ListFragment
    implements LoaderManager.LoaderCallbacks<Cursor>,
               AdapterView.OnItemClickListener {
    protected CursorAdapter mAdapter;

    static final String[] PROJECTION = {
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media.ARTIST,
        MediaStore.Audio.Media.ALBUM,
        MediaStore.Audio.Media.ALBUM_ID,
        MediaStore.Audio.Media.DURATION
    };

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setEmptyText("No songs");

        getListView().setOnItemClickListener(this);

        getLoaderManager().initLoader(0, null, this);
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String select = "(" + MediaStore.Audio.Media.IS_MUSIC + "=1)";
        return new CursorLoader(getActivity(), uri,
                                PROJECTION,
                                select, null,
                                MediaStore.Audio.Media.TITLE_KEY);
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);

        getListView().setFastScrollEnabled(true);
        getListView().setFastScrollAlwaysVisible(true);
    }

    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }
}
