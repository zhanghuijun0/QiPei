package com.gammainfo.qipei;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class ProfileMySettingAboutActivity extends BaseActivity{
	private TextView mProfileMySettingAboutVersion;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.profile_my_setting_about);
		mProfileMySettingAboutVersion = (TextView)findViewById(R.id.profile_my_setting_about_version);
		getSetVersion();
	}
	
	/*获取病设置版本号*/
	public void getSetVersion(){
		try {
			PackageManager manager = this.getPackageManager();
		    PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
		    String version = info.versionName;
		    mProfileMySettingAboutVersion.setText(getString(R.string.profile_label_my_setting_about_soft_version_text)+version);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	/*点击网址的时候执行的“调用系统浏览器的”操作*/
	public void onUrlClick(View v){
		Intent intent= new Intent();        
	    intent.setAction("android.intent.action.VIEW");    
	    Uri content_url = Uri.parse("http://www.chinaqszx.com");   
	    intent.setData(content_url);  
	    startActivity(intent);
	}
	
	
	
	/*返回上一页*/
	public void onBackClick(View v){
		finish();
	}
}
