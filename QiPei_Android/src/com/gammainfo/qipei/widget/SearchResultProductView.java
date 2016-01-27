package com.gammainfo.qipei.widget;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.gammainfo.qipei.ProductInfoActivity;
import com.gammainfo.qipei.R;
import com.gammainfo.qipei.adapter.ProductAdapter;
import com.gammainfo.qipei.convert.ProductJSONConvert;
import com.gammainfo.qipei.http.AsyncHttpResponseHandler;
import com.gammainfo.qipei.http.RequestParams;
import com.gammainfo.qipei.model.Product;
import com.gammainfo.qipei.utils.Constant;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

@SuppressLint("NewApi")
public class SearchResultProductView extends LinearLayout implements
		OnItemClickListener, PullToRefreshBase.OnRefreshListener2<ListView> {
	private Context mContext;
	private LayoutInflater mLayoutInflater;
	private PullToRefreshListView mProductListView;
	private ProductAdapter mProductAdapter;
	private View mListViewEmptyView;
	private SearchView mSearchView;
	private AsyncHttpClient mAsyncHttpClient;

	public SearchResultProductView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public SearchResultProductView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public SearchResultProductView(Context context) {
		super(context);
		init(context);
	}

	@Override
	public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
		String label = DateUtils.formatDateTime(mContext,
				System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME
						| DateUtils.FORMAT_SHOW_DATE
						| DateUtils.FORMAT_ABBREV_ALL);

		// Update the LastUpdatedLabel
		refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
		mProductAdapter.setPageNumber(1);
		search();
	}

	@Override
	public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
		search();
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			long arg3) {
		position = position - 1;
		ArrayList<Product> productList = mProductAdapter.getDataSource();
		Product selectProduct = productList.get(position);
		Intent productActivityIntent = new Intent(mContext,
				ProductInfoActivity.class);
		Bundle bundle = new Bundle();
		bundle.putInt("selectProductId", selectProduct.getId());
		productActivityIntent.putExtra("selectProduct", selectProduct);
		productActivityIntent.putExtras(bundle);
		mContext.startActivity(productActivityIntent);
	}

	private void init(Context context) {
		mContext = context;
		mLayoutInflater = LayoutInflater.from(context);
		View viewContainer = mLayoutInflater.inflate(
				R.layout.search_result_product_layout, null);
		mProductListView = (PullToRefreshListView) viewContainer
				.findViewById(R.id.prlv_search_product);
		mProductListView.setOnItemClickListener(this);
		mProductListView.setOnRefreshListener(this);
		mProductAdapter = new ProductAdapter(new ArrayList<Product>(), context);
		mProductListView.setAdapter(mProductAdapter);
		mProductAdapter.setPageNumber(1);
		mListViewEmptyView = viewContainer
				.findViewById(R.id.search_product_emptyview);
		addView(viewContainer);
	}

	public void setDataSource(ArrayList<Product> productList) {
		mProductAdapter.setDataSource(productList);
	}

	public void appendDataSource(ArrayList<Product> productList) {
		mProductAdapter.appendDataSource(productList);
	}

	public ArrayList<Product> getDataSource() {
		return mProductAdapter.getDataSource();
	}

	public void search() {
		String city = mSearchView.getCurrentCity();
		String keyword = mSearchView.getCurrentKeyword();
		RequestParams params = new RequestParams();
		params.put("area", city);
		params.put("key", keyword);
		params.put("page_index",
				String.valueOf(mProductAdapter.getPageNumber()));
		params.put("page_size", String.valueOf(mProductAdapter.getPageSize()));
		if (mAsyncHttpClient == null) {
			mAsyncHttpClient = new AsyncHttpClient();
		} else {
			mAsyncHttpClient.cancelRequests(mContext, true);
		}
		mAsyncHttpClient.get(mContext, Constant.API_SEARCH_PRODUCT_LIST,
				params, new AsyncHttpResponseHandler(mContext,
						new JsonHttpResponseHandler() {

							@Override
							public void onStart() {
								super.onStart();
								showEmptyViewIfNeed();
							}

							@Override
							public void onSuccess(JSONObject rlt) {
								try {
									if (rlt.getInt(Constant.JSON_KEY_CODE) == Constant.CODE_SUCCESS) {
										JSONObject infoJsonObject = rlt
												.getJSONObject(Constant.JSON_KEY_INFO);
										ArrayList<Product> productList = ProductJSONConvert
												.convertJsonArrayToItemList(infoJsonObject
														.getJSONArray(Constant.JSON_KEY_LIST));
										if (mProductAdapter.incPageNumber() == 2) {
											mProductAdapter
													.setDataSource(productList);
										} else if (productList.isEmpty() == false) {
											mProductAdapter
													.appendDataSource(productList);
										} else {
											if (productList.isEmpty()) {
												Toast.makeText(
														mContext,
														R.string.common_label_last_page,
														Toast.LENGTH_SHORT)
														.show();
											}
										}
										hideEmptyViewIfNeed();
									} else {
										Toast.makeText(
												mContext,
												rlt.getString(Constant.JSON_MESSAGE),
												Toast.LENGTH_SHORT).show();
									}
								} catch (JSONException e) {
									e.printStackTrace();
								}
							}

							@Override
							public void onFinish() {
								// 需要在onSuccess再调用一次hideEmptyViewIfNeed()，因为onFinish是在onSuccess之前执行的
								hideEmptyViewIfNeed();
								mProductListView.onRefreshComplete();
								super.onFinish();
							}

						}));
	}

	private void hideEmptyViewIfNeed() {
		if (mProductAdapter.isEmpty()) {
			mListViewEmptyView.setVisibility(View.VISIBLE);
		} else if (mListViewEmptyView.getVisibility() == View.VISIBLE) {
			mListViewEmptyView.setVisibility(View.GONE);
		}
	}

	private void showEmptyViewIfNeed() {
		if (mProductAdapter.isEmpty() == false
				&& mListViewEmptyView.getVisibility() == View.VISIBLE) {
			mListViewEmptyView.setVisibility(View.GONE);
		}
	}

	public void reset() {
		// TODO
		if (mProductAdapter != null) {
			mProductAdapter.removeDataSource();
		}
		if (mAsyncHttpClient != null) {
			mAsyncHttpClient.cancelRequests(mContext, true);
		}
	}

	public void setSearchView(SearchView searchView) {
		mSearchView = searchView;
	}
}
