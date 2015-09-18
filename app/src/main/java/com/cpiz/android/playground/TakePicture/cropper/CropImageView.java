/*
 * Copyright 2013, Edmodo, Inc. 
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this work except in compliance with the License.
 * You may obtain a copy of the License in the LICENSE file, or at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" 
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language 
 * governing permissions and limitations under the License. 
 */

package com.cpiz.android.playground.TakePicture.cropper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.ExifInterface;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.cpiz.android.playground.TakePicture.cropper.cropwindow.CropOverlayView;
import com.cpiz.android.playground.TakePicture.cropper.cropwindow.edge.Edge;
import com.cpiz.android.playground.TakePicture.cropper.util.ImageViewUtil;
import com.cpiz.android.utils.DimensionUtil;

import java.io.IOException;


/**
 * Custom view that provides cropping capabilities to an image.
 */
public class CropImageView extends FrameLayout {
    private static final String TAG = "CropImageView";

    // Private Constants ///////////////////////////////////////////////////////

    private static final Rect EMPTY_RECT = new Rect();

    // Member Variables ////////////////////////////////////////////////////////

    // Sets the default image guidelines to show when resizing
    public static final int DEFAULT_GUIDELINES = 1;
    public static final boolean DEFAULT_FIXED_ASPECT_RATIO = false;
    public static final int DEFAULT_ASPECT_RATIO_X = 1;
    public static final int DEFAULT_ASPECT_RATIO_Y = 1;
    public static final int DEFAULT_SCALE_TYPE = 1;

    private static final int DEFAULT_IMAGE_RESOURCE = 0;
    private static final ImageView.ScaleType[] VALID_SCALE_TYPES = new ImageView.ScaleType[]{ImageView.ScaleType.CENTER_INSIDE, ImageView.ScaleType.FIT_CENTER};

    private static final String DEGREES_ROTATED = "DEGREES_ROTATED";

    private ImageView mImageView;
    private CropOverlayView mCropOverlayView;

    private Bitmap mBitmap;
    private int mDegreesRotated = 0;

    private int mLayoutWidth;
    private int mLayoutHeight;

    // Instance variables for customizable attributes
    private int mGuidelines = DEFAULT_GUIDELINES;
    private boolean mFixAspectRatio = DEFAULT_FIXED_ASPECT_RATIO;
    private int mAspectRatioX = DEFAULT_ASPECT_RATIO_X;
    private int mAspectRatioY = DEFAULT_ASPECT_RATIO_Y;
    private int mImageResource = DEFAULT_IMAGE_RESOURCE;
    private ImageView.ScaleType mScaleType = VALID_SCALE_TYPES[DEFAULT_SCALE_TYPE];

    private int mPreferredWidth = 9999;
    private int mPreferredHeight = 9999;

    // Constructors ////////////////////////////////////////////////////////////

    public CropImageView(Context context) {
        super(context);
        init(context);
    }

    public CropImageView(Context context, AttributeSet attrs) {
        super(context, attrs);

//        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CropImageView, 0, 0);
//        try {
//            mGuidelines = ta.getInteger(R.styleable.CropImageView_guidelines, DEFAULT_GUIDELINES);
//            mFixAspectRatio = ta.getBoolean(R.styleable.CropImageView_fixAspectRatio,
//                    DEFAULT_FIXED_ASPECT_RATIO);
//            mAspectRatioX = ta.getInteger(R.styleable.CropImageView_aspectRatioX, DEFAULT_ASPECT_RATIO_X);
//            mAspectRatioY = ta.getInteger(R.styleable.CropImageView_aspectRatioY, DEFAULT_ASPECT_RATIO_Y);
//            mImageResource = ta.getResourceId(R.styleable.CropImageView_imageResource, DEFAULT_IMAGE_RESOURCE);
//            mScaleType = VALID_SCALE_TYPES[ta.getInt(R.styleable.CropImageView_scaleType, DEFAULT_SCALE_TYPE)];
//        } finally {
//            ta.recycle();
//        }

        init(context);
    }

    // View Methods ////////////////////////////////////////////////////////////

