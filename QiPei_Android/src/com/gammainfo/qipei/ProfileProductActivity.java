package com.gammainfo.qipei;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.gammainfo.qipei.adapter.ProductAdapter;
import com.gammainfo.qipei.convert.ProductJSONConvert;
import com.gammainfo.qipei.db.Preferences;
import com.gammainfo.qipei.http.AsyncHttpResponseHandler;
import com.gammainfo.qipei.http.RequestParams;
import com.gammainfo.qipei.http.RestClient;
import com.gammainfo.qipei.model.Product;
import com.gammainfo.qipei.model.User;
import com.gammainfo.qipei.utils.Constant;
import com.gammainfo.qipei.widget.ContextMenuDialog;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.loopj.android.http.JsonHttpResponseHandler;

public class ProfileProductActivity extends BaseActivity implements
		OnItemClickListener, PullToRefreshBase.OnRefreshListener2<ListView> {
	private static final int REQ_EDIT_PRODUCT = 1;
	private static final int REQ_ADD_PRODUCT = 2;
	private LinearLayout mProfileProductEmptyView;
	private ProductAdapter mProfileProductAdapter;
	private com.handmark.pulltorefresh.library.PullToRefreshListView mListView;
	private ArrayList<Product> mProductList;
	private int mCurrentPosition = 0;
	// private int mDelectProductId = 0;
	private ContextMenuDialog mContextMenuDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.profile_product_manage_list);
		mProfileProductEmptyView = (LinearLayout) findViewById(R.id.profile_product_emptyview);
		mProfileProductAdapter = new ProductAdapter(new ArrayList<Product>(),
				this);
		mListView = (com.handmark.pulltorefresh.library.PullToRefreshListView) findViewById(R.id.profile_product_manage_list);
		mListView.setOnItemClickListener(this);
		mListView.setOnRefreshListener(this);
		mListView.setAdapter(mProfileProductAdapter);
		mListView.setMode(Mode.BOTH);
		getInternetProductList();
		mListView.getRefreshableView().setOnItemLongClickListener(
				new OnItemLongClickListener() {
					@Override
					public boolean onItemLongClick(AdapterView<?> arg0,
							View arg1, int position, long id) {
						mCurrentPosition = position - 1;// 记住选择的item
						showContextMenu();
						return true;
					}
				});
	}

	/* 删除网上数据库的产品数据 */
	public void moveInternetProduct() {
		int productId = mProfileProductAdapter.getDataSource()
				.get(mCurrentPosition).getId();
		int id = Preferences.getAccountUserId();
		RequestParams params = new RequestParams();
		params.put("user_id", String.valueOf(id));
		params.put("id", String.valueOf(productId));
		RestClient.post(Constant.API_DELECT_PRODUCT_ITEM, params,
				new AsyncHttpResponseHandler(getBaseContext(),
						new JsonHttpResponseHandler() {

							@Override
							public void onSuccess(JSONObject rlt) {
								try {
									if (rlt.getInt(Constant.JSON_KEY_CODE) == Constant.CODE_SUCCESS) {
										Toast.makeText(
												getBaseContext(),
												getString(R.string.profile_toast_delete_success_text),
												Toast.LENGTH_SHORT).show();
										User user = Preferences
												.getAccountUser();
										user.setProductNum(user.getProductNum() - 1);
										Preferences.setAccountUser(user,
												Preferences.getToken());
										mProfileProductAdapter.getDataSource()
												.remove(mCurrentPosition);
										mProfileProductAdapter
												.notifyDataSetChanged();
										autoSetEmptyView();
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

	/* 获取网上数据库的产品列表信息数据 */
	public void getInternetProductList() {
		int id = Preferences.getAccountUserId();
		RequestParams params = new RequestParams();
		params.put("user_id", String.valueOf(id));
		params.put("page_index",
				String.valueOf(mProfileProductAdapter.getPageNumber()));
		params.put("page_size",
				String.valueOf(mProfileProductAdapter.getPageSize()));
		RestClient.post(Constant.API_GET_USER_PRODUCT_LIST, params,
				new AsyncHttpResponseHandler(getBaseContext(),
						new JsonHttpResponseHandler() {

							@Override
							public void onStart() {
								if (!mListView.isRefreshing()) {
									mListView.setRefreshing(true);
								}
							}

							@Override
							public void onSuccess(JSONObject rlt) {
								try {
									if (rlt.getInt(Constant.JSON_KEY_CODE) == Constant.CODE_SUCCESS) {
										JSONObject info = rlt
												.getJSONObject("info");
										JSONArray list = info
												.getJSONArray("list");
										if (list.length() == 0
												&& mProfileProductAdapter
														.getPageNumber() != 1) {
											Toast.makeText(
													getBaseContext(),
													getString(R.string.common_label_last_page),
													Toast.LENGTH_SHORT).show();
										}
										mProductList = ProductJSONConvert
												.convertJsonArrayToItemList(list);
										mProfileProductAdapter
												.appendDataSource(mProductList);
										autoSetEmptyView();
										mProfileProductAdapter
												.setPageNumber(mProfileProductAdapter
														.getPageNumber() + 1);
									} else {
										Toast.makeText(
												getBaseContext(),
												rlt.getString(Constant.JSON_MESSAGE),
												Toast.LENGTH_SHORT).show();
									}
								} catch (JSONException e) {
									e.printStackTrace();
								}
							}

							@Override
							public void onFinish() {
								mListView.onRefreshComplete();
								super.onFinish();
							}
						}));
	}

	public void autoSetEmptyView() {
		if (mProfileProductAdapter.getCount() == 0) {
			mProfileProductEmptyView.setVisibility(View.VISIBLE);
		} else {
			mProfileProductEmptyView.setVisibility(View.GONE);
		}
	}

	/* 返回按钮 */
	public void onProductManageListBackClick(View v) {
		Intent data = new Intent();
		setResult(Activity.RESULT_OK, data);
		finish();
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			long id) {
		ArrayList<Product> mGetProductList = mProfileProductAdapter
				.getDataSource();
		Product listItem = mGetProductList.get(position - 1);
		Intent intent = new Intent(ProfileProductActivity.this,
				ProductInfoActivity.class);
		Bundle date = new Bundle();
		date.putInt("selectProductId", listItem.getId());
		intent.putExtra("selectProduct", listItem);
		intent.putExtras(date);
		startActivity(intent);
	}

	@Override
	public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
		mProfileProductAdapter.removeDataSource();
		mProfileProductAdapter.setPageNumber(1);
		getInternetProductList();
	}

	@Override
	public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
		getInternetProductList();
	}

	public void onAddProductClick(View v) {
		startActivityForResult(new Intent(this, ProductPublishActivity.class),
				REQ_ADD_PRODUCT);
	}

	private void showContextMenu() {
		if (mContextMenuDialog == null) {
			ArrayList<String> datasource = new ArrayList<String>();
			datasource.add("编辑");
			datasource.add("删除");
			mContextMenuDialog = ContextMenuDialog.build(this,
					new ContextMenuDialog.OnContextMenuListener() {

						@Override
						public void onItemClick(ContextMenuDialog target,
								int index) {
							target.dismiss();
							if (index == 0) {
								// 编辑
								Intent intent = new Intent(
										ProfileProductActivity.this,
										ProductPublishActivity.class);
								intent.putExtra(
										ProductPublishActivity.EXTRA_PRODUCT,
										mProfileProductAdapter.getDataSource()
												.get(mCurrentPosition));
								startActivityForResult(intent, REQ_EDIT_PRODUCT);
							} else {
								// 删除
								moveInternetProduct();
							}
						}

						@Override
						public void onCancelClick(ContextMenuDialog target) {
							target.dismiss();
						}
					}, datasource);
		}
		mContextMenuDialog.show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			if (requestCode == REQ_EDIT_PRODUCT) {
				// TODO update listview
				Product newProduct = data.getParcelableExtra("product");
				if (newProduct != null) {
					ArrayList<Product> productList = mProfileProductAdapter
							.getDataSource();
					productList.remove(mCurrentPosition);
					productList.add(mCurrentPosition, newProduct);
					mProfileProductAdapter.notifyDataSetChanged();
				}
			} else if (requestCode == REQ_ADD_PRODUCT) {
				Product newProduct = data.getParcelableExtra("product");
				if (newProduct != null) {
					ArrayList<Product> productList = mProfileProductAdapter
							.getDataSource();
					productList.add(0, newProduct);
					mProfileProductAdapter.notifyDataSetChanged();
					mListView.getRefreshableView().setSelection(0);
					autoSetEmptyView();
				}
			}
		}
	}

}
