package com.cse.exception;

/**
 * Created by bullet on 16. 9. 8.
 */
public class IpBannedException extends Exception {
    @Override
    public String getMessage(){
        return "IP is Banned";
    }
}
