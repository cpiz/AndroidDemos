package com.cpiz.android.playground.InputOnlyEditTextTest;

import android.annotation.TargetApi;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

/**
 * 禁止粘贴的EditText
 * Created by caijw on 2016/4/26.
 */
public class InputOnlyEditText extends EditText {
    private static final String TAG = "InputOnlyEditText";
    private ClipboardManager mClipboard = null;

    public InputOnlyEditText(Context context) {
        super(context);
        init();
    }

    public InputOnlyEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public InputOnlyEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public InputOnlyEditText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        setLongClickable(false);

        setCustomSelectionActionModeCallback(new ActionMode.Callback() {
            public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                return false;
            }

            public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                return false;
            }

            public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                return false;
            }

            public void onDestroyActionMode(ActionMode actionMode) {
            }
        });

        if (mClipboard == null) {
            mClipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        }

        addTextChangedListener(new TextWatcher() {
            boolean pasted = false;
            String beforeStr = null;
            int selectionStart = 0;
            int selectionEnd = 0;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//                Log.d(TAG, "beforeTextChanged() called with: " + "s = [" + s + "], start = [" + start + "], count = [" + count + "], after = [" + after + "]");
                pasted = false;
                beforeStr = s.toString();
                selectionStart = getSelectionStart();
                selectionEnd = getSelectionEnd();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                Log.d(TAG, "onTextChanged() called with: " + "s = [" + s + "], start = [" + start + "], before = [" + before + "], count = [" + count + "]");
                if (count >= 3) {
                    if (mClipboard != null && mClipboard.hasPrimaryClip()) {
                        CharSequence str = mClipboard.getPrimaryClip().getItemAt(0).coerceToText(getContext());
                        CharSequence newAppend = s.subSequence(start, start + count);
                        Log.d(TAG, "newAppend: " + newAppend);

                        if (isEquals(str, newAppend)) {
                            pasted = true;
                        }
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
//                Log.d(TAG, "afterTextChanged() called with: " + "s = [" + s + "]");
                if (pasted) {
                    removeTextChangedListener(this);
                    InputOnlyEditText.this.setText(beforeStr);
                    setSelection(selectionStart, selectionEnd);
                    addTextChangedListener(this);
                    pasted = false;
                }
            }
        });
    }

    @Override
    public boolean onTextContextMenuItem(int id) {
        if (id == android.R.id.paste) {
            return false;
        } else {
            return super.onTextContextMenuItem(id);
        }
    }

    private boolean isEquals(CharSequence actual, CharSequence expected) {
        if (actual == expected) {
            return true;
        } else if (actual != null && expected != null) {
            if (actual.length() == expected.length()) {
                for (int i = 0; i < actual.length(); ++i) {
                    if (actual.charAt(i) != expected.charAt(i)) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }
}
