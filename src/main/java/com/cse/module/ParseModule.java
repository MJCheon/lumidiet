package com.cse.module;

import com.cse.common.Common;
import com.cse.common.LogInstance;
import com.cse.entity.Page;
import com.cse.entity.Subject;
import com.cse.exception.PageNotFoundException;
import com.cse.network.NaverBlogAPI;
import com.cse.network.NaverNewsAPI;
import com.cse.spark.Spark;
import com.cse.spark.SparkJDBC;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.catalyst.plans.logical.Except;
import org.apache.spark.storage.StorageLevel;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by bullet on 16. 9. 8.
 */
public class ParseModule implements Serializable {
    private JavaRDD<Page> pageRDD;
    private JavaRDD<Subject> savedSubjectRDD;
    private static final int BODY_MAX_LENGTH = 50000;

    public ParseModule() {
        initParseModule();
    }

    private void initParseModule() {
        savedSubjectRDD = getSavedPageList();
        savedSubjectRDD.cache();
        savedSubjectRDD.count();
    }

    /**
     * DB로부터 저장되있는 Page에 대한 url, date를 읽어들임
     * @return
     */
    private JavaRDD<Subject> getSavedPageList() {
        return SparkJDBC.getSqlReader(SparkJDBC.TABLE_PAGE).select("url", "date").toJavaRDD()
                .repartition(Spark.NUM_CORE).map(row->{
            return new Subject(row.getString(0), row.getLong(1));
        });
    }

    /**
     * keyword로부터 관련 News와 Blog를 API로부터 얻어옴
     * DB에 저장되어 있지않는 Page에 대한 Parsing
     * (본문의 내용이 너무 긴 경우 제외)
     * DB에 저장
     * @param keyword - 학습과 관련된 검색 단어
     * @throws Exception
     */

    public void start(final String keyword) throws Exception {
        JavaRDD<Subject> subjectRDD = Spark.getJavaSparkContext().parallelize(Arrays.asList(keyword)).flatMap(string->{
            ArrayList<Subject> subjects = new ArrayList<Subject>();
            NaverNewsAPI naverNewsAPI = new NaverNewsAPI();
            NaverBlogAPI naverBlogAPI = new NaverBlogAPI();
            subjects.addAll(naverNewsAPI.getUrlList(string));
            subjects.addAll(naverBlogAPI.getUrlList(string));

            return subjects;
        });

        subjectRDD.cache();
        subjectRDD.count();

        pageRDD = subjectRDD.subtract(savedSubjectRDD).map(subject->{
            if(subject.getIsBlog())
                return new Page(subject.getUrl());
            else
                return new Page(subject.getUrl(), subject.getDate());
        }).filter(page->{
            if(page.getId() == Common.ERROR)
                return false;
            else if(page.getBody().contains("다이어트") && page.getBody().length() < BODY_MAX_LENGTH)
                return true;
            else
                return false;
        });

        pageRDD.cache();
        long pageCnt = pageRDD.count();


        if(pageCnt != 0)
            savedPageTable();
        else
            throw new PageNotFoundException();

        subjectRDD.unpersist();
        pageRDD.unpersist();
    }

    /**
     * DB에 Pasing된 Page 저장
     */
    private void savedPageTable() {
        JavaRDD<Row> pageRowRDD = pageRDD.map(page->{
            return page.pageToRow();
        });

        DataFrame pageDataFrame = SparkJDBC.getSQLContext().createDataFrame(pageRowRDD, Page.getStructType());
        try {
            SparkJDBC.saveDataFrame(pageDataFrame, SparkJDBC.TABLE_PAGE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

