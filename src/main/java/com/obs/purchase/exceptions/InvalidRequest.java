package com.obs.purchase.exceptions;

public class InvalidRequest extends GenericException
{

    public InvalidRequest(String message, String specificCause) {
        super(message,   specificCause);
    }
}
