package com.cse.network;

import com.cse.common.Common;
import com.cse.exception.IpBannedException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.io.Serializable;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * Created by bullet on 16. 9. 8.
 */
public class HttpClient implements Serializable {
    protected static OkHttpClient nonRedirectClient;
    protected static OkHttpClient apiHttpClient;
    protected static OkHttpClient myHttpClient;

    public HttpClient() {
        initHttpClients();
    }

    public static boolean initHttpClients(){
        try{
            apiHttpClient = new OkHttpClient.Builder().connectTimeout(5, TimeUnit.SECONDS).build();
            nonRedirectClient = new OkHttpClient.Builder().followRedirects(false).connectTimeout(5, TimeUnit.SECONDS).build();
            myHttpClient = new OkHttpClient.Builder().connectTimeout(5, TimeUnit.SECONDS).build();

            return true;
        }
        catch (Exception e){
            return false;
        }
    }

    /**
     * Get 메소드
     * @param url 실제 url
     * @return httpResult
     * @throws IOException
     */
    public HttpResult get(String url) throws IOException{
        String body = "";
        int statusCode = 200;

        Response response = null;
        Common.sleep();

        try {
            Request request = new Request.Builder().url(url).build();
            response = myHttpClient.newCall(request).execute();
            String charset = response.header("Content-Type").toString();
            if(charset.contains("utf-8") | charset.contains("UTF-8"))
                body = new String(response.body().source().readByteArray(), "utf-8");
            else
                body = new String(response.body().source().readByteArray(), "euc-kr");

        }
        catch(Exception e){
            e.printStackTrace();
        }
        finally {
            if(response != null)
                response.body().close();
        }
        return new HttpResult(statusCode, body);
    }

    /**
     * Naver API 전용 Get 메소드
     * @param url NaverAPI url
     * @param headerList NaverAPI HeaderList
     * @return HttpResult
     * @throws IOException
     */

    protected HttpResult apiGet(String url, Properties headerList) throws IOException{
        String body = "";
        int statusCode = 200;

        Response response = null;

        try {
            Request.Builder requestBuilder = new Request.Builder().url(url);

            for(Object obj : headerList.keySet()){
                String key = (String)obj;
                String value = (String)headerList.get(key);
                requestBuilder.addHeader(key, value);
            }

            Request request = requestBuilder.build();
            response = apiHttpClient.newCall(request).execute();

            String charset = response.header("Content-Type").toString();

            if(charset.contains("utf-8"))
                body = new String(response.body().source().readByteArray(), "utf-8");
            else
                body = new String(response.body().source().readByteArray(), "euc-kr");
            statusCode = response.code();
        }
        catch (Exception e){

        }
        finally {
            if(response != null)
                response.body().close();
        }
        return new HttpResult(statusCode, body);
    }

    /**
     * redirect하여 본래의 url을 얻어옴
     * @param url api에서 넘겨주는 News, Blog url
     * @return redirectUrl(실제 Url)
     * @throws IOException
     * @throws IpBannedException
     * */
    public String getRedirectUrl(String url) throws IOException,IpBannedException {
        //request를 만들어 HTTP 통신 수행
        Request request = new Request.Builder().url(url).build();
        Response response = nonRedirectClient.newCall(request).execute();
        String redirectUrl = "";
        redirectUrl = response.header("Location", "");
        response.body().close();

        //Location 헤더가 존재하지 않는다면 IP 차단 당한 경우임
        if(redirectUrl.equals(""))
            throw new IpBannedException();

        return redirectUrl;
    }
}
