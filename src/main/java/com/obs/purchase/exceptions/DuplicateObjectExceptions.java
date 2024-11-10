package com.obs.purchase.exceptions;



public class DuplicateObjectExceptions  extends GenericException
{

    public DuplicateObjectExceptions(String message,  String specificCause) {
        super(message,  specificCause);
    }
}
