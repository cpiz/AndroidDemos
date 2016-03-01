package com.cpiz.android.playground.SmartDnsTest;

import android.os.Bundle;
import android.util.Log;

import com.cpiz.android.playground.BaseTestActivity;
import com.cpiz.android.utils.SmartDns;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

/**
 * Created by caijw on 2015/12/30.
 */
public class SmartDnsTestActivity extends BaseTestActivity {
    private static final String TAG = "SmartDnsTestActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SmartDns.Instance.init(getApplication());
    }

    @Override
    public void onLeftClick() {
        try {
            final String hostname = "WWW.YY.COM";
            List<InetAddress> ret = SmartDns.Instance.lookup(hostname);
            appendLine(String.format("%s -> %s", hostname, ret));
        } catch (UnknownHostException e) {
            appendLine(e.getMessage());
            Log.e(TAG, "Error", e);
        }
    }
}
