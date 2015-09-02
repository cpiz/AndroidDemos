package com.cpiz.android.playground.message;

/**
 * Created by caijw on 2015/9/1.
 */
public class OrderCreatedMsg extends Message<OrderCreatedMsg.xxx> {
    class xxx {
        int x;
        int y;
    }

    private String mOrderId;

    public OrderCreatedMsg() {
        super(MessageType.fromMessageClass(OrderCreatedMsg.class));
    }

    public String getOrderId() {
        return mOrderId;
    }

    @Override
    protected String serializerPayload() {
        return mOrderId;
    }

    @Override
    protected void deserializerPayload(String json) {
        mOrderId = json;
    }
}
