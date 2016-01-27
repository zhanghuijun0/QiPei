package com.gammainfo.qipei;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import cn.amose.vpi.CirclePageIndicator;
import cn.amose.vpi.PageIndicator;

import com.gammainfo.qipei.convert.ProductJSONConvert;
import com.gammainfo.qipei.http.AsyncHttpResponseHandler;
import com.gammainfo.qipei.http.RequestParams;
import com.gammainfo.qipei.http.RestClient;
import com.gammainfo.qipei.model.Image;
import com.gammainfo.qipei.model.Product;
import com.gammainfo.qipei.utils.Constant;
import com.gammainfo.qipei.widget.PicturePreviewDialog;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshScrollView;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.image.SmartImageView;

public class ProductInfoActivity extends BaseActivity implements
		PullToRefreshBase.OnRefreshListener<ScrollView> {
	public static final String EXTRA_PRODUCT_ID = "selectProductId";
	private int mSelectProductId;
	private Product mSelectProduct;
	private PullToRefreshScrollView mPullToRefreshScrollView;
	private ArrayList<Image> mImages;
	private ArrayList<Property> mPropertyList;
	private LayoutInflater mLayoutInflater;
	private ProductBannerAdapter mBannerAdapter;
	private LinearLayout mPropertyListLayout;
	private TextView mLableView;
	private WebView mIntroView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_product_info);
		Intent getIntent = getIntent();

		mSelectProduct = (Product) getIntent.getExtras().getParcelable(
				"selectProduct");
		if (mSelectProduct != null) {
			mSelectProductId = mSelectProduct.getId();
		} else {
			mSelectProductId = getIntent.getIntExtra(EXTRA_PRODUCT_ID, 0);
		}
		mLayoutInflater = getLayoutInflater();
		LinearLayout bottomView = (LinearLayout) mLayoutInflater.inflate(
				R.layout.product_bottom, null);
		mPullToRefreshScrollView = (PullToRefreshScrollView) findViewById(R.id.product_show_refesh);
		mPullToRefreshScrollView.addView(bottomView,
				new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.MATCH_PARENT,
						LinearLayout.LayoutParams.MATCH_PARENT));
		mPullToRefreshScrollView.setOnRefreshListener(this);
		changePropertyList();
		initView();
		addProperty();
		setView();
		loadProductInfo();
	}

	/* 加载数据 */
	private void loadProductInfo() {
		RequestParams params = new RequestParams();
		params.put("id", mSelectProductId + "");
		RestClient.post(Constant.API_USER_PRODUCT_ITEM_GET, params,
				new AsyncHttpResponseHandler(ProductInfoActivity.this,  new JsonHttpResponseHandler() {
					@Override
					public void onSuccess(JSONObject res) {
						JSONObject jsonInfo = null;
						try {
							if (res.getInt(Constant.JSON_KEY_CODE) == Constant.CODE_SUCCESS) {
								jsonInfo = res
										.getJSONObject(Constant.JSON_KEY_INFO);
								mSelectProduct = ProductJSONConvert
										.convertJsonToItem(jsonInfo);
								mImages = mSelectProduct.getImgs();
								mBannerAdapter.setDataSource(mImages);
								changePropertyList();
								addProperty();
								setView();
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}

					public void onFinish() {
						mPullToRefreshScrollView.onRefreshComplete();
						super.onFinish();
					}
				})); 
	}

	private void initView() {
		if (mSelectProduct != null) {
			mImages = mSelectProduct.getImgs();
		} else {
			mImages = new ArrayList<Image>();
		}
		mPropertyListLayout = (LinearLayout) findViewById(R.id.product_property_list);
		mLableView = (TextView) findViewById(R.id.product_lable_show);
		mIntroView = (WebView) findViewById(R.id.product_intro);
		ViewPager bannerPager = (ViewPager) findViewById(R.id.vp_search_pager);
		mBannerAdapter = new ProductBannerAdapter(mImages);
		bannerPager.setAdapter(mBannerAdapter);
		PageIndicator indicator = (CirclePageIndicator) findViewById(R.id.cpi_search_pager_indcator);
		indicator.setViewPager(bannerPager);
	}

	/* 添加(刷新)规格布局 */
	private void addProperty() {
		mPropertyListLayout.removeAllViews();
		for (int i = 0; i < mPropertyList.size(); i++) {
			LinearLayout listItemLayout = (LinearLayout) mLayoutInflater
					.inflate(R.layout.property_listview_item, null);
			((TextView) listItemLayout.findViewById(R.id.property_textView_key))
					.setText(mPropertyList.get(i).key);
			((TextView) listItemLayout
					.findViewById(R.id.property_textView_value))
					.setText(mPropertyList.get(i).value);
			if(i == mPropertyList.size() - 1) {
				((View) listItemLayout.findViewById(R.id.last_property_line)).setVisibility(View.VISIBLE);
			}
			mPropertyListLayout.addView(listItemLayout,
					new LinearLayout.LayoutParams(
							LinearLayout.LayoutParams.MATCH_PARENT,
							LinearLayout.LayoutParams.MATCH_PARENT));
		}
	}

	/* 当mSelectProduct改变时，调用此方法，改变规格列表mPropertyList */
	public void changePropertyList() {
		if (mSelectProduct != null) {
			try {
				String property = mSelectProduct.getProperty();
				JSONArray array = new JSONArray(property);
				// properLength = array.length();
				mPropertyList = convertJsonArrayToItemList(array);
			} catch (JSONException e) {
				mPropertyList = new ArrayList<Property>();
				e.printStackTrace();
			}
		} else {
			mPropertyList = new ArrayList<Property>();
		}
	}

	/* 数据改变时对数据进行设置 */
	private void setView() {
		if (mSelectProduct != null) {
			mLableView.setText(mSelectProduct.getProductName());
			if(!mSelectProduct.getProductInfo().trim().equals("")) {
				mIntroView.loadDataWithBaseURL("", mSelectProduct.getProductInfo()
						.toString(), "text/html", "UTF-8", "");
			}else {
				mIntroView.loadDataWithBaseURL("", mSelectProduct.getBrief(), "text/html", "UTF-8", "");
			}
			
		}
	}

	private class ProductBannerAdapter extends PagerAdapter implements
		View.OnClickListener{
		private ArrayList<Image> mBannerList;

		public ProductBannerAdapter(ArrayList<Image> imageList) {
			mBannerList = imageList;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
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
			ivBannerImage.setImageUrl(mBanner.getUrl(),R.drawable.ic_user_product_default,R.drawable.ic_user_product_default);
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
			PicturePreviewDialog.build(ProductInfoActivity.this,
					imagePathList, position).show();
		}
	}

	private class Property {
		public String key;
		public String value;
	}

	public ArrayList<Property> convertJsonArrayToItemList(JSONArray jsonArray)
			throws JSONException {
		int length = jsonArray.length();
		ArrayList<Property> list = new ArrayList<Property>();
		for (int i = 0; i < length; i++) {
			list.add(convertJsonToItem(jsonArray.getJSONObject(i)));
		}
		return list;
	}

	public Property convertJsonToItem(JSONObject json) throws JSONException {
		Property property = new Property();
		property.key = json.getString("key");
		property.value = json.getString("value");
		return property;
	}

	public void back(View v) {
		finish();
	}

	@Override
	public void onRefresh(PullToRefreshBase<ScrollView> refreshView) {
		String label = DateUtils.formatDateTime(this,
				System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME
						| DateUtils.FORMAT_SHOW_DATE
						| DateUtils.FORMAT_ABBREV_ALL);
		refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
		loadProductInfo();
	}

}
