package com.cse.exception;

/**
 * Created by bullet on 16. 9. 8.
 */
public class UnKnownException extends Exception {
    @Override
    public String getMessage(){
        return "UnKnownException";
    }
}
