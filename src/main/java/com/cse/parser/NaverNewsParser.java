package com.cse.parser;

import com.cse.exception.UnKnownException;
import org.jsoup.nodes.Element;

import java.io.Serializable;

/**
 * Created by bullet on 16. 9. 8.
 * Naver News에 대한 Parser
 */
public class NaverNewsParser extends BaseParser implements Serializable{
    private static String titleSection = "articleTitle";
    private static String bodySection = "articleBodyContents";


    public NaverNewsParser(String url) throws Exception{
        parse(url);
    }

    @Override
    public String getTitle() throws UnKnownException {
        String title = "";
        if (doc.getElementById(titleSection).text() != null)
            title = doc.getElementById(titleSection).text();
        return title;
    }

    @Override
    public String getBody() throws UnKnownException {
        String body = "";
        Element element = doc.getElementById(bodySection);

        if (element.text() != null)
            body = element.text();

        for(Element element1 : element.select(photoSection))
            body = body.replace(element1.text(), "");
        body = removeRegex(body);
        return body;
    }
}
