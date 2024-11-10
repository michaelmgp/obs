package com.obs.purchase.entity;
import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

@Entity
public class InventoryTransaction extends BaseEntity {

    @NotNull
    @JsonAlias("item_id")
    @Positive
    private long itemId;

    @NotNull
    @Positive
    private int qty;

    @NotBlank
    @NotBlank
    @Pattern(regexp = "^[TW]$", message = "Type must be either 'T' for Top Up or 'W' for Withdrawal")
    private String type;

    public InventoryTransaction() {
    }

    public InventoryTransaction(long itemId, int qty, String type) {
        this.itemId = itemId;
        this.qty = qty;
        this.type = type;
    }

    public long getItemId() {
        return itemId;
    }

    public void setItemId(long itemId) {
        this.itemId = itemId;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
