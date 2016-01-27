package com.gammainfo.qipei;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.gammainfo.qipei.db.Preferences;
import com.gammainfo.qipei.utils.Constant;

public class GpsService extends Service {
	private static final int LOCATION_UPDATE = 30 * 60 * 1000;// ms
	private static final String TAG = GpsService.class.getSimpleName();
	public static final String COMMAND = "command";
	public static final String CMD_REQUEST_LOCATION = "request_location";
	public static final String CMD_START = "start_gps";
	public static final String CMD_STOP = "stop_gps";

	public static final String EXTRA_LATITUDE = "latitude";
	public static final String EXTRA_LONGITUDE = "longitude";
	public static final String EXTRA_CITY = "city";
	public static final String EXTRA_ADDRESS = "address";

	private final IBinder mBinder = new GpsBinder();

	private GeneralLocationListener gpsLocationListener;
	private LocationManager gpsLocationManager;
	private String mProvider;

	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}

	@Override
	public void onCreate() {
	}

	@Override
	public void onStart(Intent intent, int startId) {
		handleIntent(intent);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		handleIntent(intent);
		return START_REDELIVER_INTENT;
	}

	@Override
	public void onDestroy() {
		stopGpsManager();
		Log.d(TAG, "--------------GpsService------------onDestroy---------");
		super.onDestroy();
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
	}

	private void startGpsManager() {
		if (gpsLocationListener == null) {
			gpsLocationListener = new GeneralLocationListener(this);
		}
		if (gpsLocationManager == null) {
			gpsLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		} else {
			gpsLocationManager.removeUpdates(gpsLocationListener);
			gpsLocationManager.removeGpsStatusListener(gpsLocationListener);
		}
		mProvider = getBestProvider();
		if (LocationManager.GPS_PROVIDER.equals(mProvider)
				|| LocationManager.NETWORK_PROVIDER.equals(mProvider)
				|| LocationManager.PASSIVE_PROVIDER.equals(mProvider)) {
			gpsLocationManager.requestLocationUpdates(mProvider,
					LOCATION_UPDATE, 0, gpsLocationListener);
			if (LocationManager.GPS_PROVIDER.equals(mProvider)) {
				gpsLocationManager.addGpsStatusListener(gpsLocationListener);
			}
		} else {
			// No provider available
			Intent intent = new Intent();
			intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			try {
				getApplicationContext().startActivity(intent);

			} catch (ActivityNotFoundException ex) {

				// The Android SDK doc says that the location settings activity
				// may not be found. In that case show the general settings.

				// General settings activity
				intent.setAction(Settings.ACTION_SETTINGS);
				try {
					getApplicationContext().startActivity(intent);
				} catch (Exception e) {
				}
			}
		}
	}

	private String getBestProvider() {
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE); // 设置精确度
		criteria.setAltitudeRequired(false); // 设置请求海拔
		criteria.setBearingRequired(false); // 设置请求方位
		criteria.setCostAllowed(true); // 设置允许运营商收费
		criteria.setPowerRequirement(Criteria.POWER_LOW); // 低功耗

		String provider = gpsLocationManager.getBestProvider(criteria, true);
		return provider;
	}

	private void stopGpsManager() {
		if (gpsLocationListener != null) {
			gpsLocationManager.removeUpdates(gpsLocationListener);
			gpsLocationManager.removeGpsStatusListener(gpsLocationListener);
		}
	}

	void restartGpsManagers() {
		stopGpsManager();
		startGpsManager();
	}

	public class GpsBinder extends Binder {
		public GpsService getService() {
			return GpsService.this;
		}
	}

	/**
	 * This event is raised when the GeneralLocationListener has a new location.
	 * This method in turn updates notification, writes to file, re-obtains
	 * preferences, notifies main service client and resets location managers.
	 * 
	 * @param loc
	 *            Location object
	 */
	void onLocationChanged(Location loc) {
		// long currentTimeStamp = System.currentTimeMillis();
		// // Wait some time even on 0 frequency so that the UI doesn't lock up
		// if ((currentTimeStamp - Preferences.getMyLocationLatestTimestamp()) <
		// 1000) {
		// return;
		// }
		double latitude = loc.getLatitude();
		double longitude = loc.getLongitude();
		Preferences.setMyLocation(latitude, longitude);
		Log.d(TAG, "lat / lng : " + Preferences.getMyLatitude() + " / "
				+ Preferences.getMyLongitude());
		Context context = getApplicationContext();
		Geocoder geoCoder = new Geocoder(context, Locale.getDefault());
		String city = null;
		try {
			List<Address> addresses = geoCoder.getFromLocation(latitude,
					longitude, 1);
			Address addr = null;
			if (addresses.size() > 0) {
				addr = addresses.get(0);
				city = addr.getLocality();
			} else {
				// TODO 没有获取到地址
			}
			if (city != null) {
				// 城市后缀过滤
				if (city.endsWith("市")) {
					city = city.substring(0, city.length() - 1);
				}
			} else {
				Toast.makeText(context, R.string.common_toast_location_failed,
						Toast.LENGTH_SHORT).show();
			}

		} catch (IOException e) {
			Toast.makeText(context, R.string.common_toast_location_failed,
					Toast.LENGTH_SHORT).show();
		}
		// TODO onLocationChanged
		sendLocationChangedBroadcast(loc, null, city, null, null);
	}

	private void sendLocationChangedBroadcast(Location loc, String province,
			String city, String county, String address) {
		Intent intent = new Intent();
		intent.setAction(Constant.ACTION_GPS_LOCATION_CHANGED_RECEIVER);
		intent.putExtra(EXTRA_LATITUDE, loc.getLatitude());
		intent.putExtra(EXTRA_LONGITUDE, loc.getLongitude());
		intent.putExtra(EXTRA_CITY, city);
		intent.putExtra(EXTRA_ADDRESS, address);
		sendBroadcast(intent);
	}

	private void handleIntent(Intent intent) {

		if (intent != null) {
			Bundle bundle = intent.getExtras();

			if (bundle != null) {
				String command = bundle.getString(COMMAND);
				if (CMD_START.equals(command)) {
					startGpsManager();
				} else if (CMD_STOP.equals(command)) {
					stopGpsManager();
				} else if (CMD_REQUEST_LOCATION.equals(command)) {
					startGpsManager();
				}
			} else {
				restartGpsManagers();
			}
		} else {
			// A null intent is passed in if the service has been killed and
			// restarted.
			restartGpsManagers();
		}
	}

	/**
	 * 依据Location解析城市
	 * 
	 * @param context
	 * @param latitude
	 * @param longitude
	 * @param isShort
	 *            是否是城市简称
	 * @return 如果成功，返回对应的城市，否则，返回null
	 */
	public static String parseCity(Context context, double latitude,
			double longitude, boolean isShort) {
		Geocoder geoCoder = new Geocoder(context, Locale.getDefault());
		try {
			List<Address> addresses = geoCoder.getFromLocation(latitude,
					longitude, 1);
			String add = null;
			if (addresses.size() > 0) {
				Address addr = addresses.get(0);
				add = addr.getLocality();
				if (add != null) {
					if (isShort) {
						// 城市后缀过滤
						if (add.endsWith("市")) {
							add = add.substring(0, add.length() - 1);
						}
					}
					return add;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
