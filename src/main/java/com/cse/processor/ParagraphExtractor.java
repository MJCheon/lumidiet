package com.cse.processor;

import com.clearspring.analytics.util.Lists;
import com.cse.common.Common;
import com.cse.entity.Page;
import com.cse.entity.Paragraph;
import com.cse.entity.Word;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.FlatMapFunction;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;

/**
 * Created by bullet on 16. 9. 8.
 * Page에서 문장 추출
 */
public class ParagraphExtractor implements Serializable {

    private JavaRDD<Page> pageRDD;

    public ParagraphExtractor(JavaRDD<Page> pageRDD){
        this.pageRDD = pageRDD;
    }

    /**
     * Page를 문장별로 분리
     * "." , "?", "!"를 이용한 Split
     * @return paragraphRDD
     */
    public JavaRDD<Paragraph> pageRddToParagraphRDD(){
        return pageRDD.flatMap(page->{
            String body = page.getBody();
            int id = page.getId();

            body = body.replaceAll("\n", "");
            List<Paragraph> paragraphList = Lists.newArrayList();

            String[] paragraphArr = body.split("\\. |\\? |! ");

            for(int i=0; i<paragraphArr.length; i++){
                String s = paragraphArr[i];
                paragraphList.add(new Paragraph(id, s));
            }

            return paragraphList;
        });
    }
}
