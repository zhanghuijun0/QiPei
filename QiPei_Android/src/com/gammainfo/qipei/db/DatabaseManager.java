package com.gammainfo.qipei.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseManager {
	private static final String DATABASE_NAME = "qipei.sqlite";
	private static final int DATABASE_VERSION = 1;
	private static DatabaseManager sInstance;
	private static SQLiteDatabase sDatabase;

	public static DatabaseManager initialize(Context context) {
		if (sInstance == null) {
			sInstance = new DatabaseManager(context.getApplicationContext());
		}
		return sInstance;
	}

	public static void close() {
		if (sInstance != null && sDatabase.isOpen()) {
			sDatabase.close();
		}
		sInstance = null;
	}

	public synchronized static SQLiteDatabase getDatabase(Context context) {
		if (sDatabase == null) {
			sInstance = new DatabaseManager(context.getApplicationContext());
		}
		return sDatabase;
	}

	private DatabaseManager(Context context) {
		DatabaseHelper databaseHelper = new DatabaseHelper(context,
				DATABASE_NAME);
		sDatabase = databaseHelper.getWritableDatabase();
	}

	/**
	 * Sqlite insert query
	 * 
	 * @param table
	 * @param values
	 * @return boolean result
	 */
	public static long insert(String table, ContentValues values) {
		return sDatabase.insert(table, null, values);
	}

	/**
	 * Sqlite update query
	 * 
	 * @param table
	 * @param values
	 * @param whereClause
	 * @return boolean result
	 */
	public static boolean update(String table, ContentValues values,
			String whereClause) {
		return sDatabase.update(table, values, whereClause, null) > 0;
	}

	/**
	 * Sqlite delete query
	 * 
	 * @param table
	 * @param whereClause
	 * @return boolean result
	 */
	public synchronized static boolean delete(String table, String whereClause) {
		return sDatabase.delete(table, whereClause, null) > 0;
	}

	/**
	 * Sqlite select query
	 * 
	 * @param table
	 * @param columns
	 * @param selection
	 * @param selectionArgs
	 * @param groupBy
	 * @param having
	 * @param orderBy
	 * @param limit
	 * @return cursor
	 */
	public static Cursor select(String table, String[] columns,
			String selection, String[] selectionArgs, String groupBy,
			String having, String orderBy, String limit) {
		return sDatabase.query(table, columns, selection, selectionArgs,
				groupBy, having, orderBy, limit);
	}

	/**
	 * Sqlite select query
	 * 
	 * @param table
	 * @param columns
	 * @param selection
	 * @param selectionArgs
	 * @param groupBy
	 * @param having
	 * @param orderBy
	 * @return cursor
	 */
	public static Cursor select(String table, String[] columns,
			String selection, String[] selectionArgs, String groupBy,
			String having, String orderBy) {
		return sDatabase.query(table, columns, selection, selectionArgs,
				groupBy, having, orderBy);
	}

	/**
	 * Sqlite select query
	 * 
	 * @param sql
	 * @param selectionArgs
	 * @return cursor
	 */
	public static Cursor select(String sql, String[] selectionArgs) {
		return sDatabase.rawQuery(sql, selectionArgs);
	}

	private static class DatabaseHelper extends SQLiteOpenHelper {

		private static final String HOT_USER = "CREATE TABLE [hot_user] ('id' INTEGER NOT NULL PRIMARY KEY , 'account' TEXT, 'intro' TEXT, 'phone' TEXT, 'role_id' INTEGER,'role_name' TEXT, 'grade' INTEGER DEFAULT '1', 'company_name' TEXT, 'province' TEXT, 'city' TEXT, 'county' TEXT, 'address' TEXT, 'photo' TEXT, 'is_certificated' INTEGER DEFAULT '0', 'is_member_certificated' INTEGER DEFAULT '1', 'longitude' REAL, 'latitude' REAL, 'pv' INTEGER DEFAULT '0', 'favorite_num' INTEGER DEFAULT '0', 'hot_num' INTEGER DEFAULT '0');";
		private static final String USER = "CREATE TABLE [user] ('id' INTEGER NOT NULL PRIMARY KEY , 'account' TEXT, 'intro' TEXT, 'phone' TEXT, 'role_id' INTEGER,'role_name' TEXT, 'grade' INTEGER DEFAULT '1', 'company_name' TEXT, 'province' TEXT, 'city' TEXT, 'county' TEXT, 'address' TEXT, 'photo' TEXT, 'is_certificated' INTEGER DEFAULT '0', 'is_member_certificated' INTEGER DEFAULT '1', 'longitude' REAL, 'latitude' REAL, 'pv' INTEGER DEFAULT '0', 'favorite_num' INTEGER DEFAULT '0', 'hot_num' INTEGER DEFAULT '0');";
		private static final String PRODUCT = "CREATE TABLE [product] ('id' INTEGER NOT NULL PRIMARY KEY, 'user_id' INTEGER, 'product_name' TEXT, 'product_info' TEXT, 'brand' TEXT, 'is_stock' INTEGER DEFAULT '1', 'create_time' INTEGER, 'update_time' INTEGER, 'pv' INTEGER DEFAULT '0', 'favorite_num' INTEGER DEFAULT '0', 'property' TEXT, 'hot_num' INTEGER DEFAULT '0');";
		private static final String PRODUCT_IMAGE = "CREATE TABLE [product_image] ('id' INTEGER NOT NULL PRIMARY KEY, 'product_id' INTEGER, 'url' TEXT, 'create_time' INTEGER, 'described' TEXT);";
		private static final String HOME_BANNER = "CREATE TABLE [home_banner] ('id' INTEGER NOT NULL PRIMARY KEY, 'title' TEXT, 'image_url' TEXT, 'type' INTEGER, 'type_id' INTEGER, 'click_url' TEXT, 'expires_time' INTEGER);";
		private static final String NEWS = "CREATE TABLE [news] ('id' INTEGER NOT NULL PRIMARY KEY, 'user_id' INTEGER, 'title' TEXT, 'intro' TEXT, 'content' TEXT, 'create_time' INTEGER,'is_banner' INTEGER, 'image_url' TEXT, 'type' INTEGER, 'media_duration' INTEGER,'media_url' TEXT,'page_url' TEXT);";
		private static final String SUPPLY_DEMAND = "CREATE TABLE [supply_demand] ('id' INTEGER NOT NULL PRIMARY KEY, 'title' TEXT, 'content' TEXT, 'user_id' INTEGER, 'type' INTEGER, 'area' TEXT, 'create_time' INTEGER, 'update_time' INTEGER, 'company_name' TEXT, 'phone' TEXT, 'is_favorite' INTEGER DEFAULT '0');";
		private static final String SEARCH_HISTORY = "CREATE TABLE [search_history] ('id' INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, 'keyword' TEXT);";

		public DatabaseHelper(Context context, String databaseName) {
			// calls the super constructor, requesting the default cursor
			// factory.
			super(context, databaseName, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			// TODO
			db.execSQL(USER);
			db.execSQL(HOT_USER);
			db.execSQL(PRODUCT);
			db.execSQL(PRODUCT_IMAGE);
			db.execSQL(HOME_BANNER);
			db.execSQL(NEWS);
			db.execSQL(SUPPLY_DEMAND);
			db.execSQL(SEARCH_HISTORY);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// apply necessary change of database
		}

	}

	public static void clear() {
		// TODO 不考虑id自增
		String sql_hot_user = "delete from [hot_user] where 1=1";
		sDatabase.execSQL(sql_hot_user);
		String sql_user = "delete from [user] where 1=1";
		sDatabase.execSQL(sql_user);
		String sql_product = "delete from [product] where 1=1";
		sDatabase.execSQL(sql_product);
		String sql_product_image = "delete from [product_image] where 1=1 ";
		sDatabase.execSQL(sql_product_image);
		String sql_home_banner = "delete from [home_banner] where 1=1 ";
		sDatabase.execSQL(sql_home_banner);
		String sql_news = "delete from [news] where 1=1 ";
		sDatabase.execSQL(sql_news);
		String sql_supply_demand = "delete from [supply_demand] where 1=1 ";
		sDatabase.execSQL(sql_supply_demand);
		String search_history = "delete from [search_history] where 1=1 ";
		sDatabase.execSQL(search_history);
	}
}
