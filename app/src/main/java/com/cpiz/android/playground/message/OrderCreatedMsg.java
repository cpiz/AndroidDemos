package com.cpiz.android.playground.message;

import com.google.gson.Gson;

/**
 * Created by caijw on 2015/9/1.
 */
public class OrderCreatedMsg extends Message<OrderCreatedMsg.Payload> {
    static class Payload {
        String orderId;
    }

    public OrderCreatedMsg() {
        super(MessageType.fromMessageClass(OrderCreatedMsg.class));
        setPayload(new Payload());
    }

    public String getOrderId() {
        return getPayload().orderId;
    }

    @Override
    protected Gson getGsonForPayload() {
        return new Gson();
    }
}
