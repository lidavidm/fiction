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
import android.widget.ListAdapter;
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
        // TODO some way to update song list
        return new QueueAdapter(context, this);
    }

    public class QueueAdapter implements ListAdapter {
        Context mContext;
        PlaybackQueue mQueue;
        final DataSetObservable mDataSetObservable = new DataSetObservable();

        public QueueAdapter(Context context, PlaybackQueue queue) {
            mContext = context;
            mQueue = queue;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return true;
        }

        @Override
        public boolean isEnabled(int position) {
            return true;
        }

        @Override
        public boolean isEmpty() {
            return getCount() == 0;
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

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public void registerDataSetObserver(DataSetObserver observer) {
            mDataSetObservable.registerObserver(observer);
        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver observer) {
            mDataSetObservable.unregisterObserver(observer);
        }

        public void notifyDataSetChanged() {
            mDataSetObservable.notifyChanged();
        }
    }
}
