package com.github.marcbernstein.grapi.utils;

public class StringUtils {

	public static boolean isEmpty(String str) {
		return str == null || str.trim().length() == 0;
	}

	public static boolean isNotEmpty(String str) {
		return str != null && str.trim().length() != 0;
	}

}
