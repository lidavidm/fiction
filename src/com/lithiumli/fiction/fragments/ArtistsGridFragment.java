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
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;

import org.lucasr.smoothie.AsyncGridView;
import org.lucasr.smoothie.ItemManager;
import org.lucasr.smoothie.SimpleItemLoader;

import uk.co.senab.bitmapcache.CacheableBitmapDrawable;

import com.lithiumli.fiction.ArtistImageCache;
import com.lithiumli.fiction.LibraryActivity;
import com.lithiumli.fiction.R;
import com.lithiumli.fiction.PlaybackQueue;
import com.lithiumli.fiction.PlaybackService;
import com.lithiumli.fiction.Song;

public class ArtistsGridFragment
    extends FictionGridFragment {
    static final String[] PROJECTION = {
        MediaStore.Audio.Artists._ID,
        MediaStore.Audio.Artists.ARTIST
    };

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (getGridView().getAdapter() == null) {
            mAdapter = new ArtistsCursorAdapter(getActivity(), null, 0);
            getGridView().setAdapter(mAdapter);
        }

        ImageLoader loader = new ImageLoader();
        ItemManager.Builder builder = new ItemManager.Builder(loader);
        builder.setPreloadItemsEnabled(true).setPreloadItemsCount(5);
        builder.setThreadPoolSize(4);

        ((AsyncGridView) getGridView()).setItemManager(builder.build());
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long
                            id) {
        LibraryActivity activity = (LibraryActivity) getActivity();
        // activity.onArtistSelected(position, mAdapter.getCursor());
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI;
        return new CursorLoader(getActivity(), uri,
                                PROJECTION,
                                null, null,
                                MediaStore.Audio.Artists.ARTIST_KEY);
    }

    class ArtistsCursorAdapter extends FictionCursorAdapter
    {
        public ArtistsCursorAdapter(Context context, Cursor c, int flags) {
            super(context, c, flags);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup
                            parent) {
            final View view = mInflater.inflate(R.layout.grid_item_library,
                                                parent, false);
            ViewHolder holder = new ViewHolder();
            holder.image = (ImageView) view.findViewById(R.id.artist_image);
            view.setTag(holder);
            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            TextView title = (TextView) view.findViewById(R.id.title_text);
            title.setText(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Artists.ARTIST)));
        }

        @Override
        public Cursor swapCursor(Cursor c) {
            return super.swapCursor(c);
        }

        class ViewHolder {
            public ImageView image;
        }
    }

    class ImageLoader
        extends SimpleItemLoader<String, CacheableBitmapDrawable> {
        ArtistImageCache mCache;

        public ImageLoader() {
            mCache = ArtistImageCache.getInstance(getActivity());
        }

        public String getItemParams(Adapter adapter, int position) {
            Cursor cursor = (Cursor) adapter.getItem(position);
            if (cursor.moveToPosition(position)) {
                return cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Artists.ARTIST));
            }
            return null;
        }

        @Override
        public CacheableBitmapDrawable loadItem(String artist) {
            if (artist == null) return null;
            return mCache.getImageBlocking(artist);
        }

        @Override
        public CacheableBitmapDrawable loadItemFromMemory(String artist) {
            if (artist == null) return null;
            return mCache.getImageMemory(artist);
        }

        @Override
        public void displayItem(View itemView,
                                CacheableBitmapDrawable result,
                                boolean fromMemory) {
            if (result != null) {
                ((ArtistsCursorAdapter.ViewHolder) itemView.getTag()).image.setImageDrawable(result);
            }
        }
    }
}
