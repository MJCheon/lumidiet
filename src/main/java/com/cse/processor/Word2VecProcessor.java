package com.cse.processor;

import com.cse.common.Common;
import com.cse.entity.Paragraph;
import com.cse.entity.Word;
import com.cse.spark.Spark;
import com.cse.spark.SparkJDBC;
import com.twitter.penguin.korean.KoreanPosJava;
import com.twitter.penguin.korean.KoreanTokenJava;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.ml.feature.Word2Vec;
import org.apache.spark.ml.feature.Word2VecModel;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.types.*;
import org.apache.spark.storage.StorageLevel;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * Created by bullet on 16. 9. 8.
 * 단어들을 Word2Vec 모델 학습 및 저장
 */
public class Word2VecProcessor implements Serializable {
    private JavaRDD<Paragraph> paragraphRDD;
    private static StructType schema;
    private Word2Vec word2Vec;
    private static final int MINIMUM_LINE_LENGTH = 5;
    private static final int MINIMUM_WORD_CNT = 2;

    static{
        schema = new StructType(new StructField[]{
            new StructField("text", new ArrayType(DataTypes.StringType, true), false, Metadata.empty())
        });
    }

    public Word2VecProcessor(JavaRDD<Paragraph> paragraphRDD){
        this.paragraphRDD = paragraphRDD;
    }

    /**
     * 문장에서 단어를 추출
     * 문장이 최소 길이가 5 이하인 경우 학습 데이터에서 제외
     * @param line 문장
     * @return
     */
    private String[] lineToWordArray(String line){
        if(line.length() < MINIMUM_LINE_LENGTH)
            return new String[0];

        List<String> wordList = WordExtractor.extractWordList(line);
        String str = "";

        if(wordList==null || wordList.size()==0)
            return new String[0];

        for(String string : wordList){
            str += string+" ";
        }

        return str.split(" ");
    }

    /**
     * 문장에서의 단어가 2개 이하인 경우 학습 데이터에서 제외
     * @return
     */
    private JavaRDD<Row> paragraphRddToRowRDD(){
        return paragraphRDD.map(paragraph->{
            return lineToWordArray(paragraph.getLine());
        }).filter(strArr->{
            if(strArr.length < MINIMUM_WORD_CNT)
                return false;
            else
                return true;
        }).map(strArr->{
            return RowFactory.create(Arrays.asList(strArr));
        });
    }

    public void learn() throws IOException{
        DataFrame stemmedLineDataSet = SparkJDBC.getSQLContext().createDataFrame(paragraphRddToRowRDD(), schema);
        stemmedLineDataSet.cache();
        stemmedLineDataSet.count();
        word2Vec = new Word2Vec().setNumPartitions(Spark.NUM_CORE).setInputCol("text").setOutputCol("result")
                .setVectorSize(200).setMaxIter(30).setWindowSize(5).setMinCount(40);
        Word2VecModel word2VecModel = word2Vec.setNumPartitions(Spark.NUM_CORE).fit(stemmedLineDataSet);
        word2VecModel.save("./w2model");
        stemmedLineDataSet.unpersist();
        this.paragraphRDD.unpersist();
    }
}
