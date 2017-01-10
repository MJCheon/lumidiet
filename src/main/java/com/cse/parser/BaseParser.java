package com.cse.parser;

import com.cse.exception.IpBannedException;
import com.cse.exception.UnKnownException;
import com.cse.network.HttpClient;
import com.cse.network.HttpResult;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.Serializable;

/**
 * Created by bullet on 16. 9. 8.
 * Parser에 대한 부모 클래스
 */
public abstract class BaseParser extends HttpClient implements Serializable{

    private static final String emailRegex = "(\\w+\\.)*\\w+@(\\w+\\.)+[A-Za-z]+\\S";
    private static final String otherEmailRegex = "(\\w+\\.)*\\w+@";
    private static final String theOtherEmailRegex = "@(\\w+\\.)+[A-Za-z]+\\S";
    private static final String bracketRegex = "\\[(.*?)\\]";
    private static final String otherBracketRegex = "\\【(.*?)\\】";
    private static final String roundBracketRegex = "\\((.*?)\\)";
    /**
     * News의 관련기사에 해당하는 패턴의 정규표현식
     */
    private static final String assoNewsRegex = "\\☞(.+)";
    /**
     * News의 관련기사에 해당하는 패턴의 정규표현식
     */
    private static final String assoNewsRegex1 = "\\▶(.+)";

    protected static String photoSection = ".end_photo_org";
    protected String url;
    protected Document doc;

    public BaseParser(){
        super();
    }

    public abstract String getTitle() throws UnKnownException;

    public abstract String getBody() throws UnKnownException;

    /**
     * News, Blog url의 HTML 코드 반환
     * @param url 실제 url
     * @throws Exception
     */
    protected void parse(String url) throws Exception{
        HttpResult httpResult = get(url);
        if(httpResult.getStatusCode() == httpResult.OK)
            doc = Jsoup.parse(httpResult.getBody());
        else if(httpResult.isBanned())
            throw new IpBannedException();
        else
            throw new UnKnownException();
    }

    /**
     * 본문에서의 필요 없는 내용 제거
     * @param body 페이지 본문
     * @return body
     */

    protected static String removeRegex(String body){
        String newBody = "";
        newBody = body.replaceAll(emailRegex, "");
        newBody = newBody.replaceAll(otherEmailRegex, "");
        newBody = newBody.replaceAll(theOtherEmailRegex, "");
        newBody = newBody.replaceAll(bracketRegex, "");
        newBody = newBody.replaceAll(roundBracketRegex, "");
        newBody = newBody.replaceAll(otherBracketRegex, "");
        newBody = newBody.replaceAll(assoNewsRegex, "");
        newBody = newBody.replaceAll(assoNewsRegex1, "");
        return newBody;
    }
}
