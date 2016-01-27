package com.gammainfo.qipei.utils;

public final class Constant {
	// TODO change values when release
	public static final boolean DEBUG = false;
	public static final String APP_FOLDER = "qipei";

	// public static final String BAIDU_MAP_KEY = DEBUG ?
	// "vpDEdfsHDnlQ5SmSSpYpvpRT"
	// : "5Z1Ewy5w0uEDavAUOIF1nfOY";
	public static final String SHARE_APP_ID = "wx39c73dcdca6dfb27";
	public static final String JSON_KEY_CODE = "code";
	public static final String JSON_KEY_MSG = "msg";
	public static final String JSON_KEY_INFO = "info";
	public static final String JSON_KEY_LIST = "list";
	public static final String JSON_KEY_TIMESTAMP = "timestamp";
	public static final String JSON_KEY_DEVICE_ID = "did";
	public static final String JSON_KEY_ACCESS_FORMAT = "format";
	public static final String JSON_KEY_AREA = "area";
	public static final String JSON_KEY_V = "version";
	public static final String JSON_KEY_ID = "id";
	public static final String JSON_KEY_TYPE = "type";
	public static final String JSON_KEY_PAGE = "page";
	public static final String JSON_MESSAGE = "msg";

	/**
	 * 0x000 Success 请求成功 0x001 Time out 请求/执行超时 0x002 Data fail 数据异常(参数错误)
	 * 0x003 DB error 数据库执行失败 0x004 Service error 服务器导常 0x005 User permissions
	 * 用户权限不够 0x006 Service unavailable 服务不可用 0x007 Missing Method 方法不可用 0x008
	 * Missing api version 版本丢失 0x009 API verion error API版本异常 0x010 API need
	 * update API需要升级 0x011 Error 服务异常调用 0x012 failure 与Success对立的一种逻辑状态
	 */
	public static final int CODE_SUCCESS = 0x000;
	public static final int CODE_TIMEOUT = 0x001;
	public static final int CODE_DATA_FAIL = 0x002;
	public static final int CODE_DB_ERROR = 0x003;
	public static final int CODE_SERVICE_ERROR = 0x004;
	public static final int CODE_USER_PERMISSIONS = 0x005;
	public static final int CODE_SERVICE_UNAVAILABLE = 0x006;
	public static final int CODE_MISSING_METHOD = 0x007;
	public static final int CODE_MISSING_API_VERSION = 0x008;
	public static final int CODE_API_VERSION_ERROR = 0x009;
	public static final int CODE_API_NEED_UPDATE = 0x010;
	public static final int CODE_ERROR = 0x011;
	public static final int CODE_FAILURE = 0x012;

