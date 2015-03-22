/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package me.zheteng.cbreader.utils;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

/**
 * TODO 记得添加注释
 */
public class TimeUtils {
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
    public static String getElapsedTime(long ago) {

        DateTime thenTime = new DateTime(ago);
        DateTime now = new DateTime();
        Period period = new Period(thenTime, now);


        String elapsed = formatter.print(period).split("\\|")[0];

        return elapsed;

    }
}
