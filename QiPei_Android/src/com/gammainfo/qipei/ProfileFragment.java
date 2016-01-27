package com.gammainfo.qipei;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gammainfo.qipei.MainActivity.OnTabChangedListener;
import com.gammainfo.qipei.db.Preferences;
import com.gammainfo.qipei.model.User;
import com.gammainfo.qipei.widget.AlertDialog;
import com.gammainfo.qipei.widget.AlertDialog.OnAlertDialogListener;
import com.loopj.android.image.SmartImageView;

@SuppressLint("NewApi")
public class ProfileFragment extends Fragment implements OnTabChangedListener {
	private MainActivity mMainActivity;
	private SmartImageView mProfileUserPhotoImage;
	private TextView mProfileCompanyNameView;
	private TextView mProfileGradeView;
	private TextView mProfileUserTypeView;
	private TextView mProfileIsCertificatedView;
	private TextView mProfileAccountTextView;
	private String mUserGrade;
	private RelativeLayout mMySomeInfoRelativeLayout;
	private RelativeLayout mMyFavoriteRelativeLayout;
	private Button mLoginButton;
	private Button mRegisteButton;
	private String mMessage;
	private String mSureButtonMessage;
	private String mCancelButtonMessage;
	private TextView mProductNumText;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View settingLayout = inflater.inflate(R.layout.fragment_profile,
				container, false);
		return settingLayout;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mMainActivity = (MainActivity) getActivity();
		mMainActivity.registerOnTabChangedListener(this);
		mMainActivity.findViewById(R.id.profile_my_info_relative_layout)
				.setOnClickListener(onMyInfoClikeListener);
		mMySomeInfoRelativeLayout = (RelativeLayout) mMainActivity
				.findViewById(R.id.profile_my_some_info_relative_layout);
		mMainActivity.findViewById(R.id.profile_to_login).setOnClickListener(
				onToLoginClickListener);
		mMainActivity.findViewById(R.id.profile_to_register)
				.setOnClickListener(onToRegisteClickListener);
		mMainActivity.findViewById(R.id.profile_my_favorite_layout)
				.setOnClickListener(onProfileMyFavoriteClick);
		mMainActivity.findViewById(R.id.profile_my_setting_layout)
				.setOnClickListener(onProfileMySettingClick);
		mMainActivity.findViewById(R.id.profile_my_product_layout)
				.setOnClickListener(onMyProductClickListener);
		mMainActivity.findViewById(R.id.profile_my_release).setOnClickListener(
				onMyReleaseClickListener);

		mLoginButton = (Button) mMainActivity
				.findViewById(R.id.profile_to_login);
		mRegisteButton = (Button) mMainActivity
				.findViewById(R.id.profile_to_register);

		mMySomeInfoRelativeLayout = (RelativeLayout) mMainActivity
				.findViewById(R.id.profile_my_some_info_relative_layout);

		mProfileUserPhotoImage = (SmartImageView) mMainActivity
				.findViewById(R.id.profile_user_photo_image);
		mProfileCompanyNameView = (TextView) mMainActivity
				.findViewById(R.id.profile_user_company_name);
		mProfileGradeView = (TextView) mMainActivity
				.findViewById(R.id.profile_grade);
		mProfileUserTypeView = (TextView) mMainActivity
				.findViewById(R.id.profile_user_type);
		mProfileIsCertificatedView = (TextView) mMainActivity
				.findViewById(R.id.profile_is_certificated);
		mProfileAccountTextView = (TextView) mMainActivity
				.findViewById(R.id.profile_account_text);
		mProductNumText = (TextView) mMainActivity
				.findViewById(R.id.profile_product_num_text);

