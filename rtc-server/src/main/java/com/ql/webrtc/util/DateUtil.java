package com.ql.webrtc.util;

import java.util.Date;

/**
 * @author qinlei
 * @description todo
 * @date 2021/6/9 14:32
 */
public class DateUtil {

    /**
     * 获取精确到秒的时间戳
     *
     * @return
     */
    public static int getSecondTimestamp(Date date) {
        if (null == date) {
            return 0;
        }
        String timestamp = String.valueOf(date.getTime());
        int length = timestamp.length();
        if (length > 3) {
            return Integer.valueOf(timestamp.substring(0, length - 3));
        } else {
            return 0;
        }
    }
}
