package com.cpiz.android.playground.AnnotationsTest;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
import android.support.annotation.StringRes;
import android.util.Log;
import android.widget.Toast;

import com.cpiz.android.playground.BaseTestActivity;
import com.cpiz.android.playground.R;

/**
 * Created by caijw on 2015/10/16.
 *
 * @see <a href="#https://github.com/greenrobot/EventBus/blob/master/HOWTO.md">EventBus How-To</a>
 */
public class AnnotationsActivity extends BaseTestActivity {
    private static final String TAG = "AnnotationsActivity";

    public static final int ONE = 1;
    public static final int TWO = 2;
    public static final int THREE = 3;

    @IntDef(flag = true, value = {ONE, TWO, THREE})
    public @interface Num {
    }


    public static final String A = "A";
    public static final String B = "B";
    public static final String C = "C";
    @StringDef(value = {A, B, C})
    public @interface Char {
    }

    @Override
    public void onLeftClick() {
        printNonNull(getStr()); // IDE Warning
//        Toast.makeText(this, getStr(123), Toast.LENGTH_SHORT).show();   // IDE Error
        Toast.makeText(this, getStr(R.string.abc_action_bar_home_description), Toast.LENGTH_SHORT).show();

        printNonNull(String.valueOf(getNum(ONE)));
        printNonNull(String.valueOf(getNum(ONE | TWO)));

        printNonNull(A);
    }

    private void printNonNull(@NonNull String str) {
        appendLine("" + str);
    }

    @Nullable
    private String getStr() {
        return null;
    }

    private String getStr(@StringRes int id) {
        return "";
    }

    private int getNum(@Num int i) {
        return i;
    }

    private String getChar(@Char String ch) {
        return ch;
    }
}
