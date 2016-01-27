package com.gammainfo.qipei.convert;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.gammainfo.qipei.model.Image;
import com.gammainfo.qipei.model.Product;

public class ProductJSONConvert {

	public static ArrayList<Product> convertJsonArrayToItemList(
			JSONArray jsonArray) throws JSONException {
		int length = jsonArray.length();
		ArrayList<Product> list = new ArrayList<Product>();
		for (int i = 0; i < length; i++) {
			list.add(convertJsonToItem(jsonArray.getJSONObject(i)));
		}
		return list;
	}

	public static Product convertJsonToItem(JSONObject json)
			throws JSONException {
		Product product = new Product();
		product.setId(json.optInt("id"));
		product.setCompanyName(json.optString("company_name"));
		product.setProductName(json.optString("product_name"));
		product.setProductInfo(json.optString("product_info"));
		product.setBrief(json.optString("brief"));
		product.setBrand(json.optString("brand"));
		product.setThumb(json.optString("thumb"));
		product.setPv(json.optInt("pv"));
		product.setHotNum(json.optInt("hot_num"));
		product.setFavoriteNum(json.optInt("favorite_num"));
		product.setProperty(json.optString("property"));
		try {
			product.setImgs(ImageJSONConvert.convertJsonArrayToItemList(json
					.optJSONArray("imgs")));
		} catch (JSONException e) {
			product.setImgs(new ArrayList<Image>());
		}

		return product;
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
			img.setProductId(json.optInt("product_id"));
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
			json.put("product_id", image.getProductId());
			json.put("url", image.getUrl());
			json.put("create_time", image.getCreateTime() / 1000);
			json.put("described", image.getDescribed());
			return json;
		} catch (JSONException e) {
		}
		return null;
	}

	public static JSONObject convertProduct(Product product) {
		JSONObject json = new JSONObject();
		try {
			json.put("id", product.getId());
			json.put("company_name", product.getCompanyName());
			json.put("product_name", product.getProductName());
			json.put("product_info", product.getProductInfo());
			json.put("brief", product.getBrief());
			json.put("brand", product.getBrand());
			json.put("thumb", product.getThumb());
			json.put("pv", product.getPv());
			json.put("favorite_num", product.getFavoriteNum());
			json.put("hot_num", product.getHotNum());
			json.put("property", product.getProperty());
			json.put("imgs", convertImageArray(product.getImgs()));
			return json;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
}
