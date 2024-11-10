package com.obs.purchase.entity.dto;

import com.fasterxml.jackson.annotation.JsonInclude;



public class GenericResponse<T> {
    private String message;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;

    public GenericResponse(String message) {
        this.message=message;

    }

    public GenericResponse() {
    }

    public GenericResponse(String message, T data) {
        this.message = message;
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}

