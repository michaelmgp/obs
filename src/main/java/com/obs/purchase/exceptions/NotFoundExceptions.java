package com.obs.purchase.exceptions;

public class NotFoundExceptions extends GenericException{

    public NotFoundExceptions(String message,   String specificCause) {
        super(message, specificCause);
    }
}
