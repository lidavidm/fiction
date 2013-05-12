package com.lithiumli.fiction;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.ListView;

import android.support.v4.content.LocalBroadcastManager;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingActivity;

abstract public class FictionActivity extends SlidingActivity
{
    ListView mQueueListView;
    PlaybackQueue.QueueAdapter mAdapter;

    protected PlaybackService mService;
    protected boolean mBound;
    private ServiceConnection mConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName className, IBinder service) {
                PlaybackService.LocalBinder binder = (PlaybackService.LocalBinder) service;
                mService = binder.getService();
                mBound = true;
                Log.d("fiction", "finished binding");

                mAdapter = mService.getQueue().getAdapter(getApplicationContext());
                mQueueListView.setAdapter(mAdapter);
            }

            public void onServiceDisconnected(ComponentName className) {
                mBound = false;
            }
        };

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Song song = intent.getParcelableExtra(PlaybackService.DATA_SONG);
                FictionActivity.this.onSongChange(song);
            }
        };

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        // Queue menu
        SlidingMenu menu = getSlidingMenu();
        menu.setMode(SlidingMenu.LEFT);
        menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
        menu.setShadowWidthRes(R.dimen.slidingmenu_shadow_width);
        menu.setShadowDrawable(R.drawable.shadow);
        menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        menu.setFadeDegree(0.35f);

        setSlidingActionBarEnabled(false);
        setBehindContentView(R.layout.queue);

        mQueueListView = (ListView) findViewById(R.id.queue);
        mQueueListView.setFastScrollEnabled(true);
        mQueueListView.setFastScrollAlwaysVisible(true);

        // Playback stuff
        Intent intent = new Intent(this, PlaybackService.class);
        startService(intent);
        Log.d("fiction", "binding");
        intent = new Intent(this, PlaybackService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                                                                 new IntentFilter(PlaybackService.EVENT_PLAYING));
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }

    public PlaybackService getService() {
        if (mBound) {
            Log.d("fiction", "bound");
            return mService;
        }

        Log.d("fiction", "unbound");
        return null;
    }

    // EVENTS

    public void onSongChange(Song song) {
    }
}
