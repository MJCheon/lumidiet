package com.cse.entity;

import com.cse.common.Common;
import com.cse.parser.BaseParser;
import com.cse.network.HttpClient;
import com.cse.parser.NaverBlogParser;
import com.cse.parser.ParseFactory;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import java.io.Serializable;

/**
 * Created by bullet on 16. 9. 8.
 * Page에 대한 객체
 */
public class Page extends HttpClient implements Serializable{
    private int id;
    private String url;
    private long date;
    private String title;
    private String body;

    /**
     * Blog에 대한 Page 생성자
     * @param url Blog url
     */
    public Page(String url){
        try {
            this.id = 0;
            this.url = url;
            NaverBlogParser parser = new NaverBlogParser(url);

            this.title = parser.getTitle();
            this.body = parser.getBody();
            this.date = parser.getDate();
        }
        catch (Exception e){
            this.id = Common.ERROR;
            return;
        }
    }

    /**
     * News에 대한 Page 생성자
     * @param url News url
     * @param date News date
     * @throws Exception
     */

    public Page(String url, long date) throws Exception {
        BaseParser parser = null;
        try {
            this.id = 0;
            this.url = url;
            parser = ParseFactory.getParser(url);

            this.title = parser.getTitle();
            this.body = parser.getBody();
            this.date = date;
        }
        catch (Exception e) {
            this.id = Common.ERROR;
            return;
        }
    }

    public Page(int id, String body){
        this.id = id;
        this.url = null;
        this.body = body;
        this.title = null;
        this.date = 0;
    }

    public int getId(){
        return this.id;
    }

    public void setId(int id){
        this.id = id;
    }

    public String getUrl(){
        return this.url;
    }

    public void setUrl(String url){
        this.url = url;
    }

    public String getTitle(){
        return this.title;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public String getBody(){
        return this.body;
    }

    public void setBody(String body){
        this.body = body;
    }

    public long getDate(){
        return this.date;
    }

    public void setDate(long date){
        this.date = date;
    }

    /**
     * DB에 있는 Page 테이블에 대한 구조
     * @return
     */
    public static StructType getStructType(){
        return new StructType(new StructField[]{
            new StructField("id", DataTypes.IntegerType, false, Metadata.empty()),
            new StructField("url", DataTypes.StringType, false, Metadata.empty()),
            new StructField("title", DataTypes.StringType, false, Metadata.empty()),
            new StructField("body", DataTypes.StringType, false, Metadata.empty()),
            new StructField("date", DataTypes.LongType, false, Metadata.empty())
        });
    }

    public Row pageToRow(){
        return RowFactory.create(id, url, title, body, date);
    }
}
