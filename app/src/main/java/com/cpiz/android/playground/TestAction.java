package com.cpiz.android.playground;

import android.content.Intent;

/**
 * Created by caijw on 2015/9/2.
 */
public class TestAction {
    private String mName;
    private Runnable mAction;

    public TestAction(String name, Runnable action) {
        this.mName = name;
        this.mAction = action;
    }

    public TestAction(final Class<?> cls) {
        this.mName = cls.getSimpleName();
        this.mAction = () -> {
            Intent intent = new Intent(PlaygroundApp.getInstance(), cls);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PlaygroundApp.getInstance().startActivity(intent);
        };
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public Runnable getAction() {
        return mAction;
    }

    public void setAction(Runnable action) {
        this.mAction = action;
    }
}
