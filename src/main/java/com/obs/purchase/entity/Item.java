package com.obs.purchase.entity;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;


@Entity
public class Item extends BaseEntity {

    private long id;
    @NotBlank(message = "this field cannot be empty")
    @Pattern(regexp = "^[A-Za-z ]*$", message = "Invalid Input")
    private String name;
    @NotNull(message = "this field cannot be empty")
    @PositiveOrZero
    private int price;

    public Item() {
    }

    public Item(long id, String name, int price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }
}
