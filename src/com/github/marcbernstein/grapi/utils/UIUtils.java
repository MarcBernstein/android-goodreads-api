package com.github.marcbernstein.grapi.utils;

import android.content.Context;

import com.marcbernstein.goodreadsapi.R;

public class UIUtils {

	public static boolean isSmallestWidthGreaterThan600dp(Context context) {
		return context.getResources().getBoolean(R.bool.sw600dp);
	}
}
