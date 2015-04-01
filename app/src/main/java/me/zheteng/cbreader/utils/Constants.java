/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package me.zheteng.cbreader.utils;

import java.util.regex.Pattern;

/**
 * 记录些全局常量
 */
public class Constants {
    public static final Pattern SN_PATTERN = Pattern.compile("SN:\"(.{5})\"");
}
