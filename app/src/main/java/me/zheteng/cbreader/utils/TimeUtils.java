/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package me.zheteng.cbreader.utils;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

/**
 * 时间工具类
 */
public class TimeUtils {
    public static final DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    private static PeriodFormatter formatter = new PeriodFormatterBuilder()
            .appendYears().appendSuffix("年前|")
            .appendMonths().appendSuffix("个月前|")
            .appendWeeks().appendSuffix("周前|")
            .appendDays().appendSuffix("天前|")
            .appendHours().appendSuffix("小时前|")
            .appendMinutes().appendSuffix("分钟前|")
            .appendSeconds().appendSuffix("秒前|")
            .printZeroNever()
            .toFormatter();
    public static String getEllapseTime(long ago) {

        DateTime thenTime = new DateTime(ago);
        DateTime now = new DateTime();
        Period period = new Period(thenTime, now);

        String[] all = formatter.print(period).split("\\|");
        if (all.length == 0) {
            return "未知时间";
        }

        return all.length == 1 ? all[0] : all[0] + all[1];

    }
}
