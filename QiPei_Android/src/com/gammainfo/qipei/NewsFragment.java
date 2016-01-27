package com.gammainfo.qipei;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import cn.amose.vpi.CirclePageIndicator;
import cn.amose.vpi.PageIndicator;

import com.gammainfo.qipei.MainActivity.OnTabChangedListener;
import com.gammainfo.qipei.adapter.NewsActivityAdapter;
import com.gammainfo.qipei.adapter.NewsCompanyAdapter;
import com.gammainfo.qipei.adapter.NewsCompanyBannerAdapter;
import com.gammainfo.qipei.adapter.NewsMediaAdapter;
import com.gammainfo.qipei.adapter.NewsTradeAdapter;
import com.gammainfo.qipei.adapter.NewsTradeBannerAdapter;
import com.gammainfo.qipei.convert.NewsJSONConvert;
import com.gammainfo.qipei.db.Preferences;
import com.gammainfo.qipei.http.AsyncHttpResponseHandler;
import com.gammainfo.qipei.http.RequestParams;
import com.gammainfo.qipei.model.News;
import com.gammainfo.qipei.model.helper.NewsHelper;
import com.gammainfo.qipei.utils.Constant;
import com.gammainfo.qipei.widget.UninterceptableViewPager;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.image.SmartImageView;

/**
 * 资讯
 * 
 * @author ZhangHuijun
 * 
 */
