package com.cse.parser;

import com.cse.exception.UnKnownException;

import java.io.Serializable;

/**
 * Created by bullet on 16. 10. 2.
 * Parser에 대한 팩토리 패턴
 */
public class ParseFactory implements Serializable {
    private static final String NAVER_NEWS_PREFIX = "news.naver.com";
    private static final String NAVER_BLOG_PREFIX = "blog.naver.com";

    public ParseFactory(){
    }

    /**
     * url에 해당되는 parser 반환
     * @param url Parsing할 url
     * @return Parser
     * @throws Exception
     */
    public static BaseParser getParser(String url) throws Exception {
        if(url.contains(NAVER_NEWS_PREFIX))
            return new NaverNewsParser(url);
        else if(url.contains(NAVER_BLOG_PREFIX))
            return new NaverBlogParser(url);
        else
            throw new UnKnownException();
    }
}