    @Override
    public Parcelable onSaveInstanceState() {

        final Bundle bundle = new Bundle();

        bundle.putParcelable("instanceState", super.onSaveInstanceState());
        bundle.putInt(DEGREES_ROTATED, mDegreesRotated);

        return bundle;

    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {

        if (state instanceof Bundle) {

            final Bundle bundle = (Bundle) state;

            if (mBitmap != null) {
                // Fixes the rotation of the image when orientation changes.
                mDegreesRotated = bundle.getInt(DEGREES_ROTATED);
                int tempDegrees = mDegreesRotated;
                rotateImage(mDegreesRotated);
                mDegreesRotated = tempDegrees;
            }

            super.onRestoreInstanceState(bundle.getParcelable("instanceState"));

        } else {
            super.onRestoreInstanceState(state);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {

        if (mBitmap != null) {
            final Rect bitmapRect = ImageViewUtil.getBitmapRect(mBitmap, this, mScaleType);
            mCropOverlayView.setBitmapRect(bitmapRect);
        } else {
            mCropOverlayView.setBitmapRect(EMPTY_RECT);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        if (mBitmap != null) {

            super.onMeasure(widthMeasureSpec, heightMeasureSpec);

            // Bypasses a baffling bug when used within a ScrollView, where
            // heightSize is set to 0.
            if (heightSize == 0)
                heightSize = mBitmap.getHeight();

            int desiredWidth;
            int desiredHeight;

            double viewToBitmapWidthRatio = Double.POSITIVE_INFINITY;
            double viewToBitmapHeightRatio = Double.POSITIVE_INFINITY;

            // Checks if either width or height needs to be fixed
            if (widthSize < mBitmap.getWidth()) {
                viewToBitmapWidthRatio = (double) widthSize / (double) mBitmap.getWidth();
            }
            if (heightSize < mBitmap.getHeight()) {
                viewToBitmapHeightRatio = (double) heightSize / (double) mBitmap.getHeight();
            }

            // If either needs to be fixed, choose smallest ratio and calculate
            // from there
            if (viewToBitmapWidthRatio != Double.POSITIVE_INFINITY || viewToBitmapHeightRatio != Double.POSITIVE_INFINITY) {
                if (viewToBitmapWidthRatio <= viewToBitmapHeightRatio) {
                    desiredWidth = widthSize;
                    desiredHeight = (int) (mBitmap.getHeight() * viewToBitmapWidthRatio);
                } else {
                    desiredHeight = heightSize;
                    desiredWidth = (int) (mBitmap.getWidth() * viewToBitmapHeightRatio);
                }
            }

            // Otherwise, the picture is within frame layout bounds. Desired
            // width is
            // simply picture size
            else {
                desiredWidth = mBitmap.getWidth();
                desiredHeight = mBitmap.getHeight();
            }

            int width = getOnMeasureSpec(widthMode, widthSize, desiredWidth);
            int height = getOnMeasureSpec(heightMode, heightSize, desiredHeight);

            mLayoutWidth = width;
            mLayoutHeight = height;

            final Rect bitmapRect = ImageViewUtil.getBitmapRect(mBitmap, this, mScaleType);
            mCropOverlayView.setBitmapRect(bitmapRect);

            // MUST CALL THIS
            setMeasuredDimension(mLayoutWidth, mLayoutHeight);

        } else {

            mCropOverlayView.setBitmapRect(EMPTY_RECT);
            setMeasuredDimension(widthSize, heightSize);
        }
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        super.onLayout(changed, l, t, r, b);

        if (mLayoutWidth > 0 && mLayoutHeight > 0) {
            // Gets original parameters, and creates the new parameters
            final ViewGroup.LayoutParams origparams = this.getLayoutParams();
            origparams.width = mLayoutWidth;
            origparams.height = mLayoutHeight;
            setLayoutParams(origparams);
        }
    }

    // Public Methods //////////////////////////////////////////////////////////

    /**
     * Returns the integer of the imageResource
     *
     * @return int the image resource id
     */
    public int getImageResource() {
        return mImageResource;
    }

    /**
     * Sets a Bitmap as the content of the CropImageView.
     *
     * @param bitmap the Bitmap to set
     */
    public boolean setImageBitmap(Bitmap bitmap) {

        mBitmap = bitmap;   // bitmap to crop
        Bitmap bitmapToDisplay = scaleImageToLayout(mBitmap); // bitmap to display

        Log.d(TAG, String.format("set source bitmap width=%d, height=%d", bitmap.getWidth(), bitmap.getHeight()));
        Log.d(TAG, String.format("scale bitmap to display width=%d, height=%d", bitmapToDisplay.getWidth(), bitmapToDisplay.getHeight()));

        mImageView.setImageBitmap(bitmapToDisplay);

        if (mCropOverlayView != null) {
            mCropOverlayView.resetCropOverlayView();
        }

        return true;
    }

    /**
     * Sets a Bitmap and initializes the image rotation according to the EXIT data.
     * <p>
     * The EXIF can be retrieved by doing the following:
     * <code>ExifInterface exif = new ExifInterface(path);</code>
     *
     * @param bitmap the original bitmap to set; if null, this
     * @param exif   the EXIF information about this bitmap; may be null
     */
    public boolean setImageBitmap(Bitmap bitmap, ExifInterface exif) {

        if (bitmap == null) {
            return false;
        }

        if (exif == null) {
            return setImageBitmap(bitmap);
        }

        final Matrix matrix = new Matrix();
        final int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
        int rotate = -1;

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_270:
                rotate = 270;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                rotate = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                rotate = 90;
                break;
        }

        if (rotate == -1) {
            return setImageBitmap(bitmap);
        } else {
            matrix.postRotate(rotate);
            final Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap,
                    0,
                    0,
                    bitmap.getWidth(),
                    bitmap.getHeight(),
                    matrix,
                    true);
            boolean ret = setImageBitmap(rotatedBitmap);
            bitmap.recycle();
            return ret;
        }
    }

    /**
     * 设置照片路径
     *
     * @param path
     */
    public boolean setImageFile(final String path) {
        if (path == null) {
            return false;
        }

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap bitmap = BitmapFactory.decodeFile(path, bmOptions);
        if (bitmap == null) {
            return false;
        }

        ExifInterface exif = null;

        try {
            exif = new ExifInterface(path);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return setImageBitmap(bitmap, exif);
    }

    /**
     * Sets a Drawable as the content of the CropImageView.
     *
     * @param resId the drawable resource ID to set
     */
    public void setImageResource(int resId) {
        if (resId != 0) {
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), resId);
            setImageBitmap(bitmap);
        }
    }

    /**
     * 设置导出图像的最佳尺寸，避免产生太大的图片
     *
     * @param preferredWidth  最佳宽度
     * @param preferredHeight 最佳高度
     */
    public void setPreferredSize(int preferredWidth, int preferredHeight) {
        mPreferredWidth = preferredWidth;
        mPreferredHeight = preferredHeight;
    }

    /**
     * Gets the cropped image based on the current crop window.
     *
     * @return a new Bitmap representing the cropped image
     */
    public Bitmap getCroppedImage() {
        if (!isCropEnabled()) {
            return mBitmap;
        }

        final Rect displayedImageRect = ImageViewUtil.getBitmapRect(mBitmap, mImageView, mScaleType);

        // Get the scale factor between the actual Bitmap dimensions and the
        // displayed dimensions for width.
        final float actualImageWidth = mBitmap.getWidth();
        final float displayedImageWidth = displayedImageRect.width();
        final float scaleFactorWidth = actualImageWidth / displayedImageWidth;

        // Get the scale factor between the actual Bitmap dimensions and the
        // displayed dimensions for height.
        final float actualImageHeight = mBitmap.getHeight();
        final float displayedImageHeight = displayedImageRect.height();
        final float scaleFactorHeight = actualImageHeight / displayedImageHeight;

        // Get crop window position relative to the displayed image.
        final float cropWindowX = Edge.LEFT.getCoordinate() > displayedImageRect.left ? Edge.LEFT.getCoordinate() - displayedImageRect.left : 0;
        final float cropWindowY = Edge.TOP.getCoordinate() > displayedImageRect.top ? Edge.TOP.getCoordinate() - displayedImageRect.top : 0;
        final float cropWindowWidth = Edge.getWidth();
        final float cropWindowHeight = Edge.getHeight();

        // Scale the crop window position to the actual size of the Bitmap.
        final float actualCropX = cropWindowX * scaleFactorWidth;
        final float actualCropY = cropWindowY * scaleFactorHeight;
        final float actualCropWidth = cropWindowWidth * scaleFactorWidth;
        final float actualCropHeight = cropWindowHeight * scaleFactorHeight;

        // Crop the subset from the original Bitmap.
        Matrix matrix = new Matrix();
        int scale = calculateInSampleSize((int) actualCropWidth, (int) actualCropHeight, mPreferredWidth, mPreferredHeight);
        matrix.postScale(1 / (float) scale, 1 / (float) scale);
        final Bitmap croppedBitmap = Bitmap.createBitmap(mBitmap,
                (int) actualCropX,
                (int) actualCropY,
                (int) actualCropWidth,
                (int) actualCropHeight,
                matrix,
                false);

        return croppedBitmap;
    }

    /**
     * Gets the crop window's position relative to the source Bitmap (not the image
     * displayed in the CropImageView).
     *
     * @return a RectF instance containing cropped area boundaries of the source Bitmap
     */
    public RectF getActualCropRect() {
        if (!isCropEnabled()) {
            return new RectF(0, 0, mBitmap.getWidth(), mBitmap.getHeight());
        }

        final Rect displayedImageRect = ImageViewUtil.getBitmapRect(mBitmap, mImageView, mScaleType);

        // Get the scale factor between the actual Bitmap dimensions and the
        // displayed dimensions for width.
        final float actualImageWidth = mBitmap.getWidth();
        final float displayedImageWidth = displayedImageRect.width();
        final float scaleFactorWidth = actualImageWidth / displayedImageWidth;

        // Get the scale factor between the actual Bitmap dimensions and the
        // displayed dimensions for height.
        final float actualImageHeight = mBitmap.getHeight();
        final float displayedImageHeight = displayedImageRect.height();
        final float scaleFactorHeight = actualImageHeight / displayedImageHeight;

        // Get crop window position relative to the displayed image.
        final float displayedCropLeft = Edge.LEFT.getCoordinate() - displayedImageRect.left;
        final float displayedCropTop = Edge.TOP.getCoordinate() - displayedImageRect.top;
        final float displayedCropWidth = Edge.getWidth();
        final float displayedCropHeight = Edge.getHeight();

        // Scale the crop window position to the actual size of the Bitmap.
        float actualCropLeft = displayedCropLeft * scaleFactorWidth;
        float actualCropTop = displayedCropTop * scaleFactorHeight;
        float actualCropRight = actualCropLeft + displayedCropWidth * scaleFactorWidth;
        float actualCropBottom = actualCropTop + displayedCropHeight * scaleFactorHeight;

        // Correct for floating point errors. Crop rect boundaries should not
        // exceed the source Bitmap bounds.
        actualCropLeft = Math.max(0f, actualCropLeft);
        actualCropTop = Math.max(0f, actualCropTop);
        actualCropRight = Math.min(mBitmap.getWidth(), actualCropRight);
        actualCropBottom = Math.min(mBitmap.getHeight(), actualCropBottom);

        final RectF actualCropRect = new RectF(actualCropLeft,
                actualCropTop,
                actualCropRight,
                actualCropBottom);

        return actualCropRect;
    }

    public static int calculateInSampleSize(final int rawWidth, final int rawHeight, final int reqWidth, final int reqHeight) {
        // Raw height and width of image
        int inSampleSize = 1;
        if (rawHeight > reqHeight || rawWidth > reqWidth) {

            final int halfHeight = rawHeight / 2;
            final int halfWidth = rawWidth / 2;

            while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }

            long totalPixels = rawWidth / inSampleSize * rawHeight / inSampleSize;

            final long totalReqPixelsCap = reqWidth * reqHeight * 2;

            while (totalPixels > totalReqPixelsCap) {
                inSampleSize *= 2;
                totalPixels /= 2;
            }
        }

        return inSampleSize;
    }

