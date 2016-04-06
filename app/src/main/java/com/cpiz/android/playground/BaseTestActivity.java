package com.cpiz.android.playground;

import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.trello.rxlifecycle.components.RxActivity;

/**
 * 测试用Activity基类
 *
 * Created by caijw on 2015/8/31.
 */
public abstract class BaseTestActivity extends RxActivity {
    private static final String TAG = "BaseTestActivity";

    private Button mLeftBtn;
    private Button mRightBtn;
    private EditText mEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        setContentView(R.layout.base_activity);

        mLeftBtn = (Button) findViewById(R.id.btnLeft);
        mLeftBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLeftClick();
            }
        });

        mRightBtn = (Button) findViewById(R.id.btnRight);
        mRightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRightClick();
            }
        });

        mEditText = (EditText) findViewById(R.id.editText);

        Log.d(TAG, "onCreate");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    public abstract void onLeftClick();

    public void onRightClick() {
        clearEdit();
    }

    public Button getLeftBtn() {
        return mLeftBtn;
    }

    public Button getRightBtn() {
        return mRightBtn;
    }

    public EditText getEdit() {
        return mEditText;
    }

    public void clearEdit() {
        if (Looper.myLooper() == getMainLooper()) {
            mEditText.getText().clear();
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mEditText.getText().clear();
                }
            });
        }

    }

    public void appendLine(final String str) {
        if (Looper.myLooper() == getMainLooper()) {
            mEditText.append(str + "\n");
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mEditText.append(str + "\n");
                }
            });
        }
    }

    public void appendLine() {
        appendLine("\n");
    }
}
