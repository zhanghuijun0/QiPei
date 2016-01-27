package com.gammainfo.qipei.convert;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.gammainfo.qipei.model.Image;
import com.gammainfo.qipei.model.SupplyDemand;

public class SupplyDemandJSONConvert {

	public static ArrayList<SupplyDemand> convertJsonArrayToItemList(
			JSONArray jsonArray) throws JSONException {
		int length = jsonArray.length();
		ArrayList<SupplyDemand> list = new ArrayList<SupplyDemand>();
		for (int i = 0; i < length; i++) {
			list.add(convertJsonToItem(jsonArray.getJSONObject(i)));
		}
		return list;
	}

	public static SupplyDemand convertJsonToItem(JSONObject json)
			throws JSONException {
		SupplyDemand supplyDemand = new SupplyDemand();
		supplyDemand.setId(json.optInt("id"));
		supplyDemand.setUserId(json.optInt("user_id"));
		supplyDemand.setTitle(json.optString("title"));
		supplyDemand.setContent(json.optString("content"));
		supplyDemand.setType(json.optInt("type"));
		supplyDemand.setArea(json.optString("area"));
		supplyDemand.setUpdateTime((json.optLong("update_time") * 1000));
		supplyDemand.setIsFavorite(json.optInt("is_favorite") == 0 ? false
				: true);
		supplyDemand.setCompanyName(json.optString("company_name"));
		supplyDemand.setPhone(json.optString("phone"));
		supplyDemand.setThumb(json.optString("thumb"));
		try {
			supplyDemand.setImgs(ImageJSONConvert
					.convertJsonArrayToItemList(json.optJSONArray("imgs")));
		} catch (JSONException e) {
			supplyDemand.setImgs(new ArrayList<Image>());
		}
		return supplyDemand;
	}

	private static class ImageJSONConvert {
		public static ArrayList<Image> convertJsonArrayToItemList(
				JSONArray jsonArray) throws JSONException {
			if (jsonArray == null) {
				return new ArrayList<Image>();
			}
			int length = jsonArray.length();
			ArrayList<Image> list = new ArrayList<Image>();
			for (int i = 0; i < length; i++) {
				list.add(convertJsonToItem(jsonArray.getJSONObject(i)));
			}
			return list;
		}

		public static Image convertJsonToItem(JSONObject json)
				throws JSONException {
			Image img = new Image();
			img.setId(json.optInt("id"));
			img.setProductId(json.optInt("supply_demand_id"));
			img.setUrl(json.optString("url"));
			img.setCreateTime(json.optInt("create_time") * 1000);
			img.setDescribed(json.optString("described"));
			return img;
		}
	}

	public static JSONArray convertImageArray(ArrayList<Image> imageList)
			throws JSONException {
		JSONArray jsonArray = new JSONArray();
		if (imageList != null) {
			int length = imageList.size();
			for (int i = 0; i < length; i++) {
				jsonArray.put(convertImage(imageList.get(i)));
			}
		}
		return jsonArray;
	}

	public static JSONObject convertImage(Image image) {
		try {
			JSONObject json = new JSONObject();
			json.put("id", image.getId());
			json.put("supply_demand_id", image.getProductId());
			json.put("url", image.getUrl());
			json.put("create_time", image.getCreateTime() / 1000);
			json.put("described", image.getDescribed());
			return json;
		} catch (JSONException e) {
		}
		return null;
	}

	public static JSONObject convertSupplyDemand(SupplyDemand supplyDemand) {
		JSONObject json = new JSONObject();
		try {
			json.put("id", supplyDemand.getId());
			json.put("user_id", supplyDemand.getUserId());
			json.put("company_name", supplyDemand.getCompanyName());
			json.put("phone", supplyDemand.getPhone());
			json.put("title", supplyDemand.getTitle());
			json.put("content", supplyDemand.getContent());
			json.put("thumb", supplyDemand.getThumb());
			json.put("type", supplyDemand.getType());
			json.put("area", supplyDemand.getArea());
			json.put("update_time", supplyDemand.getUpdateTime() / 1000);
			json.put("is_favorite", supplyDemand.getIsFavorite() ? 1 : 0);
			json.put("imgs", convertImageArray(supplyDemand.getImgs()));
			return json;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
}
