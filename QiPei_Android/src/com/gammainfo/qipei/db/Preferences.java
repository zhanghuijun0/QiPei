package com.gammainfo.qipei.db;

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;

import com.gammainfo.qipei.Application;
import com.gammainfo.qipei.model.User;
import com.gammainfo.qipei.utils.Constant;

public final class Preferences {
	private static final String FIRST_STARTUP = "fiststartup";
	private static final String IS_PUSH_INFORMATION = "is_push_information";
	public static final String ACCESS_TOKEN = "accesstoken";
	private static final String GEO_LATITUDE = "geo_latitude";
	private static final String GEO_LONGITUDE = "geo_longitude";
	private static final String GEO_LATEST_TIMESTAMP = "geo_latesttimestamp";
	private static final String GEO_MY_CITY = "geo_mycity";

	private static final String GEO_MY_HISTORY_CITY = "geo_myhistorycity";
	private static final String ACCOUNT_LOGINNAME = "account_loginname";
	private static final String ACCOUNT_NICKNAME = "account_nickname";
	private static final String ACCOUNT_USER_ID = "user_id";
	private static final String ACCOUNT_REALNAME = "account_realname";
	private static final String ACCOUNT_MOBILE = "account_mobile";
	private static final String SYNC_TIME_OFFSET = "dynamicpwd_synctime_offset";

	private static final String LATEST_UPLOADING_PRODUCT = "last_waiting_product";
	private static final String LATEST_UPLOADING_SUPPLY = "last_waiting_supply";
	private static final String LATEST_UPLOADING_DEAMON = "last_waiting_deamon";

	private static SharedPreferences sSHARED_REFERENCES = null;
	private static Context sAPPLICATION_CONTEXT;
	private static String sDEVICE_ID;
	private static int sUSER_ID = -1;// 说明没有初始化
	private static String sVERSION_NAME;

	public Preferences() {
	}

	// setName(String name) {
	// editor.putBoolean("company_name", name);
	// }
	public static void init(Context context) {
		if (sSHARED_REFERENCES == null) {
			sAPPLICATION_CONTEXT = context.getApplicationContext();
			sSHARED_REFERENCES = PreferenceManager
					.getDefaultSharedPreferences(sAPPLICATION_CONTEXT);
		}
	}

	public static SharedPreferences getSharedPreferences() {
		return sSHARED_REFERENCES;
	}

	public static boolean isFirstStarup() {
		return sSHARED_REFERENCES.getBoolean(FIRST_STARTUP, true);
	}

	public static boolean setFirstStarup(boolean isFirstStartup) {
		Editor editor = sSHARED_REFERENCES.edit();
		editor.putBoolean(FIRST_STARTUP, isFirstStartup);
		return editor.commit();
	}

	/* 设置是否推送消息 */
	public static boolean setIsPushInformation(boolean isPushInformation) {
		Editor editor = sSHARED_REFERENCES.edit();
		editor.putBoolean(IS_PUSH_INFORMATION, isPushInformation);
		return editor.commit();
	}

	/* 得到是否推送消息 */
	public static boolean getIsPushInformation() {
		return sSHARED_REFERENCES.getBoolean(IS_PUSH_INFORMATION, true);
	}

