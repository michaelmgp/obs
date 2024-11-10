package com.obs.purchase.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Type {
    TOP_UP("T"),
    WITHDRAWAL("W");

    private final String displayName;


    Type(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }
}
