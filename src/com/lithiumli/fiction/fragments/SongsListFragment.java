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

import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;

import com.lithiumli.fiction.LibraryActivity;
import com.lithiumli.fiction.R;
import com.lithiumli.fiction.PlaybackQueue;
import com.lithiumli.fiction.PlaybackService;
import com.lithiumli.fiction.Song;
import com.lithiumli.fiction.ui.SongsAlphabetIndexer;

public class SongsListFragment
    extends FictionListFragment
    implements View.OnClickListener, View.OnLongClickListener {
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
            mAdapter.setFilterQueryProvider(this);
        }
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long
                            id) {
        LibraryActivity activity = (LibraryActivity) getActivity();
        activity.onSongSelected(position, mAdapter.getCursor());
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String select = "(" + MediaStore.Audio.Media.IS_MUSIC + "=1)";
        return new CursorLoader(getActivity(), uri,
                                PROJECTION,
                                select, null,
                                MediaStore.Audio.Media.TITLE_KEY);
    }

    // TODO factor this out into a separate class like 'QueryUtils' or something
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
            filter = MediaStore.Audio.Media.TITLE + " LIKE '%' || ? || '%'";
            params = new String[] { query };
        }
        return getActivity().getContentResolver().query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            PROJECTION,
            filter,
            params,
            MediaStore.Audio.Media.TITLE_KEY);
    }

    @Override
    public void onClick(final View v) {
        getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    int position = getListView().getPositionForView(v);

                    Cursor data = mAdapter.getCursor();
                    data.moveToPosition(position);
                    long id = data.getLong(0);
                    Uri contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                                                id);
                    Song song = new Song(contentUri);
                    song.populate(data);

                    ((LibraryActivity) getActivity()).onSongEnqueued(song);
                }
            });
    }

    @Override
    public boolean onLongClick(View v) {
        Toast toast = Toast.makeText(
            getActivity(),
            v.getContentDescription(),
            Toast.LENGTH_SHORT);
        toast.show();
        return true;
    }

    class SongsCursorAdapter extends FictionCursorAdapter
        // implements SectionIndexer
    {
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

            // TODO refactor this out
            ((ImageButton) view.findViewById(R.id.list_enqueue)).setOnClickListener(SongsListFragment.this);
            view.findViewById(R.id.list_enqueue).setOnLongClickListener(SongsListFragment.this);
        }

        @Override
        public Cursor swapCursor(Cursor c) {
            // if (c != null) {
            //     mIndexer = new SongsAlphabetIndexer(c,
            //                                         c.getColumnIndex(MediaStore.Audio.Media.TITLE),
            //                                         "ABCDEFGHIJKLMNOPQRSTUVWXYZ");
            // }
            return super.swapCursor(c);
        }

        // @Override
        // public int getPositionForSection(int section) {
        //     return mIndexer.getPositionForSection(section);
        // }

        // @Override
        // public int getSectionForPosition(int position) {
        //     return mIndexer.getSectionForPosition(position);
        // }

        // @Override
        // public Object[] getSections() {
        //     return mIndexer.getSections();
        // }
    }
}
