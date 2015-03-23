/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package me.zheteng.cbreader.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * 其他工具函数放这里
 */
public class Utils {
    public static <T> List<T> getListFromArray(T[] array) {
        List<T> list = new ArrayList<>(array.length);
        for (T item : array) {
            list.add(item);
        }
        return list;
    }

    public static <T> List<T> getListFromArrayReverse(T[] array) {
        List<T> list = new ArrayList<>(array.length);
        for (int i = array.length - 1; i >= 0; i--) {
            list.add(array[i]);
        }
        return list;
    }
}