    /**
     * Sets whether the aspect ratio is fixed or not; true fixes the aspect ratio, while
     * false allows it to be changed.
     *
     * @param fixAspectRatio Boolean that signals whether the aspect ratio should be
     *                       maintained.
     */
    public void setFixedAspectRatio(boolean fixAspectRatio) {
        mFixAspectRatio = fixAspectRatio;
        mCropOverlayView.setFixedAspectRatio(fixAspectRatio);
    }

    /**
     * Sets the guidelines for the CropOverlayView to be either on, off, or to show when
     * resizing the application.
     *
     * @param guidelines Integer that signals whether the guidelines should be on, off, or
     *                   only showing when resizing.
     */
    public void setGuidelines(int guidelines) {
        mGuidelines = guidelines;
        mCropOverlayView.setGuidelines(guidelines);
    }

    /**
     * Sets the both the X and Y values of the aspectRatio.
     *
     * @param aspectRatioX int that specifies the new X value of the aspect ratio
     * @param aspectRatioX int that specifies the new Y value of the aspect ratio
     */
    public void setAspectRatio(int aspectRatioX, int aspectRatioY) {
        mAspectRatioX = aspectRatioX;
        mCropOverlayView.setAspectRatioX(mAspectRatioX);

        mAspectRatioY = aspectRatioY;
        mCropOverlayView.setAspectRatioY(mAspectRatioY);
    }

