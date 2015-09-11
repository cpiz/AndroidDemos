package com.cpiz.android.playground.TakePicture;

import android.graphics.Bitmap;

/**
 * Created by caijw on 2015/9/11.
 */
public enum CameraModel {
    Instance;

    private Bitmap cacheBitmap;

    public Bitmap getCacheBitmap() {
        return cacheBitmap;
    }

    public void setCacheBitmap(Bitmap cacheBitmap) {
        this.cacheBitmap = cacheBitmap;
    }

    public void clearCacheBitmap() {
        if (cacheBitmap != null) {
            cacheBitmap.recycle();
            cacheBitmap = null;
        }
    }
}
