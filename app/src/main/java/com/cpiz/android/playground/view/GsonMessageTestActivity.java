package com.cpiz.android.playground.view;

import android.os.Bundle;
import android.view.View;

import com.cpiz.android.playground.BaseTestActivity;
import com.cpiz.android.playground.message.Message;
import com.cpiz.android.playground.message.OrderCreatedMsg;
import com.cpiz.android.utils.RxBus;

import rx.functions.Action1;

/**
 * Created by caijw on 2015/9/1.
 */
public class GsonMessageTestActivity extends BaseTestActivity {
    private static final String TAG = "GsonMessageTestActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RxBus.getDefault().register(OrderCreatedMsg.class)
                .subscribe(new Action1<OrderCreatedMsg>() {
                    @Override
                    public void call(OrderCreatedMsg orderCreatedMsg) {
                        String destJson = orderCreatedMsg.toJson();
                        appendLine(String.format("received a message[%s]", orderCreatedMsg.getClass().getSimpleName()));
                        appendLine(destJson);
                        appendLine();
                    }
                });
    }

    @Override
    public void onClick(View v) {
        String srcJson = "{\"id\":\"ididididid\",\"state\":0,\"type\":1,\"isPersistent\":true,\"fromUid\":50013856,\"validity\":1440413154,\"createTime\":1440413154,\"payload\":\"oooooorderid\",\"xxx\":{\"x\":123,\"y\":456}}";

        Message msg = Message.fromJson(srcJson);
        appendLine(String.format("post a message[%s]", msg.getClass().getSimpleName()));
        RxBus.getDefault().post(msg);
    }
}
