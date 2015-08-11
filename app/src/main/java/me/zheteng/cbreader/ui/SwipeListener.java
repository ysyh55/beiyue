/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package me.zheteng.cbreader.ui;

import android.app.Activity;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;
import me.zheteng.cbreader.utils.UIUtils;

/**
 * TODO 记得添加注释
 */
public class SwipeListener {

    private static final int FLING_MIN_DISTANCE = 50;
    private static final int FLING_MIN_VELOCITY = 5;
    private static final int FLING_MIN_Y = 50; //超过这个数就当做有上下滑动, 单位为DP

    public interface OnSwipeListener {
        void onSwipeLeft();

        void onSwipeRight();
    }

    public static void setUpSwipeBack(final Activity activity, View view, final OnSwipeListener listener) {
        final float lingMinYInPixel = UIUtils.dpToPixels(activity, FLING_MIN_Y);
        if (listener == null) {
            return;
        }
        final GestureDetector mGestureDetector = new GestureDetector(activity, new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return false;
            }

            @Override
            public void onShowPress(MotionEvent e) {

            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                return false;
            }

            @Override
            public void onLongPress(MotionEvent e) {

            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (Math.abs(e1.getY() - e2.getY()) > lingMinYInPixel) {
                    // Fling up and down
                    return false;
                }
                if (e1.getX() - e2.getX() > UIUtils.dpToPixels(activity, FLING_MIN_DISTANCE)
                        && Math.abs(velocityX) > FLING_MIN_VELOCITY) {
                    // Fling left
                    Toast.makeText(activity, "向左手势", Toast.LENGTH_SHORT).show();
                    listener.onSwipeLeft();
                } else if (e2.getX() - e1.getX() > UIUtils.dpToPixels(activity, FLING_MIN_DISTANCE)
                        && Math.abs(velocityX) > FLING_MIN_VELOCITY) {
                    // Fling right
                    Toast.makeText(activity, "向右手势", Toast.LENGTH_SHORT).show();
                    listener.onSwipeRight();
                }
                return false;
            }
        });

        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return mGestureDetector.onTouchEvent(event);
            }
        });
    }
}
