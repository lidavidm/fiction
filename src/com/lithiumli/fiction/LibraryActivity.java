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
import android.view.View;
import android.widget.TextView;
import android.util.Log;

import android.support.v4.view.ViewPager;
import android.support.v13.app.FragmentPagerAdapter;

import java.util.ArrayList;

import com.lithiumli.fiction.fragments.*;

public class LibraryActivity
    extends FictionActivity
    implements ViewPager.OnPageChangeListener
{
    ViewPager mViewPager;
    TabsAdapter mTabsAdapter;
    TextView babSongTitle;
    TextView babSubtitle;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        initializeDrawer();

        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mViewPager.setOnPageChangeListener(this);

        mTabsAdapter = new TabsAdapter(this, mViewPager);
        mTabsAdapter.addTab("Songs", SongsListFragment.class, null,
                            R.drawable.ab_background_textured_fiction);
        mTabsAdapter.addTab("Playlists", PlaylistsListFragment.class, null,
                            R.drawable.ab_background_textured_fiction_pl);
        mViewPager.setAdapter(mTabsAdapter);

        babSongTitle = (TextView) findViewById(R.id.bab_song_name);
        babSubtitle = (TextView) findViewById(R.id.bab_song_subtitle);
        babSubtitle.setSelected(true);

        getActionBar().setSubtitle("Fiction Music");
        getActionBar().setTitle("Songs");

        View layout = findViewById(R.id.bab_info);
        layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(LibraryActivity.this,
                                               NowPlayingActivity.class);
                    LibraryActivity.this.startActivity(intent);
                }
            });
    }

    @Override
    public void onSongChange(Song song) {
        babSongTitle.setText(song.getTitle());
        babSubtitle.setText(song.getArtist() + " - " + song.getAlbum());
    }

    @Override
    public void onPageSelected(int position) {
        ActionBar actionBar = getActionBar();
        actionBar.setTitle(mTabsAdapter.getPageTitle(position));
        TabsAdapter.TabInfo info = mTabsAdapter.getItemInfo(position);
        actionBar.setBackgroundDrawable(getResources().getDrawable(info.bgResource));
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int
                                positionOffsetPixels) {
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    public static class TabsAdapter extends FragmentPagerAdapter {
        private final Context mContext;
        private final ViewPager mViewPager;
        private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();

        public static final class TabInfo {
            public final Class<?> clss;
            public final Bundle args;
            public final String title;
            public final int bgResource;

            public TabInfo(Class<?> _class, Bundle _args, String _title,
                           int _bgResource) {
                clss = _class;
                args = _args;
                title = _title;
                bgResource = _bgResource;
            }
        }

        public TabsAdapter(Activity activity, ViewPager pager) {
            super(activity.getFragmentManager());
            mContext = activity;
            mViewPager = pager;
            mViewPager.setAdapter(this);
        }

        public void addTab(String title, Class<?> clss, Bundle args, int resource) {
            TabInfo info = new TabInfo(clss, args, title, resource);
            mTabs.add(info);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mTabs.size();
        }

        @Override
        public Fragment getItem(int position) {
            TabInfo info = mTabs.get(position);
            return Fragment.instantiate(mContext, info.clss.getName(), info.args);
        }

        public TabInfo getItemInfo(int position) {
            return mTabs.get(position);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTabs.get(position).title;
        }
    }
}
