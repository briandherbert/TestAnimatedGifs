package com.meetme.animatedgifs;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by bherbert on 4/29/16.
 */
public class GifAdapter extends RecyclerView.Adapter<GifAdapter.GifHolder> {
    static final String TAG = GifAdapter.class.getSimpleName();

    ArrayList<String> mUrls = new ArrayList<>();
    AnimatedGifManager mGifManager;

    public GifAdapter() {}

    @Override
    public GifHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new GifHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.gif_item, parent, false));
    }

    @Override
    public void onBindViewHolder(GifHolder holder, int position) {
        holder.container.removeAllViews();
        mGifManager.addAnimation(holder.container, mUrls.get(position));
    }

    @Override
    public int getItemCount() {
        return mUrls.size();
    }

    public static class GifHolder extends RecyclerView.ViewHolder {
        ViewGroup container;

        public GifHolder(View view) {
            super(view);
            container = new FrameLayout(view.getContext());
            ((ViewGroup) view).addView(container);
        }
    }

    public void add(ArrayList<String> urls) {
        Log.v("Adapter", "adding items " + urls.size());
        notifyItemRangeInserted(mUrls.size(), mUrls.size() + urls.size());
        mUrls.addAll(urls);
        notifyDataSetChanged();
    }

    public void setGifManager(AnimatedGifManager gifManager) {
        mGifManager = gifManager;
    }

    public void clear() {
        mUrls.clear();
        notifyDataSetChanged();
    }
}
