package com.meetme.animatedgifs;

import android.app.Activity;
import android.view.ViewGroup;

/**
 * Created by bherbert on 4/25/16.
 */
public abstract class AnimatedGifManager {
    Callbacks callback;

    public AnimatedGifManager(Callbacks callback) {
        this.callback = callback;
    }

    String getName() {
        return getClass().getSimpleName();
    }

    abstract void addAnimation(ViewGroup parent, String url);

    abstract void clearCache(Activity activity);

    public interface Callbacks {
        public void onImageLoaded(AnimatedGifManager animatedGifManager);
    }
}
