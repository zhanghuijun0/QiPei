package com.gammainfo.qipei;

import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.gammainfo.qipei.db.Preferences;
import com.gammainfo.qipei.http.AsyncHttpResponseHandler;
import com.gammainfo.qipei.http.RequestParams;
import com.gammainfo.qipei.http.RestClient;
import com.gammainfo.qipei.model.User;
import com.gammainfo.qipei.utils.Constant;
import com.loopj.android.http.JsonHttpResponseHandler;

public class ProfileReleaseActivity extends BaseActivity{
	private RelativeLayout mProfileRelativeLayoutHint;
	private EditText mProfileMyReleaseTitle;
	private EditText mProfileInputDetailContent;
	private TextView mProfileShowArea;
	private RadioGroup mProfileSuppleDemandSelectType;
	private int mReleaseType=1;
	private int id;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.profile_release_activity);
		mProfileRelativeLayoutHint = (RelativeLayout)findViewById(R.id.profile_relative_layout_hint);
		mProfileMyReleaseTitle = (EditText)findViewById(R.id.profile_my_release_title);
		mProfileShowArea = (TextView)findViewById(R.id.profile_show_area);
		mProfileInputDetailContent = (EditText)findViewById(R.id.profile_input_detail_content);
		mProfileSuppleDemandSelectType = (RadioGroup)findViewById(R.id.profile_supple_demand_select_type);
		mProfileSuppleDemandSelectType.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId) {
					case R.id.profile_I_will_release_supply:
						mReleaseType = 1;
						break;
					case R.id.profile_I_will_release_demand:
						mReleaseType = 2;
						break;
					}
				}
		});
		
		mProfileMyReleaseTitle.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void afterTextChanged(Editable s) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,int count) {
				if (!TextUtils.isEmpty(s.toString())) {
					mProfileRelativeLayoutHint.setVisibility(View.INVISIBLE);
				} else {
					mProfileRelativeLayoutHint.setVisibility(View.VISIBLE);
					
				}
				
			}
			
		});
		//mProfileMyReleaseTitle.clearFocus();
		mProfileMyReleaseTitle.setOnFocusChangeListener(new OnFocusChangeListener(){
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus && mProfileMyReleaseTitle.getText().length() == 0) {// 得到焦点
					mProfileRelativeLayoutHint.setVisibility(View.VISIBLE);
                }
			}
		});
		
		/*当点击“请输入详细内容的页面时触发的事件”*/
		mProfileInputDetailContent.setOnFocusChangeListener(new OnFocusChangeListener(){
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus && mProfileInputDetailContent.getText().length() == 0) {// 失去焦点
					mProfileInputDetailContent.setHint(R.string.profile_label_input_detail_content_text);
					mProfileInputDetailContent.setGravity(Gravity.CENTER);
                }else{
                	mProfileInputDetailContent.setHint(null);
					mProfileInputDetailContent.setGravity(Gravity.LEFT);
                	
                }
			}
		});
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == 0 && resultCode ==Activity.RESULT_OK){
			mProfileShowArea.setText(data.getStringExtra(AreaBaseActivity.EXTRA_CITY));
		}
	}
	
	/*当点击选择地址的时候执行的操作*/
	public void onAreaClick(View v){
		Intent intent = new Intent(ProfileReleaseActivity.this, ProvinceActivity.class);
		intent.putExtra(AreaBaseActivity.EXTRA_LEVEL,AreaBaseActivity.LEVEL_PROVINCE_CITY);
		startActivityForResult(intent, 0);
	}
	/*当点击请输入标题时执行的操作*/
	public void onRelativeLayoutClick(View v){
		mProfileRelativeLayoutHint.setVisibility(View.GONE);
		mProfileMyReleaseTitle.setFocusable(true);
		mProfileMyReleaseTitle.setFocusableInTouchMode(true);
		mProfileMyReleaseTitle.requestFocus();
		mProfileMyReleaseTitle.requestFocusFromTouch();
	}
	
	/*当点击确认发布按钮时执行的操作*/
	public void onSureReleaseClick(View v){
		if(mProfileMyReleaseTitle.getText().length()!=0 && mProfileInputDetailContent.getText().length()!=0 && mProfileShowArea.getText().length()!=0){
			releaseSuplyDemandInfoInternet();
		}else{
			Toast.makeText(getBaseContext(), getString(R.string.profile_toast_release_information_un_complete_text), Toast.LENGTH_SHORT).show();
		}
	}
	
	/*发布到网上数据库供求信息*/
	public void releaseSuplyDemandInfoInternet(){
		RequestParams params = new RequestParams();
		params.put("id",String.valueOf(Preferences.getAccountUserId()));
		params.put("type",String.valueOf(mReleaseType));
		params.put("title",String.valueOf(mProfileMyReleaseTitle.getText()));
		params.put("area",String.valueOf(mProfileShowArea.getText()));
		params.put("content",String.valueOf(mProfileInputDetailContent.getText()));
		RestClient.post(Constant.API_PUBLISH_SUPPLY_DEMAND, params,
				new AsyncHttpResponseHandler(getBaseContext(),
						new JsonHttpResponseHandler() {
							@Override
							public void onSuccess(JSONObject rlt) {
								try {
									if(rlt.getInt(Constant.JSON_KEY_CODE)==Constant.CODE_SUCCESS){
										JSONObject info = rlt.getJSONObject("info");
										id = info.getInt("id");
										Toast.makeText(getBaseContext(), getString(R.string.profile_toast_release_success_text), Toast.LENGTH_SHORT).show();
										jumpActivity(mReleaseType);
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
	
	/*结束非当前Actviity*/
	public void jumpActivity(int type){
		Intent intent = new Intent();
		intent.putExtra("type", type);
		intent.putExtra("id", id);
		intent.putExtra("title", (mProfileMyReleaseTitle.getText()).toString());
		intent.putExtra("area", (mProfileShowArea.getText()).toString());
		intent.putExtra("content", (mProfileInputDetailContent.getText()).toString());
		setResult(Activity.RESULT_OK,intent);
		finish();
	}
}
