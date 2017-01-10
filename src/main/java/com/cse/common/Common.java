package com.cse.common;


import com.cse.spark.Spark;

import java.io.Serializable;

/**
 * Created by bullet on 16. 9. 28.
 */
public class Common {
    public static int ERROR = -1;
    private static int getSleepTime = 650 * (Spark.NUM_CORE/2);

    /**
     * Sleep 메소드
     */
    public static void sleep(){
        try {
            Thread.sleep(getSleepTime);
        } catch (Exception e) {

        }
    }
}
