package com.gammainfo.qipei;

import java.util.HashSet;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import cn.jpush.android.api.JPushInterface;

import com.gammainfo.qipei.db.DatabaseManager;
import com.gammainfo.qipei.db.Preferences;
import com.gammainfo.qipei.utils.Constant;
import com.gammainfo.qipei.utils.CrashHandler;
import com.umeng.analytics.MobclickAgent;

public class Application extends android.app.Application {
	private static final String TAG = Application.class.getSimpleName();
	private CrashHandler mCrashHandler;
	private static Application instance;
	private Intent serviceIntent;
	private static String currentCity;

	@Override
	public void onCreate() {
		super.onCreate();
		// TODO catch global excpetion
		Log.i("Application", "-----------123--------------------");
		 mCrashHandler = CrashHandler.getInstance();
		 mCrashHandler.init(this);
		 mCrashHandler.sendPreviousReportsToServer();
		 Log.i("Application", "-----------456--------------------");
		// TODO db init
		DatabaseManager.initialize(this);
		Preferences.init(this);
		new Thread(new Runnable() {

			@Override
			public void run() {
				// jpush
				JPushInterface.setDebugMode(Constant.DEBUG);
				JPushInterface.init(Application.this);
				HashSet<String> areaSet = new HashSet<String>();
				setCurrentCity(Preferences.getMyCityFromLocalFile());
				areaSet.add(getCurrentCity());
				JPushInterface.setAliasAndTags(Application.this,
						Preferences.getDeviceId(), areaSet);
				registerReceiver();
				// umeng
				MobclickAgent.setDebugMode(Constant.DEBUG);
				MobclickAgent.updateOnlineConfig(Application.this);
			}
		}).start();

	}

	// 单例模式中获取唯一的MyApplication实例
	public static Application getInstance() {
		if (null == instance) {
			instance = new Application();
		}
		return instance;
	}

	private void registerReceiver() {
		// push receiver
		IntentFilter intentFilter = new IntentFilter(
				Constant.ACTION_PUSH_RECEIVER);
		intentFilter.setPriority(-1000);
		registerReceiver(mPushReceiver, intentFilter);
		// gps receiver
		registerReceiver(mGpsReceiver, new IntentFilter(
				Constant.ACTION_GPS_SERVICE_RECEIVER));
		Log.i("Application", "-----------不知道为什么不执行--------------------");
	}

	private BroadcastReceiver mPushReceiver = new BroadcastReceiver() {

		public void onReceive(Context context, Intent intent) {
			if (Constant.DEBUG) {
				System.out.println(".................收到消息了。。。。"
						+ intent.getExtras());
				Toast.makeText(context, "... 收到消息了。。",Toast.LENGTH_SHORT).show();
			}
			if (Preferences.getIsPushInformation()) {
				Bundle bundle = intent.getExtras();
				if (bundle != null) {
					String type = bundle
							.getString(JPushInterface.EXTRA_CONTENT_TYPE);
					if ("news".equals(type)) {
						String msg = bundle
								.getString(JPushInterface.EXTRA_EXTRA);
						try {
							JSONObject msgJsonObject = new JSONObject(msg);
							String area = msgJsonObject.getString("area");
							if ("".equals(area)
									|| area.equals(Preferences.getMyCity())) {
								String pageUrl = msgJsonObject.getString("url");
								String title = msgJsonObject.getString("title");
								int newsType = msgJsonObject.getInt("type");
								// 没展示Notification bar时显示的
								android.app.Notification notification = new android.app.Notification(
										R.drawable.ic_noti, title,
										System.currentTimeMillis());

								notification.flags |= android.app.Notification.FLAG_AUTO_CANCEL;
								Intent notificationIntent = new Intent();
								notificationIntent
										.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
												| Intent.FLAG_ACTIVITY_SINGLE_TOP
												| Intent.FLAG_ACTIVITY_CLEAR_TOP);
								notificationIntent.putExtra(
										NewsContentActivity.EXTRA_TYPE,
										newsType);
								notificationIntent.putExtra(
										NewsContentActivity.EXTRA_URL, pageUrl);
								notificationIntent.putExtra("noti", true);// 标识是否是通知
								//
								// 普通通知，显示在notification bar 中，点击之后打开软件首页
								notificationIntent.setClass(context,
										MainActivity.class);
								PendingIntent contentIntent = PendingIntent
										.getActivity(
												context,
												0,
												notificationIntent,
												PendingIntent.FLAG_UPDATE_CURRENT);

								notification.contentIntent = contentIntent;
								// 展开时显示的
								notification.setLatestEventInfo(context, title,
										getText(R.string.app_name),
										contentIntent);
								NotificationManager notificationManager = (NotificationManager) context
										.getSystemService(Context.NOTIFICATION_SERVICE);
								notificationManager.notify(
										(int) System.currentTimeMillis(),
										notification);
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	};

	private BroadcastReceiver mGpsReceiver = new BroadcastReceiver() {

		public void onReceive(Context context, Intent intent) {
			if (Constant.DEBUG) {
				Toast.makeText(context, "...... gps location request 收到消息了。。",Toast.LENGTH_SHORT).show();
				System.out
						.println("................. gps location request 收到消息了。。。。");
			}
			// TODO parse message
			String command = intent.getStringExtra(GpsService.COMMAND);
			if (GpsService.CMD_REQUEST_LOCATION.equals(command)) {

			} else if (GpsService.CMD_START.equals(command)) {
				startGpsService();
				Toast.makeText(context, "......startGpsService。。",Toast.LENGTH_SHORT).show();
			} else if (GpsService.CMD_STOP.equals(command)) {
				stopGpsService();
			}
			startGpsService();
		}
	};

	private void startGpsService() {
		Context context = getApplicationContext();
		Log.d(TAG, "StartAndBindService - binding now");
		serviceIntent = new Intent(context, GpsService.class);
		context.startService(serviceIntent);
	}

	private void stopGpsService() {
		if (serviceIntent != null) {
			Context context = getApplicationContext();
			Log.d(TAG, "StopServiceIfRequired - Stopping the service");
			context.stopService(serviceIntent);
		}
	}

	public static String getCurrentCity() {
		return currentCity;
	}

	public static void setCurrentCity(String city) {
		if (Constant.DEBUG) {
			System.out.println("----------------my city : " + city);
		}
		currentCity = city;
	}
}
