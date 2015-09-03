package com.cpiz.android.playground.JsonMessageTest;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.joda.time.DateTime;

import java.lang.reflect.ParameterizedType;
import java.security.InvalidParameterException;

/**
 * 通知实体基类
 * <p/>
 * Created by caijw on 2015/9/1.
 */
public abstract class Message<T> {
    private static final String TAG = "Message";

    private static final Gson gGson = new GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .registerTypeAdapter(DateTime.class, new UnixDateTimeDeSerializer())
            .disableInnerClassSerialization()
            .create();

    /**
     * 收到通知的渠道
     */
    public enum Channel {
        Push,           // 实时推送
        Offline,        // 离线推送
        Pull,           // 主动拉取
        Remedy,         // 定时补偿
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
            msg = gGson.fromJson(json, MessageType.fromTypeId(type).getMessageClass());
            msg.setPayload(msg.getPreferPayloadGson().fromJson(jsonObject.get("payload"), msg.getPayloadType()));
        } catch (Exception ex) {
            Log.e(TAG, String.format("deserialize Message from json failed, error=\"%s\"", ex), ex);
        }

        return msg;
    }

    /**
     * 将Message对象转为Json字符串
     *
     * @return Json字符串
     */
    public String toJson() {
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(gGson.toJson(this));
        JsonElement payloadElement = getPreferPayloadGson().toJsonTree(getPayload(), getPayloadType());
        element.getAsJsonObject().add("payload", payloadElement);
        return element.toString();
    }

    /**
     * 获得子类定义的消息体转换Gson，或者使用默认的Gson
     *
     * @return
     */
    private Gson getPreferPayloadGson() {
        Gson gson = getPayloadGson();
        return gson == null ? gGson : gson;
    }

    /**
     * 获得用于转换消息内容的Gson，子类必须实现该接口
     * 若子类消息内容无须做特殊转换，可返回null
     *
     * @return
     */
    protected abstract Gson getPayloadGson();

    /**
     * 构造函数
     *
     * @param type 用于区分消息类型的唯一 type id
     */
    protected Message(MessageType type) {
        mType = type;
    }

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

    public void setPermanent(boolean permanent) {
        mPermanent = permanent;
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

    public int getVersion() {
        return mVersion;
    }

    public void setVersion(int version) {
        mVersion = version;
    }

    public T getPayload() {
        return mPayload;
    }

    public void setPayload(T payload) {
        mPayload = payload;
    }

    /**
     * 获得装载内容的类型
     *
     * @return
     */
    public Class<T> getPayloadType() {
        ParameterizedType pType = (ParameterizedType) getClass().getGenericSuperclass();
        return (Class) pType.getActualTypeArguments()[0];
    }

    @Expose
    @SerializedName("id")
    private String mId;

    @Expose
    @SerializedName("type")
    private MessageType mType;

    @Expose
    @SerializedName("fromUid")
    private Long mFromUid;

    @Expose(serialize = false, deserialize = false)
    private Channel mChannel;

    @Expose
    @SerializedName("state")
    private byte mState;

    /**
     * 是否持久化存储在本地
     */
    @Expose
    @SerializedName("isPersistent")
    private boolean mPermanent;

    @Expose
    @SerializedName("createTime")
    private DateTime mCreateTime;

    @Expose
    @SerializedName("validity")
    private DateTime mExpireTime;

    /**
     * 消息版本，解析消息时可根据version做兼容处理
     */
    @Expose
    @SerializedName("version")
    private int mVersion;

    @Expose(serialize = false, deserialize = false)
    private T mPayload;
}
