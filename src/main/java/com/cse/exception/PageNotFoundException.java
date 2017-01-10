package com.cse.exception;

/**
 * Created by bullet on 16. 10. 20.
 */
public class PageNotFoundException extends Exception {
    @Override
    public String getMessage(){
        return "Page Not Found";
    }
}
