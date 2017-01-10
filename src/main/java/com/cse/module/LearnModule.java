package com.cse.module;

import com.cse.entity.Page;
import com.cse.entity.Paragraph;
import com.cse.processor.ParagraphExtractor;
import com.cse.processor.Word2VecProcessor;
import com.cse.processor.WordExtractor;
import com.cse.spark.Spark;
import com.cse.spark.SparkJDBC;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.sql.Row;
import org.apache.spark.storage.StorageLevel;
import scala.xml.PrettyPrinter;

import java.io.IOException;
import java.io.Serializable;

/**
 * Created by bullet on 16. 10. 3.
 */
public class LearnModule implements Serializable {
    private JavaRDD<Paragraph> paragraphRDD;

    public LearnModule() {
        initLearnModule();
    }

    /**
     * DB로부터 News에 해당하는 Page를 읽어들인 후
     * Page로부터 문장 단위로 분해
     * 문장들에서 단어 추출 후 Word2Vec 모델에 학습
     */
    private void initLearnModule(){
        JavaRDD<Page> pageRDD = SparkJDBC.getSqlReader(SparkJDBC.TABLE_PAGE).select("id", "url", "body")
                .where("not url like '%blog.naver.com%'").toJavaRDD().repartition(Spark.NUM_CORE).map(row->{
            return new Page(row.getInt(0), row.getString(2));
        });

        pageRDD.cache();
        pageRDD.count();

        ParagraphExtractor paragraphExtractor = new ParagraphExtractor(pageRDD);
        this.paragraphRDD = paragraphExtractor.pageRddToParagraphRDD();
        pageRDD.unpersist();
        this.paragraphRDD.cache();
        this.paragraphRDD.count();
    }

    public void start() throws IOException {
        Word2VecProcessor word2VecProcessor = new Word2VecProcessor(this.paragraphRDD);
        word2VecProcessor.learn();
    }
}