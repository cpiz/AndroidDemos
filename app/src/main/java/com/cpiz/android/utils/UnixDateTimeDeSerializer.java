package com.cpiz.android.utils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import org.joda.time.DateTime;

import java.lang.reflect.Type;

/**
 * Unix日期与joda DateTime的Gson、序列化与反序列化工具类
 * <p/>
 * Created by caijw on 2015/8/26.
 */
public class UnixDateTimeDeSerializer implements JsonSerializer<DateTime>, JsonDeserializer<DateTime> {
    @Override
    public JsonElement serialize(DateTime src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(String.valueOf(src.getMillis() / 1000));
    }

    @Override
    public DateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        String jsonString;
        try {
            jsonString = json.getAsString();
        } catch (Exception e) {
            jsonString = json.toString();
        }

        return new DateTime(Long.valueOf(jsonString) * 1000);
    }
}
