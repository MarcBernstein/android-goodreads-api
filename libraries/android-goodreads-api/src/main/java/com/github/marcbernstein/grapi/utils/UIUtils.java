package com.github.marcbernstein.grapi.utils;

import android.content.Context;

import com.github.marcbernstein.grapi.R;

public final class UIUtils {

    private UIUtils() {}

    public static boolean isSmallestWidthGreaterThan600dp(Context context) {
        return context.getResources().getBoolean(R.bool.sw600dp);
    }
}
