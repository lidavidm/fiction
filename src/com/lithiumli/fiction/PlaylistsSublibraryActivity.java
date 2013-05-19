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
import android.widget.TextView;
import android.util.Log;

import com.lithiumli.fiction.fragments.*;

public class PlaylistsSublibraryActivity
    extends SublibraryActivity
{
    TextView babSongTitle;
    TextView babSubtitle;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playlists);

        ActionBar ab = getActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle("Playlist");
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
}
