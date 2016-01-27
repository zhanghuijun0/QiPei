package com.gammainfo.qipei;

import java.io.IOException;
import java.io.InputStream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.hardware.Camera.Area;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.gammainfo.qipei.adapter.AreaAdapter;
import com.gammainfo.qipei.db.Preferences;
import com.gammainfo.qipei.utils.Constant;
import com.gammainfo.qipei.utils.Utils;

public class ProvinceActivity extends AreaBaseActivity implements
		OnItemClickListener, View.OnClickListener {

	private static final int REQ_CITY = 1;
	private static final int LOCATION_STATUS_LOADING = 1;
	private static final int LOCATION_STATUS_SUCCESS = 2;
	private static final int LOCATION_STATUS_FAILED = 3;
	private JSONArray mJsonArray;
	private ListView mListView;
	private AreaAdapter mAreaAdapter;
	private Intent mCityIntent;
	private int mPosition;
	private Button mChooseCityBtn;
	private Button mGpsLocationBtn;
	private ProgressBar mLocationPb;
	private BroadcastReceiver mGpsLocationChangedReceiver;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_province);
		mListView = (ListView) findViewById(R.id.lv_province);
		mListView.setOnItemClickListener(this);
		mChooseCityBtn = (Button) findViewById(R.id.btn_province_choose_city);
		mGpsLocationBtn = (Button) findViewById(R.id.btn_province_gps_location);
		mLocationPb = (ProgressBar) findViewById(R.id.pb_province_location);
		mChooseCityBtn.setOnClickListener(this);
		mGpsLocationBtn.setOnClickListener(this);
		mChooseCityBtn.setText(Preferences.getMyCity());
		View locationContainerView = findViewById(R.id.fl_province_locationcontainer);
		if (mAutoLocation) {
			locationContainerView.setVisibility(View.VISIBLE);
			location();
		} else {
			locationContainerView.setVisibility(View.GONE);
		}
		AssetManager assetManager = getAssets();
		try {
			InputStream is = assetManager.open("area/province.json");
			String provinceJson = Utils.inputStream2String(is);
			mJsonArray = new JSONArray(provinceJson);
			mAreaAdapter = new AreaAdapter(this, mJsonArray, mIsAreaShort);
			mListView.setAdapter(mAreaAdapter);
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			long id) {
		mPosition = position;
		if (mLevel == LEVEL_PROVINCE) {
			setResultOk(null);
		} else {
			if (mCityIntent == null) {
				mCityIntent = new Intent(this, CityActivity.class);
			}
			mCityIntent.putExtra(EXTRA_PROVINCE,
					mAreaAdapter.getItemKey(position));
			startActivityForResult(mCityIntent, REQ_CITY);
		}
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.btn_province_choose_city) {
			// TODO get address
			String city = mChooseCityBtn.getText().toString();
			setResultOk(null, city, null);
		} else {
			// TODO go to gps location
			location();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQ_CITY) {
			if (resultCode == Activity.RESULT_OK) {
				setResultOk(data);
			}
		}
	}

	private void setResultOk(Intent data) {
		if (data == null) {
			data = new Intent();
		}
		data.putExtra(EXTRA_PROVINCE, mAreaAdapter.getItemValue(mPosition));
		setResult(Activity.RESULT_OK, data);
		finish();
	}

	private void setResultOk(String province, String city, String county) {
		Intent data = new Intent();
		data.putExtra(EXTRA_PROVINCE, province);
		data.putExtra(EXTRA_CITY, city);
		data.putExtra(EXTRA_COUNTY, county);
		setResult(Activity.RESULT_OK, data);
		finish();
	}

	private void location() {
		setLocationStatus(LOCATION_STATUS_LOADING);
		if (mGpsLocationChangedReceiver == null) {
			mGpsLocationChangedReceiver = new GpsSerivceBroadcastReceiver();
		}
		registerReceiver(mGpsLocationChangedReceiver, new IntentFilter(
				Constant.ACTION_GPS_LOCATION_CHANGED_RECEIVER));
		sendBroadcast(new Intent(Constant.ACTION_GPS_SERVICE_RECEIVER));
	}

	private void setLocationStatus(int status) {
		switch (status) {
		case LOCATION_STATUS_SUCCESS:
			mChooseCityBtn.setVisibility(View.VISIBLE);
			mGpsLocationBtn.setVisibility(View.INVISIBLE);
			mLocationPb.setVisibility(View.INVISIBLE);
			break;
		case LOCATION_STATUS_FAILED:
			mChooseCityBtn.setVisibility(View.INVISIBLE);
			mGpsLocationBtn.setVisibility(View.VISIBLE);
			mLocationPb.setVisibility(View.INVISIBLE);
			break;
		case LOCATION_STATUS_LOADING:
		default:
			mChooseCityBtn.setVisibility(View.INVISIBLE);
			mGpsLocationBtn.setVisibility(View.INVISIBLE);
			mLocationPb.setVisibility(View.VISIBLE);
			break;

		}
	}

	@Override
	public void onPause() {
		if (mGpsLocationChangedReceiver != null) {
			unregisterReceiver(mGpsLocationChangedReceiver);
			mGpsLocationChangedReceiver = null;
		}
		super.onPause();
	}

	private class GpsSerivceBroadcastReceiver extends BroadcastReceiver {

		public void onReceive(Context context, Intent intent) {
			String targetCity = intent.getStringExtra(GpsService.EXTRA_CITY);
			if (targetCity != null) {
				mChooseCityBtn.setText(targetCity);
				setLocationStatus(LOCATION_STATUS_SUCCESS);
			} else {
				Toast.makeText(context, R.string.common_toast_location_failed,
						Toast.LENGTH_SHORT).show();
				setLocationStatus(LOCATION_STATUS_FAILED);
			}
		}
	}
}
