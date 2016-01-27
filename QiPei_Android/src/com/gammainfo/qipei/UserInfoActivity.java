package com.gammainfo.qipei;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gammainfo.avatarpick.CropImageActivity;
import com.gammainfo.qipei.convert.UserJSONConvert;
import com.gammainfo.qipei.db.Preferences;
import com.gammainfo.qipei.http.AsyncHttpResponseHandler;
import com.gammainfo.qipei.http.RequestParams;
import com.gammainfo.qipei.http.RestClient;
import com.gammainfo.qipei.model.User;
import com.gammainfo.qipei.utils.Constant;
import com.gammainfo.qipei.utils.PhotoChoose;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.image.SmartImageView;

public class UserInfoActivity extends BaseActivity {
	private static final String TAG = UserInfoActivity.class.getSimpleName();
	private static final int RATIOX = 4;//方框横坐标比例
	private static final int RATIOY = 4;//方框纵坐标比例
	
	private RelativeLayout mProfileUploadImageLayout;
	private SmartImageView mPhotoImage;
	private TextView mCompanyNameText;
	private TextView mAddressText;
	private TextView mPhoneText;
	private TextView mCompanyIntroText;
	//private TextView mProductNumText;
	private TextView mUserTypeText;
	private TextView mGradeText;
	private String mUserType;
	private String mGrade;
	private Context mCon;
	private static String localTempImageFileName = "";
	private static final int FLAG_CHOOSE_IMG = 5;
	private static final int FLAG_CHOOSE_PHONE = 6;
	private static final int FLAG_MODIFY_FINISH = 7;
	public static final String IMAGE_PATH = "My_weixin";
	public static final File FILE_SDCARD = Environment
			.getExternalStorageDirectory();
	public static final File FILE_LOCAL = new File(FILE_SDCARD, IMAGE_PATH);
	public static final File FILE_PIC_SCREENSHOT = new File(FILE_LOCAL,
			"images/screenshots");
	private PhotoChoose photoChoose;
	private User user;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_info_activity);
		user = Preferences.getAccountUser();
		mProfileUploadImageLayout = (RelativeLayout) findViewById(R.id.profile_upload_image_layout);
		mPhotoImage = (SmartImageView) findViewById(R.id.photo_image);
		mCompanyNameText = (TextView) findViewById(R.id.company_name_text);
		mAddressText = (TextView) findViewById(R.id.address_text);
		mPhoneText = (TextView) findViewById(R.id.phone_text);
		mCompanyIntroText = (TextView) findViewById(R.id.company_intro_text);
		//mProductNumText = (TextView) findViewById(R.id.profile_product_num_text);
		mUserTypeText = (TextView) findViewById(R.id.user_type_text);
		mGradeText = (TextView) findViewById(R.id.grade_text);
		User user = Preferences.getAccountUser();// 从本地edit获取中获取用户信息
		setMyInfo(user);
		getInternetData();
		mCon = UserInfoActivity.this;
		// 跳出选择界面
		mProfileUploadImageLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 调用选择那种方式的dialog
				if (photoChoose == null) {
					photoChoose = PhotoChoose.build(UserInfoActivity.this,
							new PhotoChoose.OnPhotoChooseListener() {

								@Override
								public void cancel(PhotoChoose target) {
									target.dismiss();
								}

								@Override
								public void camera(PhotoChoose target) {
									target.dismiss();
									String status = Environment
											.getExternalStorageState();
									if (status
											.equals(Environment.MEDIA_MOUNTED)) {
										try {
											localTempImageFileName = "";
											localTempImageFileName = String
													.valueOf((new Date())
															.getTime())
													+ ".png";
											File filePath = FILE_PIC_SCREENSHOT;
											if (!filePath.exists()) {
												filePath.mkdirs();
											}
											Intent intent = new Intent(
													android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
											File f = new File(filePath,
													localTempImageFileName);
											// localTempImgDir和localTempImageFileName是自己定义的名字
											Uri u = Uri.fromFile(f);
											intent.putExtra(
													MediaStore.Images.Media.ORIENTATION,
													0);
											intent.putExtra(
													MediaStore.EXTRA_OUTPUT, u);
											startActivityForResult(intent,
													FLAG_CHOOSE_PHONE);
										} catch (ActivityNotFoundException e) {
											//
										}
									}
								}

								@Override
								public void album(PhotoChoose target) {
									target.dismiss();
									Intent intent = new Intent();
									intent.setAction(Intent.ACTION_PICK);
									intent.setType("image/*");
									startActivityForResult(intent,
											FLAG_CHOOSE_IMG);
								}
							});
				}
				photoChoose.show();
			}
		});
	}

	/* 设置我的资料页面的用户信息 */
	public void setMyInfo(User user) {
		mPhotoImage.setImageUrl(user.getPhotoUrl(),
				R.drawable.ic_user_product_default,R.drawable.ic_user_product_default);
		mCompanyNameText.setText(user.getCompanyName());
		mAddressText.setText((user.getProvince().equals(
				user.getCity()) ? user.getProvince()
				: (user.getProvince() + user.getCity()))
						+ user.getCounty()
						+ user.getAddress());
		mPhoneText.setText(user.getPhone());
		mCompanyIntroText.setText(user.getIntro());
		//mProductNumText.setText(String.valueOf(user.getProductNum()));

		if (user.getRoleId() == 1) {
			mUserType = getString(R.string.profile_label_vendor_text);
		} else if (user.getRoleId() == 2) {
			mUserType = getString(R.string.profile_label_dealers_text);
		} else if (user.getRoleId() == 3) {
			mUserType = getString(R.string.profile_label_terminal_customers_text);
		}else if (user.getRoleId() == 31) {
			mUserType = getString(R.string.profile_label_four_s_text);
		}else if (user.getRoleId() == 32) {
			mUserType = getString(R.string.profile_label_quick_repair_text);
		}else if (user.getRoleId() == 33) {
			mUserType = getString(R.string.profile_label_car_repair_factory_text);
		}else if (user.getRoleId() == 34) {
			mUserType = getString(R.string.profile_label_car_improve_looks_text);
		}
		
		

		if (user.getMemberGrade() == 1) {
			mGrade = getString(R.string.profile_label_usual_user_text);
		} else if (user.getMemberGrade() == 2) {
			mGrade = getString(R.string.profile_label_middle_user_text);
		} else if (user.getMemberGrade() == 3) {
			mGrade = getString(R.string.profile_label_vip_user_text);
		}
		mUserTypeText.setText(mUserType);
		mGradeText.setText(mGrade);
		if(user.getMemberGrade() == 3){
			Resources rs = this.getResources();
			mGradeText.setTextColor(rs.getColor(R.color.profile_vip_text_red_color));
		}else{
			Resources rs = this.getResources();
			mGradeText.setTextColor(rs.getColor(R.color.profile_gray_text_color));
		}
	}

	/* 从网上数据库获取用户信息 */
	public void getInternetData() {
		int id = Preferences.getAccountUserId();
		RequestParams params = new RequestParams();
		params.put("id", String.valueOf(id));
		RestClient.post(Constant.API_GET_INFO, params,
				new AsyncHttpResponseHandler(getBaseContext(),
						new JsonHttpResponseHandler() {
							@Override
							public void onSuccess(JSONObject rlt) {
								try {
									if (rlt.getInt(Constant.JSON_KEY_CODE) == Constant.CODE_SUCCESS) {
										JSONObject info = rlt
												.getJSONObject("info");
										User user = UserJSONConvert
												.convertJsonToItem(info);
										setMyInfo(user);
										Preferences.setAccountUser(user,
												Preferences.getToken());
									} else {
										String msg = rlt
												.getString(Constant.JSON_KEY_MSG);
										Toast.makeText(getBaseContext(), msg,
												Toast.LENGTH_LONG).show();
									}
								} catch (JSONException e) {
									e.printStackTrace();
								}
							}
						}));
	}

	/* 结束Activity */
	public void finishActivity() {
		Intent data = new Intent();
		setResult(Activity.RESULT_OK, data);
		finish();
	}

	/* 修改公司名称 */
	public void onChangeCompanyNameRelativeLaoutClick(View v) {
		jumpActivity("company_name");
	}

	/* 修改密码 */
	public void onRevisePasswordRelativeLaoutClick(View v) {
		startActivityForResult(new Intent(UserInfoActivity.this,
				ProfileInfoChangeRevisePasswordActivity.class), 0);
	}

	/* 修改详细地址 */
	public void onChangeAddressRelativeLaoutClick(View v) {
		startActivityForResult(new Intent(UserInfoActivity.this,
				ProfileMyInfoChangeAddressActivity.class), 0);
	}

	/* 修改电话 */
	public void onChangePhoneRelativeLaoutClick(View v) {
		jumpActivity("phone");
	}

	/* 修改公司简介 */
	public void onChangeCompanyIntroRelativeLaoutClick(View v) {
		jumpActivity("intro");
	}

	/* 修改产品管理 */
	public void onChangeProductNumoRelativeLaoutClick(View v) {
		Intent intent = new Intent(UserInfoActivity.this,
				ProfileProductActivity.class);
		startActivityForResult(intent,0);
	}

	/* 修改账户类型 */
	public void onChangeUserTypeRelativeLaoutClick(View v) {
		jumpActivity("user_type");
	}

	/* 修改账户类型 */
	public void onChangeGradeRelativeLaoutClick(View v) {
		jumpActivity("grade");
	}

	/* 实施跳转Activity,“公司名称”，“电话”，“简介”公用这一个跳转接口 */
	private void jumpActivity(String name) {
		Intent intent = new Intent(UserInfoActivity.this,
				ProfileMyInfoChangeActivity.class);
		Bundle data = new Bundle();
		data.putSerializable("changeName", name);
		intent.putExtras(data);
		startActivityForResult(intent, 0);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
			User user = Preferences.getAccountUser();// 从本地edit获取中获取用户信息
			setMyInfo(user);
		}
		if (requestCode == FLAG_CHOOSE_IMG && resultCode == RESULT_OK) {
			if (data != null) {
				Uri uri = data.getData();
				if (!TextUtils.isEmpty(uri.getAuthority())) {
					Cursor cursor = getContentResolver().query(uri,
							new String[] { MediaStore.Images.Media.DATA },
							null, null, null);
					if (null == cursor) {
						Toast.makeText(mCon, "图片没找到", 0).show();
						return;
					}
					cursor.moveToFirst();
					String path = cursor.getString(cursor
							.getColumnIndex(MediaStore.Images.Media.DATA));
					cursor.close();
					Intent intent = new Intent(this, CropImageActivity.class);
					intent.putExtra("path", path);
					intent.putExtra("rectRatioX", RATIOX);
					intent.putExtra("rectRatioY", RATIOY);
					startActivityForResult(intent, FLAG_MODIFY_FINISH);
				} else {
					Intent intent = new Intent(this, CropImageActivity.class);
					intent.putExtra("path", uri.getPath());
					intent.putExtra("rectRatioX", RATIOX);
					intent.putExtra("rectRatioY", RATIOY);
					startActivityForResult(intent, FLAG_MODIFY_FINISH);
				}
			}
		} else if (requestCode == FLAG_CHOOSE_PHONE && resultCode == RESULT_OK) {
			File f = new File(FILE_PIC_SCREENSHOT, localTempImageFileName);
			Intent intent = new Intent(this, CropImageActivity.class);
			intent.putExtra("path", f.getAbsolutePath());
			intent.putExtra("rectRatioX", RATIOX);
			intent.putExtra("rectRatioY", RATIOY);
			startActivityForResult(intent, FLAG_MODIFY_FINISH);
		} else if (requestCode == FLAG_MODIFY_FINISH && resultCode == RESULT_OK) {
			if (data != null) {
				final String path = data.getStringExtra("path");
				Bitmap b = BitmapFactory.decodeFile(path);
				
				mPhotoImage.setImageBitmap(b);
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				b.compress(Bitmap.CompressFormat.JPEG, 100, out);
				try {
					out.flush();
					out.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				byte[] buffer = out.toByteArray();
				byte[] encode = Base64.encode(buffer, Base64.DEFAULT);
				String photo = new String(encode);
				UploadPhotoToInternet(photo);
			}
		}
	}

	private ColorStateList getResources(int profileVipTextRedColor) {
		// TODO Auto-generated method stub
		return null;
	}

	/* 讲得到的图片上传到后台 */
	public void UploadPhotoToInternet(String photo) {
		int id = Preferences.getAccountUserId();
		RequestParams params = new RequestParams();
		params.put("id", String.valueOf(id));
		params.put("photo", photo);
		RestClient.post(Constant.API_SET_PHOTO, params,
				new AsyncHttpResponseHandler(getBaseContext(),
						new JsonHttpResponseHandler() {
							@Override
							public void onSuccess(JSONObject rlt) {
								try {
									if (rlt.getInt(Constant.JSON_KEY_CODE) == Constant.CODE_SUCCESS) {
										JSONObject info = rlt
												.getJSONObject("info");
										String photoUrl = info
												.getString("photo");
										User user = Preferences
												.getAccountUser();
										user.setPhotoUrl(photoUrl); // 设置本地的用户的图片路径
										Preferences.setAccountUser(user,
												Preferences.getToken());
										Toast.makeText(getBaseContext(),
												getString(R.string.profile_toast_upload_photo_success_text), Toast.LENGTH_LONG)
												.show();
									} else {
										Toast.makeText(
												getBaseContext(),
												getString(R.string.profile_toast_login_failure_text),
												Toast.LENGTH_LONG).show();
									}
								} catch (JSONException e) {
									e.printStackTrace();
								}
							}
						}));
	}

	/* 左上角返回按钮 */
	public void onBackClick(View v) {
		finishActivity();
	}
}
