package com.gammainfo.qipei;

import java.io.IOException;
import java.io.InputStream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.gammainfo.qipei.adapter.AreaAdapter;
import com.gammainfo.qipei.utils.Utils;

public class CityActivity extends AreaBaseActivity implements
		OnItemClickListener {

	private static final int REQ_COUNTY = 1;

	private JSONArray mJsonArray;
	private ListView mListView;
	private AreaAdapter mAreaAdapter;
	private Intent mCountyIntent;
	private int mPosition;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_city);
		mListView = (ListView) findViewById(R.id.lv_city);
		mListView.setOnItemClickListener(this);
		String province = mGetIntent.getStringExtra(EXTRA_PROVINCE);
		AssetManager assetManager = getAssets();
		try {
			InputStream is = assetManager.open("area/city.json");
			String jsonString = Utils.inputStream2String(is);
			mJsonArray = new JSONArray(jsonString);
			int size = mJsonArray.length();
			JSONObject item = null;
			for (int i = 0; i < size; i++) {
				item = mJsonArray.getJSONObject(i);
				if (item.has(province)) {
					mJsonArray = item.getJSONArray(province);
					break;
				}
			}
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
		if (mLevel == LEVEL_PROVINCE_CITY) {
			setResultOk(null);
		} else {
			if (mCountyIntent == null) {
				mCountyIntent = new Intent(this, CountyActivity.class);
			}
			mCountyIntent.putExtra(EXTRA_CITY,
					mAreaAdapter.getItemKey(position));
			startActivityForResult(mCountyIntent, REQ_COUNTY);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQ_COUNTY) {
			if (resultCode == Activity.RESULT_OK) {
				setResultOk(data);
			}
		}
	}

	private void setResultOk(Intent data) {
		if (data == null) {
			data = new Intent();
		}
		data.putExtra(EXTRA_CITY, mAreaAdapter.getItemValue(mPosition));
		setResult(Activity.RESULT_OK, data);
		finish();
	}
}
