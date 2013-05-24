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

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class PlaybackQueue {
    public enum QueueContext {
        SONG,
        ALBUM,
        ARTIST,
        PLAYLIST,
        QUEUE
    }

    Context mContext;
    PlaybackService mService;

    QueueContext mQueueContext;
    ArrayList<Song> mSongs = new ArrayList<Song>(10);
    Cursor mCursor;
    int mCurrent;

    QueueAdapter mAdapter;

    public PlaybackQueue() {
    }

    public QueueContext getContext() {
        return mQueueContext;
    }

    public void setContext(QueueContext context, Cursor data) {
        mQueueContext = context;

        switch (context) {
        case SONG:
            // todo: Cursor.registerContentObserver
            if (data.moveToFirst()){
                while(!data.isAfterLast()) {
                    long id = data.getLong(0);
                    Uri contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                                                id);
                    Song song = new Song(contentUri);
                    song.populate(data);
                    mSongs.add(song);

                    data.moveToNext();
                }
            }
            break;
        case PLAYLIST:
            if (data.moveToFirst()){
                while(!data.isAfterLast()) {
                    long id = data.getLong(data.getColumnIndex(MediaStore.Audio.Playlists.Members.AUDIO_ID));
                    Uri contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                                                id);
                    Song song = new Song(contentUri);
                    song.populate(data);
                    mSongs.add(song);

                    data.moveToNext();
                }
            }
            break;
        default:
            break;
        }

        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    public int getCount() {
        return mSongs.size();
    }

    public Song getCurrent() {
        return mSongs.get(mCurrent);
    }

    public int getCurrentPosition() {
        return mCurrent;
    }

    public Song getItem(int position) {
        return mSongs.get(position);
    }

    public void setCurrent(int position) {
        assert position < mSongs.size() && position >= 0 : "Invalid queue position";

        mCurrent = position;
    }

    public QueueAdapter getAdapter(Context context) {
        if (mAdapter == null) {
            mAdapter = new QueueAdapter(context, this);
        }
        return mAdapter;
    }

    public class QueueAdapter extends BaseAdapter {
        Context mContext;
        PlaybackQueue mQueue;

        public QueueAdapter(Context context, PlaybackQueue queue) {
            mContext = context;
            mQueue = queue;
        }

        @Override
        public int getCount() {
            return mQueue.getCount();
        }

        @Override
        public Song getItem(int position) {
            return mQueue.getItem(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemViewType(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView,
                            ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater)
                    mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                convertView = inflater.inflate(R.layout.list_item, parent, false);
            }

            Song song = getItem(position);

            TextView title = (TextView) convertView.findViewById(R.id.title_text);
            TextView sub = (TextView) convertView.findViewById(R.id.sub_text);

            title.setText(song.getTitle());
            sub.setText(song.getArtist() + " — " + song.getAlbum());

            if (mQueue.getCount() != 0 &&
                mQueue.getCurrentPosition() == position) {
                convertView.setBackgroundResource(R.drawable.list_item_bg);
            }
            else {
                convertView.setBackgroundResource(0);
            }

            return convertView;
        }
    }
}
