package com.cpiz.android.playground.message;

import android.util.Log;

import com.cpiz.android.utils.UnixDateTimeDeSerializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.joda.time.DateTime;

import java.lang.reflect.Type;
import java.security.InvalidParameterException;

/**
 * 通知实体基类
 * <p/>
 * Created by caijw on 2015/9/1.
 */
public abstract class Message<T> {
    private static final String TAG = "Message";

    /**
     * 收到通知的渠道
     */
    public enum Channel {
        Push,           // 实时推送
        Offline,        // 离线推送
        Pull,           // 主动拉取
        Extra,          // 定时补偿
    }

    /**
     * 从Json字符串获得一个Message子类对象
     *
     * @param json Json字符串
     * @return Message子类实例
     */
    public static Message fromJson(String json) {
        Message msg = null;

        try {
            JsonElement jsonElement = new JsonParser().parse(json);
            if (jsonElement == null || !jsonElement.isJsonObject()) {
                throw new InvalidParameterException(String.format("[%s] is not a json string", json));
            }

            JsonObject jsonObject = jsonElement.getAsJsonObject();
            if (!jsonObject.has("type")) {
                throw new InvalidParameterException(String.format("[%s] has no \"type\" property", json));
            }

            int type = jsonObject.get("type").getAsInt();
            msg = Message.fromJson(json, MessageType.fromTypeId(type).getMessageClass());
        } catch (Exception ex) {
            Log.e(TAG, String.format("deserialize Message from json failed, error=\"%s\"", ex), ex);
        }

        return msg;
    }

    /**
     * 从Json字符串获得一个Message子类对象
     *
     * @param json Json字符串
     * @param cls  Message子类
     * @return Message子类实例
     */
    public static <T extends Message> T fromJson(String json, Class<T> cls) {
        return new GsonBuilder().registerTypeAdapter(cls, new MessageSerializer()).create().fromJson(json, cls);
    }

    /**
     * 将Message对象转为Json字符串
     *
     * @return Json字符串
     */
    public String toJson() {
        return new GsonBuilder().registerTypeAdapter(this.getClass(), new MessageSerializer()).create().toJson(this);
    }

    /**
     * 构造函数
     *
     * @param type 用于区分消息类型的唯一 type id
     */
    protected Message(MessageType type) {
        mType = type;
    }

    /**
     * 将消息内容序列化为字符串，子类必须实现
     *
     * @return
     */
    protected abstract String serializerPayload();

    /**
     * 通过字符串反序列化出消息内容，子类必须实现
     *
     * @param json
     */
    protected abstract void deserializerPayload(String json);

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public MessageType getType() {
        return mType;
    }

    public void setType(MessageType type) {
        mType = type;
    }

    public Long getFromUid() {
        return mFromUid;
    }

    public void setFromUid(Long fromUid) {
        mFromUid = fromUid;
    }

    public Channel getChannel() {
        return mChannel;
    }

    public void setChannel(Channel channel) {
        mChannel = channel;
    }

    public boolean isReaded() {
        return mState == 1;
    }

    public void setReaded(boolean val) {
        mState = (byte) (val ? 1 : 0);
    }

    public boolean isPermanent() {
        return mPermanent;
    }

    public DateTime getCreateTime() {
        return mCreateTime;
    }

    public void setCreateTime(DateTime createTime) {
        mCreateTime = createTime;
    }

    public DateTime getExpireTime() {
        return mExpireTime;
    }

    public void setExpireTime(DateTime expireTime) {
        mExpireTime = expireTime;
    }

    @SerializedName("id")
    private String mId;

    @SerializedName("type")
    private MessageType mType;

    @SerializedName("fromUid")
    private Long mFromUid;

    @Expose(serialize = false, deserialize = false)
    private Channel mChannel;

    @SerializedName("state")
    private byte mState;

    @SerializedName("isPersistent")
    private boolean mPermanent;

    @SerializedName("createTime")
    private DateTime mCreateTime;

    @SerializedName("validity")
    private DateTime mExpireTime;

    @SerializedName("xxx")
    private T xxx;

    private static class MessageSerializer implements JsonSerializer<Message>, JsonDeserializer<Message> {
        @Override
        public Message deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(DateTime.class, new UnixDateTimeDeSerializer())
                    .disableInnerClassSerialization()
                    .create();
            Message msg = gson.fromJson(json, typeOfT);
            try {
                String payloadString = json.getAsJsonObject().get("payload").getAsString();
                msg.deserializerPayload(payloadString);
            } catch (Exception e) {
                Log.e(TAG, "parse payload error", e);
            }
            return msg;
        }

        @Override
        public JsonElement serialize(Message src, Type typeOfSrc, JsonSerializationContext context) {
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(DateTime.class, new UnixDateTimeDeSerializer())
                    .disableInnerClassSerialization()
                    .create();
            JsonParser parser = new JsonParser();
            JsonElement element = parser.parse(gson.toJson(src));
            element.getAsJsonObject().addProperty("payload", src.serializerPayload());
            return element;
        }
    }

}
