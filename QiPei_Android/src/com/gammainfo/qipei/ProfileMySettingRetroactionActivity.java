package com.gammainfo.qipei;

import org.json.JSONException;
import org.json.JSONObject;

import com.gammainfo.qipei.db.Preferences;
import com.gammainfo.qipei.http.AsyncHttpResponseHandler;
import com.gammainfo.qipei.http.RequestParams;
import com.gammainfo.qipei.http.RestClient;
import com.gammainfo.qipei.model.User;
import com.gammainfo.qipei.utils.Constant;
import com.loopj.android.http.JsonHttpResponseHandler;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class ProfileMySettingRetroactionActivity extends BaseActivity{
	private EditText mProfileRetroactionEdittext;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.profile_my_setting_retroaction_activity);
	}
	
	/*点击“确认发送”时执行的操作*/
	public void onSurePostClick(View v){
		mProfileRetroactionEdittext = (EditText)findViewById(R.id.profile_retroaction_edittext);
		postInternetFeedBack();
	}
	
	private void postInternetFeedBack(){
		int id = Preferences.getAccountUserId();
		RequestParams params = new RequestParams();
		params.put("user_id",String.valueOf(id));
		params.put("content",String.valueOf(mProfileRetroactionEdittext.getText()));
		RestClient.post(Constant.API_POST_FEEDBACK, params,
				new AsyncHttpResponseHandler(getBaseContext(),
						new JsonHttpResponseHandler() {
							@Override
							public void onSuccess(JSONObject rlt) {
									try {
										if(rlt.getInt(Constant.JSON_KEY_CODE) == Constant.CODE_SUCCESS){
											Toast.makeText(getBaseContext(), getString(R.string.profile_toast_retroaction_success_text), Toast.LENGTH_SHORT).show();
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
