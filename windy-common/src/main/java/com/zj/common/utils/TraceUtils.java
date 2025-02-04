package com.zj.common.utils;

import org.slf4j.MDC;

import java.util.UUID;

public class TraceUtils {
    private static final String MDC_TID_KEY = "tid";

    public static void putTrace(String traceId) {
        MDC.put(MDC_TID_KEY, traceId);
    }
    public static void initTrace() {
        MDC.put(MDC_TID_KEY, UUID.randomUUID().toString().replace("-",""));
    }
    public static void removeTrace() {
        MDC.remove(MDC_TID_KEY);
    }

    public static String getTraceId() {
        return MDC.get(MDC_TID_KEY);
    }
    public static void startNextSpan(){
        String traceId = getTraceId();
        MDC.put(MDC_TID_KEY, traceId + "." + UUID.randomUUID().toString().replace("-",""));
    }
}
