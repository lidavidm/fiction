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

import android.app.Fragment;
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
import android.widget.FilterQueryProvider;
import android.widget.CursorAdapter;
import android.widget.GridView;
import android.widget.SectionIndexer;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.lithiumli.fiction.FictionActivity;
import com.lithiumli.fiction.R;
import com.lithiumli.fiction.PlaybackQueue;
import com.lithiumli.fiction.PlaybackService;

abstract public class FictionGridFragment
    extends Fragment
    implements LoaderManager.LoaderCallbacks<Cursor> {
    protected CursorAdapter mAdapter;
    GridView mGridView;

    static final String[] PROJECTION = {};

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getLoaderManager().initLoader(0, null, this);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mGridView = (GridView) inflater.inflate(R.layout.grid_view, container, false);
        return mGridView;
    }

    public GridView getGridView() {
        return mGridView;
    }


    public void filter(String query) {
        mAdapter.getFilter().filter(query);
    }

    public boolean onItemLongClick(AdapterView<?> parent, View view,
                                   int position, long id) {
        return false;
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);

        getGridView().setFastScrollEnabled(true);
        getGridView().setFastScrollAlwaysVisible(true);
    }

    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }
}
