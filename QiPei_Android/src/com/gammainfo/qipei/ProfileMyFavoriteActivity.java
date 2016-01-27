package com.gammainfo.qipei;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.gammainfo.qipei.db.Preferences;
import com.gammainfo.qipei.http.AsyncHttpResponseHandler;
import com.gammainfo.qipei.http.RequestParams;
import com.gammainfo.qipei.http.RestClient;
import com.gammainfo.qipei.utils.Constant;
import com.loopj.android.http.JsonHttpResponseHandler;

public class ProfileMyFavoriteActivity extends BaseActivity{
	private TextView mProfileMyFavoriteVendorNum;
	private TextView mProfileMyFavoriteDealersNum;
	private TextView mProfileMyFavoriteTerminalCustomersNum;
	private TextView mProfileMyFavoriteSupplyNum;
	private TextView mProfileMyFavoriteDemandNum;
	private int VendorNum=0;
	private int DealersNum=0;
	private int TerminalCustomersNum=0;
	private int SupplyNum=0;
	private int DemandNum=0;
	private int VEDOR_TYPE_NUM=1;
	private int DEALERS_TYPE_NUM=2;
	private int TERMINAL_CUSTOMERS_TYPE_Num=3;
	private int SUPPLY_TYPE_NUM=4;
	private int DEMAND_TYPE_NUM=5;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.profile_my_favorite);
		mProfileMyFavoriteVendorNum = (TextView)findViewById(R.id.profile_my_favorite_vendor_num);
		mProfileMyFavoriteDealersNum = (TextView)findViewById(R.id.profile_my_favorite_dealers_num);
		mProfileMyFavoriteTerminalCustomersNum = (TextView)findViewById(R.id.profile_my_favorite_terminal_customers_num);
		mProfileMyFavoriteSupplyNum = (TextView)findViewById(R.id.profile_my_favorite_supply_num);
		mProfileMyFavoriteDemandNum = (TextView)findViewById(R.id.profile_my_favorite_demand_num);
		getFavoriteTypeNum();
	}
	
	/*获取网上收藏的各个类型的数量*/
	public void getFavoriteTypeNum(){
		int id = Preferences.getAccountUserId();
		RequestParams params = new RequestParams();
		params.put("id",String.valueOf(id));
		RestClient.post(Constant.API_GET_FAVORITE_TYPE_NUM, params,
				new AsyncHttpResponseHandler(getBaseContext(),
						new JsonHttpResponseHandler() {
							@Override
							public void onSuccess(JSONObject rlt) {
									try {
										if (rlt.getInt(Constant.JSON_KEY_CODE) == Constant.CODE_SUCCESS) {
											JSONObject info = rlt.getJSONObject("info");
											JSONArray list = info.getJSONArray("list");
											for(int i=0;i<list.length();i++){
												JSONObject item = (JSONObject) list.get(i);
												if(item.getInt("type")==1){
													VendorNum = item.getInt("num");
												}else if(item.getInt("type")==2){
													DealersNum = item.getInt("num");
												}else if(item.getInt("type")==3){
													TerminalCustomersNum = item.getInt("num");
												}else if(item.getInt("type")==4){
													SupplyNum = item.getInt("num");
												}else if(item.getInt("type")==5){
													DemandNum = item.getInt("num");
												}
											}
											setFavoriteTypeNum();
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
	
	/*设置本地显示的收藏数量*/
	public void setFavoriteTypeNum(){
		mProfileMyFavoriteVendorNum.setText(String.valueOf(VendorNum));
		mProfileMyFavoriteDealersNum.setText(String.valueOf(DealersNum));
		mProfileMyFavoriteTerminalCustomersNum.setText(String.valueOf(TerminalCustomersNum));
		mProfileMyFavoriteSupplyNum.setText(String.valueOf(SupplyNum));
		mProfileMyFavoriteDemandNum.setText(String.valueOf(DemandNum));
	}
	
	/*点击收藏的的厂商执行的操作*/
	public void onFavoriteVendorClick(View v){
		jumpActivity(VEDOR_TYPE_NUM);
	}
	/*点击收藏的的经销商执行的操作*/
	public void onFavoriteDealersClick(View v){
		jumpActivity(DEALERS_TYPE_NUM);
	}
	/*点击收藏的的终端客户执行的操作*/
	public void onFavoriteTerminalCustomersClick(View v){
		jumpActivity(TERMINAL_CUSTOMERS_TYPE_Num);
	}
	/*点击收藏的的供应执行的操作*/
	public void onFavoriteSupplyClick(View v){
		//Toast.makeText(getBaseContext(),"你点击了收藏的供应！",Toast.LENGTH_LONG).show();
		jumpActivity(SUPPLY_TYPE_NUM);
	}
	/*点击收藏的的求购执行的操作*/
	public void onFavoriteDemandClick(View v){
		jumpActivity(DEMAND_TYPE_NUM);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		getFavoriteTypeNum();
	}
	
	/*跳转Activity*/
	public void jumpActivity(int type){
		Intent intent = new Intent(ProfileMyFavoriteActivity.this,ProfileFavoriteTypeActivity.class);
		intent.putExtra("type",type);
		startActivityForResult(intent, 0);
	}

	/*返回按钮*/
	public void onMyFavoriteBackClick(View v){
		finish();
	}
	
	
}
