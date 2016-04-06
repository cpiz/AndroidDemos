package com.cpiz.android.controls;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.cpiz.android.playground.R;

/**
 * 支持指定纵横比例的 RelativeLayout 布局
 * Created by ruoshili on 8/28/15.
 */
public class FixedRatioLayout extends RelativeLayout {
    private static final String TAG = "FixedRatioLayout";
    private int mWidthRatio, mHeightRatio;
    private boolean mBaseOnWidth = true;

    public FixedRatioLayout(Context context) {
        super(context);
    }

    public FixedRatioLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FixedRatioLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    @TargetApi(21)
    public FixedRatioLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr);
    }

    public int getHeightRatio() {
        return mHeightRatio;
    }

    public int getWidthRatio() {
        return mWidthRatio;
    }

    public void setRatio(int widthRatio, int heightRatio) {
        if (mWidthRatio == widthRatio && mHeightRatio == heightRatio) {
            return;
        }
        this.mWidthRatio = widthRatio;
        this.mHeightRatio = heightRatio;
        requestLayout();
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray typedArray = null;
        try {
            typedArray = context.obtainStyledAttributes(attrs,
                    R.styleable.FixedRatioLayout, defStyleAttr, 0);

            mWidthRatio = typedArray.getInteger(R.styleable.FixedRatioLayout_widthRatio, 0);
            mHeightRatio = typedArray.getInteger(R.styleable.FixedRatioLayout_heightRatio, 0);
            mBaseOnWidth = typedArray.getInt(R.styleable.FixedRatioLayout_baseOn, 1) == 1;
        } finally {
            if (typedArray != null) {
                typedArray.recycle();
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mHeightRatio == 0 || mWidthRatio == 0) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        if (mBaseOnWidth) {
            int h = MeasureSpec.getSize(widthMeasureSpec) * mHeightRatio / mWidthRatio;
            int newHeightSpec = MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY);
            super.onMeasure(widthMeasureSpec, newHeightSpec);
        } else {
            final int w = MeasureSpec.getSize(heightMeasureSpec) * mWidthRatio / mHeightRatio;
            int newWidthSpec = MeasureSpec.makeMeasureSpec(w, MeasureSpec.EXACTLY);
            super.onMeasure(newWidthSpec, heightMeasureSpec);
        }
    }
}
