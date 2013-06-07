package com.lithiumli.fiction;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.Request.Method;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

import com.lithiumli.fiction.util.BitmapLruCache;

public class ArtistImageCache {
    static final String ECHO_NEST_URL = "http://developer.echonest.com/api/v4/artist/images?api_key=ETDSSZR6RAMYOU4SI&results=1&name=";
    final RequestQueue mRequestQueue;
    final ImageLoader mImageLoader;
    final BitmapLruCache mCache;

    public ArtistImageCache(Context context) {
        mCache = new BitmapLruCache();
        mRequestQueue = Volley.newRequestQueue(context);
        mImageLoader = new ImageLoader(mRequestQueue, new FakeCache());
    }

    public void getImage(String artist, CacheCallback callback) {
        try {
            artist = java.net.URLEncoder.encode(artist, "UTF-8");
        }
        catch (java.io.UnsupportedEncodingException e) {
            return;
        }

        Bitmap cached = mCache.getBitmap(artist);

        if (cached != null) {
            callback.onImageFound(cached);
        }

        JsonObjectRequest req = new JsonObjectRequest(
            Method.GET,
            ECHO_NEST_URL + artist,
            null,
            new VolleyListener(artist, callback),
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("fiction", "error");
                }
            });
        mRequestQueue.add(req);
    }

    public interface CacheCallback {
        void onImageFound(Bitmap bitmap);
    }

    class VolleyListener implements Response.Listener<JSONObject> {
        // TODO use weak reference
        CacheCallback mCallback;
        String mArtist;

        public VolleyListener(String artist, CacheCallback callback) {
            mArtist = artist;
            mCallback = callback;
        }

        @Override
        public void onResponse(JSONObject response) {
            try {
                response = response.getJSONObject("response");
                JSONArray images = response.getJSONArray("images");

                if (images.length() > 0) {
                    JSONObject image = images.getJSONObject(0);
                    String url = image.getString("url");
                    mImageLoader.get(url, new VolleyImageListener(mArtist, mCallback));
                }
            }
            catch (JSONException e) {
                Log.d("fiction", "response error");
            }
        }
    }

    class VolleyImageListener implements ImageLoader.ImageListener {
        CacheCallback mCallback;
        String mArtist;

        public VolleyImageListener(String artist, CacheCallback callback) {
            mArtist = artist;
            mCallback = callback;
        }

        @Override
        public void onErrorResponse(VolleyError e) {
            // image load error
        }

        @Override
        public void onResponse(ImageLoader.ImageContainer response,
                               boolean isImmediate) {
            Bitmap b = response.getBitmap();

            if (b != null) {
                mCache.putBitmap(mArtist, b);
            }
            mCallback.onImageFound(response.getBitmap());
        }
    }

    class FakeCache implements ImageLoader.ImageCache {
        public Bitmap getBitmap(String url) {
            return null;
        }

        public void putBitmap(String url, Bitmap bitmap) {
        }
    }
}
