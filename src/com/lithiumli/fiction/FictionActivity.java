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
import android.app.ActivityOptions;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;

import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;

abstract public class FictionActivity extends Activity
{
    ListView mQueueListView;
    DrawerLayout mDrawer;
    ActionBarDrawerToggle mDrawerToggle;
    String mOldTitle;

    PlaybackQueue.QueueAdapter mAdapter;

    protected PlaybackService mService;
    protected boolean mBound;
    private ServiceConnection mConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName className, IBinder service) {
                PlaybackService.LocalBinder binder = (PlaybackService.LocalBinder) service;
                mService = binder.getService();
                mBound = true;
                Log.d("fiction", "bound to service");

                if (mService.getQueue().getCount() > 0) {
                    onSongChange(mService.getQueue().getCurrent());
                }

                onPlayStateChange(mService.getPlayState());

                mAdapter = mService.getQueue().getAdapter(getApplicationContext());
                mQueueListView.setAdapter(mAdapter);
                mAdapter.notifyDataSetChanged();

                FictionActivity.this.onServiceConnected(mService);
            }

            public void onServiceDisconnected(ComponentName className) {
                mBound = false;
            }
        };

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action == PlaybackService.EVENT_PLAYING) {
                    Song song = intent.getParcelableExtra(PlaybackService.DATA_SONG);
                    FictionActivity.this.onSongChange(song);
                }
                else if (action == PlaybackService.EVENT_PLAY_STATE) {
                    PlaybackService.PlayState state =
                        PlaybackService.PlayState.valueOf(intent.getStringExtra(PlaybackService.DATA_STATE));
                    FictionActivity.this.onPlayStateChange(state);
                }
            }
        };

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    public void initializeDrawer(boolean indicator) {
        mQueueListView = (ListView) findViewById(R.id.queue);
        mQueueListView.setFastScrollEnabled(true);
        mQueueListView.setFastScrollAlwaysVisible(true);
        mQueueListView.setOnItemClickListener(
            new AdapterView.OnItemClickListener()
            {
                @Override
                public void onItemClick(AdapterView<?> parent, View v,
                                        int position, long id) {
                    if (isServiceBound()) {
                        getService().play(position);

                        if (mAdapter != null) {
                            mAdapter.notifyDataSetChanged();
                        }
                    }
                }
            });

        mDrawer = (DrawerLayout) findViewById(R.id.drawer);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawer,
                                                  R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {
                public void onDrawerClosed(View view) {
                    getActionBar().setTitle(mOldTitle);
                    invalidateOptionsMenu();
                }

                public void onDrawerOpened(View drawerView) {
                    mOldTitle = (String) getActionBar().getTitle();
                    getActionBar().setTitle("Queue");
                    invalidateOptionsMenu();

                    if (mAdapter != null) {
                        mAdapter.notifyDataSetChanged();
                    }

                    if (isServiceBound()) {
                        PlaybackService service = getService();
                        PlaybackQueue queue = service.getQueue();

                        if (queue.getCount() != 0) {
                            mQueueListView.setSelection(queue.getCurrentPosition());
                        }
                    }
                }
            };
        mDrawer.setDrawerListener(mDrawerToggle);
        mDrawer.setDrawerShadow(R.drawable.shadow, GravityCompat.START);
        mDrawerToggle.setDrawerIndicatorEnabled(indicator);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
    }

    public void initializeBottomActionBar() {
        View layout = findViewById(R.id.bab_info);
        layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(FictionActivity.this,
                                               NowPlayingActivity.class);
                    ActivityOptions options =
                        ActivityOptions.makeCustomAnimation(FictionActivity.this,
                                                            R.anim.activity_slide_down,
                                                            R.anim.activity_slide_up);
                    FictionActivity.this.startActivity(intent, options.toBundle());
                }
            });
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d("fiction", "resuming");

        // Playback stuff
        Intent intent = new Intent(this, PlaybackService.class);
        startService(intent);
        Log.d("fiction", "binding");
        intent = new Intent(this, PlaybackService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        IntentFilter filter = new IntentFilter();
        filter.addAction(PlaybackService.EVENT_PLAYING);
        filter.addAction(PlaybackService.EVENT_PLAY_STATE);
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                                                                 filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.queue, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean drawerOpen = isDrawerOpen();
        menu.findItem(R.id.save_queue).setVisible(drawerOpen);
        menu.findItem(R.id.clear_queue).setVisible(drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch (item.getItemId()) {
        case R.id.save_queue:
            return true;
        case R.id.clear_queue:
            if (isServiceBound()) {
                PlaybackService service = getService();
                service.stop();
                service.getQueue().clear();
                mDrawer.closeDrawers();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public PlaybackService getService() {
        if (mBound) {
            return mService;
        }

        return null;
    }

    public boolean isServiceBound() {
        return mBound;
    }

    public boolean isDrawerOpen() {
        return mDrawer.isDrawerOpen(GravityCompat.START);
    }

    // EVENTS

    public void onServiceConnected(PlaybackService service) {
    }

    public void onSongChange(Song song) {
    }

    public void onPlayStateChange(PlaybackService.PlayState state) {
    }

    public void playPauseButton(View view) {
        PlaybackService service = getService();
        ImageButton button = (ImageButton) view;
        if (service.isPlaying()) {
            service.pause();
        }
        else {
            service.unpause();

            if (!service.isPlaying() && service.getQueue().getCount() > 0) {
                service.play(0);
            }
            else {
                // TODO: restore queue/queue everything and play
                return;
            }
        }

        button.invalidate();
    }

    public void prevButton(View view) {
        getService().prev();
    }

    public void nextButton(View view) {
        getService().next();
    }
}
