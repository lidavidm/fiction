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
import android.widget.ImageButton;
import android.widget.TextView;
import android.util.Log;

public class NowPlayingActivity
    extends FictionActivity
{
    TextView mTitle;
    TextView mSubtitle;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.now_playing);

        mTitle = (TextView) findViewById(R.id.np_song_name);
        mSubtitle = (TextView) findViewById(R.id.np_song_subtitle);
        mSubtitle.setSelected(true);

        getActionBar().setSubtitle("Now Playing");
    }

    @Override
    public void onSongChange(Song song) {
        getActionBar().setTitle(song.getArtist());

        mTitle.setText(song.getTitle());
        mSubtitle.setText(song.getArtist() + " - " + song.getAlbum());
    }
}
