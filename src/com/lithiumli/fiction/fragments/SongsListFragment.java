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
import com.lithiumli.fiction.ui.SongsAlphabetIndexer;

public class SongsListFragment
    extends FictionListFragment {
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

        if (getListAdapter() == null) {
            mAdapter = new SongsCursorAdapter(getActivity(), null, 0);
            setListAdapter(mAdapter);
        }
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long
                            id) {
        Uri contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                                    id);

        FictionActivity activity = (FictionActivity) getActivity();

        if (activity.isServiceBound()) {
            PlaybackService service = activity.getService();
            PlaybackQueue queue = service.getQueue();

            if (queue.getContext() != PlaybackQueue.QueueContext.SONG) {
                queue.setContext(PlaybackQueue.QueueContext.SONG,
                                 mAdapter.getCursor());
            }
            service.play(position);
        }
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String select = "(" + MediaStore.Audio.Media.IS_MUSIC + "=1)";
        return new CursorLoader(getActivity(), uri,
                                PROJECTION,
                                select, null,
                                MediaStore.Audio.Media.TITLE_KEY);
    }

    class SongsCursorAdapter extends FictionCursorAdapter
        implements SectionIndexer {
        private SongsAlphabetIndexer mIndexer;

        public SongsCursorAdapter(Context context, Cursor c, int flags) {
            super(context, c, flags);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            TextView title = (TextView) view.findViewById(R.id.title_text);
            TextView sub = (TextView) view.findViewById(R.id.sub_text);

            String songTitle =
                cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
            String songArtist =
                cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
            String songAlbum =
                cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));

            title.setText(songTitle);

            sub.setText(songArtist + " — " + songAlbum);
        }

        @Override
        public Cursor swapCursor(Cursor c) {
            if (c != null) {
                mIndexer = new SongsAlphabetIndexer(c,
                                                    c.getColumnIndex(MediaStore.Audio.Media.TITLE),
                                                    "ABCDEFGHIJKLMNOPQRSTUVWXYZ");
            }
            return super.swapCursor(c);
        }

        @Override
        public int getPositionForSection(int section) {
            return mIndexer.getPositionForSection(section);
        }

        @Override
        public int getSectionForPosition(int position) {
            return mIndexer.getSectionForPosition(position);
        }

        @Override
        public Object[] getSections() {
            return mIndexer.getSections();
        }
    }
}
