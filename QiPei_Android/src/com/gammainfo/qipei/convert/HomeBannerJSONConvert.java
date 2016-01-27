package com.gammainfo.qipei.convert;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.gammainfo.qipei.model.HomeBanner;

public class HomeBannerJSONConvert {

	public static ArrayList<HomeBanner> convertJsonArrayToItemList(
			JSONArray jsonArray) throws JSONException {
		int length = jsonArray.length();
		ArrayList<HomeBanner> list = new ArrayList<HomeBanner>();
		for (int i = 0; i < length; i++) {
			list.add(convertJsonToItem(jsonArray.getJSONObject(i)));
		}

		return list;
	}

	public static HomeBanner convertJsonToItem(JSONObject json)
			throws JSONException {
		int id = json.getInt("id");
		String title = json.getString("title");
		String imgUrl = json.getString("image_url");
		String clickUrl = json.getString("click_url");
		int typeId = json.getInt("type_id");
		int type = json.getInt("type");
		long expires = json.getLong("expires");
		return new HomeBanner(id, title, type, typeId, imgUrl, clickUrl,
				expires * 1000);
	}
}
