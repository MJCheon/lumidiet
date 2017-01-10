package com.cse.entity;

import java.io.Serializable;
import java.util.HashSet;

/**
 * Created by bullet on 16. 9. 8.
 * 문장에 대한 객체
 */
public class Paragraph implements Serializable{
    private int pageId;
    private String line;

    /**
     * 문장에 대한 생성자
     * @param pageId - 페이지 ID
     * @param line - 문장
     */
    public Paragraph(int pageId, String line){
        this.pageId = pageId;
        this.line = line;
    }

    public int getPageId(){
        return this.pageId;
    }

    public String getLine(){
        return this.line;
    }
}
