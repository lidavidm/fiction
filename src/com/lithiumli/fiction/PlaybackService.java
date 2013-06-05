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
import android.widget.RemoteViews;

import android.support.v4.content.LocalBroadcastManager;

import java.io.IOException;
import java.util.ArrayList;

import com.lithiumli.fiction.R;
import com.lithiumli.fiction.Song;

public class PlaybackService
    extends Service
    implements MediaPlayer.OnPreparedListener,
    MediaPlayer.OnCompletionListener,
    AudioManager.OnAudioFocusChangeListener {
    public static final String EVENT_PLAYING = "com.lithiumli.fiction.PLAYING";
    public static final String EVENT_PLAY_STATE = "com.lithiumli.fiction.PLAY_STATE";
    public static final String DATA_SONG = "com.lithiumli.fiction.SONG";
    public static final String DATA_STATE = "com.lithiumli.fiction.STATE";
    public static final String ACTION_PREV = "com.lithiumli.fiction.notification.PREV";
    public static final String ACTION_PLAY_PAUSE = "com.lithiumli.fiction.notification.PLAY_PAUSE";
    public static final String ACTION_NEXT = "com.lithiumli.fiction.notification.NEXT";
    private static final int NOTIFICATION_PLAYING = 0;

    public enum PlayState {
        PLAYING,
        PAUSED,
        STOPPED
    }

    public enum RepeatMode {
        NO_REPEAT,
        REPEAT_ALL,
        REPEAT_ONE
    }

    MediaPlayer mMediaPlayer;
    MediaPlayer mNextPlayer;
    boolean mPaused = true;
    RepeatMode mRepeat;
    PlaybackQueue mQueue;
    public final IBinder mBinder = new LocalBinder();
    AudioManager mAudioManager;

    public class LocalBinder extends Binder {
        PlaybackService getService() {
            return PlaybackService.this;
        }
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mQueue == null) {
            mQueue = new PlaybackQueue();
        }

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        if (intent != null && intent.getAction() != null) {
            String action = intent.getAction();
            if (action.equals(ACTION_PLAY_PAUSE)) {
                if (mPaused) {
                    unpause();
                }
                else {
                    pause();
                }
            }
            else if (action.equals(ACTION_PREV)) {
                this.prev();
            }
            else if (action.equals(ACTION_NEXT)) {
                this.next();
            }
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("fiction", "binding in service");
        return mBinder;
    }

    protected AudioManager getAudioManager() {
        return mAudioManager;
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

        if (acquireAudioFocus()) {
            player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
            player.start();
            prepareNext();

            mPaused = false;

            showNotification();
        }
    }

    public void onCompletion(MediaPlayer player) {
        int position = mQueue.getCurrentPosition();

        if (position >= mQueue.getCount() - 1) {
            abandonAudioFocus();
            return;
        }

        mQueue.setCurrent(position + 1);
        mMediaPlayer = mNextPlayer;
        prepareNext();

        Intent intent = new Intent(EVENT_PLAYING);
        intent.putExtra(DATA_SONG, mQueue.getCurrent());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        showNotification();
    }

    @Override
    public void onDestroy() {
        Log.d("fiction", "destroying service");
        abandonAudioFocus();
        if (mMediaPlayer != null) mMediaPlayer.release();
        if (mNextPlayer != null) mNextPlayer.release();
        super.onDestroy();
    }

    public void onAudioFocusChange(int focusChange) {
        if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
            pause();
        }
        else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
            // Need to sync this so that we don't react to our own events
            if (!isPlaying()) {
                // unpause();
            }
        }
        else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
            abandonAudioFocus();
            pause();
        }
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

        broadcastPlayState(PlayState.PLAYING);

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

    public void stop() {
        hideNotification();
        broadcastPlayState(PlayState.STOPPED);
        abandonAudioFocus();
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        if (mNextPlayer != null) {
            mNextPlayer.release();
            mNextPlayer = null;
        }
    }

    public void next() {
        int position = mQueue.getCurrentPosition();

        if (position < mQueue.getCount() - 1) {
            play(position + 1);
            Log.d("fiction", "Next song");
        }
    }

    public void prev() {
        int position = mQueue.getCurrentPosition();

        if (position > 0) {
            play(position - 1);
            Log.d("fiction", "Prev song");
        }
    }

    public void pause() {
        if (mMediaPlayer != null) {
            mMediaPlayer.pause();
            mPaused = true;
            Log.d("fiction", "Pausing");

            abandonAudioFocus();
            broadcastPlayState(PlayState.PAUSED);
            showNotification();
        }
    }

    public void unpause() {
        if (mMediaPlayer != null) {
            mPaused = false;
            if (acquireAudioFocus()) {
                mMediaPlayer.start();
                broadcastPlayState(PlayState.PLAYING);
                showNotification();
            }
        }
    }

    public boolean isPlaying() {
        return (mMediaPlayer != null) && (mMediaPlayer.isPlaying()) && (!mPaused);
    }

    public PlayState getPlayState() {
        if (mPaused) {
            return PlayState.PAUSED;
        }
        else {
            return PlayState.PLAYING;
        }
    }

    public void queueChanged() {
        prepareNext();
    }

    // PRIVATE INTERFACE

    private boolean acquireAudioFocus() {
        int result = getAudioManager().requestAudioFocus(
            this,
            AudioManager.STREAM_MUSIC,
            AudioManager.AUDIOFOCUS_GAIN);

        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            return true;
        }
        return false;
    }

    private void abandonAudioFocus() {
        getAudioManager().abandonAudioFocus(this);
    }

    private void broadcastPlayState(PlayState state) {
        Intent intent = new Intent(EVENT_PLAY_STATE);
        intent.putExtra(DATA_STATE, state.name());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

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
        Intent launchPlaybackIntent = new Intent(getApplicationContext(),
                                                 NowPlayingActivity.class);
        launchPlaybackIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                      Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pi = PendingIntent.getActivity(
            getApplicationContext(),
            0,
            launchPlaybackIntent,
            PendingIntent.FLAG_UPDATE_CURRENT);

        String title = "(unknown title)";
        String artist = "(unknown artist)";
        String album = "(unknown album)";
        Uri albumArt = Song.DEFAULT_ALBUM;
        if (mQueue.getCount() != 0) {
            Song song = mQueue.getCurrent();
            title = song.getTitle();
            artist = song.getArtist();
            album = song.getAlbum();
            albumArt = song.getAlbumArt();

            // TODO see ImageView.resolveUri for a working method
            // if (albumArt.getPath() == null) {
            //     albumArt = Song.DEFAULT_ALBUM;
            // }
        }

        Notification.Builder builder = new Notification.Builder(getApplicationContext());
        builder
            .setSmallIcon(R.drawable.ic_menu_play)
            .setContentTitle("Playing" + title)
            .setOngoing(true)
            .setContentIntent(pi);
        Notification notification = builder.build();

        RemoteViews customView = new RemoteViews(getPackageName(),
                                                 R.layout.notification);
        customView.setImageViewUri(R.id.notification_cover, albumArt);
        customView.setTextViewText(R.id.notification_title, title);
        customView.setTextViewText(R.id.notification_subtitle, artist);
        notification.contentView = customView;

        customView = new RemoteViews(getPackageName(),
                                     R.layout.notification_big);
        customView.setImageViewUri(R.id.notification_cover, albumArt);
        customView.setImageViewResource(R.id.notification_play_pause,
                                        mPaused ? R.drawable.ic_menu_play : R.drawable.ic_menu_pause);
        customView.setTextViewText(R.id.notification_title, title);
        customView.setTextViewText(R.id.notification_album, album);
        customView.setTextViewText(R.id.notification_artist, artist);
        customView.setOnClickPendingIntent(R.id.notification_previous, createAction(ACTION_PREV));
        customView.setOnClickPendingIntent(R.id.notification_play_pause, createAction(ACTION_PLAY_PAUSE));
        customView.setOnClickPendingIntent(R.id.notification_next, createAction(ACTION_NEXT));
        notification.bigContentView = customView;

        startForeground(NOTIFICATION_PLAYING, notification);
        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
            .notify(NOTIFICATION_PLAYING, notification);
    }

    private PendingIntent createAction(String action) {
        Intent actionIntent = new Intent(getApplicationContext(),
                                         PlaybackService.class);
        actionIntent.setAction(action);
        PendingIntent pit = PendingIntent.getService(getApplicationContext(),
                                                     0,
                                                     actionIntent, 0);
        return pit;
    }

    private void hideNotification() {
        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
            .cancel(NOTIFICATION_PLAYING);
    }
}
