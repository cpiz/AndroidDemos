package com.cpiz.android.playground;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.trello.rxlifecycle.components.RxActivity;

/**
 * Created by caijw on 2015/8/31.
 */
public abstract class BaseTestActivity extends RxActivity implements View.OnClickListener {
    private static final String TAG = "BaseTestActivity";

    private Button mButton;
    private EditText mEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        setContentView(R.layout.base_activity);

        mButton = (Button) findViewById(R.id.button);
        mButton.setOnClickListener(this);

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

    public Button getButton() {
        return mButton;
    }

    public EditText getEditText() {
        return mEditText;
    }

    public void clearOutput() {
        mEditText.getText().clear();
    }

    public void appendLine(String str) {
        mEditText.append(str + "\n");
    }

    public void appendLine() {
        mEditText.append("\n");
    }
}
