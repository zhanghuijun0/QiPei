package com.gammainfo.qipei;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.gammainfo.qipei.db.Preferences;
import com.gammainfo.qipei.http.AsyncHttpResponseHandler;
import com.gammainfo.qipei.http.RequestParams;
import com.gammainfo.qipei.http.RestClient;
import com.gammainfo.qipei.model.User;
import com.gammainfo.qipei.utils.Constant;
import com.loopj.android.http.JsonHttpResponseHandler;

public class ProfileMyInfoChangeAddressActivity extends BaseActivity {
	private TextView mProfileChangeNameTypeText;
	private TextView mProfileProvinceCityCountyText;
	private EditText mProfileMyInfoChangeAddress;
	private String oldProvince;
	private String oldCity;
	private String oldCounty;
	private String oldAddress;
	private String newProvince;
	private String newCity;
	private String newCounty;
	private String newAddress;
	private User user;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.profile_my_info_change_address_activity);
		user = Preferences.getAccountUser();
		mProfileChangeNameTypeText = (TextView) findViewById(R.id.profile_change_name_type_text);
		mProfileChangeNameTypeText
				.setText(getString(R.string.profile_label_address_text));
		mProfileProvinceCityCountyText = (TextView) findViewById(R.id.profile_province_city_county_text);
		mProfileMyInfoChangeAddress = (EditText) findViewById(R.id.profile_my_info_change_address);
		if (user != null) {
			oldProvince = user.getProvince();
			oldCity = user.getCity();
			oldCounty = user.getCounty();
			oldAddress = user.getAddress();
			newProvince = oldProvince;
			newCity = oldCity;
			newCounty = oldCounty;
			newAddress = oldAddress;
			setProvinceCityCountyText();
			mProfileMyInfoChangeAddress.setText(oldAddress);
		} else {
			finish();
		}
	}

	private void setProvinceCityCountyText() {
		if ("".equals(newCity)) {
			mProfileChangeNameTypeText.setText("");
		} else {
			mProfileProvinceCityCountyText.setText(newProvince + "-" + newCity
					+ "-" + newCounty);
		}
	}

	/* 点击修改地址 */
	public void onProfileChangeAdressClick(View v) {
		Intent intent = new Intent(this, ProvinceActivity.class);
		intent.putExtra(AreaBaseActivity.EXTRA_LEVEL,
				AreaBaseActivity.LEVEL_PROVINCE_CITY_COUNTY);
		intent.putExtra(AreaBaseActivity.EXTRA_AUTO_LOCATIOIN, false);
		startActivityForResult(intent, 0);
	}

	/* 点击“保存”触发的事件 */
	public void onProfileSaveChangeClick(View v) {
		newAddress = mProfileMyInfoChangeAddress.getText().toString().trim();
		if ("".equals(newAddress) || "".equals(newProvince)
				|| "".equals(newCity) || "".equals(newCounty)) {
			Toast.makeText(this, "地址不完整", Toast.LENGTH_SHORT).show();
			return;
		}
		if (!oldProvince.equals(newProvince) || !oldCity.equals(newCity)
				|| !oldCounty.equals(newCounty)
				|| !oldAddress.equals(newAddress)) {
			changeInternetAdress();
		} else {
			finish();
		}
	}

	/* 设置网上数据库的详细地址 */
	public void changeInternetAdress() {
		int id = Preferences.getAccountUserId();
		RequestParams params = new RequestParams();
		params.put("id", String.valueOf(id));
		params.put("province", String.valueOf(newProvince));
		params.put("city", String.valueOf(newCity));
		params.put("county", String.valueOf(newCounty));
		params.put("address", newAddress);
		RestClient.post(Constant.API_SET_ADDRESS, params,
				new AsyncHttpResponseHandler(getBaseContext(),
						new JsonHttpResponseHandler() {
							@Override
							public void onSuccess(JSONObject rlt) {
								try {
									if (rlt.getInt(Constant.JSON_KEY_CODE) == Constant.CODE_SUCCESS) {
										Toast.makeText(
												getBaseContext(),
												getString(R.string.profile_toast_revise_province_city_county_success_text),
												Toast.LENGTH_SHORT).show();
										changeLocalAdress();
									} else {
										String msg = rlt
												.getString(Constant.JSON_KEY_MSG);
										Toast.makeText(getBaseContext(), msg,
												Toast.LENGTH_LONG).show();
									}
								} catch (JSONException e) {
									e.printStackTrace();
								}
							}
						}));
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
			newProvince = data.getStringExtra(AreaBaseActivity.EXTRA_PROVINCE);
			newCity = data.getStringExtra(AreaBaseActivity.EXTRA_CITY);
			newCounty = data.getStringExtra(AreaBaseActivity.EXTRA_COUNTY);
			setProvinceCityCountyText();
		}
	}

	/* 修改本地存储的地址 */
	public void changeLocalAdress() {
		Preferences.setProvinceCityCounty(newProvince, newCity, newCounty,
				newAddress);
		finishActivity();
	}

	/* 返回上一页 */
	public void onBackClick(View v) {
		finish();
	}

	/* 返回上一页 */
	public void finishActivity() {
		Intent data = new Intent();
		setResult(Activity.RESULT_OK, data);
		finish();
	}
}