		changeView();
	}

	@Override
	public void onChanged(MainActivity mainActivity, int selectedTabId) {
		// TODO 选中的标签发生变化时执行
		if (selectedTabId == R.id.tab_profile) {
			changeView();
		} else {

		}
	}

	/* 当点击登陆按钮时触发的事件---跳转Activity */
	View.OnClickListener onToLoginClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if (Preferences.isLogin() == false) {
				startActivityForResult(new Intent(getActivity(),
						LoginActivity.class), 0);
			} else {
				Toast.makeText(getActivity(),
						getString(R.string.profile_toast_repeat_login_text),
						Toast.LENGTH_SHORT).show();
			}
		}
	};

	/* 当点击注册按钮时触发的事件---跳转Activity */
	View.OnClickListener onToRegisteClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if (Preferences.isLogin() == false) {
				Intent intent = new Intent(getActivity(),
						ProfileRegisteBackPasswordInputAccountActivity.class);
				Bundle data = new Bundle();
				data.putSerializable("type", 2);
				intent.putExtras(data);
				startActivityForResult(intent, 0);
			}
		}
	};

	/* 当点击"我的资料"时触发的事件 */
	View.OnClickListener onMyInfoClikeListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if (Preferences.isLogin() == true) {
				startMyInfoActivity();
			} else {
				loginDialog();
			}
		}
	};

	/* 当点击“我的收藏”时触发的事件 */
	View.OnClickListener onProfileMyFavoriteClick = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if (Preferences.isLogin() == true) {
				startActivityForResult(new Intent(getActivity(),
						ProfileMyFavoriteActivity.class), 0);
			} else {
				loginDialog();
			}
		}

	};

	/* 当点击“我的发布”时触发的事件 */
	View.OnClickListener onMyReleaseClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if (Preferences.isLogin() == true) {
				startActivityForResult(new Intent(getActivity(),
						ProfileMyReleaseListActivity.class), 0);
			} else {
				loginDialog();
			}
		}

	};

	/* 当点击“设置”时触发的事件 */
	View.OnClickListener onProfileMySettingClick = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			startActivityForResult(new Intent(getActivity(),
					ProfileMySettingActivity.class), 0);
		}
	};

	/* 当点击“我的产品”时触发的事件 */
	View.OnClickListener onMyProductClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if (Preferences.isLogin() == true) {
				startActivityForResult(new Intent(getActivity(),
						ProfileProductActivity.class), 0);
			} else {
				loginDialog();
			}
		}
	};

	/* 当跳转的Activity页面结束时执行在“我的”页面加载用户信息的数据 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		changeView();
	}

	/* 改变视图即将“我的”页面的登陆和注册按钮替换成为用户信息的TextView，并向其中加载数据 */
	public void changeView() {
		User user = Preferences.getAccountUser();
		if (user != null) {
			mLoginButton.setVisibility(View.GONE);
			mRegisteButton.setVisibility(View.GONE);
			mMySomeInfoRelativeLayout.setVisibility(View.VISIBLE);
			mProfileUserPhotoImage.setImageUrl(user.getPhotoUrl(),
					R.drawable.profile_login_photo);
			mProfileCompanyNameView.setText(user.getCompanyName());
			// TODO 暂时注释，数量待加
			// if(user.getProductNum()!=0){
			// mProductNumText.setText(String.valueOf(user.getProductNum()));
			// }
			if (user.getMemberGrade() == 1) {
				mUserGrade = getString(R.string.profile_label_usual_user_text);
			} else if (user.getMemberGrade() == 2) {
				mUserGrade = getString(R.string.profile_label_middle_user_text);
			} else if (user.getMemberGrade() == 3) {
				mUserGrade = getString(R.string.profile_label_vip_user_text);
			}
			mProfileGradeView.setText(mUserGrade);
			mProfileUserTypeView.setText(user.getRoleName());
			mProfileIsCertificatedView
					.setText(user.isCertificated() ? getString(R.string.companyinfo_listuser_lable_iscertificated)
							: getString(R.string.profile_label_not_iscertificated_text));
			mProfileAccountTextView.setText(user.getAccount());
			if (TextUtils.isEmpty(user.getCompanyName())
					|| TextUtils.isEmpty(user.getPhone())) {
				AlertDialog.build(mMainActivity,
						R.string.profile_label_not_perfect_tips,
						R.string.common_label_ok, R.string.common_label_cancel,
						new OnAlertDialogListener() {

							@Override
							public void onOk(AlertDialog alertDialogView) {
								alertDialogView.dismiss();
								startMyInfoActivity();
							}

							@Override
							public void onCancel(AlertDialog alertDialogView) {
								alertDialogView.dismiss();
							}
						}).show();
			}
		} else {
			mMySomeInfoRelativeLayout.setVisibility(View.GONE);
			mLoginButton.setVisibility(View.VISIBLE);
			mRegisteButton.setVisibility(View.VISIBLE);
			mProfileUserPhotoImage
					.setImageResource(R.drawable.profile_login_photo);
		}
	}

	public void loginDialog() {
		mMessage = getString(R.string.common_label_unlogin);
		mSureButtonMessage = getString(R.string.profile_label_dialog_sure_button_login_text);
		mCancelButtonMessage = getString(R.string.profile_label_dialog_cancel_button_cancel_text);
		AlertDialog alertDialogView = AlertDialog.build(mMainActivity,
				mMessage, mSureButtonMessage, mCancelButtonMessage,
				new OnAlertDialogListener() {

					@Override
					public void onOk(AlertDialog alertDialogView) {
						startActivityForResult(new Intent(getActivity(),
								LoginActivity.class), 0);
						alertDialogView.dismiss();
					}

					@Override
					public void onCancel(AlertDialog alertDialogView) {

						alertDialogView.dismiss();
					}
				});
		alertDialogView.show();
	}

	private void startMyInfoActivity() {
		startActivityForResult(
				new Intent(mMainActivity, UserInfoActivity.class), 0);
	}
}
