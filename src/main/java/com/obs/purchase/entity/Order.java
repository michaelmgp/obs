package com.obs.purchase.entity;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "orders")
public class Order extends BaseEntity{

    private String orderNo;
    @JsonAlias("item_id")
    private long itemId;

    private Integer qty;
    private Integer price;

    public Integer getQty() {
        return qty;
    }

    public void setQty(Integer qty) {
        this.qty = qty;
    }

    public Order() {
    }

    public Order(String orderNo, long itemId, Integer qty, Integer price) {
        this.orderNo = orderNo;
        this.itemId = itemId;
        this.qty = qty;
        this.price = price;
    }





    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public long getItemId() {
        return itemId;
    }

    public void setItemId(long itemId) {
        this.itemId = itemId;
    }

    public long getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }
}
