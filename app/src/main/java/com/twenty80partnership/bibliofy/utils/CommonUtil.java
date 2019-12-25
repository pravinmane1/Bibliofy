package com.twenty80partnership.bibliofy.utils;

import android.graphics.drawable.GradientDrawable;

public class CommonUtil {

    public static GradientDrawable setRoundedCorner(int bgColor, int stroke, int radius, int shape){
        GradientDrawable gd = new GradientDrawable();
        gd.setCornerRadius(radius);
        gd.setColor(bgColor);
        gd.setStroke(1,stroke);
        gd.setShape(shape);
        return gd;
    }


}
