package com.gammainfo.qipei.model.helper;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import com.gammainfo.qipei.db.DatabaseManager;
import com.gammainfo.qipei.model.User;
import com.gammainfo.qipei.widget.SearchView;

public final class UserHelper {
	private static final String TABLE_NAME = "USER";
	private static final String COL_ID = "id";
	private static final String COL_ACCOUNT = "account";
	private static final String COL_PHONE = "phone";
	private static final String COL_ROLE_ID = "role_id";
	private static final String COL_ROLE_NAME = "role_name";
	private static final String COL_GRADE = "grade";
	private static final String COL_COMPANY_NAME = "company_name";
	private static final String COL_PROVINCE = "province";
	private static final String COL_CITY = "city";
	private static final String COL_COUNTY = "county";
	private static final String COL_ADDRESS = "address";
	private static final String COL_INTRO = "intro";
	private static final String COL_PHOTO = "photo";
	private static final String COL_IS_CERTIFICATED = "is_certificated";
	private static final String COL_IS_MEMEBER_CERTIFICATED = "is_member_certificated";
	private static final String COL_LONGITUDE = "longitude";
	private static final String COL_LATITUDE = "latitude";
	private static final String COL_PV = "pv";
	private static final String COL_FAVORITE_NUM = "favorite_num";
	private static final String COL_HOT_NUM = "hot_num";

	public static ArrayList<User> select(int roleId) {
		Cursor cursor = null;
		if(roleId == SearchView.SEARCH_TYPE_ENDUSER_QIXIUCHANG||roleId == SearchView.SEARCH_TYPE_ENDUSER||roleId == SearchView.SEARCH_TYPE_ENDUSER_4SDIAN||roleId == SearchView.SEARCH_TYPE_ENDUSER_KUAIXINDIAN||roleId == SearchView.SEARCH_TYPE_ENDUSER_MEIRONGDIAN) {
			cursor = DatabaseManager.select(String.format(
					"SELECT * FROM %s where role_id in ('%d','%d','%d','%d') order by hot_num desc", TABLE_NAME, SearchView.SEARCH_TYPE_ENDUSER_QIXIUCHANG,SearchView.SEARCH_TYPE_ENDUSER_4SDIAN,SearchView.SEARCH_TYPE_ENDUSER_MEIRONGDIAN,SearchView.SEARCH_TYPE_ENDUSER_KUAIXINDIAN),
					null);
		}else {
			cursor = DatabaseManager.select(String.format(
					"SELECT * FROM %s where role_id = '%d' order by hot_num desc", TABLE_NAME, roleId),
					null);
		}
		ArrayList<User> rlt = new ArrayList<User>();
		
		if (cursor != null) {
			while (cursor.moveToNext()) {
				User user = new User();
				user.setAccount(cursor.getString(cursor
						.getColumnIndex(COL_ACCOUNT)));
				user.setAddress(cursor.getString(cursor
						.getColumnIndex(COL_ADDRESS)));
				user.setCity(cursor.getString(cursor.getColumnIndex(COL_CITY)));
				user.setCompanyName(cursor.getString(cursor
						.getColumnIndex(COL_COMPANY_NAME)));
				user.setCounty(cursor.getString(cursor
						.getColumnIndex(COL_COUNTY)));
				user.setFavoriteNum(cursor.getInt(cursor
						.getColumnIndex(COL_FAVORITE_NUM)));
				user.setMemberGrade(cursor.getInt(cursor
						.getColumnIndex(COL_GRADE)));
				user.setHotNum(cursor.getInt(cursor.getColumnIndex(COL_HOT_NUM)));
				user.setId(cursor.getInt(cursor.getColumnIndex(COL_ID)));
				user.setIntro(cursor.getString(cursor.getColumnIndex(COL_INTRO)));
				user.setIsCertificated(cursor.getInt(cursor
						.getColumnIndex(COL_IS_CERTIFICATED)) == 0 ? false
						: true);
				user.setIsMemberCertificated(cursor.getInt(cursor
						.getColumnIndex(COL_IS_MEMEBER_CERTIFICATED)) == 0 ? false
						: true);
				user.setLatitude(cursor.getDouble(cursor
						.getColumnIndex(COL_LATITUDE)));
				user.setLongitude(cursor.getDouble(cursor
						.getColumnIndex(COL_LONGITUDE)));
				user.setPhone(cursor.getString(cursor.getColumnIndex(COL_PHONE)));
				user.setPhotoUrl(cursor.getString(cursor
						.getColumnIndex(COL_PHOTO)));
				user.setProvince(cursor.getString(cursor
						.getColumnIndex(COL_PROVINCE)));
				user.setPV(cursor.getInt(cursor.getColumnIndex(COL_PV)));
				user.setRoleId(cursor.getInt(cursor.getColumnIndex(COL_ROLE_ID)));
				user.setRoleName(cursor.getString(cursor
						.getColumnIndex(COL_ROLE_NAME)));
				rlt.add(user);
			}
			cursor.close();
		}
		return rlt;
	}

	public static void insert(Context context, List<User> userList) {
		ContentValues values = new ContentValues();
		for (User user : userList) {
			values.clear();
			values.put(COL_ID, user.getId());
			values.put(COL_ACCOUNT, user.getAccount());
			values.put(COL_ADDRESS, user.getAddress());
			values.put(COL_CITY, user.getCity());
			values.put(COL_COMPANY_NAME, user.getCompanyName());
			values.put(COL_COUNTY, user.getCounty());
			values.put(COL_FAVORITE_NUM, user.getFavoriteNum());
			values.put(COL_GRADE, user.getMemberGrade());
			values.put(COL_HOT_NUM, user.getHotNum());
			values.put(COL_ID, user.getId());
			values.put(COL_INTRO, user.getIntro());
			values.put(COL_IS_CERTIFICATED, user.isCertificated());
			values.put(COL_IS_MEMEBER_CERTIFICATED, user.isMemberCertificated());
			values.put(COL_LATITUDE, user.getLatitude());
			values.put(COL_LONGITUDE, user.getLongitude());
			values.put(COL_PHONE, user.getPhone());
			values.put(COL_PHOTO, user.getPhotoUrl());
			values.put(COL_PROVINCE, user.getProvince());
			values.put(COL_PV, user.getPV());
			values.put(COL_ROLE_ID, user.getRoleId());
			values.put(COL_ROLE_NAME, user.getRoleName());
			DatabaseManager.insert(TABLE_NAME, values);
		}
	}

	public static boolean exists(int id) {
		Cursor cursor = DatabaseManager.select("select * from user where id="
				+ id, null);
		if (cursor != null) {
			if (cursor.moveToNext()) {
				cursor.close();
				return true;
			}
		}
		return false;
	}

	/* 清除上一次的缓存 */
	public static boolean deleteBefore(int roleId) {
		if (delete("role_id = '" + roleId + "'")) {
			return true;
		}
		return false;
	}

	public static boolean delete(String whereClause) {
		return DatabaseManager.delete(TABLE_NAME, whereClause);
	}

	public static void asyncInsert(Context context, List<User> userList) {
		final Context fContext = context;
		final List<User> fUserList = userList;
		new Thread(new Runnable() {

			@Override
			public void run() {
				insert(fContext, fUserList);
			}
		}).start();
	}
}
