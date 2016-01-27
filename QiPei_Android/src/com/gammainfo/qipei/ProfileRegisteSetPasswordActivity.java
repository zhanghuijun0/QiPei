package com.gammainfo.qipei;

import org.json.JSONException;
import org.json.JSONObject;

import com.gammainfo.qipei.http.AsyncHttpResponseHandler;
import com.gammainfo.qipei.http.RequestParams;
import com.gammainfo.qipei.http.RestClient;
import com.gammainfo.qipei.utils.Constant;
import com.gammainfo.qipei.utils.Md5Util;
import com.loopj.android.http.JsonHttpResponseHandler;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ProfileRegisteSetPasswordActivity extends BaseActivity{
	private EditText mProfileInputNewPassword;
	private EditText mProfileInputNewPasswordAgain;
	private Button mProfileSettingPasswordSure;
	private String id;
	private String account;
	private String pwd;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.profile_registe_set_password_activity);
		mProfileSettingPasswordSure = (Button)findViewById(R.id.profile_setting_password_sure);
		Intent intent = getIntent();
		id = (String)intent.getSerializableExtra("id");
		account = (String)intent.getSerializableExtra("account");
		mProfileInputNewPassword = (EditText)findViewById(R.id.profile_input_new_password);
		mProfileInputNewPasswordAgain = (EditText)findViewById(R.id.profile_input_new_password_again);
	}
	
	/*返回上一页*/
	public void onSetPasswordBackClick(View v){
		finish();
	}
	
	/*确认设置的密码*/
	public void onSetPasswordSureClick(View v){
		pwd = (mProfileInputNewPassword.getText()).toString();
		String againPwd = (mProfileInputNewPasswordAgain.getText()).toString();
		if(pwd.equals(againPwd)){
			if(pwd.length()!=0 || againPwd.length()!=0){
				setInternetPassword();
			}else{
				Toast.makeText(getBaseContext(), getString(R.string.profile_toast_please_perfect_password_text), Toast.LENGTH_SHORT).show();
			}
		}else{
			Toast.makeText(getBaseContext(), getString(R.string.profile_toast_password_unsame_text), Toast.LENGTH_SHORT).show();
		}
	}
	
	/*设置网上数据库的密码*/
	public void setInternetPassword(){
		RequestParams params = new RequestParams();
		params.put("id",id);
		params.put("account",account);
		params.put("pwd",Md5Util.md5(pwd));
		RestClient.post(Constant.API_SET_PWD, params,
				new AsyncHttpResponseHandler(getBaseContext(),
						new JsonHttpResponseHandler() {
					
							@Override
							public void onFinish() {
								mProfileSettingPasswordSure.setText(getString(R.string.profile_label_password_postting_text));
								mProfileSettingPasswordSure.setClickable(true);
								super.onFinish();
							}
		
							@Override
							public void onStart() {
								mProfileSettingPasswordSure.setText(getString(R.string.profile_label_password_postting_text));
								mProfileSettingPasswordSure.setClickable(false);
								super.onStart();
							}
							
							@Override
							public void onSuccess(JSONObject rlt) {
								try {
									
									if(rlt.getInt(Constant.JSON_KEY_CODE)==Constant.CODE_SUCCESS){
										Toast.makeText(getBaseContext(), getString(R.string.profile_toast_set_password_success_to_login_text), Toast.LENGTH_SHORT).show();
										jumpActivity();
									}else{
										String msg = rlt.getString(Constant.JSON_KEY_MSG);
										Toast.makeText(getBaseContext(),msg,Toast.LENGTH_LONG).show();
									}
									/*else{
										Toast.makeText(getBaseContext(), getString(R.string.profile_toast_set_password_failure_text), Toast.LENGTH_SHORT).show();
									}*/
								} catch (JSONException e) {
									e.printStackTrace();
								}
							}
				})
		);
	}
	
	/*当设置密码成功时跳转Activity，去登陆*/
	public void jumpActivity(){
		Intent intent = new Intent();
		setResult(Activity.DEFAULT_KEYS_SEARCH_LOCAL,intent);
		finish();
	}
}
