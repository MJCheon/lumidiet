package com.cse.entity;

import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.dmg.pmml.DataType;

import java.io.Serializable;

/**
 * Created by bullet on 16. 9. 8.
 * 문서의 단어에 대한 객체
 */
public class Word implements Serializable{
    private long id;
    private int pageId;
    private String word;
    private double tf;
    private double tfidf;
    private double cnt;

    public Word(int pageId, String word){
        this.id = 0;
        this.pageId = pageId;
        this.word = word;
        this.tf = 0;
        this.tfidf = 0;
        this.cnt = 0;
    }

    @Override
    public int hashCode(){
        String identity = pageId+"/"+word;
        return identity.hashCode();
    }

    @Override
    public boolean equals(Object obj){
        if(this.hashCode() == obj.hashCode())
            return true;
        else
            return false;
    }
    public long getId(){
        return this.id;
    }

    public void setId(long id){
        this.id = id;
    }

    public int getPageId(){
        return this.pageId;
    }

    public void setPageId(int pageId){
        this.pageId = pageId;
    }

    public String getWord(){
        return this.word;
    }

    public void setWord(String word){
        this.word = word;
    }

    public double getTf(){
        return this.tf;
    }

    public void setTf(double tf){
        this.tf = tf;
    }

    public double getTfidf(){
        return this.tfidf;
    }

    public void setTfidf(double tfidf){
        this.tfidf = tfidf;
    }

    public void setCnt(double cnt){
        this.cnt = cnt;
    }

    public double getCnt(){
        return this.cnt;
    }

    /**
     * DB에 있는 docword 테이블에 대한 구조
     * @return
     */
    public static StructType getStructType(){
        return new StructType(new StructField[]{
                new StructField("id", DataTypes.LongType, false, Metadata.empty()),
                new StructField("pageid", DataTypes.IntegerType, false, Metadata.empty()),
                new StructField("word", DataTypes.StringType, false, Metadata.empty()),
                new StructField("cnt", DataTypes.DoubleType, false, Metadata.empty()),
                new StructField("tf", DataTypes.DoubleType, false, Metadata.empty()),
                new StructField("tfidf", DataTypes.DoubleType, false, Metadata.empty())
        });
    }

    public Row wordToRow(){
        return RowFactory.create(id, pageId, word, cnt, tf, tfidf);
    }
}
