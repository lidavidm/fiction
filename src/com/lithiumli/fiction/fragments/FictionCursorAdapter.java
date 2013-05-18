package com.lithiumli.fiction.fragments;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.SectionIndexer;
import android.widget.SimpleCursorAdapter;

import com.lithiumli.fiction.R;
import com.lithiumli.fiction.ui.SongsAlphabetIndexer;

abstract public class FictionCursorAdapter extends CursorAdapter {
    private Cursor mCursor;
    private Context mContext;
    private final LayoutInflater mInflater;

    public FictionCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        mInflater = LayoutInflater.from(context);
        mContext = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup
                        parent) {
        final View view = mInflater.inflate(R.layout.list_item,
                                            parent, false);
        return view;
    }
}
