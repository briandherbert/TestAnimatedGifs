package com.meetme.animatedgifs;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.koushikdutta.ion.Ion;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class MainActivity extends AppCompatActivity implements AnimatedGifManager.Callbacks {
    public static final String TAG = "Gif test";

    TextView lblName;
    EditText txtSearch;
    RecyclerView mRecyclerView;
    GridLayoutManager mLayoutManager;

    GifAdapter mGifAdapter = new GifAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Fresco.initialize(this);

        setContentView(R.layout.activity_main);

        lblName = (TextView) findViewById(R.id.lbl_header);
        txtSearch = (EditText) findViewById(R.id.txt_search);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_photos);

        //mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mLayoutManager = new GridLayoutManager(this, 2);
        mLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return 1;
            }
        });

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mGifAdapter);
    }

    static long sStartTime;

    AnimatedGifManager mCurrentGifManager = null;

    public void onClick(View view) {
        mGifAdapter.clear();

        sStartTime = System.currentTimeMillis();

        switch (view.getId()) {
            case R.id.btn_glide:
                mCurrentGifManager = new GlideGifManager(this);
                break;

            case R.id.btn_webview:
                mCurrentGifManager = new WebviewGifManager(this);
                break;

            case R.id.btn_ion:
                mCurrentGifManager = new IonGifManager(this);
                break;

            case R.id.btn_fresco:
                mCurrentGifManager = new FrescoGifManager(this);
                break;
        }

        if (mCurrentGifManager != null) {
            lblName.setText(mCurrentGifManager.getName() + " - loading...");
            //mCurrentGifView.clearCache(this);

            mGifAdapter.setGifManager(mCurrentGifManager);

            final String search = txtSearch.getText().toString();
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    final ArrayList<String> urls = getUrls(search);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onGotUrls(urls);
                        }
                    });
                }
            });
        }
    }

    public void onGotUrls(ArrayList<String> urls) {
        if (mCurrentGifManager == null || urls == null || urls.isEmpty()) return;

        mGifAdapter.add(urls);
    }

    @Override
    public void onImageLoaded(AnimatedGifManager animatedGifManager) {
        lblName.setText(animatedGifManager.getName() + " - " + (System.currentTimeMillis() - sStartTime) + "ms");
    }

    static class GlideGifManager extends AnimatedGifManager {
        public GlideGifManager(Callbacks callback) {
            super(callback);
        }

        @Override
        public String getName() {
            return "Glide";
        }

        @Override
        public void addAnimation(ViewGroup parent, String url) {
            ImageView img = new ImageView(parent.getContext());

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            img.setLayoutParams(lp);

            parent.addView(img);

            Glide.with(parent.getContext())
                    .load(url)
                    .error(new ColorDrawable(0xFF0000))
                    .listener(new RequestListener<String, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                            Log.e(getName(), "Glide resource failed", e);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            callback.onImageLoaded(GlideGifManager.this);
                            return false;
                        }
                    })
                    .into(img);
        }

        @Override
        public void clearCache(final Activity activity) {
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    Glide.get(activity).clearDiskCache();
                }
            });
        }
    }

    static class WebviewGifManager extends AnimatedGifManager {
        String mime = "text/html";
        String encoding = "utf-8";

        public WebviewGifManager(Callbacks callback) {
            super(callback);
        }

        @Override
        public String getName() {
            return "Webview";
        }

        public static String getHtml(String url) {
            return "<html>\n" +
                    "<body bgcolor=\"white\">\n" +
                    "    <table width=\"100%\" height=\"100%\">\n" +
                    "        <tr>\n" +
                    "            <td align=\"center\" valign=\"center\">\n" +
                    "                <img src=\" " + url + " \">\n" +
                    "            </td>\n" +
                    "        </tr>\n" +
                    "    </table>\n" +
                    "</body>";
        }

        @Override
        public void addAnimation(ViewGroup parent, String url) {
            WebView webView = new WebView(parent.getContext());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            webView.setLayoutParams(lp);
            webView.loadDataWithBaseURL(null, getHtml(url), mime, encoding, null);
            parent.addView(webView);
            callback.onImageLoaded(this);
        }

        @Override
        void clearCache(Activity activity) {
            // NOOP?
        }
    }

    static class IonGifManager extends AnimatedGifManager {
        public IonGifManager(Callbacks callback) {
            super(callback);
        }

        @Override
        void addAnimation(ViewGroup parent, String url) {
            ImageView img = new ImageView(parent.getContext());

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            img.setLayoutParams(lp);

            parent.addView(img);
            Ion.with(parent.getContext()).load(url).intoImageView(img);
            callback.onImageLoaded(this);
        }

        @Override
        public void clearCache(final Activity activity) {
            Ion.getDefault(activity).getCache().clear();
        }
    }

    static class FrescoGifManager extends AnimatedGifManager {
        public FrescoGifManager(Callbacks callback) {
            super(callback);
        }

        @Override
        void addAnimation(ViewGroup parent, String url) {
            SimpleDraweeView img = new SimpleDraweeView(parent.getContext());

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            img.setLayoutParams(lp);

            parent.addView(img);

            DraweeController controller = Fresco.newDraweeControllerBuilder()
                    .setUri(Uri.parse(url))
                    .setAutoPlayAnimations(true)
                    .build();

            img.setController(controller);
            callback.onImageLoaded(this);
        }

        @Override
        void clearCache(Activity activity) {
            // TODO
        }
    }

    /// Network stuff to get urls ///
    public static String SEARCH_URL_PREFIX = "http://api.giphy.com/v1/gifs/search?api_key=dc6zaTOxFJmzC&limit=20&q=";

    final OkHttpClient okHttpClient = new OkHttpClient();

    /**
     * Get image URLs from Giphy.com. Standard stuff, JSON response and all that. No need to dig
     * into this method, just the usual, boring old parsing stuff
     */
    public ArrayList<String> getUrls(String search) {

        Request request = new Request.Builder()
                .url(SEARCH_URL_PREFIX + search)
                .build();

        String json = null;

        try {
            okhttp3.Response response = okHttpClient.newCall(request).execute();
            json = response.body().string();
        } catch (IOException e) {
            return null;
        }

        ArrayList<String> urls = new ArrayList<>();

        int i = json.indexOf(startPhrase, 0);

        // oh hey, um, i didn't see you there... no, no i'm not parsing JSON, haha! what kind of
        // unenlightened moron would manually look through JSON for urls? *runs away*
        while (i > 0) {
            int end = json.indexOf('"', i + startPhrase.length());
            String url = json.substring(i + startPhrase.length(), end).replace("\\", "");
            urls.add(url);
            i = json.indexOf(startPhrase, end);

            Log.v(TAG, "found url " + url);
        }

        return urls;
    }

    // oh, how'd this old thing get down here? Probably just some unused, debug code- NO NO DON'T,
    // don't delete it. It, um, it might have sentimental value. No harm in keeping a few extra
    // bytes around
    final static String startPhrase = "\"fixed_height_small\":{\"url\":\"";
}
