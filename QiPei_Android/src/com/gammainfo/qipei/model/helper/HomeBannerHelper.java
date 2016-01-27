package com.gammainfo.qipei.model.helper;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.gammainfo.qipei.db.DatabaseManager;
import com.gammainfo.qipei.model.HomeBanner;

public final class HomeBannerHelper {
	private static final String TABLE_NAME = "home_banner";
	private static final String COL_ID = "id";
	private static final String COL_TITLE = "title";
	private static final String COL_IMAGE_URL = "image_url";
	private static final String COL_TYPE = "type";
	private static final String COL_TYPE_ID = "type_id";
	private static final String COL_CLICK_URL = "click_url";
	private static final String COL_EXPIRES_TIME = "expires_time";

	public static ArrayList<HomeBanner> select() {

		Cursor cursor = DatabaseManager
				.select(String
						.format("SELECT * FROM %s WHERE expires_time=0 or expires_time<=%s ORDER BY expires_time desc,id desc",
								TABLE_NAME, System.currentTimeMillis()), null);
		ArrayList<HomeBanner> list = new ArrayList<HomeBanner>();
		if (cursor != null) {
			while (cursor.moveToNext()) {
				int id = cursor.getInt(cursor.getColumnIndex(COL_ID));
				String title = cursor.getString(cursor
						.getColumnIndex(COL_TITLE));
				int type = cursor.getInt(cursor.getColumnIndex(COL_TYPE));
				int typeId = cursor.getInt(cursor.getColumnIndex(COL_TYPE_ID));
				String imgUrl = cursor.getString(cursor
						.getColumnIndex(COL_IMAGE_URL));
				String clickUrl = cursor.getString(cursor
						.getColumnIndex(COL_CLICK_URL));
				long expires = cursor.getLong(cursor
						.getColumnIndex(COL_EXPIRES_TIME));
				list.add(new HomeBanner(id, title, type, typeId, imgUrl,
						clickUrl, expires));
			}
			cursor.close();
		}
		return list;
	}

	public static boolean insert(HomeBanner homeBanner) {
		ContentValues values = new ContentValues();
		values.put(COL_CLICK_URL, homeBanner.getClickUrl());
		values.put(COL_EXPIRES_TIME, homeBanner.getExpires());
		values.put(COL_IMAGE_URL, homeBanner.getImgUrl());
		values.put(COL_TITLE, homeBanner.getTitle());
		values.put(COL_TYPE, homeBanner.getType());
		values.put(COL_TYPE_ID, homeBanner.getTypeId());
		return DatabaseManager.insert(TABLE_NAME, values) > 0 ? true : false;
	}

	public static boolean update(HomeBanner homeBanner) {
		ContentValues values = new ContentValues();
		values.put(COL_CLICK_URL, homeBanner.getClickUrl());
		values.put(COL_EXPIRES_TIME, homeBanner.getExpires());
		values.put(COL_IMAGE_URL, homeBanner.getImgUrl());
		values.put(COL_TITLE, homeBanner.getTitle());
		values.put(COL_TYPE, homeBanner.getType());
		values.put(COL_TYPE_ID, homeBanner.getTypeId());
		return DatabaseManager.update(TABLE_NAME, values,
				"id=" + homeBanner.getId());
	}

	/**
	 * 判断HomeBanner是否存在
	 * 
	 * @param id
	 */
	public static boolean exists(int id) {
		Cursor cursor = DatabaseManager.select(
				String.format("select * from %s where id=%s", TABLE_NAME, id),
				null);
		if (cursor != null) {
			if (cursor.moveToNext()) {
				cursor.close();
				return true;
			}
		}
		return false;
	}

	public static boolean delete(int id) {
		return DatabaseManager.delete(TABLE_NAME, "id=" + id);
	}

	public static boolean clear() {
		return DatabaseManager.delete(TABLE_NAME, "1");
	}

	public static void asyncInsert(Context context, ArrayList<HomeBanner> list) {
		final Context fContext = context;
		final List<HomeBanner> fList = list;
		new Thread(new Runnable() {

			@Override
			public void run() {
				// DatabaseManager.getDatabase(fContext).beginTransaction();
				clear();
				for (HomeBanner homeBanner : fList) {
					insert(homeBanner);
				}
				// DatabaseManager.getDatabase(fContext)
				// .setTransactionSuccessful();
				// DatabaseManager.getDatabase(fContext).endTransaction();
			}
		}).start();
	}
}
