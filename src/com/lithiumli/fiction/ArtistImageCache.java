package com.lithiumli.fiction;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import uk.co.senab.bitmapcache.BitmapLruCache;
import uk.co.senab.bitmapcache.CacheableBitmapDrawable;

import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.Request.Method;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.HttpStack;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NoCache;

public class ArtistImageCache {
    static final String ECHO_NEST_URL = "http://developer.echonest.com/api/v4/artist/images?api_key=ETDSSZR6RAMYOU4SI&results=1&name=";
    final RequestQueue mRequestQueue;
    final ImageLoader mImageLoader;
    final BitmapLruCache mCache;
    final Resources mResources;
    AsyncTask mTask = null;

    public ArtistImageCache(Context context) {
        mRequestQueue = newRequestQueue(context);
        mImageLoader = new ImageLoader(mRequestQueue, new FakeCache());
        mResources = context.getResources();

        File cacheLocation;
        cacheLocation = new File(context.getFilesDir() + "/fiction-artists");
        cacheLocation.mkdirs();

        BitmapLruCache.Builder builder = new BitmapLruCache.Builder(context);
        builder.setMemoryCacheEnabled(true).setMemoryCacheMaxSizeUsingHeapSize();
        builder.setDiskCacheEnabled(true).setDiskCacheLocation(cacheLocation);
        mCache = builder.build();
    }

    public static String escapeArtist(String artist) {
        try {
            return java.net.URLEncoder.encode(artist, "UTF-8");
        }
        catch (java.io.UnsupportedEncodingException e) {
        }
        return artist;
    }

    public static String getCacheKey(String artist) {
        // https://gist.github.com/avilches/750151
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(artist.getBytes());

            StringBuffer result = new StringBuffer();
            for (byte byt : md.digest()) {
                result.append(Integer.toString((byt & 0xff) + 0x100, 16).substring(1));
            }
            return result.toString();
        }
        catch (NoSuchAlgorithmException e) {
        }
        return "";
    }

    public BitmapLruCache getCache() {
        return mCache;
    }

    public void cancelAll() {
        mRequestQueue.cancelAll(new RequestQueue.RequestFilter() {
                @Override
                public boolean apply(Request<?> request) {
                    return true;
                }
            });
        if (mTask != null) {
            mTask.cancel(true);
        }
    }

    public void getImage(String artist, CacheCallback callback) {
        if (mTask != null) {
            mTask.cancel(true);
        }
        mTask = new CheckCacheTask()
            .execute(new CheckCacheParams(escapeArtist(artist), callback));
    }

    public void loadImage(String artist, CacheCallback callback) {
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

    public void storeImage(String key, Bitmap b) {
        new StoreCacheTask().execute(new StoreCacheParams(key, b));
    }

    // disables cache in Volley
    public static RequestQueue newRequestQueue(Context context) {
        String userAgent = "volley/0";
        try {
            String packageName = context.getPackageName();

            PackageInfo info = context.getPackageManager().getPackageInfo(packageName, 0);
            userAgent = packageName + "/" + info.versionCode;
        } catch (NameNotFoundException e) {
        }

        HttpStack stack = new HurlStack();
        Network network = new BasicNetwork(stack);

        RequestQueue queue = new RequestQueue(new NoCache(), network);
        queue.start();

        return queue;
    }

    public interface CacheCallback {
        void onImageFound(BitmapDrawable bitmap);
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
                storeImage(getCacheKey(mArtist), b);
            }

            mCallback.onImageFound(new BitmapDrawable(mResources, response.getBitmap()));
        }
    }

    class FakeCache implements ImageLoader.ImageCache {
        public Bitmap getBitmap(String url) {
            return null;
        }

        public void putBitmap(String url, Bitmap bitmap) {
        }
    }

    class CheckCacheParams {
        String artist;
        CacheCallback callback;

        public CheckCacheParams(String _artist, CacheCallback _callback) {
            artist = _artist;
            callback = _callback;
        }
    }

    class StoreCacheParams {
        String key;
        Bitmap bitmap;

        public StoreCacheParams(String _key, Bitmap _bitmap) {
            key = _key;
            bitmap = _bitmap;
        }
    }

    class CheckCacheTask extends AsyncTask<CheckCacheParams, Void, CacheableBitmapDrawable> {
        String artist;
        CacheCallback callback;

        @Override
        protected CacheableBitmapDrawable doInBackground(CheckCacheParams... params) {
            artist = params[0].artist;
            callback = params[0].callback;

            String key = ArtistImageCache.getCacheKey(artist);
            return getCache().get(key);
        }

        @Override
        protected void onPostExecute(CacheableBitmapDrawable result) {
            if (result != null) {
                callback.onImageFound(result);
            }
            else {
                ArtistImageCache.this.loadImage(artist, callback);
            }
        }
    }

    class StoreCacheTask extends AsyncTask<StoreCacheParams, Void, Void> {
        @Override
        protected Void doInBackground(StoreCacheParams... params) {
            String key = params[0].key;
            Bitmap b = params[0].bitmap;

            getCache().put(key, b);
            return null;
        }
    }
}
