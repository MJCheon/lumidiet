package com.cse.network;

import com.cse.entity.Subject;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by bullet on 16. 9. 8.
 */
public class NaverNewsAPI extends NaverAPI implements Serializable {

    public NaverNewsAPI(){
        super();
        BASE_URL = "https://openapi.naver.com/v1/search/news.xml?display=100";
    }

    /**
     * API의 ITEM 태그로부터 News에 대한 Subject 객체 생성하여 반환
     * @param body API의 ITEM 태그에 대한 HTML 코드
     * @return Subject List
     * @throws Exception
     */
    public ArrayList<Subject> parseSubjects(String body) throws Exception {
        ArrayList<Subject> subjects = new ArrayList<Subject>();
        NodeList itemList = parseItems(body);
        int docCnt = itemList.getLength();

        for(int i=0; i< docCnt; i++){
            Node node = itemList.item(i);
            if(node.getNodeType() == Node.ELEMENT_NODE){
                Element element = (Element) node;
                String link = getTagValue(TAG_LINK, element);
                String date = getTagValue(TAG_DATE, element);
                subjects.add(new Subject(link, date));
            }
        }
        return subjects;
    }
}
