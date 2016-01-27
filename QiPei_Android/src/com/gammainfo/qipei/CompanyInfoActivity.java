package com.gammainfo.qipei;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.gammainfo.qipei.adapter.ProductAdapter;
import com.gammainfo.qipei.adapter.UserAdapter;
import com.gammainfo.qipei.convert.ProductJSONConvert;
import com.gammainfo.qipei.convert.UserJSONConvert;
import com.gammainfo.qipei.db.Preferences;
import com.gammainfo.qipei.http.AsyncHttpResponseHandler;
import com.gammainfo.qipei.http.RequestParams;
import com.gammainfo.qipei.http.RestClient;
import com.gammainfo.qipei.model.Product;
import com.gammainfo.qipei.model.User;
import com.gammainfo.qipei.utils.Constant;
import com.gammainfo.qipei.widget.AlertDialog;
import com.gammainfo.qipei.widget.AlertDialog.OnAlertDialogListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.image.SmartImageView;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.bean.SocializeConfig;
import com.umeng.socialize.controller.RequestType;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.controller.UMWXHandler;
import com.umeng.socialize.sso.QZoneSsoHandler;

public class CompanyInfoActivity extends BaseActivity implements
		OnItemClickListener, PullToRefreshBase.OnRefreshListener2<ListView> {
	private final static String SHARE_CONTENT = "Warning！我正在使用中国汽商中心拓展业务，抢占市场先机，作为您的小伙伴，我义不容辞的向您推荐！中国汽商中心那么实用，小伙伴们都知道吗？http://chinaqszx.com/downloads/";
	private final static String SHARE_URL = "http://chinaqszx.com/downloads/";

	final UMSocialService mController = UMServiceFactory.getUMSocialService(
			"com.umeng.share", RequestType.SOCIAL);// 分享模块的引用

	public static final String EXTRA_USER_ID = "user_id";
	private User mSelectUser;// 被选中的用户（此页面显示此用户的详细信息）
	private PullToRefreshListView mPullToRefreshListView;
	private LayoutInflater mLayoutInflater;
	private TextView mTitle;
	private TextView mStatus;
	private SmartImageView mImage;
	private TextView mIntro;
	private TextView mAdress;
	private TextView mPhone;
	private TextView mTopLable;
	private TextView mNoProductListLable;
	private TextView mCompanyTypeView;
	private TextView mCompanySplit;
	private ProductAdapter mProductAdapter;
	private ArrayList<Product> mProductList;
	private AlertDialog mLoginAlertDialog;
	private Drawable mFavoriteTrueDrawableTrue;
	private Drawable mFavoriteTrueDrawableFalse;
	private View companyHeaderView;
	private Button favoriteButton;
	private int mUserId;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_company_info);
		Intent intent = getIntent();
		mSelectUser = intent.getParcelableExtra("selectUser");
		if (mSelectUser == null) {
			mUserId = intent.getIntExtra(EXTRA_USER_ID, 0);
		} else {
			mUserId = mSelectUser.getId();
		}
		initView();
		setData();// 初始化界面并添加数据
		mProductList = new ArrayList<Product>();
		mProductAdapter = new ProductAdapter(mProductList, this);
		mPullToRefreshListView.setMode(Mode.BOTH);
		mPullToRefreshListView.setAdapter(mProductAdapter);
		mPullToRefreshListView.setOnRefreshListener(this);
		mPullToRefreshListView.setOnItemClickListener(this);
		if (mUserId != 0) {
			loadProductDetails();
			loadProductList();
		}
	}

	private void loadProductDetails() {
		RequestParams params = new RequestParams();
		params.put("id", mUserId + "");
		params.put("user_id", Preferences.getAccountUserId() + "");
		RestClient.post(Constant.API_COMPANY_ITEM_GET, params,
				new AsyncHttpResponseHandler(this,
						new JsonHttpResponseHandler() {
							@Override
							public void onSuccess(JSONObject res) {
								JSONObject jsonInfo = null;
								try {
									if (res.getInt(Constant.JSON_KEY_CODE) == Constant.CODE_SUCCESS) {
										jsonInfo = res
												.getJSONObject(Constant.JSON_KEY_INFO);
										mSelectUser = UserJSONConvert
												.convertJsonToItem(jsonInfo);
										setData();
									}
								} catch (JSONException e) {
									e.printStackTrace();
								}
							}
						}));
	}

	/* 从数据库中获取产品列表数据 */
	private void loadProductList() {
		RequestParams params = new RequestParams();
		params.put("user_id", String.valueOf(mUserId));
		params.put("page_index",
				String.valueOf(mProductAdapter.getPageNumber()));
		params.put("page_size", String.valueOf(mProductAdapter.getPageSize()));
		RestClient.post(Constant.API_USER_PRODUCT_LIST_GET, params,
				new AsyncHttpResponseHandler(this,
						new JsonHttpResponseHandler() {
							@Override
							public void onSuccess(JSONObject res) {
								JSONObject jsonInfo = null;
								ArrayList<Product> productList;
								try {
									if (res.getInt(Constant.JSON_KEY_CODE) == Constant.CODE_SUCCESS) {
										jsonInfo = res
												.getJSONObject(Constant.JSON_KEY_INFO);
										JSONArray jsonListArray = jsonInfo
												.getJSONArray("list");
										productList = ProductJSONConvert
												.convertJsonArrayToItemList(jsonListArray);
										/* 如果检测到数据且与上次检测到的数据不同，则进行数据转换 */
										mProductList = productList;
										if (mProductAdapter.getPageNumber() == 1) {
											mProductAdapter
													.setDataSource(productList);
											if (jsonListArray.length() == 0) {
												mNoProductListLable
														.setVisibility(View.VISIBLE);
											} else {
												mNoProductListLable
														.setVisibility(View.GONE);
											}
										} else {
											if (jsonListArray.length() == 0) {
												Toast.makeText(
														CompanyInfoActivity.this,
														R.string.common_label_last_page,
														Toast.LENGTH_SHORT)
														.show();
											} else {
												mNoProductListLable
														.setVisibility(View.GONE);
											}
											mProductAdapter
													.appendDataSource(productList);
										}
										mProductAdapter
												.setPageNumber(mProductAdapter
														.getPageNumber() + 1);
									}

								} catch (JSONException e) {
									e.printStackTrace();
								}
							}

							@Override
							public void onFinish() {
								mPullToRefreshListView.onRefreshComplete();
								super.onFinish();
							}
						}));
	}

	/* 添加组件，初始化界面 */
	private void initView() {
		mPullToRefreshListView = (PullToRefreshListView) findViewById(R.id.companyinfo_list_product);
		mLayoutInflater = getLayoutInflater();
		companyHeaderView = mLayoutInflater.inflate(
				R.layout.companyinfo_listview_header, null);
		mPullToRefreshListView.getRefreshableView().addHeaderView(
				companyHeaderView);
		mTitle = (TextView) findViewById(R.id.tv_list_listitem_title);
		mStatus = (TextView) findViewById(R.id.tv_list_listitem_status);
		mImage = (SmartImageView) findViewById(R.id.siv_list_listitem_image);
		mIntro = (TextView) findViewById(R.id.companyinfo_textview_intro);
		mPhone = (TextView) findViewById(R.id.companyinfo_show_phone);
		mAdress = (TextView) findViewById(R.id.companyinfo_show_adress);
		mTopLable = (TextView) findViewById(R.id.companyinfo_labl_top);
		mNoProductListLable = (TextView) findViewById(R.id.product_no_list);
		mCompanySplit = (TextView) findViewById(R.id.tv_company_split);
		mCompanyTypeView = (TextView) findViewById(R.id.tv_company_listitem_user_type);
		final LinearLayout favoriteLayout = (LinearLayout) companyHeaderView
				.findViewById(R.id.siv_company_listitem_button_favorite);
		final LinearLayout shareLayout = (LinearLayout) companyHeaderView
				.findViewById(R.id.siv_company_listitem_button_share);
		favoriteButton = (Button) companyHeaderView
				.findViewById(R.id.list_company_favorite_bt);
		mFavoriteTrueDrawableTrue = getResources().getDrawable(
				R.drawable.list_item_bt_left_favorite_true);
		mFavoriteTrueDrawableFalse = getResources().getDrawable(
				R.drawable.list_item_bt_left_favorite_false);

		shareLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (mSelectUser != null) {
					mController.setShareContent(SHARE_CONTENT);// 设置分享内容
					SocializeConfig shareConfig = mController.getConfig();
					shareConfig.removePlatform(SHARE_MEDIA.RENREN,
							SHARE_MEDIA.DOUBAN, SHARE_MEDIA.TENCENT);
					shareConfig.setShareMail(false);

					/* QQ分享内容设置 */
					shareConfig.supportQQPlatform(CompanyInfoActivity.this,
							SHARE_URL);// 引用QQ模块
					mController.getConfig().setQZoneAddSharePermission(false);
					/* 微信以及朋友圈模块 */
					String appID = Constant.SHARE_APP_ID;// 微信开发平台注册应用的AppID
					String contentUrl = SHARE_URL;// 微信图文分享必须设置一个url
					// 添加微信平台，参数1为当前Activity, 参数2为用户申请的AppID,
					// 参数3为点击分享内容跳转到的目标url
					UMWXHandler wxHandler = mController.getConfig()
							.supportWXPlatform(CompanyInfoActivity.this, appID,
									contentUrl);
					// 设置分享标题
					wxHandler.setWXTitle(SHARE_CONTENT);
					// 支持微信朋友圈
					UMWXHandler circleHandler = mController.getConfig()
							.supportWXCirclePlatform(CompanyInfoActivity.this,
									appID, contentUrl);
					circleHandler.setCircleTitle(SHARE_CONTENT);
					/* 设置分享到QQ空间和朋友圈时点击应用跳转的网址 */
					mController.setAppWebSite(SHARE_MEDIA.RENREN, SHARE_URL);
					mController.setAppWebSite(SHARE_MEDIA.QZONE, SHARE_URL);
					mController.openShare(CompanyInfoActivity.this, false);
				}
			}
		});

		favoriteLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!Preferences.isLogin()) {
					if (mLoginAlertDialog == null) {
						mLoginAlertDialog = AlertDialog.build(
								CompanyInfoActivity.this,
								R.string.common_label_unlogin,
								R.string.common_label_gotologin,
								R.string.common_label_cancel,
								new OnAlertDialogListener() {

									@Override
									public void onOk(AlertDialog alertDialogView) {
										startActivity(new Intent(
												CompanyInfoActivity.this,
												LoginActivity.class));
										alertDialogView.dismiss();
									}

									@Override
									public void onCancel(
											AlertDialog alertDialogView) {
										alertDialogView.dismiss();
									}
								});
					}
					mLoginAlertDialog.show();
					return;
				}
				if (mSelectUser != null) {
					if (mSelectUser.isIsFavorite()) {
						favoriteLayout.setClickable(false);
						RequestParams params = new RequestParams();
						params.put("id", Preferences.getAccountUserId() + "");
						params.put("dest_user_id", mSelectUser.getId() + "");
						params.put("dest_res_id", 0 + "");
						RestClient.post(Constant.API_LIST_FAVORITE_CANCEL,
								params, new AsyncHttpResponseHandler(
										CompanyInfoActivity.this,
										new JsonHttpResponseHandler() {
											@Override
											public void onSuccess(JSONObject res) {
												try {
													if (res.getInt(Constant.JSON_KEY_CODE) == Constant.CODE_SUCCESS) {
														UserAdapter
																.setFavoriteDrawable(
																		mFavoriteTrueDrawableFalse,
																		favoriteButton);
														mSelectUser
																.setIsFavorite(false);
														Toast.makeText(
																CompanyInfoActivity.this,
																R.string.list_cancle_favorite_success,
																Toast.LENGTH_SHORT)
																.show();
													} else {
														Toast.makeText(
																CompanyInfoActivity.this,
																R.string.list_cancle_favorite_fail,
																Toast.LENGTH_SHORT)
																.show();
													}
												} catch (JSONException e) {
													// block
													e.printStackTrace();
													Toast.makeText(
															CompanyInfoActivity.this,
															R.string.list_cancle_favorite_fail,
															Toast.LENGTH_SHORT)
															.show();
												}
												favoriteLayout
														.setClickable(true);
											}

											@Override
											public void onFailure(Throwable e,
													JSONObject rlt) {
												super.onFailure(e, rlt);
												favoriteLayout
														.setClickable(true);
												Toast.makeText(
														CompanyInfoActivity.this,
														R.string.list_cancle_favorite_fail,
														Toast.LENGTH_SHORT)
														.show();
											}

										}));

					} else {
						favoriteLayout.setClickable(false);
						RequestParams params = new RequestParams();
						params.put("id", Preferences.getAccountUserId() + "");
						params.put("dest_user_id", mSelectUser.getId() + "");
						params.put("dest_res_id", 0 + "");
						params.put("type", mSelectUser.getRoleId() + "");
						RestClient.post(Constant.API_LIST_FAVORITE_ADD, params,
								new AsyncHttpResponseHandler(
										CompanyInfoActivity.this,
										new JsonHttpResponseHandler() {
											@Override
											public void onSuccess(JSONObject res) {
												try {
													if (res.getInt(Constant.JSON_KEY_CODE) == Constant.CODE_SUCCESS) {
														UserAdapter
																.setFavoriteDrawable(
																		mFavoriteTrueDrawableTrue,
																		favoriteButton);
														mSelectUser
																.setIsFavorite(true);
														Toast.makeText(
																CompanyInfoActivity.this,
																R.string.list_is_favorite_success,
																Toast.LENGTH_SHORT)
																.show();
													} else {
														Toast.makeText(
																CompanyInfoActivity.this,
																R.string.list_is_favorite_fail,
																Toast.LENGTH_SHORT)
																.show();
													}
												} catch (JSONException e) {
													// block
													e.printStackTrace();
													Toast.makeText(
															CompanyInfoActivity.this,
															R.string.list_is_favorite_fail,
															Toast.LENGTH_SHORT)
															.show();
												}
												favoriteLayout
														.setClickable(true);
											}

											@Override
											public void onFailure(Throwable e,
													JSONObject rlt) {
												super.onFailure(e, rlt);
												favoriteLayout
														.setClickable(true);
												Toast.makeText(
														CompanyInfoActivity.this,
														R.string.list_is_favorite_fail,
														Toast.LENGTH_SHORT)
														.show();
											}
										}));
					}

				}
			}
		});
	}

	/* 对界面里的数据进行设置 */
	private void setData() {
		if (mSelectUser != null) {
			switch (mSelectUser.getRoleId()) {
			case 1:
				mTopLable.setText(R.string.search_label_manufacturer);
				mCompanyTypeView.setText(R.string.search_label_manufacturer);
				break;
			case 2:
				mTopLable.setText(R.string.search_label_agency);
				mCompanyTypeView.setText(R.string.search_label_agency);
				break;
			case 3:
				mTopLable.setText(R.string.search_label_enduser);
				mCompanyTypeView.setText(R.string.search_label_enduser);
				break;
			case 31:
				mTopLable.setText(R.string.search_label_enduser);
				mCompanyTypeView.setText(R.string.search_label_enduser_4sdian);
				break;
			case 32:
				mTopLable.setText(R.string.search_label_enduser);
				mCompanyTypeView
						.setText(R.string.search_label_enduser_kuaixiudian);
				break;
			case 33:
				mTopLable.setText(R.string.search_label_enduser);
				mCompanyTypeView
						.setText(R.string.search_label_enduser_qixiuchang);
				break;
			case 34:
				mTopLable.setText(R.string.search_label_enduser);
				mCompanyTypeView
						.setText(R.string.search_label_enduser_meirongdian);
				break;
			default:
				mTopLable.setText(R.string.search_label_enduser);
			}
			mTitle.setText(mSelectUser.getCompanyName());
			mStatus.setText(mSelectUser.isCertificated() ? getText(R.string.companyinfo_listuser_lable_iscertificated)
					: null);
			mCompanySplit
					.setVisibility(mSelectUser.isCertificated() ? View.VISIBLE
							: View.GONE);
			mImage.setImageUrl(mSelectUser.getPhotoUrl(),
					R.drawable.ic_user_product_default,
					R.drawable.ic_user_product_default);
			if (!mSelectUser.getIntro().trim().equals("")) {
				mIntro.setText(mSelectUser.getIntro());
			} else {
				mIntro.setText("暂无简介");
			}
			mPhone.setText(getResources().getString(
					R.string.profile_label_phone_text));

			mAdress.setText((mSelectUser.getProvince().equals(
					mSelectUser.getCity()) ? mSelectUser.getProvince()
					: (mSelectUser.getProvince() + mSelectUser.getCity()))
							+ mSelectUser.getCounty()
							+ mSelectUser.getAddress());
			
			Drawable nowDrawable = mSelectUser.isIsFavorite() ? mFavoriteTrueDrawableTrue
					: mFavoriteTrueDrawableFalse;
			UserAdapter.setFavoriteDrawable(nowDrawable, favoriteButton);
		}
	}

	/* 简介显示或隐藏的监听事件 */
	public void showOrHide(View v) {
		mIntro.setVisibility(mIntro.getVisibility() == View.VISIBLE ? View.GONE
				: View.VISIBLE);

		Drawable imgShow = getResources().getDrawable(
				R.drawable.companyinfo_textview_intro_show);
		Drawable imgHide = getResources().getDrawable(
				R.drawable.companyinfo_textview_intro_hide);
		Drawable nowDrawable = mIntro.getVisibility() == View.VISIBLE ? imgShow
				: imgHide;
		((TextView) v).setCompoundDrawablesWithIntrinsicBounds(null, null,
				nowDrawable, null); // 设置左图标
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			finishActivity();
		}
		return super.onKeyDown(keyCode, event);
	}

	/* 后退图标的监听 */
	public void back(View view) {
		finishActivity();
	}

	/* 结束Activity */
	private void finishActivity() {
		Intent data = getIntent();
		data.putExtra("selectUser", mSelectUser);
		setResult(Activity.RESULT_OK, data);
		finish();
	}

	public void callToCompany(View v) {
		if (!Preferences.isLogin()) {
			if (mLoginAlertDialog == null) {
				mLoginAlertDialog = AlertDialog.build(this,
						R.string.common_label_unlogin,
						R.string.common_label_gotologin,
						R.string.common_label_cancel,
						new OnAlertDialogListener() {

							@Override
							public void onOk(AlertDialog alertDialogView) {
								startActivity(new Intent(
										CompanyInfoActivity.this,
										LoginActivity.class));
								alertDialogView.dismiss();
							}

							@Override
							public void onCancel(AlertDialog alertDialogView) {
								alertDialogView.dismiss();
							}
						});
			}
			mLoginAlertDialog.show();
			return;
		}
		if (!TextUtils.isEmpty(mSelectUser.getPhone())) {
			Intent callIntent = new Intent();
			callIntent.setAction(Intent.ACTION_DIAL);
			callIntent.setData(Uri.parse(String.format("tel:%s",
					mSelectUser.getPhone())));
			startActivity(callIntent);
		} else {
			Toast.makeText(this, R.string.list_call_phone_fail,
					Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			long id) {
		ArrayList<Product> productList = mProductAdapter.getDataSource();
		Product selectProduct = productList.get(position - 2);
		Intent productActivityIntent = new Intent(this,
				ProductInfoActivity.class);
		Bundle bundle = new Bundle();
		bundle.putInt("selectProductId", selectProduct.getId());
		productActivityIntent.putExtra("selectProduct", selectProduct);
		productActivityIntent.putExtras(bundle);
		startActivity(productActivityIntent);
	}

	@Override
	public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
		String label = DateUtils.formatDateTime(this,
				System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME
						| DateUtils.FORMAT_SHOW_DATE
						| DateUtils.FORMAT_ABBREV_ALL);
		// Update the LastUpdatedLabel
		refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
		mProductAdapter.setPageNumber(1);
		loadProductDetails();
		loadProductList();

	}

	@Override
	public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
		// Update the LastUpdatedLabel
		loadProductList();
	}

	public void openMap(View v) {
		// TODO Auto-generated method stub
		Intent mapIntent = new Intent(this, MapActivity.class);
		mapIntent.putExtra("latitude", mSelectUser.getLatitude());
		mapIntent.putExtra("longitude", mSelectUser.getLongitude());
		mapIntent.putExtra(
				"adress",
				mSelectUser.getProvince() + mSelectUser.getCity()
						+ mSelectUser.getCounty() + mSelectUser.getAddress());
		startActivity(mapIntent);
	}
}
