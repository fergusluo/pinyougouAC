package com.pinyougou.pojogroup;

import com.pinyougou.pojo.TbOrder;
import com.pinyougou.pojo.TbOrderItem;

import java.io.Serializable;
import java.util.List;

public class Order implements Serializable {
    private TbOrder order;
    private List<TbOrderItem> orderItem;
    private String orderId;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public TbOrder getOrder() {
        return order;
    }

    public void setOrder(TbOrder order) {
        this.order = order;
        this.orderId = order.getOrderId().toString();
    }

    public List<TbOrderItem> getOrderItem() {
        return orderItem;
    }

    public void setOrderItem(List<TbOrderItem> orderItem) {
        this.orderItem = orderItem;
    }

    @Override
    public String toString() {
        return "Order{" +
                "order=" + order +
                ", orderItem=" + orderItem +
                ", orderId='" + orderId + '\'' +
                '}';
    }
}