package com.gammainfo.qipei.model.helper;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.gammainfo.qipei.db.DatabaseManager;
import com.gammainfo.qipei.model.News;
import com.gammainfo.qipei.utils.Constant;

public final class NewsHelper {
	private static final String TABLE_NAME = "news";
	// id' , 'user_id' , 'title' , 'intro' , 'content' ,
	// 'create_time' ,'is_banner' , 'image_url' , 'type' ,
	// 'media_duration','page_url'
	private static final String COL_ID = "id";
	private static final String COL_TITLE = "title";
	private static final String COL_INTRO = "intro";
	private static final String COL_CREATE_TIME = "create_time";
	private static final String COL_IMAGE_URL = "image_url";
	private static final String COL_TYPE = "type";
	private static final String COL_MEDIA_DURATION = "media_duration";
	private static final String COL_MEDIA_URL = "media_url";
	private static final String COL_IS_BANNER = "is_banner";
	private static final String COL_PAGE_URL = "page_url";

	/**
	 * 从手机数据库查找数据
	 * 
	 * @param type
	 *            查询类别: 1:公司,2:行业,3:活动,4:媒体
	 * @return
	 */
	public static ArrayList<News> select(int type, int isBanner) {
		ArrayList<News> rlt = new ArrayList<News>();
		Cursor cursor = DatabaseManager.select("SELECT * FROM '" + TABLE_NAME
				+ "' where type = '" + type + "' AND is_banner = '" + isBanner
				+ "' order by create_time DESC ;", null);
		if (cursor != null) {
			while (cursor.moveToNext()) {
				News news = new News();
				news.setId(cursor.getInt(cursor.getColumnIndex(COL_ID)));
				news.setInfro(cursor.getString(cursor.getColumnIndex(COL_INTRO)));
				news.setTitle(cursor.getString(cursor.getColumnIndex(COL_TITLE)));
				news.setType(cursor.getInt(cursor.getColumnIndex(COL_TYPE)));
				news.setCreateTime(cursor.getInt(cursor
						.getColumnIndex(COL_CREATE_TIME)));
				news.setImageUrl(cursor.getString(cursor
						.getColumnIndex(COL_IMAGE_URL)));
				news.setMediaDuration(cursor.getInt(cursor
						.getColumnIndex(COL_MEDIA_DURATION)));
				news.setMediaUrl(cursor.getString(cursor
						.getColumnIndex(COL_MEDIA_URL)));
				news.setMediaUrl(cursor.getString(cursor
						.getColumnIndex(COL_PAGE_URL)));
				rlt.add(news);
			}
			cursor.close();
		} else {
			System.out.println("=====zhj=====查询数据====没有数据");
		}
		return rlt;
	}

	public static void insert(Context context, List<News> newsList, int isBanner) {
		ContentValues values = new ContentValues();
		// DatabaseManager.getDatabase(context).beginTransaction();
		for (News news : newsList) {
			values.clear();
			int id = news.getId();
			values.put(COL_INTRO, news.getInfro());
			values.put(COL_TITLE, news.getTitle());
			values.put(COL_TYPE, news.getType());
			values.put(COL_CREATE_TIME, news.getCreateTime());
			values.put(COL_IMAGE_URL, news.getImageUrl());
			values.put(COL_MEDIA_DURATION, news.getMediaDuration());
			values.put(COL_MEDIA_URL, news.getMediaUrl());
			values.put(COL_IS_BANNER, isBanner);
			values.put(COL_PAGE_URL, news.getPageUrl());
			if (!exists(id)) {
				values.put(COL_ID, id);
				DatabaseManager.insert(TABLE_NAME, values);
			} else {
				DatabaseManager.update(TABLE_NAME, values, "id = " + id);
			}
			// System.out.println("====zhj===插入数据===isBanner:" + isBanner);

		}
		// DatabaseManager.getDatabase(context).setTransactionSuccessful();
		// DatabaseManager.getDatabase(context).endTransaction();
	}

	public static boolean exists(int id) {
		Cursor cursor = DatabaseManager.select("select * from news where id="
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
	public static boolean deleteBefore(int type, int isBanner) {
		if (delete("type = '" + type + "'AND is_banner = '" + isBanner + "' ")) {
			if (Constant.DEBUG) {
				System.out.println("=====zhj=====删除数据(完毕)=======type：" + type
						+ ",isBanner=" + isBanner);
			}
			return true;
		}
		return false;
	}

	public static boolean delete(String whereClause) {
		return DatabaseManager.delete(TABLE_NAME, whereClause);
	}

	public static void asyncInsert(Context context, List<News> newsList,
			int isBanner, int selectType) {
		final Context fContext = context;
		final List<News> fNewsList = newsList;
		final int fisBanner = isBanner;
		final int fselectType = selectType;
		new Thread(new Runnable() {
			@Override
			public void run() {
				deleteBefore(fselectType, fisBanner);
				insert(fContext, fNewsList, fisBanner);
			}
		}).start();
	}
}
