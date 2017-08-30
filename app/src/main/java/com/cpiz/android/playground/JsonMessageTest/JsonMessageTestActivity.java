package com.cpiz.android.playground.JsonMessageTest;

import android.os.Bundle;

import com.cpiz.android.playground.BaseTestActivity;
import com.cpiz.android.utils.RxBus;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;


/**
 * 实现了一个通用的消息包装类，使用Gson序列化反序列化对象
 *
 * Created by caijw on 2015/9/1.
 */
public class JsonMessageTestActivity extends BaseTestActivity {
    private static final String TAG = "JsonMessageTestActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RxBus.getDefault().register(OrderCreatedMsg.class)
                .subscribe(orderCreatedMsg -> {
                    String destJson = orderCreatedMsg.toJson();
                    appendLine(String.format("received a message[%s]", orderCreatedMsg.getClass().getSimpleName()));
                    appendLine(destJson);
                    appendLine();
                });
    }

    @Override
    public void onLeftClick() {
        clearEdit();

        final String srcJson = "{\"id\":\"ididididid\",\"state\":0,\"type\":1,\"isPersistent\":true,\"fromUid\":50013856,\"validity\":1440413154,\"createTime\":1440413154,\"payload\":{\"orderId\":\"ooooooorderiiiiid\",\"y\":456}}";
        Message msg = Message.fromJson(srcJson);
        RxBus.getDefault().post(msg);
        appendLine(String.format("post a message[%s]", msg.getClass().getSimpleName()));

        final String srcJsonArray = "[{\"id\":\"id1\",\"state\":0,\"type\":1,\"isPersistent\":true,\"fromUid\":50013856,\"validity\":1440413154,\"createTime\":1440413154,\"payload\":{\"orderId\":\"ooooooorderid1\"}},{\"id\":\"id2\",\"state\":0,\"type\":1,\"isPersistent\":true,\"fromUid\":50013856,\"validity\":1440413154,\"createTime\":1440413154,\"payload\":{\"orderId\":\"ooooooorderid2\"}},{\"id\":\"id3\",\"state\":0,\"type\":1,\"isPersistent\":true,\"fromUid\":50013856,\"validity\":1440413154,\"createTime\":1440413154,\"payload\":{\"orderId\":\"ooooooorderid3\"}}]";
        List<Message> listMsg = Message.listFromJson(srcJsonArray);

        Gson gson = new GsonBuilder()
                .registerTypeHierarchyAdapter(Message.class, new Message.MessageJsonAdapter())
                .create();
        Message msg2 = gson.fromJson(srcJson, Message.class);
        String desJson = gson.toJson(msg2);

        final int times = 1000;
        long startTime, stopTime;

        // serialize performance test
        startTime = System.currentTimeMillis();
        for (int i = 0; i < times; ++i) {
            Message.fromJson(srcJson);
        }
        stopTime = System.currentTimeMillis();
        appendLine(String.format("serialize %d messages in %dms", times, stopTime - startTime));

        // deserialize performance test
        startTime = System.currentTimeMillis();
        for (int i = 0; i < times; ++i) {
            msg.toJson();
        }
        stopTime = System.currentTimeMillis();
        appendLine(String.format("deserialize %d messages in %dms", times, stopTime - startTime));
    }
}
