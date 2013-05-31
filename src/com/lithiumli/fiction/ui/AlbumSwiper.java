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
import com.lithiumli.fiction.R;
import com.lithiumli.fiction.Song;

public class AlbumSwiper extends View {
    Context mContext;
    PlaybackQueue mQueue;
    Drawable mPrev, mCurrent, mNext;
    Drawable mNoCover;
    int mOldPosition = -1;

    public AlbumSwiper(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        // TODO make this an XML attr
        mNoCover =
            mContext.getResources().getDrawable(R.drawable.filler_album);
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

                int offset = getWidth() / 3;

                if (position > 0) {
                    prev = mQueue.getItem(position - 1);
                    mPrev = resolve(prev.getAlbumArt(), -offset, 0, 0.75f);
                    mPrev.setAlpha(192);
                }
                else {
                    mPrev = null;
                }

                current = mQueue.getCurrent();
                mCurrent = resolve(current.getAlbumArt(), 0, 0, 1f);
                mCurrent.setAlpha(224);

                if (position < count - 1) {
                    next = mQueue.getItem(position + 1);
                    mNext = resolve(next.getAlbumArt(), offset, 0, 0.75f);
                    mNext.setAlpha(192);
                }
            }

            if (mPrev != null) {
                mPrev.draw(canvas);
            }
            if (mNext != null) {
                mNext.draw(canvas);
            }
            if (mCurrent != null) {
                mCurrent.draw(canvas);
            }
        }
    }

    private Drawable resolve(Uri uri, int offsetX, int offsetY, float scale) {
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
            return mNoCover;
        }
        else {
            BitmapDrawable b = (BitmapDrawable) d;
            int w = (int) (0.9 * getWidth());
            int h = getHeight();
            int bw = b.getIntrinsicWidth();
            int bh = b.getIntrinsicHeight();

            int fw, fh;

            offsetX += (int) (0.05 * getWidth());

            if (bw > bh) {
                fw = w;
                fh = (int) (((float) bh / (float) bw) * fw);
            }
            else {
                fh = h;
                fw = (int) (((float) bw / (float) bh) * fh);
            }

            fh = (int) (scale * fh);
            fw = (int) (scale * fw);

            b.setBounds(offsetX, (h - fh) / 2, fw + offsetX, (h + fh) / 2);

            return b;
        }
    }
}
