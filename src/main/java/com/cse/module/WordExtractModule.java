package com.cse.module;

import akka.io.Tcp;
import com.cse.common.Common;
import com.cse.common.LogInstance;
import com.cse.entity.Page;
import com.cse.entity.Paragraph;
import com.cse.entity.Word;
import com.cse.processor.ParagraphExtractor;
import com.cse.processor.WordExtractor;
import com.cse.spark.Spark;
import com.cse.spark.SparkJDBC;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.*;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;
import org.apache.spark.storage.StorageLevel;
import scala.Tuple2;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
/**
 * Created by bullet on 16. 10. 11.
 */
public class WordExtractModule implements Serializable {
    private JavaRDD<Page> pageRDD;
    private JavaRDD<Word> docWordRDD;

    public WordExtractModule(){
        initWordExtracModule();
    }

    /**
     * Page 테이블로부터 페이지번호와 본문 읽어들임
     */
    private void initWordExtracModule(){
        pageRDD = SparkJDBC.getSqlReader(SparkJDBC.TABLE_PAGE).select("id", "body").toJavaRDD()
                .repartition(Spark.NUM_CORE).map(row->{
            return new Page(row.getInt(0), row.getString(1));
        });
        pageRDD.cache();
        pageRDD.count();
    }

    /**
     * 본문으로부터 Word 객체 생성하여 단어 추출
     * TF-IDF 값 계산 후
     * docword 테이블에 저장
     */
    public void start(){
        JavaPairRDD<Integer, Iterable<Word>> wordPairRDD = pageRDD.flatMapToPair(page->{
            ArrayList<Tuple2<Integer, Word>> wordPairList = new ArrayList<Tuple2<Integer, Word>>();
            List<Word> wordList = WordExtractor.extractWordFromParagraph(page.getId(), page.getBody());

            if(wordList == null || wordList.size()==0)
                wordPairList.add(new Tuple2<>(Common.ERROR, new Word(Common.ERROR,"")));
            
            for (Word word : wordList) {
                if(word.getCnt()>1)
                    wordPairList.add(new Tuple2<>(word.getPageId(), word));
            }
            return wordPairList;
        }).filter(tuple->{
            if(tuple._1() == Common.ERROR)
                return false;
            else
                return true;
        }).groupByKey();

        wordPairRDD.cache();
        wordPairRDD.count();
        final long allDocCnt = wordPairRDD.keys().count();

        docWordRDD = wordPairRDD.flatMap(tuple->{
            ArrayList<Word> wordsList = new ArrayList<Word>();
            for(Word word : tuple._2()){
                Word newWord = new Word(word.getPageId(), word.getWord());
                newWord.setCnt(word.getCnt());
                double tf = Math.log(1+word.getTf());
                double idf = Math.log10((double)allDocCnt / word.getCnt());
                newWord.setTf(word.getTf());
                newWord.setTfidf(tf*idf);
                wordsList.add(newWord);
            }
            return wordsList;
        });

        docWordRDD.cache();
        docWordRDD.count();

        docWordRDD.foreachPartition(wordIterator->{
            saveDocWordTable(wordIterator);
        });

        pageRDD.unpersist();
        wordPairRDD.unpersist();
        docWordRDD.unpersist();
    }

    /**
     * docword 테이블에 저장
     * @param wordIterator
     */
    private void saveDocWordTable(Iterator<Word> wordIterator){
        Connection connection = null;
        try {
            connection = SparkJDBC.getMysqlConnection();
            int rowCnt = 0;
            String query = "INSERT INTO docword VALUES (?,?,?,?,?,?) ON DUPLICATE KEY UPDATE tf=?, tfidf=?";
            PreparedStatement statement = connection.prepareStatement(query);

            while(wordIterator.hasNext()) {
                Word docWord = wordIterator.next();
                try {
                    statement.setLong(1,docWord.getId());
                    statement.setInt(2,docWord.getPageId());
                    statement.setString(3, docWord.getWord());
                    statement.setDouble(4, docWord.getCnt());
                    statement.setDouble(5, docWord.getTf());
                    statement.setDouble(6, docWord.getTfidf());
                    statement.setDouble(7, docWord.getTf());
                    statement.setDouble(8, docWord.getTfidf());
                    statement.addBatch();
                    statement.clearParameters();
                    if(rowCnt<100) {
                        statement.executeBatch();
                        statement.clearBatch();
                        rowCnt = 0;
                    }
                    rowCnt++;
                }
                catch(Exception e){
                    LogInstance.getLogger().debug(e.getMessage());
                }
            }
            statement.executeBatch();
            statement.clearBatch();
            statement.close();
            connection.close();
        }
        catch(Exception e){
            LogInstance.getLogger().debug(e.getMessage());
        }
    }
}
