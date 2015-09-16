package com.cpiz.android.controls;

/**
 * 加强版ImageButton
 * <p/>
 * 支持Check状态，setCheckable(true)之后点击按钮将自动触发Check/Uncheck
 * 支持通过app:tint属性设定一个颜色selector，根据按钮不同状态对图标进行染色
 * <p/>
 * 参考 http://stackoverflow.com/questions/11095222/android-imageview-change-tint-to-simulate-button-click/18724834#18724834
 */

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Checkable;
import android.widget.ImageButton;

import com.cpiz.android.playground.R;

public class ImageButtonEx extends ImageButton implements Checkable {

    private ColorStateList mTint;
    private boolean mChecked = false;
    private boolean mCheckable = false;
    private boolean mBroadcasting = false;

    private OnCheckedChangeListener mOnCheckedChangeListener;
    private OnClickListener mInternalOnClickListener = null;
    private OnClickListener mExternalOnClickListener = null;

    private static final int CHECKED_STATE = 16842912; // R.attr.state_checked
    private static final int[] CHECKED_STATE_SET = {
            CHECKED_STATE
    };

    public ImageButtonEx(Context context) {
        super(context);
        init(context, null, 0);
    }

    public ImageButtonEx(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public ImageButtonEx(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        mInternalOnClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mExternalOnClickListener != null) {
                    mExternalOnClickListener.onClick(v);
                } else if (mCheckable) {
                    toggle();
                }
            }
        };
        super.setOnClickListener(mInternalOnClickListener);

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ImageButtonEx, defStyle, 0);

            mTint = a.getColorStateList(R.styleable.ImageButtonEx_tint);

            final boolean checked = a.getBoolean(R.styleable.ImageButtonEx_checked, false);
            setChecked(checked);

            final boolean checkable = a.getBoolean(R.styleable.ImageButtonEx_checkable, false);
            setCheckable(checkable);

            a.recycle();
        }
    }

    public void setColorFilter(ColorStateList tint) {
        this.mTint = tint;
        super.setColorFilter(tint.getColorForState(getDrawableState(), 0));
    }

    /**
     * 是否点击自动切换 Check/Uncheck
     *
     * @return
     */
    public boolean isCheckable() {
        return mCheckable;
    }

    /**
     * 设置点击自动切换 Check/Uncheck
     * 注意：如果通过{@link this#setOnClickListener}自行接管了点击事件，将不再自动切换
     *
     * @param checkable
     */
    public void setCheckable(boolean checkable) {
        mCheckable = checkable;
    }

    /**
     * 设置选中/取消选中状态
     *
     * @param checked
     */
    @Override
    public void setChecked(boolean checked) {
        if (checked != mChecked) {
            mChecked = checked;

            refreshDrawableState();

            // Avoid infinite recursions if setChecked() is called from a listener
            if (mBroadcasting) {
                return;
            }

            mBroadcasting = true;
            if (mOnCheckedChangeListener != null) {
                mOnCheckedChangeListener.onCheckedChanged(this, mChecked);
            }
            mBroadcasting = false;
        }
    }

    /**
     * 判断是否选中
     *
     * @return
     */
    @Override
    public boolean isChecked() {
        return mChecked;
    }

    /**
     * 切换选中状态
     */
    @Override
    public void toggle() {
        setChecked(!mChecked);
    }

    /**
     * 设置点击时间监听
     * 设置非null后，控件不再支持点击触发切换
     *
     * @param listener 点击事件监听器
     */
    @Override
    public void setOnClickListener(OnClickListener listener) {
        mExternalOnClickListener = listener;
    }

    /**
     * 设置Check状态监听
     *
     * @param listener 选中状态变化监听器
     */
    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        mOnCheckedChangeListener = listener;
    }

    @Override
    public int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (isChecked()) {
            mergeDrawableStates(drawableState, CHECKED_STATE_SET);
        }
        return drawableState;
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if (mTint != null && mTint.isStateful())
            updateTintColor();
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();

        SavedState ss = new SavedState(superState);

        ss.checked = isChecked();
        return ss;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;

        super.onRestoreInstanceState(ss.getSuperState());
        setChecked(ss.checked);
        requestLayout();
    }

    private void updateTintColor() {
        int color = mTint.getColorForState(getDrawableState(), 0);
        if (Color.alpha(color) == 0xFF) {
            setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        } else {
            // 若Tint里带透明色，则使用MULTIPLY颜色
            setColorFilter(color, PorterDuff.Mode.MULTIPLY);
        }
    }

    static class SavedState extends BaseSavedState {
        boolean checked;

        /**
         * Constructor called from {CompoundButton#onSaveInstanceState()}
         */
        SavedState(Parcelable superState) {
            super(superState);
        }

        /**
         * Constructor called from {@link #CREATOR}
         */
        private SavedState(Parcel in) {
            super(in);
            checked = (Boolean) in.readValue(null);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeValue(checked);
        }

        @Override
        public String toString() {
            return "CompoundButton.SavedState{"
                    + Integer.toHexString(System.identityHashCode(this))
                    + " checked=" + checked + "}";
        }

        public static final Parcelable.Creator<SavedState> CREATOR
                = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    /**
     * Interface definition for a callback to be invoked when the checked state
     * of a compound button changed.
     */
    public interface OnCheckedChangeListener {
        /**
         * Called when the checked state of a compound button has changed.
         *
         * @param buttonView The compound button view whose state has changed.
         * @param isChecked  The new checked state of buttonView.
         */
        void onCheckedChanged(ImageButtonEx buttonView, boolean isChecked);
    }
}