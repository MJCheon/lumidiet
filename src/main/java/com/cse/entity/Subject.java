package com.cse.entity;

import com.cse.exception.IpBannedException;
import com.cse.common.LogInstance;
import com.cse.network.HttpClient;

import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by bullet on 16. 9. 27.
 */
public class Subject extends HttpClient implements Serializable {
    private boolean isBlog;
    private String url;
    private long date;

    public Subject(String url, long date){
        this.isBlog = isBlog(url);
        this.url = url;
        this.date = date;
    }

    /**
     * Blog에 대한 Subject 생성자
     * @param url API에서의 Blog url
     */
    public Subject(String url){
        try{
            this.url = getRedirectUrl(url);
            this.isBlog = isBlog(url);
        }
        catch (Exception e){
            LogInstance.getLogger().debug(e.getMessage());
        }
    }

    /**
     * News에 대한 Subject 생성자
     * @param url API에서의 News url
     * @param date News date
     * @throws IOException
     * @throws IpBannedException
     */

    public Subject(String url, String date) throws IOException, IpBannedException {
        try {
            this.url = getRedirectUrl(url);
            this.isBlog = isBlog(url);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
            this.date = simpleDateFormat.parse(date).getTime();
        }
        catch (Exception e){
            LogInstance.getLogger().debug(e.getMessage());
        }
    }

    @Override
    public int hashCode(){
        return this.url.hashCode();
    }

    @Override
    public boolean equals(Object obj){
        if(this.hashCode() == obj.hashCode())
            return true;
        else
            return false;
    }

    public void setIsBlog(boolean isBlog){
        this.isBlog = isBlog;
    }

    public boolean getIsBlog(){
        return this.isBlog;
    }

    public void setUrl(String url){
        this.url = url;
    }

    public String getUrl(){
        return this.url;
    }

    public void setDate(long date){
        this.date = date;
    }

    public long getDate(){
        return this.date;
    }

    private static boolean isBlog(String url){
        if(url.contains("blog.naver.com"))
            return true;
        else
            return false;
    }
}
