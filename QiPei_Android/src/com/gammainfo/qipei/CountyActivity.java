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

public class CountyActivity extends AreaBaseActivity implements
		OnItemClickListener {

	private JSONArray mJsonArray;
	private ListView mListView;
	private AreaAdapter mAreaAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_county);
		String city = getIntent().getStringExtra(EXTRA_CITY);
		mListView = (ListView) findViewById(R.id.lv_county);
		mListView.setOnItemClickListener(this);
		AssetManager assetManager = getAssets();
		try {
			InputStream is = assetManager.open("area/county.json");
			String jsonString = Utils.inputStream2String(is);
			mJsonArray = new JSONArray(jsonString);
			int size = mJsonArray.length();
			JSONObject item = null;
			for (int i = 0; i < size; i++) {
				item = mJsonArray.getJSONObject(i);
				if (item.has(city)) {
					mJsonArray = item.getJSONArray(city);
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
		Intent data = new Intent();
		data.putExtra(EXTRA_COUNTY, mAreaAdapter.getItemValue(position));
		setResult(Activity.RESULT_OK, data);
		finish();
	}
}
