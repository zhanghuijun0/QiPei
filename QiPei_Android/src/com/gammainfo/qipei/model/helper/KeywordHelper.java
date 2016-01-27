package com.gammainfo.qipei.model.helper;

import java.util.ArrayList;

import android.content.ContentValues;
import android.database.Cursor;

import com.gammainfo.qipei.db.DatabaseManager;

public final class KeywordHelper {
	private static final String TABLE_NAME = "search_history";
	private static final String COL_ID = "id";
	private static final String COL_KEYWORD = "keyword";

	/**
	 * 获取关键字列表
	 * 
	 * @param keyword
	 * @return 包括指定关键字的列表，如果{@keyword}为null或空串，则获取全部关键字列表
	 */
	public static ArrayList<String> select(String keyword) {
		String sql;
		if (keyword == null || keyword.equals("")) {
			sql = String.format("SELECT * FROM %s ORDER BY id DESC", TABLE_NAME);
		} else {
			sql = "SELECT * FROM " + TABLE_NAME + " where keyword LIKE '%"
					+ keyword + "%'  ORDER BY id DESC";
		}
		Cursor cursor = DatabaseManager.select(sql, null);
		ArrayList<String> list = new ArrayList<String>();
		if (cursor != null) {
			while (cursor.moveToNext()) {
				list.add(cursor.getString(cursor.getColumnIndex(COL_KEYWORD)));
			}
			cursor.close();
		}
		return list;
	}

	public static boolean insert(String keyword) {
		if(keyword==null){
			return false;
		}
		keyword = keyword.trim();
		if(keyword.length()==0){
			return false;
		}
		int id = exists(keyword);
		if (id == 0) {
			ContentValues values = new ContentValues();
			values.put(COL_KEYWORD, keyword);
			return DatabaseManager.insert(TABLE_NAME, values) > 0 ? true
					: false;
		}
		return true;
	}

	/**
	 * 判断keyword是否存在
	 * 
	 * @param keyword
	 * @return 如果存在返回其对应的id，否则返回0
	 */
	public static int exists(String keyword) {
		Cursor cursor = DatabaseManager.select(String.format(
				"select * from %s where keyword='%s'", TABLE_NAME, keyword),
				null);
		int id = 0;
		if (cursor != null) {
			if (cursor.moveToNext()) {
				id = cursor.getInt(cursor.getColumnIndex(COL_ID));
			}
			cursor.close();
		}
		return id;
	}

	public static boolean delete(int id) {
		return DatabaseManager.delete(TABLE_NAME, "id=" + id);
	}

	public static boolean clear() {
		return DatabaseManager.delete(TABLE_NAME, "1");
	}

}
