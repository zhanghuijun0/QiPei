package com.gammainfo.qipei;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.gammainfo.qipei.http.AsyncHttpResponseHandler;
import com.gammainfo.qipei.http.RequestParams;
import com.gammainfo.qipei.http.RestClient;
import com.gammainfo.qipei.utils.Constant;
import com.loopj.android.http.JsonHttpResponseHandler;

public class ProfileRegisteBackPasswordInputValidCodeActivity extends
		BaseActivity {
	private TextView mTime;
	private EditText mProfileInputValidCodeEditText;
	private View mDelValidCodeNameBtn;
	private int TIME = 60;
	private int mNum = TIME;
	private Button mProfileRepostButton;
	private Button mProfileRegisteInputValidCodeNext;
	private String account;
	private String API;
	private String API2;
	private int type;
	private int HANDLECODE = 0x123;

	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == HANDLECODE) {
				mTime.setText(String.valueOf(mNum));
				if (mNum > 0) {
					mNum--;
					handler.sendEmptyMessageDelayed(HANDLECODE, 1000);
				} else {
					mProfileRepostButton.setEnabled(true);
				}
			}
			super.handleMessage(msg);
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.profile_registe_back_password_input_valid_code);
		Intent intent = getIntent();
		account = (String) intent.getSerializableExtra("account");
		type = (Integer) intent.getSerializableExtra("type");
		mTime = (TextView) findViewById(R.id.profile_get_valid_code_time);
		mProfileInputValidCodeEditText = (EditText) findViewById(R.id.profile_input_valid_code_edittext);
		mProfileRegisteInputValidCodeNext = (Button) findViewById(R.id.profile_registe_input_valid_code_get_password);
		mProfileRepostButton = (Button) findViewById(R.id.profile_repost_button);
		mDelValidCodeNameBtn = findViewById(R.id.ibtn_input_valid_code_delusername);
		mProfileRegisteInputValidCodeNext.setEnabled(false);
		mTime.setText(String.valueOf(mNum));

		mProfileInputValidCodeEditText
				.addTextChangedListener(new TextWatcher() {

					@Override
					public void afterTextChanged(Editable s) {
						if (!TextUtils.isEmpty(s.toString())) {
							mDelValidCodeNameBtn.setVisibility(View.VISIBLE);
						} else {
							mDelValidCodeNameBtn.setVisibility(View.INVISIBLE);
						}
					}

					@Override
					public void beforeTextChanged(CharSequence s, int start,
							int count, int after) {
					}

					@Override
					public void onTextChanged(CharSequence s, int start,
							int before, int count) {
					}
				});

	}

	public void onDelValidCodeNameClick(View v) {
		mDelValidCodeNameBtn.setVisibility(View.INVISIBLE);
		mProfileInputValidCodeEditText.getText().clear();
	}

	/* 点击“获取密码”或“下一步”时的操作，因为当type=1和type=2时按钮上的字分别为“获取密码”，“下一步” */
	public void onInputValidCodeClick(View v) {

		String mValidCode = (mProfileInputValidCodeEditText.getText())
				.toString();
		if (mValidCode.length() == 6) { // 验证码不能为空并且得为6位
			if (type == 1) {
				API = Constant.API_VALID_FORGET_PWD_CODE;
			} else if (type == 2) {
				API = Constant.API_VALID_REG_CODE;
			}
			getInternetPassword(mValidCode);
			mProfileRegisteInputValidCodeNext.setEnabled(false);
		} else {
			Toast.makeText(
					ProfileRegisteBackPasswordInputValidCodeActivity.this,
					R.string.profile_toast_please_input_six_valid_code_text,
					Toast.LENGTH_SHORT).show();
		}
	}

	/* 点击“重新发送验证码”时触发的事件 */
	public void onRepostClick(View v) {
		handler.sendEmptyMessage(HANDLECODE);
		mNum = TIME;
		mProfileRegisteInputValidCodeNext.setEnabled(true);
		mProfileRepostButton.setEnabled(false);
		mProfileRepostButton
				.setText(getString(R.string.profile_label_repost_valid_code_text));
		reGetPassword();
	}

	/* 验证网上数据库的验证码 */
	public void getInternetPassword(String mValidCode) {
		RequestParams params = new RequestParams();
		params.put("account", account);
		params.put("validcode", mValidCode);
		RestClient.post(API, params, new AsyncHttpResponseHandler(
				ProfileRegisteBackPasswordInputValidCodeActivity.this,
				new JsonHttpResponseHandler() {

					@Override
					public void onFinish() {
						mProfileRegisteInputValidCodeNext
								.setText(getString(R.string.profile_label_next_text));
						mProfileRegisteInputValidCodeNext.setEnabled(true);
						super.onFinish();
					}

					@Override
					public void onStart() {
						mProfileRegisteInputValidCodeNext
								.setText(getString(R.string.profile_label_valid_code_validing_text));
						mProfileRegisteInputValidCodeNext.setEnabled(false);
						super.onStart();
					}

					@Override
					public void onSuccess(JSONObject rlt) {
						try {

							if (rlt.getInt(Constant.JSON_KEY_CODE) == Constant.CODE_SUCCESS) {
								JSONObject info = rlt.getJSONObject("info");
								int id = info.getInt("id");
								Intent intent = new Intent(
										ProfileRegisteBackPasswordInputValidCodeActivity.this,
										ProfileRegisteSetPasswordActivity.class);
								Bundle data = new Bundle();
								data.putSerializable("id", String.valueOf(id));
								data.putSerializable("account", account);
								intent.putExtras(data);
								startActivityForResult(intent, 0);
							} else {
								String msg = rlt
										.getString(Constant.JSON_KEY_MSG);
								Toast.makeText(
										ProfileRegisteBackPasswordInputValidCodeActivity.this,
										msg, Toast.LENGTH_LONG).show();
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				}));
	}

	/* 重新发送验证码 */
	public void reGetPassword() {
		if (type == 1) {
			API2 = Constant.API_GET_FORGET_PWD_CODE;
		} else if (type == 2) {
			API2 = Constant.API_GET_REG_CODE;
		}
		RequestParams params = new RequestParams();
		params.put("account", account);
		RestClient.post(API2, params, new AsyncHttpResponseHandler(
				ProfileRegisteBackPasswordInputValidCodeActivity.this,
				new JsonHttpResponseHandler() {
					@Override
					public void onSuccess(JSONObject rlt) {
						try {

							if (rlt.getInt(Constant.JSON_KEY_CODE) != Constant.CODE_SUCCESS) {
								mProfileRegisteInputValidCodeNext
										.setText(getString(R.string.profile_label_next_text));
								mProfileRegisteInputValidCodeNext
										.setEnabled(false);
								handler.removeMessages(HANDLECODE);
								mNum = TIME;
								mTime.setText(String.valueOf(TIME));
								mProfileRepostButton.setEnabled(true);

								String msg = rlt
										.getString(Constant.JSON_KEY_MSG);
								Toast.makeText(
										ProfileRegisteBackPasswordInputValidCodeActivity.this,
										msg, Toast.LENGTH_SHORT).show();
							}else{
								Toast.makeText(
										ProfileRegisteBackPasswordInputValidCodeActivity.this,
										R.string.common_toast_send_sms_code, Toast.LENGTH_SHORT).show();
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
		if (requestCode == 0
				&& resultCode == Activity.DEFAULT_KEYS_SEARCH_LOCAL) {
			Intent intent = new Intent();
			setResult(Activity.DEFAULT_KEYS_SEARCH_LOCAL, intent);
			finish();
		}
	}

	/* 返回登录页面 */
	public void backLogin() {
		Intent intent = getIntent();
		setResult(Activity.DEFAULT_KEYS_SHORTCUT, intent);
		finish();
	}

	/* 返回按钮 */
	public void onInputValidCodeBackClick(View v) {
		finish();
	}

	@Override
	protected void onDestroy() {
		handler.removeMessages(HANDLECODE);
		super.onDestroy();
	}

}
