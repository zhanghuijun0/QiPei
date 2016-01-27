package com.gammainfo.qipei.http;

import com.gammainfo.qipei.utils.Constant;
import com.loopj.android.http.AsyncHttpClient;

public class RestClient {

	private static AsyncHttpClient client = new AsyncHttpClient();

	public static void get(String url, RequestParams params,
			AsyncHttpResponseHandler responseHandler) {
		if (params == null) {
			params = new RequestParams();
		}
		if (Constant.DEBUG) {
			dump(url, params);
		}
		client.get(url, params, responseHandler);
	}

	public static void post(String url, RequestParams params,
			AsyncHttpResponseHandler responseHandler) {
		if (params == null) {
			params = new RequestParams();
		}
		if (Constant.DEBUG) {
			dump(url, params);
		}
		client.post(url, params, responseHandler);
	}

	public static void dump(String url, RequestParams params) {
		System.out.println("================== " + url
				+ " ========================\r\n " + params.toString());
	}
}
