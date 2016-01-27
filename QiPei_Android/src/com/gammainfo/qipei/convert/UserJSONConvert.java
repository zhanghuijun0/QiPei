package com.gammainfo.qipei.convert;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.gammainfo.qipei.model.User;

public class UserJSONConvert {
	public static ArrayList<User> convertJsonArrayToItemList(JSONArray jsonArray)
			throws JSONException {
		int length = jsonArray.length();
		ArrayList<User> list = new ArrayList<User>();
		for (int i = 0; i < length; i++) {
			list.add(convertJsonToItem(jsonArray.getJSONObject(i)));
		}

		return list;
	}

	public static User convertJsonToItem(JSONObject json) throws JSONException {
		User user = new User();
		user.setId(json.getInt("id"));
		user.setAccount(json.optString("account"));
		user.setCompanyName(json.optString("company_name"));
		user.setAddress(json.optString("address"));
		user.setPhone(json.optString("phone"));
		user.setIntro(json.optString("intro"));
		user.setRoleId(json.optInt("role_id"));
		// TODO 接口写好后，取消注释
		user.setRoleName(json.optString("role_name"));
		user.setMemberGrade(json.optInt("grade"));
		user.setProvince(json.optString("province"));
		user.setCity(json.optString("city"));
		user.setCounty(json.optString("county"));
		user.setPhotoUrl(json.optString("photo"));
		user.setIsCertificated(json.optInt("is_certificated") == 1 ? true
				: false);
		user.setIsMemberCertificated(json.optInt("is_member_certificated") == 1 ? true
				: false);
		user.setLatitude(json.optDouble("latitude"));
		user.setLongitude(json.optDouble("longitude"));
		user.setPV(json.optInt("pv"));
		user.setProductNum(json.optInt("product_num"));
		user.setFavoriteNum(json.optInt("favorite_num"));
		user.setHotNum(json.optInt("hot_num"));
		user.setIsFavorite(json.optInt("is_favorite") == 1 ? true : false);
		user.setDistance((float)json.optDouble("distance"));
		return user;
	}
}
