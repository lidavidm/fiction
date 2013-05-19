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

import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;
import android.widget.ImageButton;


public class SublibraryActivity
    extends FictionActivity
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        initializeDrawer(false);

        getActionBar().setSubtitle("Fiction Music");

        View layout = findViewById(R.id.bab_info);
        layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(SublibraryActivity.this,
                                               NowPlayingActivity.class);
                    SublibraryActivity.this.startActivity(intent);
                }
            });
    }

    @Override
    public void onSongChange(Song song) {
        ((TextView) findViewById(R.id.bab_song_name)).setText(song.getTitle());
        ((TextView) findViewById(R.id.bab_song_subtitle)).setText(song.getArtist() + " - " + song.getAlbum());
    }

    @Override
    public void onPlayStateChange(PlaybackService.PlayState state) {
        ImageButton button = (ImageButton) findViewById(R.id.bab_play_pause);

        switch (state) {
        case PLAYING:
            button.setImageResource(R.drawable.ic_menu_pause);
            break;
        case PAUSED:
            button.setImageResource(R.drawable.ic_menu_play);
            break;
        default:
            break;
        }
    }
}