	// api url
	public static final String API_URL_PRE = DEBUG ? "http://192.168.1.74/QiPei_Api/index.php/Api/"
			: "http://chinaqszxapi.sinaapp.com/index.php/Api/";
	public static final String API_GET_HOT_USER = API_URL_PRE
			+ "Company/get_hot_company_list";
	public static final String API_COMPANY_LIST_GET = API_URL_PRE
			+ "Company/search_company_list";
	public static final String API_COMPANY_ITEM_GET = API_URL_PRE
			+ "Company/get_company_item";
	public static final String API_LIST_ADDFAVORITE_GET = API_URL_PRE
			+ "Favorite/add_favorite";
	public static final String API_LIST_FAVORITE_ADD = API_URL_PRE
			+ "Favorite/add_favorite";
	public static final String API_LIST_FAVORITE_CANCEL = API_URL_PRE
			+ "Favorite/cancel_favorite";
	public static final String API_USER_PRODUCT_LIST_GET = API_URL_PRE
			+ "Product/get_user_product_list";
	public static final String API_USER_PRODUCT_ITEM_GET = API_URL_PRE
			+ "Product/get_product_item";
	public static final String API_SUPPLY_DEMAND_LIST_GET = API_URL_PRE
			+ "SupplyDemand/search_supply_demand";
	public static final String API_SUPPLY_DEMAND_DETAIL_GET = API_URL_PRE
			+ "SupplyDemand/get_supply_demand_detail";
	public static final String API_LOGIN = API_URL_PRE + "Login/login";
	public static final String API_GET_INFO = API_URL_PRE + "User/get_info";
	public static final String API_SET_COMPANY_NAME = API_URL_PRE
			+ "User/set_companyname";
	public static final String API_SET_ADDRESS = API_URL_PRE
			+ "User/set_address";
	public static final String API_SET_PHONE = API_URL_PRE + "User/set_phone";
	public static final String API_SET_INTRO = API_URL_PRE + "User/set_intro";
	public static final String API_SET_USER_TYPE = API_URL_PRE
			+ "User/set_type";
	public static final String API_SET_GRADE = API_URL_PRE + "User/set_grade";
	public static final String API_GET_USER_PRODUCT_LIST = API_URL_PRE
			+ "Product/get_user_product_list";
	public static final String API_GET_FAVORITE_TYPE_NUM = API_URL_PRE
			+ "Favorite/get_favorite_type_num";
	public static final String API_DELECT_PRODUCT_ITEM = API_URL_PRE
			+ "Product/delete_product_item";
	public static final String API_GET_FAVORITE_VENDOR = API_URL_PRE
			+ "Favorite/get_favorite";
	public static final String API_SEARCH_PRODUCT_LIST = API_URL_PRE
			+ "Product/search_product_list";
	public static final String API_GET_FORGET_PWD_CODE = API_URL_PRE
			+ "Login/get_forget_pwd_code";
	public static final String API_VALID_FORGET_PWD_CODE = API_URL_PRE
			+ "Login/valid_forget_pwd_code";
	public static final String API_GET_REG_CODE = API_URL_PRE
			+ "Login/get_reg_code";
	public static final String API_VALID_REG_CODE = API_URL_PRE
			+ "Login/valid_reg_code";
	public static final String API_SET_PWD = API_URL_PRE + "Login/set_pwd";
	public static final String API_PUBLISH_SUPPLY_DEMAND = API_URL_PRE
			+ "SupplyDemand/publish_supply_demand";
	public static final String API_UPDATE_SUPPLY_DEMAND = API_URL_PRE
	+ "SupplyDemand/update_supply_demand";
	public static final String API_GET_SUPPLY_DEMAND = API_URL_PRE
			+ "SupplyDemand/get_supply_demand";
	public static final String API_DELETE_SUPPLY_DEMAND = API_URL_PRE
			+ "SupplyDemand/delete_supply_demand";
	public static final String API_GET_SUPPLY_DEMAND_DETAIL = API_URL_PRE
			+ "SupplyDemand/get_supply_demand_detail";
	
	public static final String API_UPDATE_PWD = API_URL_PRE + "User/update_pwd";
	public static final String API_SET_PHOTO = API_URL_PRE + "User/set_photo";
	public static final String API_POST_FEEDBACK = API_URL_PRE
			+ "Home/post_feedback";
	public static final String API_CHECK_MOBILE_EXISTS = API_URL_PRE
			+ "Login/check_mobile_exists";

	public static final String API_GET_NEWS_BANNER = API_URL_PRE
			+ "News/get_banner";
	public static final String API_NEWS_LIST_GET = API_URL_PRE
			+ "News/get_news_list";
	public static final String API_NEWS_LIST_ITEM_GET = API_URL_PRE
			+ "News/get_news_item";
	public static final String API_GET_HOME_BANNER = API_URL_PRE
			+ "Home/get_home_banner";
	public static final String API_ADD_PRODUCT = API_URL_PRE
			+ "Product/add_product";
	public static final String API_EDIT_PRODUCT = API_URL_PRE
			+ "Product/edit_product";
	public static final String API_UPLOAD_PHOTO = API_URL_PRE
			+ "Home/post_picture";

	// TODO intent action
	public static final String ACTION_PUSH_RECEIVER = "com.gammainfo.qipei.jpushreceiver";
	public static final String ACTION_NEW_NOTIFICATION_RECEIVER = "com.gammainfo.qipei.newnoti";
	public static final String ACTION_GPS_SERVICE_RECEIVER = "com.gammainfo.qipei.gpsreceiver";
	public static final String ACTION_GPS_LOCATION_CHANGED_RECEIVER = "com.gammainfo.qipei.gpslocationchangedreceiver";

	public static final int PAGE_SIZE = 20;
	// TODO 请求gps 位置的周期（单位：秒）
	public static final int REQUEST_LOCATION_PERIOD = DEBUG ? 10000
			: 3 * 60 * 60 * 1000;
	// 毫秒
	public static final int REQUEST_GPS_LOCATION_INTERVAL = 300000;
}
