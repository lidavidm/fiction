package com.lithiumli.fiction;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

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
        mQueueContext = context;

        switch (context) {
        case SONG:
            // Load all the crap and add it to our queue
            // alternatively...just get the cursor
            mCursor = data;
            break;
        default:
            break;
        }
    }

    public Song getCurrent() {
        if (mQueueContext == QueueContext.QUEUE) {
            return mSongs.get(mCurrent);
        }
        mCursor.moveToPosition(mCurrent);

        long id = mCursor.getLong(0);
        Uri contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                                    id);
        Song song = new Song(contentUri);
        song.populate(mCursor);

        return song;
    }

    public void setCurrent(int position) {
        assert position < mSongs.size() && position >= 0 : "Invalid queue position";

        mCurrent = position;
    }
}