public class NewsFragment extends Fragment implements OnItemClickListener,
		PullToRefreshBase.OnRefreshListener2<ListView>, OnTabChangedListener {
	private News mNews;
	private RadioGroup mGroup;
	private MainActivity mContext;
	private LayoutInflater mLayoutInflater;
	private View mNewsLayout;
	private LinearLayout headerView;
	private UninterceptableViewPager mBannerPager;
	private SmartImageView mImageUrl;
	private TextView mTitle;

	private int mSelectType;// 资讯的类别，默认为公司
	private static final int COMPANY = 1; // 公司
	private static final int TRADE = 2; // 行业
	private static final int ACTIVITY = 3; // 活动
	private static final int MEDIA = 4; // 媒体
	private NewsCompanyBannerAdapter mNewsCompanyBannerAdapter;
	private NewsTradeBannerAdapter mNewsTradeBannerAdapter;
	private NewsCompanyAdapter mNewsCompanyAdapter;
	private NewsTradeAdapter mNewsTradeAdapter;
	private NewsActivityAdapter mNewsActivityAdapter;
	private NewsMediaAdapter mNewsMediaAdapter;
	private PullToRefreshListView mCompanyPullToRefreshListView;
	private PullToRefreshListView mTradePullToRefreshListView;
	private PullToRefreshListView mActivityPullToRefreshListView;
	private PullToRefreshListView mMadiaPullToRefreshListView;
	private AsyncHttpClient mHttpClient = new AsyncHttpClient();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mNewsLayout = inflater
				.inflate(R.layout.fragment_news, container, false);
		return mNewsLayout;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mContext = (MainActivity) getActivity();
		mContext.registerOnTabChangedListener(this);
		initViews();
	}

	@Override
	public void onChanged(MainActivity mainActivity, int selectedTabId) {
		// TODO 选中的标签发生变化时执行
		if (selectedTabId == R.id.tab_news) {
			mBannerPager.enableFlip();
		} else {
			mBannerPager.disableFlip();
			if(mHttpClient != null) {
				mHttpClient.cancelRequests(mContext, true);
			}
		}
	}

	/*
	 * 初始化
	 */
	private void initViews() {
		mSelectType = COMPANY;
		mLayoutInflater = mContext.getLayoutInflater();
		mGroup = (RadioGroup) mContext.findViewById(R.id.rg_news_type);
		mGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// TODO Auto-generated method stub
				setNewsTypeSelection(checkedId);
			}
		});
		companyViewCreated();// 第一次启动时选中Company
		loadDataByType(COMPANY);
		getNewsBanner(COMPANY);
		mCompanyPullToRefreshListView.setVisibility(View.VISIBLE);
	}

	/*
	 * 隐藏已经初始化的View
	 */
	private void hideListView() {
		if (mCompanyPullToRefreshListView != null) {
			mCompanyPullToRefreshListView.setVisibility(View.GONE);
		}
		if (mTradePullToRefreshListView != null) {
			mTradePullToRefreshListView.setVisibility(View.GONE);
		}
		if (mActivityPullToRefreshListView != null) {
			mActivityPullToRefreshListView.setVisibility(View.GONE);
		}
		if (mMadiaPullToRefreshListView != null) {
			mMadiaPullToRefreshListView.setVisibility(View.GONE);
		}
	}

	public void companyViewCreated() {
		if (mCompanyPullToRefreshListView == null) {
			// 实例化ListView对象
			LinearLayout contentView = (LinearLayout) mLayoutInflater.inflate(
					R.layout.news_company_listiew, null);
			FrameLayout mFrameLayout = (FrameLayout) mContext
					.findViewById(R.id.news_content);
			mFrameLayout.addView(contentView);// 在刷新控件上添加整体布局
			// 得到刷新控件
			mCompanyPullToRefreshListView = (PullToRefreshListView) contentView
					.findViewById(R.id.prlv_news_comapny);

			View headerView = mLayoutInflater.inflate(
					R.layout.news_listview_company_header_layout, null);
			mCompanyPullToRefreshListView.getRefreshableView().addHeaderView(
					headerView);// 在刷新控件上添加Banner布局

			mBannerPager = (UninterceptableViewPager) headerView
					.findViewById(R.id.vp_news_company_pager);
			mNewsCompanyBannerAdapter = new NewsCompanyBannerAdapter(mContext,
					new ArrayList<News>());

			mBannerPager.setAdapter(mNewsCompanyBannerAdapter);
			PageIndicator indicator = (CirclePageIndicator) headerView
					.findViewById(R.id.cpi_news_company_pager_indcator);
			indicator.setViewPager(mBannerPager);
			// 加载列表适配器
			mNewsCompanyAdapter = new NewsCompanyAdapter(mContext,
					new ArrayList<News>());
			mCompanyPullToRefreshListView.setAdapter(mNewsCompanyAdapter);
			mCompanyPullToRefreshListView.setOnRefreshListener(this);
			mCompanyPullToRefreshListView.setOnItemClickListener(this);
			mCompanyPullToRefreshListView.setMode(Mode.BOTH);
		}
	}

	public void tradeViewCreated() {
		if (mTradePullToRefreshListView == null) {
			// 实例化ListView对象
			LinearLayout contentView = (LinearLayout) mLayoutInflater.inflate(
					R.layout.news_trade_listview, null);
			FrameLayout mFrameLayout = (FrameLayout) mContext
					.findViewById(R.id.news_content);
			mFrameLayout.addView(contentView);// 在刷新控件上添加整体布局
			// 得到刷新控件
			mTradePullToRefreshListView = (PullToRefreshListView) contentView
					.findViewById(R.id.prlv_news_trade);

			View headerView = mLayoutInflater.inflate(
					R.layout.news_listview_trade_header_layout, null);
			mTradePullToRefreshListView.getRefreshableView().addHeaderView(
					headerView);// 在刷新控件上添加Banner布局

			mBannerPager = (UninterceptableViewPager) headerView
					.findViewById(R.id.vp_news_trade_pager);
			mNewsTradeBannerAdapter = new NewsTradeBannerAdapter(mContext,
					new ArrayList<News>());
			mBannerPager.setAdapter(mNewsTradeBannerAdapter);
			PageIndicator indicator = (CirclePageIndicator) headerView
					.findViewById(R.id.cpi_news_trade_pager_indcator);
			indicator.setViewPager(mBannerPager);
			// 加载列表适配器
			mNewsTradeAdapter = new NewsTradeAdapter(mContext,
					new ArrayList<News>());
			mTradePullToRefreshListView.setAdapter(mNewsTradeAdapter);
			mTradePullToRefreshListView.setOnRefreshListener(this);
			mTradePullToRefreshListView.setOnItemClickListener(this);
			mTradePullToRefreshListView.setMode(Mode.BOTH);
		}
	}

	public void activityViewCreated() {
		if (mActivityPullToRefreshListView == null) {
			// 实例化ListView对象
			LinearLayout contentView = (LinearLayout) mLayoutInflater.inflate(
					R.layout.news_activity_listiew, null);
			FrameLayout mFrameLayout = (FrameLayout) mContext
					.findViewById(R.id.news_content);
			mFrameLayout.addView(contentView);// 在刷新控件上添加整体布局
			// 得到刷新控件
			mActivityPullToRefreshListView = (PullToRefreshListView) contentView
					.findViewById(R.id.prlv_news_activity);
			mNewsActivityAdapter = new NewsActivityAdapter(mContext,
					new ArrayList<News>());
			mActivityPullToRefreshListView.setAdapter(mNewsActivityAdapter);
			mActivityPullToRefreshListView.setOnRefreshListener(this);
			mActivityPullToRefreshListView.setOnItemClickListener(this);
			mActivityPullToRefreshListView.setMode(Mode.BOTH);

			View emptyView = mLayoutInflater.inflate(
					R.layout.listview_emptyview_news_activity, null);
			((ViewGroup) mActivityPullToRefreshListView.getParent())
					.addView(emptyView);
			emptyView.setVisibility(View.GONE);
			mActivityPullToRefreshListView.setEmptyView(emptyView);
		}
	}

	public void mediaViewCreated() {
		if (mMadiaPullToRefreshListView == null) {
			// 实例化ListView对象
			LinearLayout contentView = (LinearLayout) mLayoutInflater.inflate(
					R.layout.news_media_listview, null);
			FrameLayout mFrameLayout = (FrameLayout) mContext
					.findViewById(R.id.news_content);
			mFrameLayout.addView(contentView);// 在刷新控件上添加整体布局
			// 得到刷新控件
			mMadiaPullToRefreshListView = (PullToRefreshListView) contentView
					.findViewById(R.id.prlv_news_media);

			headerView = (LinearLayout) mLayoutInflater.inflate(
					R.layout.news_listitem_media_header_layout, null);
			mMadiaPullToRefreshListView.getRefreshableView().addHeaderView(
					headerView);// 在刷新控件上添加Banner布局
			mImageUrl = (SmartImageView) headerView
					.findViewById(R.id.siv_news_media_header_image);
			mTitle = (TextView) headerView
					.findViewById(R.id.tv_news_media_header_title);

			// 对media上面的banner事件添加监听
			headerView.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					if (mNews != null) {
						Intent companyActivityIntent = new Intent(mContext,
								NewsContentActivity.class);
						Bundle bundle = new Bundle();
						bundle.putInt(NewsContentActivity.EXTRA_TYPE,
								NewsContentActivity.TYPE_MEDIA);
						bundle.putString(NewsContentActivity.EXTRA_URL,
								mNews.getPageUrl());
						companyActivityIntent.putExtras(bundle);
						startActivity(companyActivityIntent);
					}
				}
			});
			mNewsMediaAdapter = new NewsMediaAdapter(mContext,
					new ArrayList<News>());
			mMadiaPullToRefreshListView.setAdapter(mNewsMediaAdapter);
			mMadiaPullToRefreshListView.setOnRefreshListener(this);
			mMadiaPullToRefreshListView.setOnItemClickListener(this);
			mMadiaPullToRefreshListView.setMode(Mode.BOTH);
		}
	}

	/**
	 * 根据传入的resId参数来设置选中的News类型页。
	 * 
	 * @param resId
	 *            每个tab页中对应的资源ID(layout id)
	 */
	private void setNewsTypeSelection(int resId) {
		mHttpClient.cancelRequests(mContext, true);
		hideListView(); // TODO隐藏所有的listview
		switch (resId) {
		case R.id.rb_company:
			companyViewCreated();
			mCompanyPullToRefreshListView.setVisibility(View.VISIBLE);
			mSelectType = COMPANY;
			mNewsCompanyAdapter.setPageNumber(1);
			loadDataByType(COMPANY);
			break;
		case R.id.rb_trade:
			tradeViewCreated();
			mTradePullToRefreshListView.setVisibility(View.VISIBLE);
			mSelectType = TRADE;
			mNewsTradeAdapter.setPageNumber(1);
			loadDataByType(TRADE);
			break;
		case R.id.rb_activity:
			activityViewCreated();
			mActivityPullToRefreshListView.setVisibility(View.VISIBLE);
			mSelectType = ACTIVITY;
			mNewsActivityAdapter.setPageNumber(1);
			loadDataByType(ACTIVITY);
			break;
		case R.id.rb_media:
			mediaViewCreated();
			mMadiaPullToRefreshListView.setVisibility(View.VISIBLE);
			mSelectType = MEDIA;
			mNewsMediaAdapter.setPageNumber(1);
			loadDataByType(MEDIA);
			break;
		}
	}

	/**
	 * 列表条目点击事件
	 */
	@Override
	public void onItemClick(AdapterView<?> arg0, View view, int position,
			long id) {
		Intent companyActivityIntent = new Intent(mContext,
				NewsContentActivity.class);
		Bundle bundle = new Bundle();
		ArrayList<News> newsList = null;
		switch (mSelectType) {
		case COMPANY:
			position = position - 2;
			newsList = mNewsCompanyAdapter.getDataSource();
			bundle.putInt(NewsContentActivity.EXTRA_TYPE,
					NewsContentActivity.TYPE_COMPANY);
			bundle.putString(NewsContentActivity.EXTRA_URL,
					newsList.get(position).getPageUrl());
			break;
		case TRADE:
			position = position - 2;
			newsList = mNewsTradeAdapter.getDataSource();
			bundle.putInt(NewsContentActivity.EXTRA_TYPE,
					NewsContentActivity.TYPE_TRADE);
			bundle.putString(NewsContentActivity.EXTRA_URL,
					newsList.get(position).getPageUrl());
			break;
		case ACTIVITY:
			position = position - 1;
			newsList = mNewsActivityAdapter.getDataSource();
			bundle.putInt(NewsContentActivity.EXTRA_TYPE,
					NewsContentActivity.TYPE_ACTIVITY);
			bundle.putString(NewsContentActivity.EXTRA_URL,
					newsList.get(position).getPageUrl());
			break;
		case MEDIA:
			position = position - 2;
			newsList = mNewsMediaAdapter.getDataSource();
			bundle.putInt(NewsContentActivity.EXTRA_TYPE,
					NewsContentActivity.TYPE_MEDIA);
			if (newsList.size() != 0) {
				bundle.putString(NewsContentActivity.EXTRA_URL,
						newsList.get(position).getPageUrl());
			}
			break;
		}
		companyActivityIntent.putExtras(bundle);
		startActivity(companyActivityIntent);
	}

	// private void testExample() {
	// RequestParams params = new RequestParams();
	// params.put("key", " ");
	// params.put("type", String.valueOf(1));
	// params.put("area", Preferences.getMyCity());
	// mHttpClient.post(mContext, "url", params, new AsyncHttpResponseHandler(
	// mContext, new JsonHttpResponseHandler() {
	// public void onSuccess(JSONObject res) {
	// // TODO Auto-generated method stub
	// JSONObject jsonInfo = null;
	// int isBanner = 0;
	// }
	// }));
	// //在公司、行业等之间切换的时候
	// mHttpClient.cancelRequests(mContext, true);
	// //mHttpClient.post(url, responseHandler)
	//
	// }

	/* 通过所选用户的种类获取并加载所需要的数据 */
	private void loadDataByType(int selectType) {
		ArrayList<News> newsList = NewsHelper.select(selectType, 0);
		ArrayList<News> newsBannerList = NewsHelper.select(selectType, 1);
		switch (selectType) {
		case COMPANY:
			mNewsCompanyAdapter.setDataSource(newsList);
			mNewsCompanyBannerAdapter.setDataSource(newsBannerList);
			break;
		case TRADE:
			mNewsTradeAdapter.setDataSource(newsList);
			mNewsTradeBannerAdapter.setDataSource(newsBannerList);
			break;
		case ACTIVITY:
			mNewsActivityAdapter.setDataSource(newsList);
			break;
		case MEDIA:
			// TODO 可能出问题
			switch (newsList.size()) {
			case 0:// 如果没有消息则提示
//				mImageUrl.setImageUrl("", R.drawable.ic_user_product_default,
//						R.drawable.ic_user_product_default);
//				mTitle.setText("");
//				mNewsMediaAdapter.setDataSource(newsList);
				break;
			case 1:// 如果有一条消息则加载到banner上边
				mNews = newsList.get(0);
				mImageUrl.setImageUrl(mNews.getImageUrl(),
						R.drawable.ic_user_product_default,
						R.drawable.ic_user_product_default);
				mTitle.setText(mNews.getTitle());
				break;
			default:// 如果有超过一条数据则执行下面的方法
				mNews = newsList.get(0);
				newsList.remove(0);
				mImageUrl.setImageUrl(mNews.getImageUrl(),
						R.drawable.ic_user_product_default,
						R.drawable.ic_user_product_default);
				mTitle.setText(mNews.getTitle());
				mNewsMediaAdapter.setDataSource(newsList);
				break;
			}
			break;
		}
		if (selectType == COMPANY || selectType == TRADE) {
			getNewsBanner(selectType);
		}
		getNewsDataById(selectType);
	}

	/**
	 * 调用API，从服务器数据库获取数据并插入手机数据库
	 * 
	 * @param selectType
	 *            资讯类型（1：公司，2：行业，3：活动，4：媒体）
	 */
	public void getNewsDataById(final int selectType) {
		RequestParams params = new RequestParams();
		params.put("key", " ");
		params.put("type", String.valueOf(selectType));
		params.put("area", Preferences.getMyCity());
		switch (selectType) {
		case COMPANY:
			params.put("page_index",
					String.valueOf(mNewsCompanyAdapter.getPageNumber()));
			params.put("page_size",
					String.valueOf(mNewsCompanyAdapter.getPageSize()));
			break;
		case TRADE:
			params.put("page_index",
					String.valueOf(mNewsTradeAdapter.getPageNumber()));
			params.put("page_size",
					String.valueOf(mNewsTradeAdapter.getPageSize()));
			break;
		case ACTIVITY:
			params.put("page_index",
					String.valueOf(mNewsActivityAdapter.getPageNumber()));
			params.put("page_size",
					String.valueOf(mNewsActivityAdapter.getPageSize()));
			break;
		case MEDIA:
			params.put("page_index",
					String.valueOf(mNewsMediaAdapter.getPageNumber()));
			params.put("page_size",
					String.valueOf(mNewsMediaAdapter.getPageSize()));
			break;
		}

		mHttpClient.post(mContext, Constant.API_NEWS_LIST_GET, params,
				new AsyncHttpResponseHandler(mContext,
						new JsonHttpResponseHandler() {
							public void onSuccess(JSONObject res) {
								// TODO Auto-generated method stub
								JSONObject jsonInfo = null;
								int isBanner = 0;
								ArrayList<News> newsList;
								try {
									if (res.getInt(Constant.JSON_KEY_CODE) == Constant.CODE_SUCCESS) {
										System.out.println("=====zhj======获取数据成功");
										jsonInfo = res
												.getJSONObject(Constant.JSON_KEY_INFO);
										newsList = NewsJSONConvert
												.convertJsonArrayToItemList(jsonInfo
														.getJSONArray(Constant.JSON_KEY_LIST));
										switch (selectType) {
										case COMPANY:
											if (mNewsCompanyAdapter
													.getPageNumber() == 1) {// 如果为缓存数据，获得真实数据时清空列表
												mNewsCompanyAdapter
														.setDataSource(newsList);
												NewsHelper.asyncInsert(
														mContext, newsList,
														isBanner, selectType);// 将接口返回的数据加入到本地数据库
											} else {
												if (newsList.size() == 0) {
													Toast.makeText(
															mContext,
															getString(R.string.common_label_last_page),
															Toast.LENGTH_SHORT)
															.show();
												}
												mNewsCompanyAdapter
														.appendDataSource(newsList);
											}
											mNewsCompanyAdapter.incPageNumber();
											break;
										case TRADE:
											if (mNewsTradeAdapter
													.getPageNumber() == 1) {// 如果为缓存数据，获得真实数据时清空列表
												mNewsTradeAdapter
														.setDataSource(newsList);
												NewsHelper.asyncInsert(
														mContext, newsList,
														isBanner, selectType);// 将接口返回的数据加入到本地数据库
											} else {
												if (newsList.size() == 0) {
													Toast.makeText(
															mContext,
															getString(R.string.common_label_last_page),
															Toast.LENGTH_SHORT)
															.show();
												}
												mNewsTradeAdapter
														.appendDataSource(newsList);
											}
											mNewsTradeAdapter.incPageNumber();
											break;
										case ACTIVITY:
											if (mNewsActivityAdapter
													.getPageNumber() == 1) {// 如果为缓存数据，获得真实数据时清空列表
												mNewsActivityAdapter
														.setDataSource(newsList);
												NewsHelper.asyncInsert(
														mContext, newsList,
														isBanner, selectType);// 将接口返回的数据加入到本地数据库
											} else {
												if (newsList.size() == 0) {
													Toast.makeText(
															mContext,
															getString(R.string.common_label_last_page),
															Toast.LENGTH_SHORT)
															.show();
												}
												mNewsActivityAdapter
														.appendDataSource(newsList);
											}
											mNewsActivityAdapter
													.incPageNumber();
											break;
										case MEDIA:
											if (mNewsMediaAdapter
													.getPageNumber() == 1) {
												ArrayList<News> newsList2 = new ArrayList<News>();
												for(int i = 0;i<newsList.size();i++) {
													newsList2.add(newsList.get(i));
												}
												NewsHelper.asyncInsert(
														mContext, newsList2,
														isBanner, selectType);// 将接口返回的数据加入到本地数据库
											}

											switch (newsList.size()) {
											case 0:
												if (mNewsMediaAdapter
														.getPageNumber() != 1) {
													Toast.makeText(
															mContext,
															getString(R.string.common_label_last_page),
															Toast.LENGTH_SHORT)
															.show();
												}else{
													newsList.removeAll(newsList);
													Toast.makeText(
															mContext,
															"没有数据！",
															Toast.LENGTH_SHORT)
															.show();
												}
												break;
											case 1:
												if (mNewsMediaAdapter
														.getPageNumber() == 1) {
													mNews = newsList.get(0);
													mImageUrl.setImageUrl(mNews.getImageUrl(),
															R.drawable.ic_user_product_default,
															R.drawable.ic_user_product_default);
													mTitle.setText(mNews.getTitle());
													newsList.removeAll(newsList);
													mNewsMediaAdapter
															.appendDataSource(newsList);
												}else{
													
												}
												break;
											default:// 如果有超过一条数据则执行下面的方法
												System.out.println("=====zhj=====newsList.size()++"+newsList.size());
												if (mNewsMediaAdapter
														.getPageNumber() == 1) {// 如果为缓存数据，获得真实数据时清空列表
													mNews = newsList.get(0);
													newsList.remove(0);
													mImageUrl.setImageUrl(mNews.getImageUrl(),
															R.drawable.ic_user_product_default,
															R.drawable.ic_user_product_default);
													mTitle.setText(mNews.getTitle());
													mNewsMediaAdapter
															.setDataSource(newsList);
												} else {
													if (newsList.size() == 0) {
														Toast.makeText(
																mContext,
																getString(R.string.common_label_last_page),
																Toast.LENGTH_SHORT)
																.show();
													}
													newsList.removeAll(newsList);
													mNewsMediaAdapter
															.appendDataSource(newsList);
												}
												mNewsMediaAdapter
														.incPageNumber();
												break;
											}
											break;
										}
									} else {
										Toast.makeText(
												mContext,
												res.getString(Constant.JSON_MESSAGE),
												Toast.LENGTH_SHORT).show();
									}
								} catch (JSONException e) {
									e.printStackTrace();
								}
							}

							@Override
							public void onFinish() {
								switch (selectType) {
								case COMPANY:
									mCompanyPullToRefreshListView
											.onRefreshComplete();
									break;
								case TRADE:
									mTradePullToRefreshListView
											.onRefreshComplete();
									break;
								case ACTIVITY:
									mActivityPullToRefreshListView
											.onRefreshComplete();
									break;
								case MEDIA:
									mMadiaPullToRefreshListView
											.onRefreshComplete();
									break;
								}
								super.onFinish();
							}
						}));
	}

	private void getNewsBanner(final int type) {
		RequestParams params = new RequestParams();
		params.put("type", type + "");
		params.put("area", Preferences.getMyCity());
		mHttpClient.post(mContext, Constant.API_GET_NEWS_BANNER, params,
				new AsyncHttpResponseHandler(mContext,
						new JsonHttpResponseHandler() {
							public void onSuccess(JSONObject res) {
								JSONObject jsonInfo = null;
								int isBanner = 1;
								ArrayList<News> newsBannerList;
								try {
									if (res.getInt(Constant.JSON_KEY_CODE) == Constant.CODE_SUCCESS) {
										jsonInfo = res
												.getJSONObject(Constant.JSON_KEY_INFO);
										newsBannerList = NewsJSONConvert
												.convertJsonArrayToItemList(jsonInfo
														.getJSONArray(Constant.JSON_KEY_LIST));
										NewsHelper.asyncInsert(mContext,
												newsBannerList, isBanner, type);// 将接口返回的数据加入到本地数据库
										switch (type) {
										case COMPANY:
											mNewsCompanyBannerAdapter
													.setDataSource(newsBannerList);
											break;
										case TRADE:
											mNewsTradeBannerAdapter
													.setDataSource(newsBannerList);
											break;
										}
									} else {
										Toast.makeText(
												mContext,
												res.getString(Constant.JSON_MESSAGE),
												Toast.LENGTH_SHORT).show();
									}
								} catch (JSONException e) {
									e.printStackTrace();
								}
							}

							@Override
							public void onFinish() {
								if (type == COMPANY) {
									mCompanyPullToRefreshListView
											.onRefreshComplete();
								}
								if (type == TRADE) {
									mTradePullToRefreshListView
											.onRefreshComplete();
								}
								super.onFinish();
							}
						}));
	}

	/*
	 * 刷新数据 (non-Javadoc)
	 * 
	 * @see
	 * com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2
	 * #onPullDownToRefresh
	 * (com.handmark.pulltorefresh.library.PullToRefreshBase)
	 */
	@Override
	public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
		// TODO Auto-generated method stub
		String label = DateUtils.formatDateTime(mContext,
				System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME
						| DateUtils.FORMAT_SHOW_DATE
						| DateUtils.FORMAT_ABBREV_ALL);
		// Update the LastUpdatedLabel
		refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);

		// Do work to refresh the list here.
		if (mSelectType == COMPANY) {
			mNewsCompanyAdapter.setPageNumber(1);
			getNewsBanner(COMPANY);
			getNewsDataById(COMPANY);
		}
		if (mSelectType == TRADE) {
			mNewsTradeAdapter.setPageNumber(1);
			getNewsBanner(TRADE);
			getNewsDataById(TRADE);
		}
		if (mSelectType == ACTIVITY) {
			mNewsActivityAdapter.setPageNumber(1);
			getNewsDataById(ACTIVITY);
		}
		if (mSelectType == MEDIA) {
			mNews = null;
			mNewsMediaAdapter.setPageNumber(1);
			getNewsDataById(MEDIA);
		}
	}

	/*
	 * 加载更多数据 (non-Javadoc)
	 * 
	 * @see
	 * com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2
	 * #onPullUpToRefresh(com.handmark.pulltorefresh.library.PullToRefreshBase)
	 */
	@Override
	public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
		if (mSelectType == COMPANY) {
			getNewsDataById(COMPANY);
		}
		if (mSelectType == TRADE) {
			getNewsDataById(TRADE);
		}
		if (mSelectType == ACTIVITY) {
			getNewsDataById(ACTIVITY);
		}
		if (mSelectType == MEDIA) {
			getNewsDataById(MEDIA);
		}
	}
}
