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

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.util.Log;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;

public class NowPlayingActivity
    extends FictionActivity
{
    TextView mSongName;
    TextView mSongAlbum;
    TextView mSongArtist;
    ViewPager mCoverPager;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.now_playing);

        mCoverPager = (ViewPager) findViewById(R.id.cover_pager);
        mCoverPager.setAdapter(adapter);
        mCoverPager.setCurrentItem(CoverPager.COVER_MIDDLE, false);

        mSongName = (TextView) findViewById(R.id.np_song_name);
        mSongAlbum = (TextView) findViewById(R.id.np_song_album);
        mSongArtist = (TextView) findViewById(R.id.np_song_artist);
        mSongArtist.setSelected(true);

        ActionBar ab = getActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle("Now Playing");
        ab.setSubtitle("Fiction Music");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            Intent parentIntent = new Intent(this, LibraryActivity.class);
            parentIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(parentIntent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSongChange(Song song) {
        mSongName.setText(song.getTitle());
        mSongAlbum.setText(song.getAlbum());
        mSongArtist.setText(song.getArtist());
    }

    // with thanks to
    // thehayro.blogspot.com/2012/12/enable-infinite-paging-with-android.html
    public static class CoverAdapter
        extends PagerAdapter
        implements ViewPager.OnPageChangeListener {
        private static final int COVER_LEFT = 0;
        private static final int COVER_MIDDLE = 1;
        private static final int COVER_RIGHT = 2;

        int mSelectedCoverIndex;

        ImageView mCovers = new ImageView[3];

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public ImageView instantiateItem(ViewGroup container, int position) {
            // TextView textView = (TextView)mInflater.inflate(R.layout.content, null);
            // PageModel currentPage = mPageModel[position];
            // currentPage.textView = textView;
            // textView.setText(currentPage.getText());
            // container.addView(textView);
            // return textView;
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view == obj;
        }

        private Song getSong(int offset) {
            // get song info from queue
        }

        private void setCover(int cover, Song song) {
            // set cover image
        }

        @Override
        public void onPageSelected(int position) {
            mSelectedPageIndex = position;
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            if (state == ViewPager.SCROLL_STATE_IDLE) {
                Song prevSong = getSong(-1);
                Song currentSong = getSong(0);
                Song nextSong = getSong(1);

                // swipe to right (left page)
                if (mSelectedCoverIndex == COVER_LEFT) {
                    setCover(COVER_RIGHT);
                    setCover(COVER_MIDDLE);
                    setCover(COVER_LEFT);
                }
                // swipe to left (right page)
                else if (mSelectedCoverIndex == COVER_RIGHT) {
                    setCover(COVER_LEFT);
                    setCover(COVER_MIDDLE);
                    setCover(COVER_RIGHT);
                }
                this.setCurrentItem(COVER_MIDDLE, false);
            }
        }

        @Override
        public void onPageScrolled(int position,
                                   float positionOffset,
                                   int positionOffsetPixels) {
        }
    }
