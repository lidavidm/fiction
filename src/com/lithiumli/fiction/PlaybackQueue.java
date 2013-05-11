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

    QueueContext mQueueContext;
    ArrayList<Song> mSongs = new ArrayList<Song>(10);
    Cursor mCursor;
    int mCurrent;

    QueueAdapter mAdapter;

    public PlaybackQueue(// Context context
                         ) {
        // mContext = context;
    }

    public QueueContext getContext() {
        return mQueueContext;
    }

    public void setContext(QueueContext context, Cursor data) {
        if (mQueueContext == context) {
            return;
        }

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
            LayoutInflater inflater = (LayoutInflater)
                mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            Song song = getItem(position);

            View view = inflater.inflate(R.layout.list_item, parent, false);

            TextView title = (TextView) view.findViewById(R.id.title_text);
            TextView sub = (TextView) view.findViewById(R.id.sub_text);

            title.setText(song.getTitle());
            sub.setText(song.getArtist() + " — " + song.getAlbum());

            return view;
        }
    }
}
