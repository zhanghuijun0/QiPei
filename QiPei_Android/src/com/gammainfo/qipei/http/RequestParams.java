package com.gammainfo.qipei.http;

import java.util.Map;

import com.gammainfo.qipei.db.Preferences;
import com.gammainfo.qipei.utils.Constant;

public class RequestParams extends com.loopj.android.http.RequestParams {

	public RequestParams() {
		super();
		put(Constant.JSON_KEY_ACCESS_FORMAT, "json");
		put("device_id", Preferences.getDeviceId());
		put(Constant.JSON_KEY_V, "1.0");
		put("app_name", "Api");
		put("platform", "1");
		put("terminal_version", Preferences.getVersionName());
	}

	public RequestParams(Map<String, String> arg0) throws Exception {
		throw new Exception("不支持");
	}

	public RequestParams(Object... arg0) throws Exception {
		throw new Exception("不支持");
	}

	public RequestParams(String arg0, String arg1) throws Exception {
		throw new Exception("不支持");
	}

}
