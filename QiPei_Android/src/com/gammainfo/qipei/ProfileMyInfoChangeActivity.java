package com.gammainfo.qipei;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gammainfo.qipei.db.Preferences;
import com.gammainfo.qipei.http.AsyncHttpResponseHandler;
import com.gammainfo.qipei.http.RequestParams;
import com.gammainfo.qipei.http.RestClient;
import com.gammainfo.qipei.model.User;
import com.gammainfo.qipei.utils.Constant;
import com.loopj.android.http.JsonHttpResponseHandler;

public class ProfileMyInfoChangeActivity extends BaseActivity{
	private EditText mEditChangeName;
	private TextView mChangeNameTypeText;
	private ImageView mChooseVendor;//厂商对勾
	private ImageView mChooseDealers;//经销商对勾
	private ImageView mChooseTerminalCustomers;//终端客户对勾
	private ImageView mChooseFourS;//4s店对勾
	private ImageView mChooseQuickRepair;//快修店对勾
	private ImageView mChooseRepairFactory;//汽车修理厂对勾
	private ImageView mChooseImproveLooks;//汽车美容店对勾
	
	
	private ImageView mChooseUsualUser;//普通用户对勾
	private ImageView mChooseMiddleUser;//中级用户对勾
	private ImageView mChooseVipUser;//VIP用户对勾
	private String changeName;
	private String API_NAME;
	private String CHANGE_NAME_TEXT;
	private String oldMessage;
	
