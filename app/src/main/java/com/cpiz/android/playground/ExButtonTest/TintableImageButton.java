package com.cpiz.android.playground.ExButtonTest;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ImageButton;

import com.cpiz.android.playground.R;

/**
 * 支持染色(Tint)的ImageButton
 * <p>
 * 在xml指定 imgTint 和 bgTint 属性，让图标与按钮背景在不同状态下有各自的染色，能避免为按钮不同的状态指定多份UI素材，减小工作量和安装包体积。
 * 使用纯色背景结合bgTint时，在不同Android版本可能有不一的表现，需要注意验证
 * <p>
 * Created by caijw on 2017/8/31.
 */
@SuppressWarnings("ALL")
@SuppressLint("AppCompatCustomView")
public class TintableImageButton extends ImageButton {

    private static int DEFAULT_TINT_COLOR = Color.WHITE;
    private ColorStateList mImgTintList = null;
    private ColorStateList mBgTintList = null;
    private PorterDuff.Mode mImgTintMode = PorterDuff.Mode.MULTIPLY;
    private PorterDuff.Mode mBgTintMode = PorterDuff.Mode.MULTIPLY;

    public TintableImageButton(Context context) {
        super(context, null);
    }

    public TintableImageButton(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.imageButtonStyle);
    }

    public TintableImageButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TintableImageButton, defStyleAttr, 0);
            mImgTintList = a.getColorStateList(R.styleable.TintableImageButton_imgTint);
            mBgTintList = a.getColorStateList(R.styleable.TintableImageButton_bgTint);
            a.recycle();
        }
    }

    public void setImgTintList(ColorStateList tint) {
        this.mImgTintList = tint;
        applyImgTintColor();
    }

    public ColorStateList getImgTintList() {
        return mImgTintList;
    }

    public void setImgTintMode(PorterDuff.Mode mode) {
        this.mImgTintMode = mode;
        applyImgTintColor();
    }

    public PorterDuff.Mode getImgTintMode() {
        return mImgTintMode;
    }

    public void setBgTintList(ColorStateList tint) {
        this.mBgTintList = tint;
        applyBackgroundTintColor();
    }

    public ColorStateList getBgTintList() {
        return mBgTintList;
    }

    public void setBgTintMode(PorterDuff.Mode mode) {
        this.mBgTintMode = mode;
        applyBackgroundTintColor();
    }

    public PorterDuff.Mode getBgTintMode() {
        return mBgTintMode;
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        applyImgTintColor();
        applyBackgroundTintColor();
    }

    private void applyImgTintColor() {
        applyTint(getDrawable(), getDrawableState(), mImgTintList, mImgTintMode);
    }

    private void applyBackgroundTintColor() {
        applyTint(getBackground(), getDrawableState(), mBgTintList, mBgTintMode);
    }

    private static void applyTint(Drawable drawable, int[] drawableState, ColorStateList tintList, PorterDuff.Mode tintMode) {
        if (drawable == null) {
            return;
        }

        if (tintList == null) {
            drawable.clearColorFilter();
        } else {
            int color = tintList.getColorForState(drawableState, DEFAULT_TINT_COLOR);
            drawable.setColorFilter(color, tintMode);
        }

        if (Build.VERSION.SDK_INT <= 23) {
            // Pre-v23 there is no guarantee that a state change will invoke an invalidation,
            // so we force it ourselves
            drawable.invalidateSelf();
        }
    }
}
