package com.cpiz.android.playground.butterKnifeTest;

import android.os.Bundle;
import android.widget.EditText;

import com.cpiz.android.playground.R;
import com.trello.rxlifecycle2.components.RxActivity;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by caijw on 2016/3/1.
 */
public class ButterKnifeTestActivity extends RxActivity {
    @BindView(R.id.editText)
    EditText mEditText; // bind view

    @BindString(R.string.app_name)
    String mAppName; // bind string

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_activity);
        ButterKnife.bind(this);

        appendLine(mAppName);
    }

    @OnClick(R.id.btnLeft)
    void onLeftClick() { // bind method
        appendLine("LeftClick");
    }

    @OnClick(R.id.btnRight)
    void onRightClick() {
        appendLine("RightClick");
    }

    public void appendLine(String str) {
        mEditText.append(str + "\n");
    }
}
