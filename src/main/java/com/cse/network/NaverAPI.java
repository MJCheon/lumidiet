package com.cse.network;

import com.cse.common.LogInstance;
import com.cse.entity.Subject;
import com.esotericsoftware.minlog.Log;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Properties;

/**
 * Created by bullet on 16. 9. 8.
 */
public abstract class NaverAPI extends HttpClient implements Serializable{
    protected static final String KEY_ID = "X-Naver-Client-Id";
    protected static final String VALUE_ID = "J84aKYz5UXQqxRAMMMQi";
    protected static final String KEY_SECRET = "X-Naver-Client-Secret";
    protected static final String VALUE_SECRET = "FBUrGiSdsb";
    protected static String TAG_LINK = "link";
    protected static String TAG_ITEM = "item";
    protected static String TAG_DATE = "pubDate";
    protected String BASE_URL;
    protected static final int MAX_COLLECT = 10;
    protected static Properties headerList;

    protected NaverAPI(){
        if(headerList == null){
            headerList = new Properties();
            headerList.put(KEY_ID, VALUE_ID);
            headerList.put(KEY_SECRET, VALUE_SECRET);
        }
    }

    /**
     * api 요청에 대한 페이지에서 ITEM 태그에 해당하는 부분 반환
     * @param body - api의 한 페이지
     * @return NodeList
     * @throws Exception
     */
    protected NodeList parseItems(String body) throws Exception{
        org.w3c.dom.Document doc;
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        doc = dBuilder.parse(new InputSource(new StringReader(body)));
        doc.getDocumentElement().normalize();

        NodeList itemList = doc.getElementsByTagName(TAG_ITEM);

        return itemList;
    }

    /**
     * Tag의 원소 반환
     * @param sTag api의 고유 태그
     * @param eElement HTML Element
     * @return
     */
    protected String getTagValue(String sTag, Element eElement){
        NodeList nlList = eElement.getElementsByTagName(sTag).item(0).getChildNodes();
        Node nValue = (Node) nlList.item(0);
        return nValue.getNodeValue();
    }

    public abstract ArrayList<Subject> parseSubjects(String body) throws Exception;

    /**
     * keyword로부터 MAX_COLLECT * 100개에 해당하는 Page들을 반환
     * @param keyword 학습과 관련된 검색 단어
     * @return Subject
     */
    public ArrayList<Subject> getUrlList(String keyword){
        ArrayList<Subject> subjects = new ArrayList<Subject>();
        HttpResult result = null;
        for(int i=0 ;i<MAX_COLLECT; i++){
            String requestUrl = BASE_URL + "&start=" + (i*100+1) + "&query=" + keyword;
            try {
                HttpClient httpClient = new HttpClient();
                result = httpClient.apiGet(requestUrl, headerList);
                subjects.addAll(parseSubjects(result.getBody()));
            }
            catch (Exception e){
                LogInstance.getLogger().debug(e.getMessage());
                LogInstance.getLogger().debug(result.getBody());
            }
        }
        return subjects;
    }
}
