package com.gammainfo.qipei.utils;

public final class UrlAssert {

	private UrlAssert() {
	}

	public static boolean isUrl(String text) {
		if(text==null){
			return false;
		}
		
		if (text.startsWith("http://") || text.startsWith("https://")) {
			return true;
		}
		return false;
	}

}
