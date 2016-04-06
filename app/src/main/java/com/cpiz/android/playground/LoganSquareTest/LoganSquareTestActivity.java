package com.cpiz.android.playground.LoganSquareTest;

import android.os.SystemClock;
import android.util.Log;

import com.bluelinelabs.logansquare.LoganSquare;
import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.cpiz.android.playground.BaseTestActivity;
import com.google.gson.Gson;

import java.io.IOException;

/**
 * LoganSquare 与 Gson 的性能对比测试
 *
 * Created by caijw on 2016/4/5.
 *
 * 测试结果
 * LoganSquareTestActivity: Gson deserialize 10000 times cost 680ms
 * LoganSquareTestActivity: LoganSquare deserialize 10000 t   imes cost 1169ms
 * LoganSquareTestActivity: Gson serialize 10000 times cost 615ms
 * LoganSquareTestActivity: LoganSquare serialize 10000 times cost 302ms
 */
public class LoganSquareTestActivity extends BaseTestActivity {
    private static final String TAG = "LoganSquareTestActivity";
    private static int LOOP_CYCLE = 10000;

    @Override
    public void onLeftClick() {
        String json = "{\"id\":98134,\"name\":\"xiaoming\",\"gender\":true,\"birth\":\"2016-04-05\",\"parentId\":1241234123412}";
        String json2 = null;
        Gson gson = new Gson();
        Person1 p1 = gson.fromJson(json, Person1.class);
        Person2 p2 = null;
        try {
            p2 = LoganSquare.parse(json, Person2.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        long beginTime = 0, endTime = 0;

        beginTime = SystemClock.elapsedRealtime();
        for (int i = 0; i < LOOP_CYCLE; ++i) {
            gson.fromJson(json, Person1.class);
        }
        endTime = SystemClock.elapsedRealtime();
        Log.i(TAG, String.format("Gson deserialize %d times cost %dms", LOOP_CYCLE, endTime - beginTime));


        beginTime = SystemClock.elapsedRealtime();
        try {
            for (int i = 0; i < LOOP_CYCLE; ++i) {
                LoganSquare.parse(json, Person2.class);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        endTime = SystemClock.elapsedRealtime();
        Log.i(TAG, String.format("LoganSquare deserialize %d times cost %dms", LOOP_CYCLE, endTime - beginTime));


        beginTime = SystemClock.elapsedRealtime();
        for (int i = 0; i < LOOP_CYCLE; ++i) {
            json2 = gson.toJson(p1);
        }
        endTime = SystemClock.elapsedRealtime();
        Log.i(TAG, String.format("Gson serialize %d times cost %dms", LOOP_CYCLE, endTime - beginTime));
        Log.i(TAG, String.format("Gson serialize result: %s", json2));


        beginTime = SystemClock.elapsedRealtime();
        try {
            for (int i = 0; i < LOOP_CYCLE; ++i) {
                json2 = LoganSquare.serialize(p2);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        endTime = SystemClock.elapsedRealtime();
        Log.i(TAG, String.format("LoganSquare serialize %d times cost %dms", LOOP_CYCLE, endTime - beginTime));
        Log.i(TAG, String.format("LoganSquare serialize result: %s", json2));
    }

    public static class Person1 {
        public long id;
        public String name;
        public boolean gender;
        public String birth;
        public long parentId;
    }

    @JsonObject
    public static class Person2 {
        @JsonField
        public long id;
        @JsonField
        public String name;
        @JsonField
        public boolean gender;
        @JsonField
        public String birth;
        @JsonField
        public long parentId;
    }
}
