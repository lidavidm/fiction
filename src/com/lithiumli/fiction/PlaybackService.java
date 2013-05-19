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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.RemoteException;
import android.util.Log;

import android.support.v4.content.LocalBroadcastManager;

import java.io.IOException;
import java.util.ArrayList;

import com.lithiumli.fiction.R;
import com.lithiumli.fiction.Song;

public class PlaybackService
    extends Service
    implements MediaPlayer.OnPreparedListener,
               MediaPlayer.OnCompletionListener {
    public static final String EVENT_PLAYING = "com.lithiumli.fiction.PLAYING";
    public static final String EVENT_PLAY_STATE = "com.lithiumli.fiction.PLAY_STATE";
    public static final String DATA_SONG = "com.lithiumli.fiction.SONG";
    public static final String DATA_STATE = "com.lithiumli.fiction.STATE";

    private static final int NOTIFICATION_PLAYING = 0;

    public enum PlayState {
        PLAYING,
        PAUSED,
        STOPPED
    }

    MediaPlayer mMediaPlayer;
    MediaPlayer mNextPlayer;
    boolean mPaused = false;
    Song mCurrentSong;
    PlaybackQueue mQueue;
    public final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        PlaybackService getService() {
            return PlaybackService.this;
        }
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mQueue == null) {
            mQueue = new PlaybackQueue();
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("fiction", "binding in service");
        return mBinder;
    }

    public void onPrepared(MediaPlayer player) {
        if (mMediaPlayer == null) {
            mMediaPlayer = player;
        }
        else {
            mMediaPlayer.setNextMediaPlayer(player);
            if (mMediaPlayer.isPlaying()) {
                Log.d("fiction", "Was playing");
                mMediaPlayer.stop();
            }
            Log.d("fiction", "Starting music");
            mMediaPlayer.release();
            mMediaPlayer = null;
            mMediaPlayer = player;
        }
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        player.start();
        prepareNext();

        mPaused = false;

        showNotification();
    }

    public void onCompletion(MediaPlayer player) {
        int position = mQueue.getCurrentPosition();

        if (position >= mQueue.getCount() - 1) {
            return;
        }

        mQueue.setCurrent(position + 1);
        mMediaPlayer = mNextPlayer;
        prepareNext();

        Intent intent = new Intent(EVENT_PLAYING);
        intent.putExtra(DATA_SONG, mQueue.getCurrent());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        Log.d("fiction", "destroying service");
        if (mMediaPlayer != null) mMediaPlayer.release();
        super.onDestroy();
    }

    // PUBLIC INTERFACE

    public PlaybackQueue getQueue() {
        return mQueue;
    }

    public void play(int index) {
        mQueue.setCurrent(index);
        Song song = mQueue.getCurrent();

        Intent intent = new Intent(EVENT_PLAYING);
        intent.putExtra(DATA_SONG, song);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        intent = new Intent(EVENT_PLAY_STATE);
        intent.putExtra(DATA_STATE, PlayState.PLAYING.name());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        Log.d("fiction", "Playing new song");
        try {
            MediaPlayer nextMediaPlayer = new MediaPlayer();
            nextMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            nextMediaPlayer.setDataSource(getApplicationContext(),
                                          song.getUri());
            nextMediaPlayer.setOnPreparedListener(this);
            nextMediaPlayer.prepareAsync();
        }
        catch (IOException e) {
        }
    }

    public void next() {
        int position = mQueue.getCurrentPosition();

        if (position <= mQueue.getCount()) {
            play(position + 1);
        }
    }

    public void prev() {
        int position = mQueue.getCurrentPosition();

        if (position > 0) {
            play(position - 1);
        }
    }

    public void pause() {
        if (mMediaPlayer != null) {
            mMediaPlayer.pause();
            mPaused = true;
        }

        Intent intent = new Intent(EVENT_PLAY_STATE);
        intent.putExtra(DATA_STATE, PlayState.PAUSED.name());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public void unpause() {
        if (mMediaPlayer != null) {
            mMediaPlayer.start();
            mPaused = false;
        }

        Intent intent = new Intent(EVENT_PLAY_STATE);
        intent.putExtra(DATA_STATE, PlayState.PLAYING.name());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public boolean isPlaying() {
        return (mMediaPlayer != null) && (mMediaPlayer.isPlaying());
    }

    // PRIVATE INTERFACE

    private void prepareNext() {
        int position = mQueue.getCurrentPosition();

        if (position >= mQueue.getCount() - 1) {
            // TODO: repeat modes
            return;
        }

        Song song = mQueue.getItem(position + 1);
        try {
            mNextPlayer = new MediaPlayer();
            mNextPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mNextPlayer.setDataSource(getApplicationContext(),
                                          song.getUri());
            mNextPlayer.
                setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer player) {
                            if (mMediaPlayer == null) {
                            }
                            else {
                                mMediaPlayer.setNextMediaPlayer(player);
                                mMediaPlayer.setOnCompletionListener(PlaybackService.this);
                                player.setOnCompletionListener(PlaybackService.this);
                            }
                        }
                });
            mNextPlayer.prepareAsync();
        }
        catch (IOException e) {
        }
    }

    private void showNotification() {
        // Intent launchPlaybackIntent = new Intent(getApplicationContext(),
        //                                          FictionPlaybackActivity.class);
        // launchPlaybackIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
        //                               Intent.FLAG_ACTIVITY_SINGLE_TOP);
        // PendingIntent pi = PendingIntent.getActivity(
        //     getApplicationContext(),
        //     0,
        //     launchPlaybackIntent,
        //     PendingIntent.FLAG_UPDATE_CURRENT);

        // String title = "(unknown title)";
        // if (mCurrentSong != null) {
        //     title = mCurrentSong.getTitle();
        // }

        // Intent intent = new Intent(FictionMusicPlayerService.ACTION_PAUSE,
        //                            Uri.EMPTY,
        //                            getApplicationContext(),
        //                            PlaybackService.class);

        // Notification.Builder builder = new Notification.Builder(getApplicationContext());
        // builder
        //     .setSmallIcon(R.drawable.av_play)
        //     .setContentTitle(title)
        //     .setContentText("playing")
        //     .setOngoing(true)
        //     .setContentIntent(pi)
        //     .addAction(R.drawable.av_pause, "Pause",
        //                PendingIntent.getService(getApplicationContext(),
        //                                         0,
        //                                         intent,
        //                                         0));
        // Notification notification = builder.build();

        // startForeground(NOTIFICATION_PLAYING, notification);
        // ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
        //     .notify(NOTIFICATION_PLAYING, notification);
    }

    private void hideNotification() {
        // ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
        //     .cancel(NOTIFICATION_PLAYING);
    }
}
