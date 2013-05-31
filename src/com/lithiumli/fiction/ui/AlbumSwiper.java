package com.lithiumli.fiction.ui;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.lithiumli.fiction.PlaybackQueue;
import com.lithiumli.fiction.Song;

public class AlbumSwiper extends View {
    Context mContext;
    PlaybackQueue mQueue;
    Drawable mPrev, mCurrent, mNext;
    int mOldPosition = -1;

    public AlbumSwiper(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public void setQueue(PlaybackQueue queue) {
        mQueue = queue;
    }

    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mQueue == null) return;

        int count = mQueue.getCount();

        if (count > 0) {
            int position = mQueue.getCurrentPosition();
            Song prev = null, current = null, next = null;

            // TODO: make this more robust (perhaps use a callback from
            // mQueue instead)
            if (position != mOldPosition) {
                mOldPosition = position;

                if (position > 0) {
                    prev = mQueue.getItem(position - 1);
                    mPrev = resolve(prev.getAlbumArt());
                }

                current = mQueue.getCurrent();
                mCurrent = resolve(current.getAlbumArt());

                if (position < count - 1) {
                    next = mQueue.getItem(position + 1);
                    mNext = resolve(next.getAlbumArt());
                }
            }

            if (mCurrent != null) {
                mCurrent.draw(canvas);
            }
            else {
            }
        }
    }

    private Drawable resolve(Uri uri) {
        Drawable d = null;

        if (uri != null) {
            String scheme = uri.getScheme();

            if (ContentResolver.SCHEME_CONTENT.equals(scheme) ||
                ContentResolver.SCHEME_FILE.equals(scheme)) {
                try {
                    d = Drawable.createFromStream(
                        mContext.getContentResolver().openInputStream(uri),
                        null);
                }
                catch (Exception e) {
                }
            }
            else {
                d = Drawable.createFromPath(uri.toString());
            }
        }

        if (d == null) {
            Log.d("fiction", "no cover");
            return null;
        }
        else {
            BitmapDrawable b = (BitmapDrawable) d;
            int w = getWidth();
            int h = getHeight();
            int bw = b.getIntrinsicWidth();
            int bh = b.getIntrinsicHeight();

            int fw, fh;

            if (bw > bh) {
                fw = w;
                fh = (int) (((float) bh / (float) bw) * fw);
            }
            else {
                fh = h;
                fw = (int) (((float) bw / (float) bh) * fh);
            }

            Log.d("fiction", Integer.toString(fw) + "," + Integer.toString(fh));
            Log.d("fiction", Integer.toString(w) + ",," + Integer.toString(h));
            Log.d("fiction", Integer.toString(bw) + ",,," + Integer.toString(bh));

            b.setBounds(0, (h - fh) / 2, fw, (h + fh) / 2);

            return b;
        }
    }
}