    /**
     * 设置是否启用裁剪功能
     * 功能未启用时调用裁剪接口将返回原图
     *
     * @param enabled 是否启用裁剪功能
     */
    public void setCropEnabled(boolean enabled) {
        mCropOverlayView.setVisibility(enabled ? VISIBLE : INVISIBLE);
    }

    /**
     * @return 是否启用了裁剪功能
     */
    public boolean isCropEnabled() {
        return mCropOverlayView.isShown();
    }

    /**
     * Rotates image by the specified number of degrees clockwise. Cycles from 0 to 360
     * degrees.
     *
     * @param degrees Integer specifying the number of degrees to rotate.
     */
    public void rotateImage(int degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        mBitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), matrix, true);
        setImageBitmap(mBitmap);

        mDegreesRotated += degrees;
        mDegreesRotated = mDegreesRotated % 360;
    }

    private void setScaleType(ImageView.ScaleType scaleType) {
        mScaleType = scaleType;
        if (mImageView != null) mImageView.setScaleType(mScaleType);
    }

    // Private Methods /////////////////////////////////////////////////////////

    private void init(Context context) {
        initView(context, this);
        mCropOverlayView.setInitialAttributeValues(mGuidelines, mFixAspectRatio, mAspectRatioX, mAspectRatioY);
        setImageResource(mImageResource);
    }

    /**
     * Determines the specs for the onMeasure function. Calculates the width or height
     * depending on the mode.
     *
     * @param measureSpecMode The mode of the measured width or height.
     * @param measureSpecSize The size of the measured width or height.
     * @param desiredSize     The desired size of the measured width or height.
     * @return The final size of the width or height.
     */
    private static int getOnMeasureSpec(int measureSpecMode, int measureSpecSize, int desiredSize) {

        // Measure Width
        int spec;
        if (measureSpecMode == MeasureSpec.EXACTLY) {
            // Must be this size
            spec = measureSpecSize;
        } else if (measureSpecMode == MeasureSpec.AT_MOST) {
            // Can't be bigger than...; match_parent value
            spec = Math.min(desiredSize, measureSpecSize);
        } else {
            // Be whatever you want; wrap_content
            spec = desiredSize;
        }

        return spec;
    }

    private void initView(Context context, ViewGroup root) {
        if (root == null || root.getChildCount() > 0) {
            return;
        }

        mImageView = new ImageView(context);
        mImageView.setScaleType(mScaleType);
        mImageView.setAdjustViewBounds(true);
        addView(mImageView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        mCropOverlayView = new CropOverlayView(context);
        addView(mCropOverlayView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    private Bitmap scaleImageToLayout(Bitmap srcBitmap) {
        int layoutWidth = mLayoutWidth;
        int layoutHeight = mLayoutHeight;
        if (layoutWidth == 0 || layoutHeight == 0) {
            Point screenSize = new Point();
            DimensionUtil.getScreenSize(getContext(), screenSize);
            layoutWidth = screenSize.x;
            layoutHeight = screenSize.y;
        }

        int sampleSize = calculateInSampleSize(srcBitmap.getWidth(), srcBitmap.getHeight(), layoutWidth, layoutHeight);
        if (sampleSize == 1) {
            return srcBitmap;
        } else {
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(srcBitmap, srcBitmap.getWidth() / sampleSize, srcBitmap.getHeight() / sampleSize, false);
            return scaledBitmap;
        }
    }
}
