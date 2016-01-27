package com.gammainfo.qipei;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import cn.amose.vpi.CirclePageIndicator;
import cn.amose.vpi.PageIndicator;

import com.gammainfo.qipei.adapter.UserAdapter;
import com.gammainfo.qipei.convert.SupplyDemandJSONConvert;
import com.gammainfo.qipei.convert.UserJSONConvert;
import com.gammainfo.qipei.db.Preferences;
import com.gammainfo.qipei.http.AsyncHttpResponseHandler;
import com.gammainfo.qipei.http.RequestParams;
import com.gammainfo.qipei.http.RestClient;
import com.gammainfo.qipei.model.Image;
import com.gammainfo.qipei.model.SupplyDemand;
import com.gammainfo.qipei.model.User;
import com.gammainfo.qipei.utils.Constant;
import com.gammainfo.qipei.widget.AlertDialog;
import com.gammainfo.qipei.widget.PicturePreviewDialog;
import com.gammainfo.qipei.widget.AlertDialog.OnAlertDialogListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshScrollView;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.image.SmartImageView;

public class SupplyDemandInfoActivity extends BaseActivity implements
		PullToRefreshBase.OnRefreshListener<ScrollView> {
	public static final String EXTRA_SUPPLY_DEMAND = "selectSupplyDemand";
	private PullToRefreshScrollView mPullToRefreshScrollView;
	private LayoutInflater mLayoutInflater;
	private SupplyDemand mSelectSupplyDemand;
	private User mCompany;
	private TextView mTitle;
	private TextView mContent;
	private TextView mUpdateTime;
	private TextView mCompanyName;
	private Drawable mFavoriteTrueDrawableTrue;
	private Drawable mFavoriteTrueDrawableFalse;
	private Button mFavoriteButton;
	private LinearLayout mFavoriteLayout;

	private ArrayList<Image> mImages;
	private SupplyDemandBannerAdapter mBannerAdapter;
	
	private ViewPager mBannerPager;
	private RelativeLayout mBannerView;
	private TextView mAdress;
	private TextView mPhone;
	private AlertDialog mLoginAlertDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_supply_demand_info);
		initView();
		loadSupplyDemandData();
		updateView();
		loadCompanyData();
	}

	private void initView() {
		mLayoutInflater = getLayoutInflater();
		mFavoriteTrueDrawableTrue = getResources().getDrawable(
				R.drawable.list_item_bt_left_favorite_true_big);
		mFavoriteTrueDrawableFalse = getResources().getDrawable(
				R.drawable.list_item_bt_left_favorite_false_white);
		LinearLayout bottomView = (LinearLayout) mLayoutInflater.inflate(
				R.layout.supply_demand_info_content, null);
		
		mPullToRefreshScrollView = (PullToRefreshScrollView) findViewById(R.id.product_show_refesh);
		mPullToRefreshScrollView.addView(bottomView,
				new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.MATCH_PARENT,
						LinearLayout.LayoutParams.MATCH_PARENT));
		mPullToRefreshScrollView.setOnRefreshListener(this);
		mPhone = (TextView) findViewById(R.id.supply_demand_show_phone);
		mAdress = (TextView) findViewById(R.id.supply_demand_show_adress);
		mTitle = (TextView) findViewById(R.id.supply_demand_listitem_title);
		mContent = (TextView) findViewById(R.id.supply_demand_listitem_content);
		mUpdateTime = (TextView) findViewById(R.id.supply_demand_listitem_update_time);
		mCompanyName = (TextView) findViewById(R.id.company_name);
		mFavoriteButton = (Button) findViewById(R.id.supply_demand_favorite_button);
		mFavoriteLayout = (LinearLayout) findViewById(R.id.supply_demand_favorite_layout);
		Intent intent = getIntent();
		mSelectSupplyDemand = intent.getParcelableExtra(EXTRA_SUPPLY_DEMAND);

		mImages = new ArrayList<Image>();
		mBannerPager = (ViewPager) findViewById(R.id.vp_supply_demand_pager);
		mBannerView = (RelativeLayout) findViewById(R.id.supply_demand_banner);
		
		mBannerAdapter = new SupplyDemandBannerAdapter(mImages);
		mBannerPager.setAdapter(mBannerAdapter);
		PageIndicator indicator = (CirclePageIndicator) findViewById(R.id.cpi_search_pager_indcator);
		indicator.setViewPager(mBannerPager);
	}

	private void updateView() {
		mTitle.setText(mSelectSupplyDemand.getTitle());
		mContent.setText(mSelectSupplyDemand.getContent());
		mCompanyName.setText(mSelectSupplyDemand.getCompanyName());
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
		String date = sdf
				.format(new Date((mSelectSupplyDemand.getUpdateTime())));

		mUpdateTime.setText(date);

		if(mImages.size()==0) {
			mBannerView.setVisibility(View.GONE);
		}else {
			mBannerView.setVisibility(View.VISIBLE);
		}
		Drawable nowDrawable = mSelectSupplyDemand.getIsFavorite() ? mFavoriteTrueDrawableTrue
				: mFavoriteTrueDrawableFalse;
		UserAdapter.setFavoriteDrawable(nowDrawable, mFavoriteButton);
	}

	public void updateCompanyInfo() {
		mCompanyName.setText(mCompany.getCompanyName());
		mPhone.setText(getResources().getString(
				R.string.profile_label_phone_text));
		mAdress.setText((mCompany.getProvince().equals(mCompany.getCity()) ? mCompany
				.getProvince() : (mCompany.getProvince() + mCompany.getCity()))
				+ mCompany.getCounty() + mCompany.getAddress());
	}

	private void loadSupplyDemandData() {
		RequestParams params = new RequestParams();
		params.put("user_id", String.valueOf(Preferences.getAccountUserId()));
		params.put("id", mSelectSupplyDemand.getId() + "");
		RestClient.post(Constant.API_SUPPLY_DEMAND_DETAIL_GET, params,
				new AsyncHttpResponseHandler(SupplyDemandInfoActivity.this,
						new JsonHttpResponseHandler() {
							public void onSuccess(JSONObject res) {
								JSONObject jsonInfo = null;
								try {
									if (res.getInt(Constant.JSON_KEY_CODE) == Constant.CODE_SUCCESS) {
										jsonInfo = res
												.getJSONObject(Constant.JSON_KEY_INFO);

										mSelectSupplyDemand = SupplyDemandJSONConvert
												.convertJsonToItem(jsonInfo);
										mImages = mSelectSupplyDemand.getImgs();
										mBannerAdapter.setDataSource(mImages);
										updateView();
									}
								} catch (JSONException e) {
									e.printStackTrace();
								}
							}
						}));

	}

	private void loadCompanyData() {
		RequestParams params = new RequestParams();
		params.put("id", mSelectSupplyDemand.getUserId() + "");
		params.put("user_id", Preferences.getAccountUserId() + "");
		RestClient.post(Constant.API_COMPANY_ITEM_GET, params,
				new AsyncHttpResponseHandler(SupplyDemandInfoActivity.this,
						new JsonHttpResponseHandler() {
							@Override
							public void onSuccess(JSONObject res) {
								JSONObject jsonInfo = null;
								try {
									if (res.getInt(Constant.JSON_KEY_CODE) == Constant.CODE_SUCCESS) {
										jsonInfo = res
												.getJSONObject(Constant.JSON_KEY_INFO);

										mCompany = UserJSONConvert
												.convertJsonToItem(jsonInfo);

										updateCompanyInfo();
									}
								} catch (JSONException e) {
									e.printStackTrace();
								}
							}

							@Override
							public void onFinish() {
								mPullToRefreshScrollView.onRefreshComplete();
								super.onFinish();
							}

						}));
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
										SupplyDemandInfoActivity.this,
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
		if (mCompany != null && !TextUtils.isEmpty(mCompany.getPhone())) {
			Intent callIntent = new Intent();
			callIntent.setAction(Intent.ACTION_DIAL);
			callIntent.setData(Uri.parse(String.format("tel:%s",
					mCompany.getPhone())));
			startActivity(callIntent);
		} else {
			Toast.makeText(this, R.string.list_call_phone_fail,
					Toast.LENGTH_SHORT).show();
		}
	}

	public void openMap(View v) {
		if (mCompany != null) {
			Intent mapIntent = new Intent(this, MapActivity.class);
			mapIntent.putExtra("latitude", mCompany.getLatitude());
			mapIntent.putExtra("longitude", mCompany.getLongitude());
			mapIntent.putExtra(
					"adress",
					mCompany.getProvince() + mCompany.getCity()
							+ mCompany.getCounty() + mCompany.getAddress());
			startActivity(mapIntent);
		} else {
			Toast.makeText(this, R.string.list_not_can_find_user,
					Toast.LENGTH_SHORT).show();
		}
	}

	public void toCompany(View v) {
		if (mCompany != null) {
			Intent companyIntent = new Intent(this, CompanyInfoActivity.class);
			Bundle bundle = new Bundle();
			companyIntent.putExtra("selectUser", mCompany);
			companyIntent.putExtras(bundle);
			startActivity(companyIntent);
		}
	}

	public void release(View v) {
		startActivity(new Intent(this, SupplyDemandPublishActivity.class));
	}

	public void back(View v) {
		finish();
	}
	
	public void setFavotite(View v) {
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
										SupplyDemandInfoActivity.this,
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
		if (mSelectSupplyDemand != null) {
			if (mSelectSupplyDemand.getIsFavorite()) {
				mFavoriteLayout.setClickable(false);
				RequestParams params = new RequestParams();
				params.put("id", Preferences.getAccountUserId() + "");
				params.put("dest_user_id", mSelectSupplyDemand.getUserId() + "");
				params.put("dest_res_id", mSelectSupplyDemand.getId() + "");
				RestClient.post(Constant.API_LIST_FAVORITE_CANCEL, params,
						new AsyncHttpResponseHandler(this,
								new JsonHttpResponseHandler() {
									public void onSuccess(JSONObject res) {
										try {
											if (res.getInt(Constant.JSON_KEY_CODE) == Constant.CODE_SUCCESS) {
												UserAdapter
														.setFavoriteDrawable(
																mFavoriteTrueDrawableFalse,
																mFavoriteButton);
												mSelectSupplyDemand
														.setIsFavorite(false);
												Toast.makeText(
														SupplyDemandInfoActivity.this,
														R.string.list_cancle_favorite_success,
														Toast.LENGTH_SHORT)
														.show();
												setReturnValue();
											} else {
												Toast.makeText(
														SupplyDemandInfoActivity.this,
														res.getString("msg"),
														Toast.LENGTH_SHORT)
														.show();
											}
										} catch (JSONException e) {
											// block
											e.printStackTrace();
											Toast.makeText(
													SupplyDemandInfoActivity.this,
													R.string.list_cancle_favorite_fail,
													Toast.LENGTH_SHORT).show();
										}
									}

									@Override
									public void onFinish() {
										mFavoriteLayout.setClickable(true);
										super.onFinish();
									}
								}));
			} else {
				mFavoriteLayout.setClickable(false);
				RequestParams params = new RequestParams();
				params.put("id", Preferences.getAccountUserId() + "");
				params.put("dest_user_id", mSelectSupplyDemand.getUserId() + "");
				params.put("dest_res_id", mSelectSupplyDemand.getId() + "");
				params.put("type", (mSelectSupplyDemand.getType() + 3) + "");
				RestClient.post(Constant.API_LIST_FAVORITE_ADD, params,
						new AsyncHttpResponseHandler(this,
								new JsonHttpResponseHandler() {
									@Override
									public void onSuccess(JSONObject res) {
										try {
											if (res.getInt(Constant.JSON_KEY_CODE) == Constant.CODE_SUCCESS) {
												UserAdapter
														.setFavoriteDrawable(
																mFavoriteTrueDrawableTrue,
																mFavoriteButton);
												mSelectSupplyDemand
														.setIsFavorite(true);
												Toast.makeText(
														SupplyDemandInfoActivity.this,
														R.string.list_is_favorite_success,
														Toast.LENGTH_SHORT)
														.show();
												setReturnValue();
											} else {
												Toast.makeText(
														SupplyDemandInfoActivity.this,
														res.getString("msg"),
														Toast.LENGTH_SHORT)
														.show();
											}
										} catch (JSONException e) {
											// block
											e.printStackTrace();
											Toast.makeText(
													SupplyDemandInfoActivity.this,
													R.string.list_is_favorite_fail,
													Toast.LENGTH_SHORT).show();
										}
									}

									@Override
									public void onFinish() {
										mFavoriteLayout.setClickable(true);
										super.onFinish();
									}
								}));
			}

		}
	}

	private void setReturnValue() {
		Intent data = getIntent();
		data.putExtra(EXTRA_SUPPLY_DEMAND, mSelectSupplyDemand);
		setResult(Activity.RESULT_OK, data);
	}

	@Override
	public void onRefresh(PullToRefreshBase<ScrollView> refreshView) {
		loadSupplyDemandData();
		loadCompanyData();
	}

	private class SupplyDemandBannerAdapter extends PagerAdapter implements
	View.OnClickListener {
		private ArrayList<Image> mBannerList;

		public SupplyDemandBannerAdapter(ArrayList<Image> imageList) {
			mBannerList = imageList;
		}
		
		@Override
		public int getCount() {
			return mBannerList.size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			// TODO Auto-generated method stub
			return arg0 == arg1;
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			View view = mLayoutInflater.inflate(
					R.layout.search_listitem_banner, null);
			SmartImageView ivBannerImage = (SmartImageView) view
					.findViewById(R.id.siv_search_banner_image);
			Image mBanner = mBannerList.get(position);
			ivBannerImage.setImageUrl(mBanner.getUrl(),
					R.drawable.ic_user_product_default,
					R.drawable.ic_user_product_default);
			view.setClickable(true);
			view.setId(position);
			view.setOnClickListener(this);
			container.addView(view);
			return view;
		}

		@Override
		public void destroyItem(View container, int position, Object object) {
			((ViewPager) container).removeView((View) object);
		}

		public void setDataSource(ArrayList<Image> bannerList) {
			mBannerList = bannerList;
			notifyDataSetChanged();
		}

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			int position = v.getId();
	
			int length = mBannerList.size();
			ArrayList<String> imagePathList = new ArrayList<String>(length);
			for (int i = 0; i < length; i++) {
				String path = mBannerList.get(i).getUrl();
				
				imagePathList.add(path);
			}
			PicturePreviewDialog.build(SupplyDemandInfoActivity.this,
					imagePathList, position).show();
		}
	}

}
