package com.obs.purchase.exceptions;

public class GenericException extends RuntimeException{

    private String specificCause;
    public GenericException(String message,  String specificCause) {
        super(message);
        this.specificCause = specificCause;
    }

    public String getSpecificCause() {
        return specificCause;
    }

    public void setSpecificCause(String specificCause) {
        this.specificCause = specificCause;
    }
}
