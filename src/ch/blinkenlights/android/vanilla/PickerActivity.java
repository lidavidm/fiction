/*
 * Copyright (C) 2012 Christopher Eby <kreed@kreed.org>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package ch.blinkenlights.android.vanilla;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

public class PickerActivity extends LibraryActivity
{
    private long mPlaylistId;
    private String mPlaylistName;

    @Override
    public void onCreate(Bundle state)
    {
        super.onCreate(state);

        mActionControls.setVisibility(View.INVISIBLE);

        Intent intent = getIntent();
		mPlaylistId = intent.getLongExtra("playlist", 0);
        mPlaylistName = intent.getStringExtra("playlistName");

        getLayoutInflater().inflate(R.layout.picker_done, null);
    }

    @Override
    public void onItemClicked(Intent rowData)
    {
        rowData.putExtra("playlistName", mPlaylistName);
        addToPlaylist(mPlaylistId, rowData);
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		return false;
	}
}
