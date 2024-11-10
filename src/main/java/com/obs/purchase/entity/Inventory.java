package com.obs.purchase.entity;

import com.obs.purchase.repository.BaseRepository;
import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import org.springframework.stereotype.Repository;

import java.util.List;

@Entity
public class Inventory extends BaseEntity {
    @Column(nullable = false)
    private long itemId;
    private Integer stock;

    public long getItemId() {
        return itemId;
    }

    public void setItemId(long itemId) {
        this.itemId = itemId;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Inventory() {
    }

    public Inventory(long itemId, Integer stock) {
        this.itemId = itemId;
        this.stock = stock;
    }
}
