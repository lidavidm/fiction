package com.lithiumli.fiction.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
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

import com.lithiumli.fiction.FictionActivity;
import com.lithiumli.fiction.PlaybackQueue;
import com.lithiumli.fiction.R;
import com.lithiumli.fiction.Song;

public class AlbumSwiper extends View {
    Context mContext;
    PlaybackQueue mQueue;
    Cover[] mCovers = new Cover[5];
    Cover mNoCover;
    GestureDetector mDetector;
    double mTheta;
    FictionActivity mListener;

    float mStartX, mStartY;

    public AlbumSwiper(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        // TODO make this an XML attr
        mNoCover = new Cover(
            (BitmapDrawable) mContext.getResources().getDrawable(R.drawable.filler_album),
            400, 400, 1.0);
        mDetector = new GestureDetector(context, new Listener());
    }

    public void setQueue(PlaybackQueue queue) {
        mQueue = queue;
    }

    public void setListener(FictionActivity activity) {
        mListener = activity;
    }
    public int getRadius() {
        return (int) (getWidth() / 3f);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Cover[] covers = java.util.Arrays.copyOf(mCovers, 5);
        java.util.Arrays.sort(covers);
        for (Cover c : covers) {
            if (c != null) {
                c.draw(canvas);
            }
        }
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldw, int oldh) {
        if (width == 0 || height == 0) return;
        updateCovers();
    }

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
        case MotionEvent.ACTION_DOWN:

            break;
        case MotionEvent.ACTION_SCROLL:
            if (mQueue != null) {
                if (mQueue.getCurrentPosition() == 0) {
                }
                else if (mQueue.getCurrentPosition() == mQueue.getCount() - 1) {
                }
            }
            break;
        case MotionEvent.ACTION_UP:
        case MotionEvent.ACTION_CANCEL:
            if (mTheta >= Math.PI / 5) {
                scrollTo(Math.PI / 5, new Runnable() {
                        @Override
                        public void run() {
                            if (mListener != null) {
                                mListener.prevButton(AlbumSwiper.this);
                            }
                        }
                    });
            }
            else if (mTheta <= -Math.PI / 5) {
                scrollTo(-Math.PI / 5, new Runnable() {
                        @Override
                        public void run() {
                            if (mListener != null) {
                                mListener.nextButton(AlbumSwiper.this);
                            }
                        }
                    });
            }
            else {
                scrollTo(0.0, null);
            }
            break;
        default:
            break;
        }
        return true;
    }

    private void scroll(double theta) {
        mTheta = theta;
        int r = getRadius();
        double t = theta - (2 * Math.PI / 4);
        for (Cover c : mCovers) {
            if (c != null) {
                c.setAngle(t);
                int alpha = (int) (320 * Math.cos(theta));
                if (alpha > 255) alpha = 255;
                else if (alpha < 192) alpha = 192;
                c.setAlpha(alpha);
            }
            t += Math.PI / 4;
        }
    }

    private void scrollTo(double theta, final Runnable callback) {
        ValueAnimator a = ValueAnimator.ofFloat((float) mTheta, (float) theta);
        a.setDuration(200);
        a.addUpdateListener(
            new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator a) {
                    Float angle = (Float) a.getAnimatedValue();
                    scroll(angle.doubleValue());
                    invalidate();
                }
            });

        if (callback != null) {
            a.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator a) {
                        callback.run();
                    }
                });
        }

        a.start();
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
            if (bw > bh) {
                fw = w;
                fh = (int) (((float) bh / (float) bw) * fw);

                if (fh > h) {
                    fw = (int) (((float) h / fh) * fw);
                    fh = h;
                }
            }
            else {
                fh = h;
                fw = (int) (((float) bw / (float) bh) * fh);

                if (fw > w) {
                    fh = (int) (((float) w / fw) * fh);
                    fw = w;
                }
            }

            return new Cover(b, fw, fh, angle);
        }
    }

    class Listener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                float distanceX, float distanceY) {
            double theta = (e2.getX() - e1.getX()) / AlbumSwiper.this.getRadius();
            AlbumSwiper.this.scroll(theta);
            AlbumSwiper.this.invalidate();
            return true;
        }
    }

    class Cover implements Comparable {
        int width, height;
        BitmapDrawable b;
        double theta;

        final float MIN_SCALE = (float) Math.cos(Math.PI / 4);

        public Cover(BitmapDrawable _b, int _width, int _height, double _theta) {
            b = _b;
            width = _width;
            height = _height;
            theta = _theta;

            setBounds();
        }

        public void setBounds() {
            int r = AlbumSwiper.this.getRadius();
            int viewHeight = AlbumSwiper.this.getHeight();
            int viewWidth = AlbumSwiper.this.getWidth();
            float scale = (float) Math.cos(theta);

            if (scale < MIN_SCALE) scale = MIN_SCALE;

            int fw = (int) (scale * width);
            int fh = (int) (scale * height);
            int offsetX = (int) ((viewWidth / 2f) - (fw / 2f) +  (r * Math.sin(theta)));
            int offsetY = (viewHeight - fh) / 2;

            b.setBounds(offsetX, offsetY, fw + offsetX, fh + offsetY);
        }

        public void setAlpha(int alpha) {
            b.setAlpha(alpha);
        }

        public double getAngle() {
            return theta;
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

        public int compareTo(Object o) {
            if (o == null) {
                return -1;
            }
            Cover c = (Cover) o;
            double a1 = Math.abs(getAngle());
            double a2 = Math.abs(c.getAngle());

            // angle of 0 is "largest"
            if (a1 > a2) {
                return -1;
            }
            else if (a2 > a1) {
                return 1;
            }
            return 0;
        }
    }
}
