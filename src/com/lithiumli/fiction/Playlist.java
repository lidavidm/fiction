package com.lithiumli.fiction;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;

import java.util.List;

public class Playlist {
    private Uri mUri;
    private long mId;
    private String mName;

    public Playlist(Uri uri) {
        mUri = uri;
        mId = ContentUris.parseId(uri);
    }

    public Playlist(long id) {
        mUri = ContentUris.withAppendedId(
            MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
            id);
        mId = id;
    }

    private Playlist(Parcel in) {
    }

    public void populate(Cursor cursor) {
        mName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Playlists.NAME));
    }

    public void addSong(ContentResolver resolver, Song song) {
        // 1. Find largest PLAY_ORDER in playlist
        Uri contentUri = MediaStore.Audio.Playlists.Members.getContentUri("external", mId);
        String[] projection = new String[] {MediaStore.Audio.Playlists.Members.PLAY_ORDER };
        Cursor cursor = resolver.query(contentUri, projection, null, null, null);
        int position = 0;
        if (cursor.moveToLast()) {
            position = cursor.getInt(0) + 1;
        }
        cursor.close();

        // 2. Perform insert
        long songId = song.getId();
        ContentValues value = new ContentValues(2);
        value.put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, position);
        value.put(MediaStore.Audio.Playlists.Members.AUDIO_ID, songId);
        resolver.insert(contentUri, value);
    }

    public void addSongs(ContentResolver resolver, List<Song> songs) {
        // 1. Find largest PLAY_ORDER in playlist
        Uri contentUri = MediaStore.Audio.Playlists.Members.getContentUri("external", mId);
        String[] projection = new String[] {MediaStore.Audio.Playlists.Members.PLAY_ORDER };
        Cursor cursor = resolver.query(contentUri, projection, null, null, null);
        int position = 0;
        if (cursor.moveToLast()) {
            position = cursor.getInt(0) + 1;
        }
        cursor.close();

        ContentValues[] values = new ContentValues[songs.size()];
        int index = 0;
        for (Song song : songs) {
            long songId = song.getId();
            ContentValues value = new ContentValues(2);
            value.put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, position);
            value.put(MediaStore.Audio.Playlists.Members.AUDIO_ID, songId);
            values[index] = value;
            position++;
            index++;
        }

        resolver.bulkInsert(contentUri, values);
    }

    public void rename(ContentResolver resolver, String name) {
        ContentValues value = new ContentValues(1);
        value.put(MediaStore.Audio.Playlists.NAME, name);
        resolver.update(
            MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
            value,
            "_id = " + mId,
            null);
    }

    public void delete(ContentResolver resolver) {
        resolver.delete(mUri, "_id = " + mId, null);
    }

    public static Playlist create(ContentResolver resolver, String name) {
        ContentValues value = new ContentValues(1);
        value.put(MediaStore.Audio.Playlists.NAME, name);
        Uri uri = resolver.insert(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, value);
        Playlist playlist = new Playlist(uri);
        playlist.mName = name;
        return playlist;
    }

    public void writeToParcel(Parcel out, int flags) {
    }

    public static final Parcelable.Creator<Playlist> CREATOR
        = new Parcelable.Creator<Playlist>() {
        public Playlist createFromParcel(Parcel in) {
            return new Playlist(in);
        }

        public Playlist[] newArray(int size) {
            return new Playlist[size];
        }
    };
}
