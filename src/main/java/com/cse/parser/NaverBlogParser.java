package com.cse.parser;

import com.cse.exception.IpBannedException;
import com.cse.exception.UnKnownException;
import com.cse.network.HttpResult;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.Serializable;
import java.util.Calendar;

/**
 * Created by bullet on 16. 10. 6.
 * Naver Blog에 대한 Parser
 */
public class NaverBlogParser extends BaseParser implements Serializable {
    private static final String NAVERBLOG_BASEURL = "http://blog.naver.com";
    private static final String NAVER_BODY_SECTION = "postViewArea";
    private static final String NAVER_BODY_SECTION2 = "postListBody";
    private static final String NAVER_TITLE_SECTION = "title_1";
    private static final String NAVER_TITLE_SECTION2 = "se_textarea";
    private static final String NAVER_DATE_SECTION = "_postAddDate";
    private static final String NAVER_DATE_SECTION2 = "se_publishDate";
    private static final int BODY_MIN_LENGTH = 5;

    private String frameUrl;

    public NaverBlogParser(String url) throws Exception{
        this.url = url;
        initFrameUrl(url);
        parse(frameUrl);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }


    private boolean initFrameUrl(String url) throws IOException,IpBannedException,UnKnownException {
        HttpResult httpResult = get(url);
        int statusCode = httpResult.getStatusCode();

        if (statusCode != HttpResult.OK) {
            if (statusCode == HttpResult.REDIRECT1 || statusCode == HttpResult.REDIRECT2)
                throw new IpBannedException();
            else {
                throw new UnKnownException();
            }
        }
        String blogBody = httpResult.getBody();
        frameUrl = getFrameUrl(url, blogBody);

        if (frameUrl.equals(url))
            return false;

        else
            return true;
    }

    private String getFrameUrl(String url, String body) throws UnKnownException {
        Document doc = Jsoup.parse(body);
        //else문을 제외하고는 Naver Blog의 Frame 부분을 정확히 받아온 경우임
        //안에 Frame이 존재해서 실제 url이 다른 경우
        if (doc.getElementById("mainFrame") != null)
            return NAVERBLOG_BASEURL + doc.getElementById("mainFrame").attr("src");
            //실제 url인 경우
        else if (doc.getElementById("postViewArea") != null)
            return url;
        else if (doc.getElementById("postListBody") != null)
            return url;
            //본문 부분이 추출되지 않은 경우
        else
            throw new UnKnownException();

    }

    public String getTitle() {
        if (doc.getElementById(NAVER_TITLE_SECTION) != null) {
            Element titleSection = doc.getElementById(NAVER_TITLE_SECTION);
            if (titleSection.childNodeSize() != 0) {
                if (titleSection.child(0).hasText())
                    return titleSection.child(0).text();
                else
                    return titleSection.child(1).text();
            }
            else
                return doc.title();
        } else if (doc.getElementsByTag("h3") != null) {
            Elements elements = doc.getElementsByTag("h3");
            for (Element element : elements) {
                if (element.hasClass(NAVER_TITLE_SECTION2))
                    return element.text();
            }
            return doc.title();
        }
        return doc.title();
    }

    public String getBody() throws UnKnownException{
        if (doc.getElementById(NAVER_BODY_SECTION) != null) {
            if(doc.getElementById(NAVER_BODY_SECTION).hasText()) {
                if(doc.getElementById(NAVER_BODY_SECTION).text().length() > BODY_MIN_LENGTH)
                    return doc.getElementById(NAVER_BODY_SECTION).text();
                else
                    throw new UnKnownException();
            }
        }
        else if (doc.getElementById(NAVER_BODY_SECTION2) != null) {
            if(doc.getElementById(NAVER_BODY_SECTION2).hasText()) {
                if(doc.getElementById(NAVER_BODY_SECTION2).text().length() > BODY_MIN_LENGTH)
                    return doc.getElementById(NAVER_BODY_SECTION2).text();
                else
                    throw new UnKnownException();
            }
        }
        throw new UnKnownException();
    }

    public long getDate() {
        Elements elements = doc.getElementsByClass(NAVER_DATE_SECTION);
        if (elements.size() != 0) {
            String date = elements.get(0).text();
            try {
                return getRealDate(date);
            } catch (Exception e) {
                return 0;
            }
        } else if (doc.getElementsByClass(NAVER_DATE_SECTION2) != null) {
            Elements d_elements = doc.getElementsByClass(NAVER_DATE_SECTION2);
            try {
                return getRealDate(d_elements.text());
            } catch (Exception e) {
                return 0;
            }
        } else
            return 0;
    }

    private long getRealDate(String date) throws Exception {
        String[] splitedData = date.split("\\.");
        if (splitedData.length == 4) {
            int year = Integer.parseInt(splitedData[0]);
            int month = Integer.parseInt(splitedData[1]);
            int day = Integer.parseInt(splitedData[2]);
            Calendar cal = Calendar.getInstance();
            cal.set(year, month - 1, day);
            return cal.getTimeInMillis();
        } else
            return 0;

    }
}