	public static String getDeviceId() {
		if (sDEVICE_ID == null) {
			sDEVICE_ID = ((TelephonyManager) sAPPLICATION_CONTEXT
					.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
		}
		return sDEVICE_ID;
	}

	public static String getVersionName() {
		if (sVERSION_NAME == null) {
			try {
				PackageInfo info = sAPPLICATION_CONTEXT.getPackageManager()
						.getPackageInfo(sAPPLICATION_CONTEXT.getPackageName(),
								0);
				// 当前应用的版本名称
				sVERSION_NAME = info.versionName;
				// 当前版本的版本号
				// int versionCode = info.versionCode;

				// 当前版本的包名
				// String packageNames = info.packageName;
			} catch (NameNotFoundException e) {
			}
		}
		return sVERSION_NAME;
	}

	/**
	 * 获取登录用户的ID
	 * 
	 * @return 如果已登录返回用户ID，否则返回0
	 */
	public static int getAccountUserId() {
		if (sUSER_ID == -1) {
			sUSER_ID = sSHARED_REFERENCES.getInt("user_id", 0);
		}
		return sUSER_ID;
	}

	public static String getToken() {
		return sSHARED_REFERENCES.getString(ACCESS_TOKEN, null);
	}

	/**
	 * 获取当前登录的用户
	 * 
	 * @return 如果已经登录返回User，否则返回null
	 */
	public static User getAccountUser() {
		sUSER_ID = sSHARED_REFERENCES.getInt("user_id", 0);
		if (sUSER_ID == 0) {
			return null;
		}
		User user = new User();
		user.setId(sUSER_ID);
		user.setCompanyName(sSHARED_REFERENCES.getString("companyname", null));
		user.setAccount(sSHARED_REFERENCES.getString("account", null));
		user.setPhone(sSHARED_REFERENCES.getString("phone", null));
		user.setRoleId(sSHARED_REFERENCES.getInt("role_id", 0));
		user.setRoleName(sSHARED_REFERENCES.getString("role_name", null));
		user.setMemberGrade(sSHARED_REFERENCES.getInt("grade", 0));
		user.setProvince(sSHARED_REFERENCES.getString("province", null));
		user.setCity(sSHARED_REFERENCES.getString("city", null));
		user.setCounty(sSHARED_REFERENCES.getString("county", null));
		user.setAddress(sSHARED_REFERENCES.getString("address", null));
		user.setIntro(sSHARED_REFERENCES.getString("intro", null));
		user.setPhotoUrl(sSHARED_REFERENCES.getString("photo", null));
		user.setIsCertificated(sSHARED_REFERENCES.getBoolean("is_certificated",
				false));
		user.setIsMemberCertificated(sSHARED_REFERENCES.getBoolean(
				"is_member_certificated", false));
		user.setLongitude(sSHARED_REFERENCES.getFloat("longitude", 0));
		user.setLatitude(sSHARED_REFERENCES.getFloat("latitude", 0));
		user.setPV(sSHARED_REFERENCES.getInt("pv", 0));
		user.setProductNum(sSHARED_REFERENCES.getInt("product_num", 0));
		user.setFavoriteNum(sSHARED_REFERENCES.getInt("favorite_num", 0));
		user.setHotNum(sSHARED_REFERENCES.getInt("hot_num", 0));
		return user;
	}

	public static boolean setAccountUser(User user, String accessToken) {
		sUSER_ID = user.getId();
		Editor editor = sSHARED_REFERENCES.edit();
		editor.putString(ACCESS_TOKEN, accessToken);
		editor.putInt("user_id", sUSER_ID);
		editor.putString("companyname", user.getCompanyName());
		editor.putString("account", user.getAccount());
		editor.putString("phone", user.getPhone());
		editor.putInt("role_id", user.getRoleId());
		editor.putString("role_name", user.getRoleName());
		editor.putInt("grade", user.getMemberGrade());
		editor.putString("province", user.getProvince());
		editor.putString("city", user.getCity());
		editor.putString("county", user.getCounty());
		editor.putString("address", user.getAddress());
		editor.putString("intro", user.getIntro());
		editor.putString("photo", user.getPhotoUrl());
		editor.putBoolean("is_certificated", user.isCertificated());
		editor.putBoolean("is_member_certificated", user.isMemberCertificated());
		editor.putFloat("longitude", (float) (user.getLongitude()));
		editor.putFloat("latitude", (float) (user.getLatitude()));
		editor.putInt("pv", user.getPV());
		editor.putInt("product_num", user.getProductNum());
		editor.putInt("favorite_num", user.getFavoriteNum());
		editor.putInt("hot_num", user.getHotNum());
		return editor.commit();
	}

	public static boolean isLogin() {
		return getAccountUserId() > 0 ? true : false;
		// return sSHARED_REFERENCES.getString(ACCESS_TOKEN, null) == null
		// || sSHARED_REFERENCES.getInt(ACCOUNT_USER_ID, 0) == 0 ? false
		// : true;
	}

	public static long setSyncTimeOffset(long serverTime) {
		long offset = System.currentTimeMillis() - serverTime;
		Editor editor = sSHARED_REFERENCES.edit();
		editor.putLong(SYNC_TIME_OFFSET, offset);
		editor.commit();
		return offset;
	}

	public static long getSyncTimeOffset() {
		return sSHARED_REFERENCES.getLong(SYNC_TIME_OFFSET, 0);
	}

	public static boolean setMyLocation(double latitude, double longitude) {
		if (Constant.DEBUG) {
			System.out.println("----------------my location update(lat/lng) : "
					+ latitude + "/" + longitude);
		}
		Editor editor = sSHARED_REFERENCES.edit();
		editor.putFloat(GEO_LATITUDE, (float) latitude);
		editor.putFloat(GEO_LONGITUDE, (float) longitude);
		editor.putLong(GEO_LATEST_TIMESTAMP, System.currentTimeMillis());
		return editor.commit();
	}

	public static double getMyLatitude() {
		return sSHARED_REFERENCES.getFloat(GEO_LATITUDE, 0);
	}

	public static double getMyLongitude() {
		return sSHARED_REFERENCES.getFloat(GEO_LONGITUDE, 0);
	}

	public static long getMyLocationLatestTimestamp() {
		return sSHARED_REFERENCES.getLong(GEO_LATEST_TIMESTAMP, 0);
	}

	public static boolean setProvinceCityCounty(String province, String city,
			String county, String address) {
		Editor editor = sSHARED_REFERENCES.edit();
		editor.remove("province");
		editor.remove("city");
		editor.remove("county");
		editor.remove("address");
		editor.putString("province", province);
		editor.putString("city", city);
		editor.putString("county", county);
		editor.putString("address", address);
		return editor.commit();
	}

	public static boolean setMyCity(String city) {
		Editor editor = sSHARED_REFERENCES.edit();
		editor.putString(GEO_MY_CITY, city);
		Application.setCurrentCity(city);
		return editor.commit();
	}

	/**
	 * 从Application中获取我的城市
	 * 
	 * @return 如果存在返回城市名称，否则返回null
	 */
	public static String getMyCity() {
		return Application.getCurrentCity();
	}

	/**
	 * 从SHARED_REFERENCES文件中获取我的城市
	 * 
	 * @return 如果存在返回城市名称，否则返回null
	 */
	public static String getMyCityFromLocalFile() {
		return sSHARED_REFERENCES.getString(GEO_MY_CITY, null);
	}

	/**
	 * 获取我选择的城市历史记录（保存最近5个）
	 * 
	 * @return 如果没有选择过，返回null
	 */
	public static ArrayList<String> getMyHistoryCity() {
		String historyCity = sSHARED_REFERENCES.getString(GEO_MY_HISTORY_CITY,
				null);
		if (historyCity == null) {
			return null;
		}
		String[] historyCityArray = historyCity.split(",");
		ArrayList<String> historyCityList = new ArrayList<String>();
		for (String city : historyCityArray) {
			historyCityList.add(city);
		}
		return historyCityList;
	}

	public static boolean putCityInHistoryPool(String city) {
		ArrayList<String> historyCity = getMyHistoryCity();
		StringBuilder sbCity = new StringBuilder();
		sbCity.append(city);
		if (historyCity != null) {
			if (historyCity.contains(city)) {
				historyCity.remove(city);
			}
			int count = historyCity.size();
			count = count >= 6 ? 6 : count;
			for (int i = 0; i < count; i++) {
				sbCity.append(',').append(historyCity.get(i));
			}
		}

		Editor editor = sSHARED_REFERENCES.edit();
		editor.putString(GEO_MY_HISTORY_CITY, sbCity.toString());
		return editor.commit();
	}

	public static boolean loginOutClearUserInfo() {
		Editor editor = sSHARED_REFERENCES.edit();
		editor.remove(ACCESS_TOKEN);
		editor.remove("user_id");
		sUSER_ID = -1;
		editor.remove("companyname");
		editor.remove("account");
		editor.remove("phone");
		editor.remove("role_id");
		editor.remove("role_name");
		editor.remove("grade");
		editor.remove("province");
		editor.remove("city");
		editor.remove("county");
		editor.remove("address");
		editor.remove("intro");
		editor.remove("photo");
		editor.remove("is_certificated");
		editor.remove("is_member_certificated");
		editor.remove("longitude");
		editor.remove("latitude");
		editor.remove("pv");
		editor.remove("product_num");
		editor.remove("favorite_num");
		editor.remove("hot_num");
		editor.remove(GEO_MY_HISTORY_CITY);
		editor.remove(LATEST_UPLOADING_DEAMON);
		editor.remove(LATEST_UPLOADING_PRODUCT);
		editor.remove(LATEST_UPLOADING_SUPPLY);
		return editor.commit();
	}

	public static boolean saveUploadingSupply(String json) {
		Editor editor = sSHARED_REFERENCES.edit();
		editor.putString(LATEST_UPLOADING_SUPPLY, json);
		return editor.commit();
	}

	public static boolean saveUploadingDeamon(String json) {
		Editor editor = sSHARED_REFERENCES.edit();
		editor.putString(LATEST_UPLOADING_DEAMON, json);
		return editor.commit();
	}

	public static boolean saveUploadingProduct(String json) {
		Editor editor = sSHARED_REFERENCES.edit();
		editor.putString(LATEST_UPLOADING_PRODUCT, json);
		return editor.commit();
	}

	/**
	 * 获取将要发布的供应信息
	 * 
	 * @return 如果有需要发布的供应信息，则返回json格式的字符串，否则返回null
	 */
	public static String getUploadingSupply() {
		return sSHARED_REFERENCES.getString(LATEST_UPLOADING_SUPPLY, null);
	}

	/**
	 * 获取将要发布的求购信息
	 * 
	 * @return 如果有需要发布的求购信息，则返回json格式的字符串，否则返回null
	 */
	public static String getUploadingDeamon() {
		return sSHARED_REFERENCES.getString(LATEST_UPLOADING_DEAMON, null);
	}

	/**
	 * 获取将要发布的产品
	 * 
	 * @return 如果有需要发布的产品，则返回json格式的字符串，否则返回null
	 */
	public static String getUploadingProduct() {
		return sSHARED_REFERENCES.getString(LATEST_UPLOADING_PRODUCT, null);
	}
}
