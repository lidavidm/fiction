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
import android.database.Cursor;
import android.net.Uri;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.util.Log;

import android.support.v4.view.ViewPager;
import android.support.v13.app.FragmentPagerAdapter;

import de.keyboardsurfer.android.widget.crouton.Crouton;

import com.viewpagerindicator.LinePageIndicator;

import java.util.ArrayList;

import com.lithiumli.fiction.fragments.*;
import com.lithiumli.fiction.ui.UiUtils;

public class LibraryActivity
    extends FictionActivity
    implements ViewPager.OnPageChangeListener
{
    ViewPager mViewPager;
    TabsAdapter mTabsAdapter;
    TextView babSongTitle;
    TextView babSubtitle;
    ImageView babCover;
    Crouton mCrouton;
    LinePageIndicator mIndicator;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        initializeDrawer(true);

        mViewPager = (ViewPager) findViewById(R.id.view_pager);

        mTabsAdapter = new TabsAdapter(this, mViewPager);
        mTabsAdapter.addTab("Artists", ArtistsGridFragment.class, null);
        mTabsAdapter.addTab("Songs", SongsListFragment.class, null);
        mTabsAdapter.addTab("Playlists", PlaylistsListFragment.class, null);
        mViewPager.setAdapter(mTabsAdapter);

        mIndicator = (LinePageIndicator)
            findViewById(R.id.view_pager_indicator);
        mIndicator.setViewPager(mViewPager);
        mIndicator.setOnPageChangeListener(this);

        mViewPager.setCurrentItem(1);

        babSongTitle = (TextView) findViewById(R.id.bab_song_name);
        babSongTitle.setSelected(true);
        babSubtitle = (TextView) findViewById(R.id.bab_song_subtitle);
        babSubtitle.setSelected(true);
        babCover = (ImageView) findViewById(R.id.bab_cover);

        getActionBar().setSubtitle("Songs");
        getActionBar().setTitle("Library");

        initializeBottomActionBar();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.library, menu);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();

        searchView.setOnQueryTextListener(
            new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextChange(String query) {
                    // TODO XXX causes NPE on orientation change
                    // FictionListFragment f = mTabsAdapter.getFragment(mViewPager.getCurrentItem());
                    // f.filter(query);
                    return true;
                }

                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }
            });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean drawerOpen = isDrawerOpen();
        menu.findItem(R.id.search).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onSongChange(Song song) {
        showPlayerInfo();

        babSongTitle.setText(song.getTitle());
        babSubtitle.setText(song.getArtist() + " - " + song.getAlbum());
        babCover.setImageURI(song.getAlbumArt());
    }

    @Override
    public void onServiceConnected(PlaybackService service) {
        if (service.getQueue().getCount() == 0) {
            findViewById(R.id.bab).setVisibility(View.GONE);
        }
    }

    @Override
    public void onPlayStateChange(PlaybackService.PlayState state) {
        ImageButton button = (ImageButton) findViewById(R.id.bab_play_pause);

        switch (state) {
        case PLAYING:
            button.setImageResource(R.drawable.ic_menu_pause);
            break;
        case STOPPED:
            hidePlayerInfo();
        case PAUSED:
            button.setImageResource(R.drawable.ic_menu_play);
            break;
        default:
            break;
        }
    }

    // TODO move this stuff into a fragment
    private boolean isPlayerInfoShowing() {
        return findViewById(R.id.bab).getVisibility() == View.VISIBLE;
    }

    private void showPlayerInfo() {
        View bab = findViewById(R.id.bab);
        View bab_hr = findViewById(R.id.bab_hr);
        if (bab.getVisibility() == View.GONE) {
            bab.setVisibility(View.VISIBLE);
            Animation showBab = AnimationUtils.loadAnimation(this, R.anim.bab);
            bab.startAnimation(showBab);
            bab_hr.startAnimation(showBab);
        }
    }

    private void hidePlayerInfo() {
        View bab = findViewById(R.id.bab);
        View bab_hr = findViewById(R.id.bab_hr);
        if (bab.getVisibility() == View.VISIBLE) {
            Animation hideBab = AnimationUtils.loadAnimation(this, R.anim.bab_hide);
            bab.startAnimation(hideBab);
            bab_hr.startAnimation(hideBab);
            bab.setVisibility(View.GONE);
        }
    }

    public void onSongSelected(int position, Cursor cursor) {
        if (this.isServiceBound()) {
            PlaybackService service = this.getService();
            PlaybackQueue queue = service.getQueue();

            if (queue.getContext() != PlaybackQueue.QueueContext.SONG) {
                queue.setContext(PlaybackQueue.QueueContext.SONG,
                                 cursor);
            }

            boolean wasShuffling = false;
            if (queue.isShuffling()) {
                queue.restoreShuffle();
                wasShuffling = true;
            }

            service.play(position);

            if (wasShuffling) {
                queue.shuffle();
            }
        }
    }

    public void onPlaylistSelected(Uri uri) {
        Intent intent = new Intent(this, PlaylistsSublibraryActivity.class);
        intent.putExtra(PlaylistsSublibraryActivity.DATA_URI, uri);
        startActivity(intent);
    }

    public void onSongEnqueued(Song song) {
        if (this.isServiceBound()) {
            PlaybackService service = this.getService();
            PlaybackQueue queue = service.getQueue();

            if (queue.getContext() != PlaybackQueue.QueueContext.QUEUE) {
                Song currentSong = null;
                if (queue.getCount() > 0) {
                    currentSong = queue.getCurrent();
                }

                queue.setContext(PlaybackQueue.QueueContext.QUEUE, null);

                if (currentSong != null) {
                    queue.enqueue(currentSong);
                }
                queue.setCurrent(0);
            }

            queue.enqueue(song);
            service.queueChanged();

            if (mCrouton != null) {
                Crouton.hide(mCrouton);
            }

            mCrouton = Crouton.makeText(
                this,
                String.format(getResources().getString(R.string.song_enqueued),
                              song.getTitle()),
                UiUtils.STYLE_INFO);
            mCrouton.show();

            if (!isPlayerInfoShowing()) {
                onSongChange(queue.getCurrent());
                showPlayerInfo();
            }
        }
    }

    @Override
    public void onPageSelected(int position) {
        ActionBar actionBar = getActionBar();
        actionBar.setSubtitle(mTabsAdapter.getPageTitle(position));

        // TODO: replace with something better
        switch (position) {
        case 0:
            mIndicator.setSelectedColor(0xFF9933CC);
            break;
        case 1:
            mIndicator.setSelectedColor(0xFFFF8800);
            break;
        case 2:
            mIndicator.setSelectedColor(0xFFCC0000);
            break;
        default:
            mIndicator.setSelectedColor(0xFF9933CC);
        }
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
            // public FictionListFragment fragment;

            public TabInfo(Class<?> _class, Bundle _args, String _title) {
                clss = _class;
                args = _args;
                title = _title;
            }
        }

        public TabsAdapter(Activity activity, ViewPager pager) {
            super(activity.getFragmentManager());
            mContext = activity;
            mViewPager = pager;
            mViewPager.setAdapter(this);
        }

        public void addTab(String title, Class<?> clss, Bundle args) {
            TabInfo info = new TabInfo(clss, args, title);
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
            // info.fragment = (FictionListFragment) Fragment.instantiate(mContext, info.clss.getName(), info.args);
            // return info.fragment;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTabs.get(position).title;
        }

        // public FictionListFragment getFragment(int position) {
        //     return mTabs.get(position).fragment;
        // }
    }
}