	private int oldRoleId;
	private int newRoleId;
	private int oldGrade;
	private int newGrade;
	private User user;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.profile_my_info_change_layout);
		user = Preferences.getAccountUser();
		Intent intent = getIntent();
		changeName = (String) intent.getSerializableExtra("changeName");
		mChangeNameTypeText = (TextView) findViewById(R.id.profile_change_name_type_text);
		mEditChangeName = (EditText)findViewById(R.id.profile_my_info_change_new_company_name);
		if(changeName.equals(new String("company_name"))){
			CHANGE_NAME_TEXT=getString(R.string.profile_label_company_name_text);
			mEditChangeName.setSingleLine(true);
			mEditChangeName.setText(user.getCompanyName());
		}else if(changeName.equals(new String("phone"))){
			CHANGE_NAME_TEXT=getString(R.string.profile_label_phone_text);
			mEditChangeName.setText(user.getPhone());
			//InputMethodManager imm = (InputMethodManager)getSystemService(this.INPUT_METHOD_SERVICE);
			//imm.showSoftInputFromInputMethod(mEditChangeName.getWindowToken(), 0);
			mEditChangeName.setInputType(InputType.TYPE_CLASS_NUMBER);
		}else if(changeName.equals(new String("intro"))){
			CHANGE_NAME_TEXT=getString(R.string.profile_label_company_intro_text);
			mEditChangeName.setText(user.getIntro());
		}else if(changeName.equals(new String("user_type"))){
			setContentView(R.layout.profile_my_info_change_user_type_layout);
			oldRoleId = user.getRoleId();
			newRoleId = user.getRoleId();
			mChooseVendor=(ImageView)findViewById(R.id.profile_label_vendor_image);
			mChooseDealers=(ImageView)findViewById(R.id.profile_label_dealers_image);
			mChooseTerminalCustomers=(ImageView)findViewById(R.id.profile_label_terminal_customers_image);
			
			mChooseFourS=(ImageView)findViewById(R.id.profile_label_four_s_image);
			mChooseQuickRepair=(ImageView)findViewById(R.id.profile_label_quick_repair_image);
			mChooseRepairFactory=(ImageView)findViewById(R.id.profile_label_car_repair_factory_image);
			mChooseImproveLooks=(ImageView)findViewById(R.id.profile_label_car_improve_looks_image);
			
			
			
			
			
			if(user.getRoleId()==1){
				mChooseVendor.setVisibility(View.VISIBLE);
			}else if(user.getRoleId()==2){
				mChooseDealers.setVisibility(View.VISIBLE);
			}else if(user.getRoleId()==3){
				mChooseTerminalCustomers.setVisibility(View.VISIBLE);
			}else if(user.getRoleId()==31){
				mChooseFourS.setVisibility(View.VISIBLE);
			}else if(user.getRoleId()==32){
				mChooseQuickRepair.setVisibility(View.VISIBLE);
			}else if(user.getRoleId()==33){
				mChooseRepairFactory.setVisibility(View.VISIBLE);
			}else if(user.getRoleId()==34){
				mChooseImproveLooks.setVisibility(View.VISIBLE);
			}
			
			
			
			
		}else if(changeName.equals(new String("grade"))){
			CHANGE_NAME_TEXT=getString(R.string.profile_label_grade_text);
			setContentView(R.layout.profile_my_info_change_grade_layout);
			oldGrade = user.getMemberGrade();
			newGrade = user.getMemberGrade();
			mChooseUsualUser=(ImageView)findViewById(R.id.profile_usual_user_image);
			mChooseMiddleUser=(ImageView)findViewById(R.id.profile_middle_user_image);
			mChooseVipUser=(ImageView)findViewById(R.id.profile_vip_user_image);
			if(user.getMemberGrade()==1){
				mChooseUsualUser.setVisibility(View.VISIBLE);
			}else if(user.getMemberGrade()==2){
				mChooseMiddleUser.setVisibility(View.VISIBLE);
			}else if(user.getMemberGrade()==3){
				mChooseVipUser.setVisibility(View.VISIBLE);
			}
		}
		oldMessage = (mEditChangeName.getText()).toString(); 
		mChangeNameTypeText.setText(CHANGE_NAME_TEXT);
	}
	
	/*设置账户类型选择厂商*/
	public void onSetVendorClick(View v){
		if(mChooseVendor.getVisibility()==View.INVISIBLE){
			mChooseDealers.setVisibility(View.INVISIBLE);
			mChooseTerminalCustomers.setVisibility(View.INVISIBLE);
			mChooseVendor.setVisibility(View.VISIBLE);
			
			mChooseFourS.setVisibility(View.INVISIBLE);
			mChooseQuickRepair.setVisibility(View.INVISIBLE);
			mChooseRepairFactory.setVisibility(View.INVISIBLE);
			mChooseImproveLooks.setVisibility(View.INVISIBLE);
			oldRoleId = user.getRoleId();
			newRoleId =1;
		}
	}
	
	/*设置账户类型选择经销商*/
	public void onSetDealersClick(View v){
		if(mChooseDealers.getVisibility()==View.INVISIBLE){
			mChooseVendor.setVisibility(View.INVISIBLE);
			mChooseTerminalCustomers.setVisibility(View.INVISIBLE);
			mChooseDealers.setVisibility(View.VISIBLE);
			
			mChooseFourS.setVisibility(View.INVISIBLE);
			mChooseQuickRepair.setVisibility(View.INVISIBLE);
			mChooseRepairFactory.setVisibility(View.INVISIBLE);
			mChooseImproveLooks.setVisibility(View.INVISIBLE);
			oldRoleId = user.getRoleId();
			newRoleId =2;
		}
	}
	
	/*设置账户类型选择终端客户*/
	public void onSetTerminalCustomersClick(View v){
		if(mChooseTerminalCustomers.getVisibility()==View.INVISIBLE){
			mChooseVendor.setVisibility(View.INVISIBLE);
			mChooseDealers.setVisibility(View.INVISIBLE);
			mChooseTerminalCustomers.setVisibility(View.VISIBLE);
			
			mChooseFourS.setVisibility(View.INVISIBLE);
			mChooseQuickRepair.setVisibility(View.INVISIBLE);
			mChooseRepairFactory.setVisibility(View.INVISIBLE);
			mChooseImproveLooks.setVisibility(View.INVISIBLE);
			oldRoleId = user.getRoleId();
			newRoleId =3;
		}
	}
	
	/*设置账户类型选择4s店*/
	public void onFourSClick(View v){
		if(mChooseFourS.getVisibility()==View.INVISIBLE){
			mChooseDealers.setVisibility(View.INVISIBLE);
			mChooseTerminalCustomers.setVisibility(View.INVISIBLE);
			mChooseVendor.setVisibility(View.INVISIBLE);
			
			mChooseFourS.setVisibility(View.VISIBLE);
			mChooseQuickRepair.setVisibility(View.INVISIBLE);
			mChooseRepairFactory.setVisibility(View.INVISIBLE);
			mChooseImproveLooks.setVisibility(View.INVISIBLE);
			oldRoleId = user.getRoleId();
			newRoleId =31;
		}
	}
	
	/*设置账户类型选择快修店*/
	public void onQuickRepairClick(View v){
		if(mChooseQuickRepair.getVisibility()==View.INVISIBLE){
			mChooseDealers.setVisibility(View.INVISIBLE);
			mChooseTerminalCustomers.setVisibility(View.INVISIBLE);
			mChooseVendor.setVisibility(View.INVISIBLE);
			
			mChooseFourS.setVisibility(View.INVISIBLE);
			mChooseQuickRepair.setVisibility(View.VISIBLE);
			mChooseRepairFactory.setVisibility(View.INVISIBLE);
			mChooseImproveLooks.setVisibility(View.INVISIBLE);
			oldRoleId = user.getRoleId();
			newRoleId =32;
		}
	}
	
	/*设置账户类型选择汽车修理厂*/
	public void onRepairFactoryClick(View v){
		if(mChooseRepairFactory.getVisibility()==View.INVISIBLE){
			mChooseDealers.setVisibility(View.INVISIBLE);
			mChooseTerminalCustomers.setVisibility(View.INVISIBLE);
			mChooseVendor.setVisibility(View.INVISIBLE);
			
			mChooseFourS.setVisibility(View.INVISIBLE);
			mChooseQuickRepair.setVisibility(View.INVISIBLE);
			mChooseRepairFactory.setVisibility(View.VISIBLE);
			mChooseImproveLooks.setVisibility(View.INVISIBLE);
			oldRoleId = user.getRoleId();
			newRoleId =33;
		}
	}
	
	/*设置账户类型选择汽车美容店*/
	public void onImproveLooksClick(View v){
		if(mChooseImproveLooks.getVisibility()==View.INVISIBLE){
			mChooseDealers.setVisibility(View.INVISIBLE);
			mChooseTerminalCustomers.setVisibility(View.INVISIBLE);
			mChooseVendor.setVisibility(View.INVISIBLE);
			
			mChooseFourS.setVisibility(View.INVISIBLE);
			mChooseQuickRepair.setVisibility(View.INVISIBLE);
			mChooseRepairFactory.setVisibility(View.INVISIBLE);
			mChooseImproveLooks.setVisibility(View.VISIBLE);
			oldRoleId = user.getRoleId();
			newRoleId =34;
		}
	}
	
	
	
	/*设置本地的role_id*/
	public void saveLocalUserType(int roleId){
		//User user = Preferences.getAccountUser();
		user.setRoleId(roleId);
		if(roleId == 1){
			user.setRoleName(getString(R.string.search_label_manufacturer));
		}else if(roleId == 2){
			user.setRoleName(getString(R.string.search_label_agency));
		}else if(roleId == 31){
			user.setRoleName(getString(R.string.profile_label_four_s_text));
		}else if(roleId == 32){
			user.setRoleName(getString(R.string.profile_label_quick_repair_text));
		}else if(roleId == 33){
			user.setRoleName(getString(R.string.profile_label_car_repair_factory_text));
		}else if(roleId == 34){
			user.setRoleName(getString(R.string.profile_label_car_improve_looks_text));
		}
		user.setIsCertificated(false);
		Preferences.setAccountUser(user, Preferences.getToken());
	}
	
	/*设置网上数据库的role_id即user_type*/
	private void saveInternetUserType(final int roleId, final int oldRoleId){
		int id = Preferences.getAccountUserId();
		RequestParams params = new RequestParams();
		params.put("id",String.valueOf(id));
		params.put("role_id",String.valueOf(roleId));
		RestClient.post(Constant.API_SET_USER_TYPE, params,
				new AsyncHttpResponseHandler(getBaseContext(),
						new JsonHttpResponseHandler() {
							@Override
							public void onSuccess(JSONObject rlt) {
								try {
									if(rlt.getInt(Constant.JSON_KEY_CODE)==Constant.CODE_SUCCESS){
										Toast.makeText(getBaseContext(), getString(R.string.profile_toast_revise_success_text), Toast.LENGTH_SHORT).show();
										saveLocalUserType(roleId);
										finishActivity();
									}else{
										saveLocalUserType(oldRoleId);
										String msg = rlt.getString(Constant.JSON_KEY_MSG);
										Toast.makeText(getBaseContext(),msg,Toast.LENGTH_LONG).show();
									}
								} catch (JSONException e) {
									e.printStackTrace();
								}
							}
							
							@Override
							public void onFailure(Throwable e,
									JSONArray errorResponse) {
								// TODO Auto-generated method stub
								super.onFailure(e, errorResponse);
								saveLocalUserType(oldRoleId);
							}
				})
		);
	}
	
	/*设置会员等级选择普通用户*/
	public void onSetUsualUserClick(View v){
		if(mChooseUsualUser.getVisibility()==View.INVISIBLE){
			mChooseMiddleUser.setVisibility(View.INVISIBLE);
			mChooseVipUser.setVisibility(View.INVISIBLE);
			mChooseUsualUser.setVisibility(View.VISIBLE);
			oldGrade = user.getRoleId();
			newGrade = 1;
			//saveInternetGrade(1,oldGrade);
		}
	}
	
	/*设置会员等级选择中级用户*/
	public void onSetMiddleUserClick(View v){
		if(mChooseMiddleUser.getVisibility()==View.INVISIBLE){
			mChooseUsualUser.setVisibility(View.INVISIBLE);
			mChooseVipUser.setVisibility(View.INVISIBLE);
			mChooseMiddleUser.setVisibility(View.VISIBLE);
			oldGrade = user.getRoleId();
			newGrade = 2;
			//saveInternetGrade(2,oldGrade);
		}
	}
	
	/*设置会员等级选择VIP用户*/
	public void onSetVipUserClick(View v){
		if(mChooseVipUser.getVisibility()==View.INVISIBLE){
			mChooseUsualUser.setVisibility(View.INVISIBLE);
			mChooseMiddleUser.setVisibility(View.INVISIBLE);
			mChooseVipUser.setVisibility(View.VISIBLE);
			oldGrade = user.getRoleId();
			newGrade = 3;
			//saveInternetGrade(3,oldGrade);
		}
	}
	
	
	/*设置本地的grade即用户等级*/
	public void saveLocalGrade(int grade){
		user.setMemberGrade(grade);
		user.setIsCertificated(false);
		Preferences.setAccountUser(user, Preferences.getToken());
	}
	
	/*设置网上数据库的grade即会员等级*/
	private void saveInternetGrade(final int grade, final int oldGrade){
		int id = Preferences.getAccountUserId();
		RequestParams params = new RequestParams();
		params.put("id",String.valueOf(id));
		params.put("grade",String.valueOf(grade));
		RestClient.post(Constant.API_SET_GRADE, params,
				new AsyncHttpResponseHandler(getBaseContext(),
						new JsonHttpResponseHandler() {
							@Override
							public void onSuccess(JSONObject rlt) {
								try {
									if(rlt.getInt(Constant.JSON_KEY_CODE)==Constant.CODE_SUCCESS){
										Toast.makeText(getBaseContext(), getString(R.string.profile_toast_revise_success_text), Toast.LENGTH_SHORT).show();
										saveLocalGrade(grade);
										finishActivity();
									}else{
										saveLocalGrade(oldGrade);
										String msg = rlt.getString(Constant.JSON_KEY_MSG);
										Toast.makeText(getBaseContext(),msg,Toast.LENGTH_LONG).show();
									}
								} catch (JSONException e) {
									e.printStackTrace();
								}
							}

							

							@Override
							public void onFailure(Throwable e,
									JSONArray errorResponse) {
								super.onFailure(e, errorResponse);
								saveLocalGrade(oldGrade);
							}	
				})
		);
	}
	
	/*账户类型的保存修改*/
	public void onSaveUserTypeChangeClick(View v){
		if(newRoleId == oldRoleId){
			finishActivity();
		}else{
			saveInternetUserType(newRoleId,oldRoleId);
		}
	}
	
	/*会员等级的保存修改*/
	public void onSaveGradeChangeClick(View v){
		if(newGrade == oldGrade){
			finishActivity();
		}else{
			saveInternetGrade(newGrade,oldGrade);
		}
	}
	
	/*会员等级的保存修改*/
	public void onProfileSaveChangeClick(View v){
		if(!oldMessage.equals((mEditChangeName.getText()).toString())){
			changeLocalChangeName((mEditChangeName.getText()).toString().trim());
			changeInternetChangeName((mEditChangeName.getText()).toString().trim());
		}
		finishActivity();
	}
	
	/*修改本地数据库的数据,修改“公司名称”和“电话”都是用共用这个方法*/
	private void changeLocalChangeName(String name){
		if(changeName.equals(new String("company_name"))){
			user.setCompanyName(name);
			API_NAME=Constant.API_SET_COMPANY_NAME;
		}else if(changeName.equals(new String("phone"))){
			user.setPhone(name);
			API_NAME=Constant.API_SET_PHONE;
		}else if(changeName.equals(new String("intro"))){
			user.setIntro(name);
			API_NAME=Constant.API_SET_INTRO;
		}
		user.setIsCertificated(false);
		Preferences.setAccountUser(user,Preferences.getToken());
	}
	
	/*修改网上数据库的数据,修改“公司名称”和“电话”都是用共用这个方法*/
	private void changeInternetChangeName(String name){
		int id = Preferences.getAccountUserId();
		RequestParams params = new RequestParams();
		params.put("id",String.valueOf(id));
		params.put(changeName,name);
		RestClient.post(API_NAME, params,
				new AsyncHttpResponseHandler(getBaseContext(),
						new JsonHttpResponseHandler() {
							@Override
							public void onSuccess(JSONObject rlt) {
								try {
									if(rlt.getInt(Constant.JSON_KEY_CODE)==Constant.CODE_SUCCESS){
										Toast.makeText(getBaseContext(), getString(R.string.profile_toast_revise_success_text), Toast.LENGTH_SHORT).show();
									}else{
										Toast.makeText(getBaseContext(), getString(R.string.profile_toast_revise_fail_text), Toast.LENGTH_SHORT).show();
									}
								} catch (JSONException e) {
									e.printStackTrace();
								}
							}
				})
		);
	}
	
	/*修改等级权益*/
	/*public void onSeteQuityClick(View v){
		//Toast.makeText(getBaseContext(), "您点击了等级权益", Toast.LENGTH_SHORT).show();
	}*/
	
	/*返回按钮结束Activity*/
	public void onChangeNameBackClick(View v){
		finishActivity();
	}
	
	/*返回上一页*/
	public void onBackClick(View v){
		finishActivity();
	}
	
	/*结束Activity*/
	private void finishActivity(){
		Intent data = new Intent();
		setResult(Activity.RESULT_OK, data);
		finish();
	}
}
