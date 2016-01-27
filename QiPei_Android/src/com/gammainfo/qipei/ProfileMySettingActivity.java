package com.gammainfo.qipei;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gammainfo.qipei.db.DatabaseManager;
import com.gammainfo.qipei.db.Preferences;
import com.gammainfo.qipei.model.User;
import com.gammainfo.qipei.utils.ToastHelper;
import com.gammainfo.qipei.widget.AlertDialog;
import com.gammainfo.qipei.widget.AlertDialog.OnAlertDialogListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.bean.SocializeEntity;
import com.umeng.socialize.controller.RequestType;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.controller.listener.SocializeListeners.SocializeClientListener;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UmengUpdateListener;
import com.umeng.update.UpdateResponse;
import com.umeng.update.UpdateStatus;

public class ProfileMySettingActivity extends BaseActivity {
	private RelativeLayout mProfileDeleteShareEmpower;
	private TextView mProfileMySettingVersionText;
	private CheckBox mProfileMySettingPushInformationCheckBox;
	private Button mProfileLoginOut;
	private UMSocialService mController;
	private String mMessage;
	private String mSureButtonMessage;
	private String mCancelButtonMessage;
	private ToastHelper mToastHelper;
	private User user;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.profile_my_setting);
		user = Preferences.getAccountUser();
		mProfileDeleteShareEmpower = (RelativeLayout)findViewById(R.id.profile_delete_share_empower);
		mProfileMySettingVersionText = (TextView) findViewById(R.id.profile_my_setting_version_text);
		mProfileMySettingPushInformationCheckBox = (CheckBox) findViewById(R.id.profile_my_setting_push_information_check_box);
		mProfileLoginOut = (Button) findViewById(R.id.profile_login_out);
		if (Preferences.isLogin() == false) {
			mProfileLoginOut.setVisibility(View.GONE);
		}
		PackageManager manager = this.getPackageManager();
		try {
			PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
			String version = info.versionName;
			mProfileMySettingVersionText.setText(version);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		mProfileMySettingPushInformationCheckBox.setChecked(Preferences
				.getIsPushInformation());
		mProfileMySettingPushInformationCheckBox
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						Preferences.setIsPushInformation(isChecked);
					}
				});
		
	}

	/* 退出登录 */
	public void onLoginOutClick(View v) {
		mMessage = getString(R.string.profile_label_dialog_message_is_login_out_text);
		mSureButtonMessage = getString(R.string.profile_label_dialog_sure_button_login_out_text);
		mCancelButtonMessage = getString(R.string.profile_label_dialog_cancel_button_cancel_text);
		AlertDialog alertDialogView = AlertDialog.build(this, mMessage,
				mSureButtonMessage, mCancelButtonMessage,
				new OnAlertDialogListener() {

					@Override
					public void onOk(AlertDialog alertDialogView) {
						Preferences.loginOutClearUserInfo();
						DatabaseManager.clear();
						Intent intent = new Intent();
						setResult(Activity.RESULT_OK, intent);
						finish();
						Toast.makeText(
								getBaseContext(),
								getString(R.string.profile_toast_login_out_success_text),
								Toast.LENGTH_SHORT).show();
						alertDialogView.dismiss();
					}

					@Override
					public void onCancel(AlertDialog alertDialogView) {
						alertDialogView.dismiss();
					}
				});
		alertDialogView.show();
	}
	
	/*删除授权*/
	public void onDeleteClick(View v){
		mMessage = getString(R.string.profile_label_is_share_empower_text);
		mSureButtonMessage = getString(R.string.profile_label_yes_text);
		mCancelButtonMessage = getString(R.string.profile_label_no_text);
		AlertDialog alertDialogView = AlertDialog.build(this, mMessage,
				mSureButtonMessage, mCancelButtonMessage,
				new OnAlertDialogListener() {
					
					@Override
					public void onOk(AlertDialog alertDialogView) {
						deleteShareEmpower();
						alertDialogView.dismiss();
					}
					
					@Override
					public void onCancel(AlertDialog alertDialogView) {
						alertDialogView.dismiss();
					}
				});
		alertDialogView.show();
	}
	
	
	public void deleteShareEmpower(){
		mProfileDeleteShareEmpower.setClickable(false);
		mController = UMServiceFactory.getUMSocialService(
				"com.umeng.share", RequestType.SOCIAL);// 分享模块的引用
		mController.deleteOauth(this, SHARE_MEDIA.SINA,null);//新浪
		mController.deleteOauth(this, SHARE_MEDIA.WEIXIN,null);//微信
		mController.deleteOauth(this, SHARE_MEDIA.QQ,null);//QQ
		mController.deleteOauth(this, SHARE_MEDIA.QZONE,null);//QQ空间
		mController.deleteOauth(this, SHARE_MEDIA.WEIXIN_CIRCLE,null);//朋友圈
		mController.deleteOauth(this, SHARE_MEDIA.SINA,
				new SocializeClientListener() {
				@Override
				public void onStart() {
				}
				
				@Override
				public void onComplete(int status, SocializeEntity entity) {
					// TODO Auto-generated method stub
					if (status == 200) {
						mProfileDeleteShareEmpower.setClickable(true);
						Toast.makeText(ProfileMySettingActivity.this, getString(R.string.profile_toast_delete_success_text),Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(ProfileMySettingActivity.this, getString(R.string.profile_toast_delete_failure_text),Toast.LENGTH_SHORT).show();
					}
				}
		});
	}
	
	/* 点击“二维码”时触发的事件 */
	public void onTwoDimensionCodeClick(View v) {
		startActivity(new Intent(ProfileMySettingActivity.this,
				ProfileMySettingTwoDimensionCodeActivity.class));
	}

	/* 当点击“反馈”的时候触发的事件 */
	public void onRetroactionClick(View v) {
		startActivity(new Intent(ProfileMySettingActivity.this,
				ProfileMySettingRetroactionActivity.class));
	}

	/* 当点击“版本”的时候触发的事件 */
	public void onVersionClick(View v) {
		if (mToastHelper == null) {
			mToastHelper = ToastHelper.make(this, "正在检测新版本...");
		}
		if (mToastHelper.isShowing()) {
			return;
		}
		mToastHelper.show(mProfileLoginOut.getRootView());
		UmengUpdateAgent.setUpdateOnlyWifi(false);
		UmengUpdateAgent.setUpdateAutoPopup(false);
		UmengUpdateAgent.setUpdateListener(new UmengUpdateListener() {
			@Override
			public void onUpdateReturned(int updateStatus,
					UpdateResponse updateInfo) {
				switch (updateStatus) {
				case UpdateStatus.Yes: // has update
					mToastHelper.dismiss();
					UmengUpdateAgent.showUpdateDialog(
							ProfileMySettingActivity.this, updateInfo);
					break;
				case UpdateStatus.No: // has no update
					// Toast.makeText(ProfileMySettingActivity.this, "当前已是最新版本",
					// Toast.LENGTH_SHORT).show();
					mToastHelper.dismiss("当前已是最新版本", 1500);
					break;
				case UpdateStatus.NoneWifi: // none wifi
					mToastHelper.dismiss("没有wifi连接， 只在wifi下更新", 1500);
					// Toast.makeText(ProfileMySettingActivity.this,
					// "没有wifi连接， 只在wifi下更新", Toast.LENGTH_SHORT).show();
					break;
				case UpdateStatus.Timeout: // time out
					mToastHelper.dismiss("超时", 1500);
					// Toast.makeText(ProfileMySettingActivity.this, "超时",
					// Toast.LENGTH_SHORT).show();
					break;
				}
			}
		});
		UmengUpdateAgent.forceUpdate(this);
	}

	/* 点击“关于”时触发的事件 */
	public void onAboutClick(View v) {
		startActivity(new Intent(ProfileMySettingActivity.this,
				ProfileMySettingAboutActivity.class));
	}

	/* 返回上一页 */
	public void onBackClick(View v) {
		finish();
	}

	@Override
	protected void onDestroy() {
		if (mToastHelper != null) {
			mToastHelper.dismiss();
		}
		super.onDestroy();
	}

}
