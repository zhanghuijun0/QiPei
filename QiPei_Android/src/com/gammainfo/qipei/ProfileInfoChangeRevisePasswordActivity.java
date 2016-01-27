package com.gammainfo.qipei;

import org.json.JSONException;
import org.json.JSONObject;

import com.gammainfo.qipei.convert.UserJSONConvert;
import com.gammainfo.qipei.db.Preferences;
import com.gammainfo.qipei.http.AsyncHttpResponseHandler;
import com.gammainfo.qipei.http.RequestParams;
import com.gammainfo.qipei.http.RestClient;
import com.gammainfo.qipei.model.User;
import com.gammainfo.qipei.utils.Constant;
import com.gammainfo.qipei.utils.Md5Util;
import com.loopj.android.http.JsonHttpResponseHandler;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ProfileInfoChangeRevisePasswordActivity extends BaseActivity{
	private EditText mProfileRevisePasswordInputOldPassword;
	private EditText mProfileRevisePasswordInputNewPassword;
	private Button mProfileRevisePostPasswordSure;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.profile_my_info_change_revise_password_activity);
		mProfileRevisePasswordInputOldPassword = (EditText)findViewById(R.id.profile_revise_password_input_old_password);
		mProfileRevisePasswordInputNewPassword = (EditText)findViewById(R.id.profile_revise_password_input_new_password);
		mProfileRevisePostPasswordSure = (Button)findViewById(R.id.profile_revise_post_password_sure);
	}
	
	/*当点击“确认”提交密码时执行的操作*/
	public void onRevisePasswordSureClick(View v){
		String oldPassword = (mProfileRevisePasswordInputOldPassword.getText()).toString();
		String newPassword = (mProfileRevisePasswordInputNewPassword.getText()).toString();
		if(oldPassword.length()!=0 && newPassword.length()!=0){
			mProfileRevisePostPasswordSure.setText(getString(R.string.profile_label_revise_password_postting_text));
			mProfileRevisePostPasswordSure.setClickable(false);
			reviseInternetPassword(oldPassword,newPassword);
		}else{
			Toast.makeText(getBaseContext(), getString(R.string.profile_toast_please_perfect_password_text), Toast.LENGTH_SHORT).show();
		}
	}
	
	/*修改网上数据库的密码*/
	public void reviseInternetPassword(String oldPassword,String newPassword){
		int id = Preferences.getAccountUserId();
		RequestParams params = new RequestParams();
		params.put("id",String.valueOf(id));
		params.put("old_pwd",Md5Util.md5(oldPassword));
		params.put("new_pwd",Md5Util.md5(newPassword));
		RestClient.post(Constant.API_UPDATE_PWD, params,
				new AsyncHttpResponseHandler(getBaseContext(),
						new JsonHttpResponseHandler() {
							@Override
							public void onSuccess(JSONObject rlt) {
									try {
										mProfileRevisePostPasswordSure.setText(getString(R.string.profile_label_sure_text));
										mProfileRevisePostPasswordSure.setClickable(true);
										if(rlt.getInt(Constant.JSON_KEY_CODE)==Constant.CODE_SUCCESS){
											Toast.makeText(getBaseContext(), getString(R.string.profile_toast_revise_password_success_text), Toast.LENGTH_SHORT).show();
											finish();
										}else{
											String msg = rlt.getString(Constant.JSON_KEY_MSG);
											Toast.makeText(getBaseContext(),msg,Toast.LENGTH_LONG).show();
										}
									} catch (JSONException e) {
										e.printStackTrace();
									}
							}
				})
		);
	}
	
	/*返回上一页*/
	public void onBackClick(View v){
		finish();
	}
	
}
