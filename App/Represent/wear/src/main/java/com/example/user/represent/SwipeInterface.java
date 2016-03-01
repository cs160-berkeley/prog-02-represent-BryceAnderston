package com.example.user.represent;

import android.view.View;

/**
 * Created by User on 2/27/2016.
 */
public interface SwipeInterface {
    public void bottom2top(View v);

    public void left2right(View v);

    public void right2left(View v);

    public void top2bottom(View v);

    // ListView Coding based off https://www.youtube.com/watch?v=WRANgDgM2Zg
    // Swiping Coding based off http://stackoverflow.com/questions/937313/fling-gesture-detection-on-grid-layout
}
