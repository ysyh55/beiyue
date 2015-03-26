/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package me.zheteng.cbreader.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.os.Environment;
import android.webkit.URLUtil;

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

    public static String getFileNameFromURL(String url) {
        String fileNameWithExtension = null;
        String fileNameWithoutExtension = null;
        if (URLUtil.isValidUrl(url)) {
            fileNameWithExtension = URLUtil.guessFileName(url, null, null);
            if (fileNameWithExtension != null && !fileNameWithExtension.isEmpty()) {
                String[] f = fileNameWithExtension.split(".");
                if (f != null & f.length > 1) {
                    fileNameWithoutExtension = f[0];
                }
            }
        }
        return fileNameWithExtension;
    }

    public static File getSaveImageDir() {
        String path = Environment.getExternalStorageDirectory().toString();
        File file = new File(path + "/CNBETANow/saved/");
        file.mkdirs();
        return file;
    }
}
