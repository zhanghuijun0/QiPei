package com.gammainfo.qipei;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Toast;

import com.gammainfo.qipei.convert.UserJSONConvert;
import com.gammainfo.qipei.db.Preferences;
import com.gammainfo.qipei.http.AsyncHttpResponseHandler;
import com.gammainfo.qipei.http.RequestParams;
import com.gammainfo.qipei.http.RestClient;
import com.gammainfo.qipei.model.User;
import com.gammainfo.qipei.utils.Constant;
import com.gammainfo.qipei.utils.Md5Util;
import com.loopj.android.http.JsonHttpResponseHandler;

public class LoginActivity extends BaseActivity{
	private EditText account;
	private EditText pwd;
	public static final String ACCOUNT = "account";
	private View mDelUserNameBtn;
	private View mDelPassordBtn;
	private ScrollView scrollView;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_activity);
		account = (EditText) findViewById(R.id.account);
		pwd = (EditText) findViewById(R.id.password);
		mDelUserNameBtn = findViewById(R.id.ibtn_login_delusername);
		mDelPassordBtn = findViewById(R.id.ibtn_login_delpassword);
		scrollView = (ScrollView) findViewById(R.id.profile_login_scrollview); 
		account.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				if (!TextUtils.isEmpty(s.toString())) {
					mDelUserNameBtn.setVisibility(View.VISIBLE);
				} else {
					mDelUserNameBtn.setVisibility(View.INVISIBLE);
				}
			}
		});
		pwd.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				if (!TextUtils.isEmpty(s.toString())) {
					mDelPassordBtn.setVisibility(View.VISIBLE);
				} else {
					mDelPassordBtn.setVisibility(View.INVISIBLE);
				}
			}
		});

	}
	
	public void onDelUserNameClick(View v) {
		mDelUserNameBtn.setVisibility(View.INVISIBLE);
		account.getText().clear();
	}

	public void onDelPasswordClick(View v) {
		mDelPassordBtn.setVisibility(View.INVISIBLE);
		pwd.getText().clear();
	}

	public void onLoginClick(final View v) {
		final Button loginButton = ((Button) v);
		if (account.getText().length() == 0 || pwd.getText().length() == 0) {
			Toast.makeText(getBaseContext(),
					getString(R.string.profile_toast_account_pwd_empty_text),
					Toast.LENGTH_LONG).show();
		} else {
			RequestParams params = new RequestParams();
			params.put("account", (account.getText()).toString());
			params.put("pwd", Md5Util.md5((pwd.getText()).toString()));
			RestClient.post(Constant.API_LOGIN, params,
					new AsyncHttpResponseHandler(getBaseContext(),
							new JsonHttpResponseHandler() {

								@Override
								public void onFinish() {
									loginButton.setText(getString(R.string.profile_label_login));
									loginButton.setEnabled(true);
									super.onFinish();
								}

								@Override
								public void onStart() {
									super.onStart();
									loginButton.setText(getString(R.string.profile_label_logining_text));
									loginButton.setEnabled(false);
								}

								@Override
								public void onSuccess(JSONObject rlt) {
									try {
										if (rlt.getInt(Constant.JSON_KEY_CODE) == Constant.CODE_SUCCESS) {
											JSONObject info = rlt
													.getJSONObject("info");
											String token = (info
													.getString("token"))
													.toString();
											JSONObject userObject = info
													.getJSONObject("user");
											User user = UserJSONConvert
													.convertJsonToItem(userObject);
											Preferences.setAccountUser(user,
													token);
											Intent data = new Intent();
											data.putExtra("account",
													user.getAccount());
											setResult(Activity.RESULT_OK, data);
											// 下面这两行实现的功能是关闭键盘
											InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
											imm.hideSoftInputFromWindow(
													pwd.getWindowToken(), 0);
											finish();
										} else {
											String msg = rlt
													.getString(Constant.JSON_KEY_MSG);
											Toast.makeText(getBaseContext(),
													msg, Toast.LENGTH_LONG)
													.show();
										}
									} catch (JSONException e) {
										e.printStackTrace();
									}
								}
							}));
		}
	}

	
	public void onBackClick(View v){
		finish();
	}
	
	/* 找回密码 */
	public void onBackPasswordClick(View v) {
		jumpActivity(1);
	}

	/* 点击注册时触发的事件 */
	public void onRegisteClick(View v) {
		jumpActivity(2);
	}

	/* 跳转Activity */
	private void jumpActivity(int type) {
		Intent intent = new Intent(LoginActivity.this,
				ProfileRegisteBackPasswordInputAccountActivity.class);
		Bundle data = new Bundle();
		data.putSerializable("type", type);
		intent.putExtras(data);
		startActivityForResult(intent, 0);
	}

	/* 返回上一页 */
	public void back(View v) {
		finish();
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
		Toast.makeText(getBaseContext(),"123",Toast.LENGTH_LONG).show();
		scrollView.post(new Runnable() { 
			@Override   
			public void run() { 
		        scrollView.fullScroll(ScrollView.FOCUS_DOWN); 
		       } 
		});
	}



	

}
