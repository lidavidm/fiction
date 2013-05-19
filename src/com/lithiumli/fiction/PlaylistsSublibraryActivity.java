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


package com.lithiumli.fiction;

import android.app.ActionBar;
import android.app.LoaderManager;
import android.os.Bundle;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import android.widget.TextView;
import com.lithiumli.fiction.fragments.*;

public class PlaylistsSublibraryActivity
    extends SublibraryActivity
    implements LoaderManager.LoaderCallbacks<Cursor>
{
    public static final String DATA_URI = "com.lithiumli.fiction.PLAYLIST_URI";

    static final String[] PROJECTION = new String[] {
            MediaStore.Audio.Playlists.Members._ID,
            MediaStore.Audio.Playlists.Members.TITLE,
            MediaStore.Audio.Playlists.Members.ARTIST,
            MediaStore.Audio.Playlists.Members.ALBUM,
            MediaStore.Audio.Playlists.Members.AUDIO_ID,
            MediaStore.Audio.Playlists.Members.PLAY_ORDER
    };

    ListView mListView;
    FictionCursorAdapter mAdapter;
    Uri mUri;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playlists);

        mUri = getIntent().getParcelableExtra(DATA_URI);

        ActionBar ab = getActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle("Playlist");

        mListView = (ListView) findViewById(R.id.playlist_list);

        getLoaderManager().initLoader(0, null, this);

        if (mListView.getAdapter() == null) {
            mAdapter = new FictionCursorAdapter(this, null, 0) {
                @Override
                public void bindView(View view, Context context, Cursor cursor) {
                    TextView title = (TextView) view.findViewById(R.id.title_text);
                    TextView sub = (TextView) view.findViewById(R.id.sub_text);

                    String songTitle =
                            cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Playlists.Members.TITLE));
                    String songArtist =
                            cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Playlists.Members.ARTIST));
                    String songAlbum =
                            cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Playlists.Members.ALBUM));

                    title.setText(songTitle);

                    sub.setText(songArtist + " — " + songAlbum);
                }
            };
            mListView.setAdapter(mAdapter);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            Intent parentIntent = new Intent(this, LibraryActivity.class);
            parentIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(parentIntent);
            break;
        default:
            return super.onOptionsItemSelected(item);
        }
        return true;
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, mUri,
                PROJECTION,
                "", null,
                MediaStore.Audio.Playlists.Members.PLAY_ORDER);
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }
}
