package com.gammainfo.qipei;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Toast;

import com.gammainfo.qipei.http.AsyncHttpResponseHandler;
import com.gammainfo.qipei.http.RequestParams;
import com.gammainfo.qipei.http.RestClient;
import com.gammainfo.qipei.utils.Constant;
import com.loopj.android.http.JsonHttpResponseHandler;

public class ProfileRegisteBackPasswordInputAccountActivity extends
		BaseActivity {
	private EditText mProfileRegisteInputAccount;
	private Button mNextButton;
	private CheckBox mIsAgree;
	private View mDelUserNameBtn;
	private int type;
	private String API;
	private String mAccount;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.profile_registe_input_account_activity);
		type = (Integer) getIntent().getSerializableExtra("type");
		mProfileRegisteInputAccount = (EditText) findViewById(R.id.profile_registe_input_account);
		mNextButton = (Button) findViewById(R.id.profile_input_account_next);
		mIsAgree = (CheckBox) findViewById(R.id.profile_registe_agree);
		mDelUserNameBtn = findViewById(R.id.ibtn_input_account_delusername);
		mProfileRegisteInputAccount.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable s) {
				if (!TextUtils.isEmpty(s.toString().trim())) {
					mDelUserNameBtn.setVisibility(View.VISIBLE);
					if (mIsAgree.isChecked()) {
						mNextButton.setEnabled(true);
					} else {
						mNextButton.setEnabled(false);
					}
				} else {
					mDelUserNameBtn.setVisibility(View.INVISIBLE);
					mNextButton.setEnabled(false);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
			}

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {

			}
		});
		mIsAgree.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				String account = (mProfileRegisteInputAccount.getText())
						.toString().trim();
				if (isChecked && account.length() > 0) {
					mNextButton.setEnabled(true);
				} else {
					mNextButton.setEnabled(false);
				}
			}
		});
	}

	public void onDelUserNameClick(View v) {
		mDelUserNameBtn.setVisibility(View.INVISIBLE);
		mProfileRegisteInputAccount.getText().clear();
	}

	/* 点击“下一步”时执行的操作 */
	public void onInputAccountNextClick(View v) {
		// String account = (mProfileRegisteInputAccount.getText()).toString();
		String account = (mProfileRegisteInputAccount.getText()).toString()
				.trim();

		if (!isMobileNum(account)) {
			Toast.makeText(getBaseContext(),
					getString(R.string.profile_toast_mobile_invalid),
					Toast.LENGTH_SHORT).show();
			return;
		}
		mAccount = account;
		if (mIsAgree.isChecked()) {
			if(type == 2){
				validIsUserExists();
			}else{
				jumpActivity();
			}
		} else {
			Toast.makeText(getBaseContext(),
					getString(R.string.profile_toast_you_not_agree_text),
					Toast.LENGTH_SHORT).show();
		}
	}
	
	/*注册的时候验证用户是否已经存在*/
	public void validIsUserExists(){
		RequestParams params = new RequestParams();
		params.put("mobile",String.valueOf(mProfileRegisteInputAccount.getText()));
		RestClient.post(Constant.API_CHECK_MOBILE_EXISTS, params,
				new AsyncHttpResponseHandler(getBaseContext(),
						new JsonHttpResponseHandler() {
							@Override
							public void onSuccess(JSONObject rlt) {
								try {
									if (rlt.getInt(Constant.JSON_KEY_CODE) == Constant.CODE_SUCCESS) {
										jumpActivity();
									}else{
										String msg = rlt.getString(Constant.JSON_KEY_MSG);
										Toast.makeText(getBaseContext(), msg,Toast.LENGTH_LONG).show();
									}
								} catch (JSONException e) {
									e.printStackTrace();
								}
							}
				})
		);
	}
	
	
	/* 找回密码时向后台发送账号获取验证码 */
	public void postAccount(String account) {
		RequestParams params = new RequestParams();
		params.put("account", account);
		RestClient.post(API, params, new AsyncHttpResponseHandler(
				getBaseContext(), new JsonHttpResponseHandler() {

					@Override
					public void onFinish() {
						mNextButton
								.setText(getString(R.string.profile_label_next_text));
						mNextButton.setClickable(true);
						super.onFinish();
					}

					@Override
					public void onStart() {
						mNextButton
								.setText(getString(R.string.profile_label_valid_getting_text));
						mNextButton.setClickable(false);
						super.onStart();
					}

					@Override
					public void onSuccess(JSONObject rlt) {
						try {

							if (rlt.getInt(Constant.JSON_KEY_CODE) == Constant.CODE_SUCCESS) {
								jumpActivity();
							} else {
								String msg = rlt
										.getString(Constant.JSON_KEY_MSG);
								Toast.makeText(getBaseContext(), msg,
										Toast.LENGTH_LONG).show();
							}
							/*
							 * else{ Toast.makeText(getBaseContext(),
							 * getString(R
							 * .string.profile_toast_get_valid_code_failure_text
							 * ), Toast.LENGTH_SHORT).show(); }
							 */
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				}));
	}

	/* 跳转Activity */
	public void jumpActivity() {
		Intent intent = new Intent(
				ProfileRegisteBackPasswordInputAccountActivity.this,
				ProfileRegisteBackPasswordInputValidCodeActivity.class);
		Bundle date = new Bundle();
		date.putSerializable("account", mAccount);
		date.putSerializable("type", type);
		intent.putExtras(date);
		startActivityForResult(intent, 0);
	}

	public void onAbleClick(View v) {
		if (mNextButton.isEnabled() == true) {
			mNextButton.setEnabled(false);
		} else {
			mNextButton.setEnabled(true);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 0 && resultCode == Activity.DEFAULT_KEYS_SHORTCUT) {
			finish();
		}
		if (requestCode == 0
				&& resultCode == Activity.DEFAULT_KEYS_SEARCH_LOCAL) {
			finish();
		}
	}

	/* 返回上一页 */
	public void onInputAccountBackClick(View v) {
		finish();
	}

	public boolean isMobileNum(String mobile) {
		Pattern p = Pattern
				.compile("^((13[0-9])|(15[^4,\\D])|(18[0,3-9]))\\d{8}$");
		Matcher m = p.matcher(mobile);
		return m.matches();

	}
}
