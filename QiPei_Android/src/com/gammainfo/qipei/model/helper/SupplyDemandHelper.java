package com.gammainfo.qipei.model.helper;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.gammainfo.qipei.db.DatabaseManager;
import com.gammainfo.qipei.model.SupplyDemand;

public class SupplyDemandHelper {
	private static final String TABLE_NAME = "supply_demand";

	private static final String COL_ID = "id";
	private static final String COL_TITLE = "title";
	private static final String COL_CONTENT = "content";
	private static final String COL_USER_ID = "user_id";
	private static final String COL_PHONE = "phone";
	private static final String COL_COMPANY_NAME = "company_name";
	private static final String COL_TYPE = "type";
	private static final String COL_AREA = "area";
	private static final String COL_UPDATE_TIME = "update_time";
	private static final String COL_FAVORITE = "is_favorite";

	public static ArrayList<SupplyDemand> select(int selectType) {
		ArrayList<SupplyDemand> rlt = new ArrayList<SupplyDemand>();
		Cursor cursor = DatabaseManager.select(String.format(
				"SELECT * FROM %s where type = '%d' order by update_time desc", TABLE_NAME, selectType),
				null);
		if (cursor != null) {
			while (cursor.moveToNext()) {
				SupplyDemand supplyDemand = new SupplyDemand();
				supplyDemand
						.setId(cursor.getInt(cursor.getColumnIndex(COL_ID)));
				supplyDemand.setTitle(cursor.getString(cursor
						.getColumnIndex(COL_TITLE)));
				supplyDemand.setContent(cursor.getString(cursor
						.getColumnIndex(COL_CONTENT)));
				supplyDemand.setUserId(cursor.getInt(cursor
						.getColumnIndex(COL_USER_ID)));
				supplyDemand.setPhone((cursor.getString(cursor
						.getColumnIndex(COL_PHONE))));
				supplyDemand.setCompanyName((cursor.getString(cursor
						.getColumnIndex(COL_COMPANY_NAME))));
				supplyDemand.setType((cursor.getInt(cursor
						.getColumnIndex(COL_TYPE))));
				supplyDemand.setArea(cursor.getString(cursor
						.getColumnIndex(COL_AREA)));
				supplyDemand.setUpdateTime(cursor.getInt(cursor
						.getColumnIndex(COL_UPDATE_TIME)));

				supplyDemand.setIsFavorite(cursor.getInt(cursor
						.getColumnIndex(COL_FAVORITE)) == 0 ? false : true);
				rlt.add(supplyDemand);
			}
			cursor.close();
		}
		return rlt;
	}

	public static void insert(Context context,
			List<SupplyDemand> supplyDemandList) {
		ContentValues values = new ContentValues();
		//DatabaseManager.getDatabase(context).beginTransaction();
		for (SupplyDemand supplyDemand : supplyDemandList) {
			values.clear();
			values.put(COL_ID, supplyDemand.getId());
			values.put(COL_TITLE, supplyDemand.getTitle());
			values.put(COL_CONTENT, supplyDemand.getContent());
			values.put(COL_USER_ID, supplyDemand.getUserId());
			values.put(COL_PHONE, supplyDemand.getPhone());
			values.put(COL_COMPANY_NAME, supplyDemand.getCompanyName());
			values.put(COL_TYPE, supplyDemand.getType());
			values.put(COL_AREA, supplyDemand.getArea());
			values.put(COL_UPDATE_TIME, supplyDemand.getUpdateTime());
			values.put(COL_FAVORITE, supplyDemand.getIsFavorite() ? 1 : 0);
			DatabaseManager.insert(TABLE_NAME, values);
		}
//		DatabaseManager.getDatabase(context).setTransactionSuccessful();
//		DatabaseManager.getDatabase(context).endTransaction();
	}

	/* 清除上一次的缓存 */
	public static boolean deleteBefore(int type) {
		if (delete("type = '" + type + "'")) {
			return true;
		}
		return false;
	}

	public static boolean delete(String whereClause) {
		return DatabaseManager.delete(TABLE_NAME, whereClause);
	}
}
