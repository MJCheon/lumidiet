package com.cse.network;

import java.io.Serializable;

/**
 * Created by bullet on 16. 9. 8.
 * 웹페이지의 HTML 코드와 status코드에 대한 객체
 */
public class HttpResult implements Serializable{

    public static final transient int OK = 200;
    public static final transient int REDIRECT1 = 302;
    public static final transient int REDIRECT2 = 503;

    private String body;
    private int statusCode;

    public HttpResult(int statusCode, String body){
        this.statusCode = statusCode;
        this.body = body;
    }

    public boolean isBanned(){
        if(statusCode == REDIRECT1 || statusCode == REDIRECT2)
            return true;
        else
            return false;
    }

    public String getBody(){
        return body;
    }

    public void setBody(String body){
        this.body = body;
    }

    public int getStatusCode(){
        return this.statusCode;
    }

    public void setStatusCode(int statusCode){
        this.statusCode = statusCode;
    }
}
