/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package me.zheteng.cbreader.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.view.View;

/**
 * 和界面有关的工具类
 */
public class UIUtils {
    /**
     * 在给定的图片的右上角加上联系人数量。数量用红色表示
     *
     * @param icon 给定的图片
     *
     * @return 带联系人数量的图片
     */
    public static Bitmap generatorCountIcon(Context context, Bitmap icon, int count) {
        // 初始化画布
        int width = icon.getWidth();
        int height = icon.getHeight();
        Bitmap contactIcon = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(contactIcon);

        // 拷贝图片
        Paint iconPaint = new Paint();
        iconPaint.setDither(true);// 防抖动
        iconPaint.setFilterBitmap(true);// 用来对 Bitmap 进行滤波处理，这样，当你选择 Drawable 时，会有抗锯齿的效果
        Rect src = new Rect(0, 0, width, height);
        Rect dst = new Rect(0, 0, width, height);
        canvas.drawBitmap(icon, src, dst, iconPaint);

        // 启用抗锯齿和使用设备的文本字距
        Paint countPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DEV_KERN_TEXT_FLAG);
        countPaint.setColor(Color.RED);
        countPaint.setTextSize(spToPixels(context, 10f));
        countPaint.setTypeface(Typeface.DEFAULT_BOLD);
        canvas.drawText(String.valueOf(count), width - 18, 25, countPaint);
        return contactIcon;
    }

    public static float spToPixels(Context context, float sp) {
        float scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
        return sp * scaledDensity;
    }

    public static float dpToPixels(Context context, float dp) {
        float scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
        return dp * scaledDensity;
    }

    public static void setAccessibilityIgnore(View view) {
        view.setClickable(false);
        view.setFocusable(false);
        view.setContentDescription("");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        }
    }
}
