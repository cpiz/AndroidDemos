package com.cpiz.android.playground.message;

import java.util.HashMap;
import java.util.Map;

/**
 * 通知消息类型定义
 * 所有通知类消息，都统一使用该枚举定义类型
 * Created by caijw on 2015/9/1.
 */
public enum MessageType {
    OrderCreated(1, OrderCreatedMsg.class),
//    AppointTrialLesson(2, null),
//    AppointRegularLesson(3, null),
//    ReappointLesson(4, null),
//    RenewCourse(5, null),
//    StudentHasLogin(6, null),
//    RegisterParent(7, null),
//    Calling(1000, null),
//    UserInOut(1001, null),
//    MicrophoneChanged(1002, null),
    ;

    MessageType(int type, Class<? extends Message> cls) {
        mType = type;
        mClass = cls;
    }

    public int getType() {
        return mType;
    }

    public Class<? extends Message> getMessageClass() {
        return mClass;
    }

    public static MessageType fromTypeId(int type) {
        return typeMap.get(type);
    }

    public static MessageType fromMessageClass(Class<? extends Message> cls) {
        return classMap.get(cls);
    }

    private int mType;
    private Class<? extends Message> mClass;

    private static final Map<Integer, MessageType> typeMap = new HashMap<>();
    private static final Map<Class<? extends Message>, MessageType> classMap = new HashMap<>();

    static {
        for (MessageType obj : MessageType.values()) {
            typeMap.put(obj.getType(), obj);
            classMap.put(obj.getMessageClass(), obj);
        }
    }
}
