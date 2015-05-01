package com.sarinregmi.gallery;

import android.content.Context;
import android.content.res.Resources;

/**
 * Created by sarinregmi on 4/28/15.
 */
 final class GridViewMetrics {

    public static int getImageWidth(Context context) {
        Resources resources =  context.getResources();
        int numberOfColumns =  getNumberOfColumns(context);
        int displayWidth = resources.getDisplayMetrics().widthPixels;
        int recylerViewMargin = 2 * resources.getDimensionPixelSize(R.dimen.activity_vertical_margin);
        int columnSpacing = 2 * numberOfColumns * resources.getDimensionPixelOffset(R.dimen.image_spacing_margin);
        int usuableSpace = displayWidth - recylerViewMargin - columnSpacing;
        return usuableSpace/numberOfColumns;
    }

    public static int getNumberOfColumns(Context context) {
        Resources resources =  context.getResources();
        return resources.getDisplayMetrics().widthPixels / resources.getDimensionPixelSize(R.dimen.rough_image_width);
    }

    public static int dpToPixels(int dp, Context context) {
        float logicalDensity = context.getResources().getDisplayMetrics().density;
        int px = (int) Math.ceil(dp * logicalDensity);
        return px;
    }

}
