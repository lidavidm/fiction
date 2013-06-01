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
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.Scroller;

import com.lithiumli.fiction.PlaybackQueue;
import com.lithiumli.fiction.R;
import com.lithiumli.fiction.Song;

public class AlbumSwiper extends View {
    Context mContext;
    PlaybackQueue mQueue;
    Cover[] mCovers = new Cover[5];
    Cover mNoCover;
    GestureDetector mDetector;

    float mStartX, mStartY;

    public AlbumSwiper(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        // TODO make this an XML attr
        mNoCover = new Cover(
            (BitmapDrawable) mContext.getResources().getDrawable(R.drawable.filler_album),
            400, 400, 0, 1.0f);
        mDetector = new GestureDetector(context, new Listener());
    }

    public void setQueue(PlaybackQueue queue) {
        mQueue = queue;
    }

    public int getRadius() {
        return (int) (getWidth() / 3f);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        _draw(canvas, 1);
        _draw(canvas, 3);
        _draw(canvas, 2);
    }

    private void _draw(Canvas canvas, int index) {
        Cover c = mCovers[index];
        if (c != null) {
            c.draw(canvas);
        }
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldw, int oldh) {
        if (width == 0 || height == 0) return;
        updateCovers();
    }

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
        mDetector.onTouchEvent(ev);
		// float x = ev.getX();
		// float y = ev.getY();

		switch (ev.getAction()) {
		// case MotionEvent.ACTION_DOWN:
        //     mStartX = x;
        //     mStartY = y;
        //     break;
        // case MotionEvent.ACTION_MOVE:
        //     int dx = (int) (x - mStartX);
        //     float scale = 1.0f - 0.75f * Math.abs(dx) / getWidth();  // 1/3 = 75%
        //     if (scale < 0.25f) scale = 0.33f;

        //     if (mCurrent != null) {
        //         mCurrent.setOffsetX(dx);
        //         mCurrent.setScale(scale);
        //     }
        //     invalidate();
        //     break;
        case MotionEvent.ACTION_UP:
        case MotionEvent.ACTION_CANCEL:
            scroll(0.0);
            invalidate();
            break;
        default:
            break;
        }
        return true;
    }

    private void scroll(double theta) {
        int r = getRadius();
        double t = theta - (2 * Math.PI / 4);
        for (Cover c : mCovers) {
            if (c != null) {
                c.setAngle(t);
                c.setAlpha((int) (255 * Math.cos(theta)));
            }
            t += Math.PI / 4;
        }
    }

    public void updateCovers() {
        if (mQueue == null) return;

        int count = mQueue.getCount();
        if (count <= 0) return;

        int position = mQueue.getCurrentPosition();
        Song prev = null, current = null, next = null;

        double r = (int) (getWidth() / 3.0);
        double theta = -Math.PI / 2;
        for (int posOffset = -2; posOffset <= 2; posOffset ++) {
            int index = position + posOffset;
            int coverOffset = posOffset + 2;

            if (index >= 0) {
                Song song = mQueue.getItem(index);
                mCovers[coverOffset] = resolve(
                    song.getAlbumArt(),
                    theta);
                mCovers[coverOffset].setAlpha((int) (255 * Math.cos(theta)));
            }
            else {
                mCovers[coverOffset] = null;
            }
            theta += Math.PI / 4;
        }
    }

    private Cover resolve(Uri uri, double angle) {
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
            Log.d("fiction", Integer.toString(w) + "," + Integer.toString(h));
            if (bw > bh) {
                fw = w;
                fh = (int) (((float) bh / (float) bw) * fw);
            }
            else {
                fh = h;
                fw = (int) (((float) bw / (float) bh) * fh);
            }

            int offsetX = (int) (0.05 * getWidth());

            return new Cover(b, fw, fh, offsetX, angle);
        }
    }

    class Listener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                float distanceX, float distanceY) {
            int dx = (int) (e2.getX() - e1.getX());
            double theta = Math.atan((double) dx / AlbumSwiper.this.getRadius());

            AlbumSwiper.this.scroll(theta);
            AlbumSwiper.this.invalidate();
            return true;
        }
    }

    class Cover {
        int width, height, offset;
        BitmapDrawable b;
        double theta;

        public Cover(BitmapDrawable _b, int _width, int _height,
                     int _offset, double _theta) {
            b = _b;
            width = _width;
            height = _height;
            offset = _offset;
            theta = _theta;

            setBounds();
        }

        public void setBounds() {
            int r = AlbumSwiper.this.getRadius();
            int viewHeight = AlbumSwiper.this.getHeight();
            float scale = (float) Math.cos(theta);

            int fw = (int) (scale * width);
            int fh = (int) (scale * height);
            int offsetX = offset + (int) (r * Math.sin(theta));
            int offsetY = (viewHeight - fh) / 2;

            Log.d("fiction", Double.toString(theta / Math.PI) + "," +
                             Float.toString(scale) + "," +
                             Integer.toString(offsetX));

            b.setBounds(offsetX, offsetY, fw + offsetX, fh + offsetY);
        }

        public void setAlpha(int alpha) {
            b.setAlpha(alpha);
        }

        public void setAngle(double _theta) {
            theta = _theta;
        }

        public BitmapDrawable getDrawable() {
            return b;
        }

        public void draw(Canvas c) {
            setBounds();
            b.draw(c);
        }
    }
}
