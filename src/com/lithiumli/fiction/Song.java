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
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;

public class Song implements Parcelable {
    private Uri mUri;
    private String mTitle = "(No title)";
    private String mArtist = "(Unknown)";
    private String mAlbum = "(Unknown album)";
    private Uri mAlbumArt = Uri.EMPTY;
    private long mDuration;

    public Song(Uri uri) {
        mUri = uri;
    }

    private Song(Parcel in) {
        mTitle = in.readString();
        mArtist = in.readString();
        mAlbum = in.readString();
        mAlbumArt = in.readParcelable(null);
        mDuration = in.readLong();
    }

    public void populate(Cursor cursor) {
        mTitle = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
        mArtist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
        mAlbum = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
        Long albumId =
            cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
        Uri artworkUri = Uri.parse("content://media/external/audio/albumart");
        mAlbumArt = ContentUris.withAppendedId(artworkUri, albumId);
        mDuration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
    }

    public Uri getUri() {
        return mUri;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getArtist() {
        return mArtist;
    }

    public String getAlbum() {
        return mAlbum;
    }

    public Uri getAlbumArt() {
        return mAlbumArt;
    }

    public long getDuration() {
        return mDuration;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(mTitle);
        out.writeString(mArtist);
        out.writeString(mAlbum);
        out.writeParcelable(mAlbumArt, 0);
        out.writeLong(mDuration);
    }

    public static final Parcelable.Creator<Song> CREATOR
        = new Parcelable.Creator<Song>() {
        public Song createFromParcel(Parcel in) {
            return new Song(in);
        }

        public Song[] newArray(int size) {
            return new Song[size];
        }
    };
}
