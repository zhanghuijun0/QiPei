package com.gammainfo.qipei.convert;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.gammainfo.qipei.model.News;
import com.gammainfo.qipei.model.NewsBanner;

public class NewsBannerJSONConvert {
	private static final String COL_ID = "id";
	private static final String COL_TITLE = "title";
	private static final String COL_IMAGE_URL = "image_url";
	private static final String COL_TYPE = "type";
	private static final String COL_EXPIRES = "expires";
	
	public static ArrayList<NewsBanner> convertJsonArrayToItemList(JSONArray jsonArray)
			throws JSONException {
		int length = jsonArray.length();
		ArrayList<NewsBanner> list = new ArrayList<NewsBanner>();
		for (int i = 0; i < length; i++) {
			list.add(convertJsonToItem(jsonArray.getJSONObject(i)));
		}
		return list;
	}

	public static NewsBanner convertJsonToItem(JSONObject json) throws JSONException {
		NewsBanner newsBanner = new NewsBanner();

		newsBanner.setId(Integer.valueOf(json.getString(COL_ID)));
		newsBanner.setImageUrl(json.getString(COL_IMAGE_URL));
		newsBanner.setTitle(json.getString(COL_TITLE));
		newsBanner.setType(Integer.valueOf(json.getString(COL_TYPE)));
		newsBanner.setExpires(json.getInt(COL_EXPIRES));
		
		return newsBanner;
	}
}
