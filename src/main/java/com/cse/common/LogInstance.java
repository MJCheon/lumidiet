package com.cse.common;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by bullet on 16. 9. 8.
 */
public class LogInstance {
    private static final String PRODUCTION_LOG = "PRODUCTION_LOGGER";
    private static final String DEBUG_LOG = "DEBUG_LOOGER";
    public static final boolean isDebug = true;

    /**
     * Logger 싱글톤 메소드
     * @return Logger
     */
    public static Logger getLogger(){
        if(isDebug)
            return LogManager.getLogger(DEBUG_LOG);
        else
            return LogManager.getLogger(PRODUCTION_LOG);
    }
}
