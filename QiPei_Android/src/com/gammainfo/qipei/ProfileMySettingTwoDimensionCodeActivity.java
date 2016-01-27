package com.gammainfo.qipei;

import android.os.Bundle;
import android.view.View;

public class ProfileMySettingTwoDimensionCodeActivity extends BaseActivity{
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.profile_my_setting_two_dimension_code_activity);
	}
	
	/*返回上一页*/
	public void onBackClick(View v){
		finish();
	}
	
}
