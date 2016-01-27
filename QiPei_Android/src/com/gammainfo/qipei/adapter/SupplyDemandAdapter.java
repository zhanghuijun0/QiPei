package com.gammainfo.qipei.adapter;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gammainfo.qipei.CompanyInfoActivity;
import com.gammainfo.qipei.LoginActivity;
import com.gammainfo.qipei.R;
import com.gammainfo.qipei.db.Preferences;
import com.gammainfo.qipei.http.AsyncHttpResponseHandler;
import com.gammainfo.qipei.http.RequestParams;
import com.gammainfo.qipei.http.RestClient;
import com.gammainfo.qipei.model.SupplyDemand;
import com.gammainfo.qipei.utils.Constant;
import com.gammainfo.qipei.widget.AlertDialog;
import com.gammainfo.qipei.widget.AlertDialog.OnAlertDialogListener;
import com.loopj.android.http.JsonHttpResponseHandler;

public class SupplyDemandAdapter extends BaseAdapter {
	private final int mPageSize = Constant.PAGE_SIZE;

	private ArrayList<SupplyDemand> mSupplyDemandList;
	private int mPageNumber = 1;
	private Drawable favoriteTrueDrawableTrue;
	private Drawable favoriteTrueDrawableFalse;

	private Context mContext;
	private LayoutInflater mLayoutInflater;
	private AlertDialog mLoginAlertDialog;

	public SupplyDemandAdapter(ArrayList<SupplyDemand> userList, Context context) {
		mSupplyDemandList = userList;
		mContext = context;
		mLayoutInflater = LayoutInflater.from(mContext);
	}

	public void setDataSource(ArrayList<SupplyDemand> supplyDemandsList) {
		mSupplyDemandList = supplyDemandsList;
		notifyDataSetChanged();
	}

	public void appendDataSource(ArrayList<SupplyDemand> supplyDemandsList) {
		mSupplyDemandList.addAll(supplyDemandsList);
		notifyDataSetChanged();
	}

	public SupplyDemand removeOneDataSource(int i) {
		SupplyDemand item = mSupplyDemandList.get(i - 1);
		mSupplyDemandList.remove(i - 1);
		notifyDataSetChanged();
		return item;
	}

	public ArrayList<SupplyDemand> getDataSource() {
		return mSupplyDemandList;
	}

	public synchronized int incPageNumber() {
		return ++mPageNumber;
	}

	public synchronized void setPageNumber(int pageNumber) {
		mPageNumber = pageNumber;
	}

	public int getPageNumber() {
		return mPageNumber;
	}

	public int getPageSize() {
		return mPageSize;
	}

	@Override
	public int getCount() {
		return mSupplyDemandList.size();
	}

	@Override
	public Object getItem(int position) {
		return mSupplyDemandList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder viewHolder;
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = mLayoutInflater.inflate(
					R.layout.supply_demand_listitem, null);

			viewHolder.mTitle = (TextView) convertView
					.findViewById(R.id.supply_demand_listitem_title);
			viewHolder.mContent = (TextView) convertView
					.findViewById(R.id.supply_demand_listitem_content);
			viewHolder.mCompanyName = (TextView) convertView
					.findViewById(R.id.supply_demand_listitem_company_name);

			viewHolder.callPhonelayout = (LinearLayout) convertView
					.findViewById(R.id.supply_demand_list_listitem_button_phone);
			viewHolder.favoriteLayout = (LinearLayout) convertView
					.findViewById(R.id.supply_demand_list_listitem_favorite);
			viewHolder.favoriteButton = (Button) convertView
					.findViewById(R.id.supply_demand_list_listitem_favorite_bt);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		final SupplyDemand supplyDemand = mSupplyDemandList.get(position);

		viewHolder.mCompanyName.setText(TextUtils.isEmpty(supplyDemand
				.getCompanyName()) ? "----" : supplyDemand.getCompanyName());
		viewHolder.mContent.setText(supplyDemand.getContent());
		viewHolder.mTitle.setText(supplyDemand.getTitle());

		/* 设置是否被收藏 */
		favoriteTrueDrawableTrue = mContext.getResources().getDrawable(
				R.drawable.list_item_bt_left_favorite_true);
		favoriteTrueDrawableFalse = mContext.getResources().getDrawable(
				R.drawable.list_item_bt_left_favorite_false);
		Drawable nowDrawable = supplyDemand.getIsFavorite() ? favoriteTrueDrawableTrue
				: favoriteTrueDrawableFalse;
		setFavoriteDrawable(nowDrawable, viewHolder.favoriteButton);
		/* 给拨电话以及收藏按钮添加监听 */
		viewHolder.callPhonelayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (!Preferences.isLogin()) {
					if (mLoginAlertDialog == null) {
						mLoginAlertDialog = AlertDialog.build(mContext,
								R.string.common_label_unlogin,
								R.string.common_label_gotologin,
								R.string.common_label_cancel,
								new OnAlertDialogListener() {

									@Override
									public void onOk(AlertDialog alertDialogView) {
										mContext.startActivity(new Intent(
												mContext, LoginActivity.class));
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
				callToCompany(supplyDemand.getPhone());
			}

		});
		viewHolder.favoriteLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (!Preferences.isLogin()) {
					if (mLoginAlertDialog == null) {
						mLoginAlertDialog = AlertDialog.build(mContext,
								R.string.common_label_unlogin,
								R.string.common_label_gotologin,
								R.string.common_label_cancel,
								new OnAlertDialogListener() {

									@Override
									public void onOk(AlertDialog alertDialogView) {
										mContext.startActivity(new Intent(
												mContext, LoginActivity.class));
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
				if (supplyDemand.getIsFavorite()) {
					cancelFavorite(supplyDemand, v);
				} else {
					addFavorite(supplyDemand, v);
				}

			}
		});

		return convertView;
	}

	final private class ViewHolder {
		TextView mTitle;
		TextView mContent;
		TextView mCompanyName;

		Button favoriteButton;
		LinearLayout callPhonelayout;
		LinearLayout favoriteLayout;
	}

	/* 拨打所选公司的电话 */
	public void callToCompany(String phoneNum) {
		if (!phoneNum.equals("")) {
			Intent callIntent = new Intent();
			callIntent.setAction(Intent.ACTION_DIAL);
			callIntent.setData(Uri.parse(String.format("tel:%s", phoneNum)));
			mContext.startActivity(callIntent);
		} else {
			Toast.makeText(mContext, R.string.list_call_phone_not_find,
					Toast.LENGTH_SHORT).show();
		}

	}

	private void cancelFavorite(final SupplyDemand isFavoriteSupplyDemand,
			View v) {
		final LinearLayout favoriteLayout = (LinearLayout) v;
		final Button favoriteButton = (Button) favoriteLayout
				.findViewById(R.id.supply_demand_list_listitem_favorite_bt);
		favoriteLayout.setClickable(false);
		RequestParams params = new RequestParams();
		params.put("id", Preferences.getAccountUserId() + "");
		params.put("dest_user_id", isFavoriteSupplyDemand.getUserId() + "");
		params.put("dest_res_id", isFavoriteSupplyDemand.getId() + "");
		RestClient.post(Constant.API_LIST_FAVORITE_CANCEL, params,
				new AsyncHttpResponseHandler(mContext,
						new JsonHttpResponseHandler() {
							@Override
							public void onSuccess(JSONObject res) {
								try {
									if (res.getInt(Constant.JSON_KEY_CODE) == Constant.CODE_SUCCESS) {
										setFavoriteDrawable(
												favoriteTrueDrawableFalse,
												favoriteButton);
										isFavoriteSupplyDemand
												.setIsFavorite(false);
										Toast.makeText(
												mContext,
												R.string.list_cancle_favorite_success,
												Toast.LENGTH_SHORT).show();
									} else {
										Toast.makeText(
												mContext,
												R.string.list_cancle_favorite_fail,
												Toast.LENGTH_SHORT).show();
									}
								} catch (JSONException e) {
									e.printStackTrace();
									Toast.makeText(mContext,
											R.string.list_cancle_favorite_fail,
											Toast.LENGTH_SHORT).show();
								}
								favoriteLayout.setClickable(true);
							}

							@Override
							public void onFailure(Throwable e, JSONObject rlt) {
								super.onFailure(e, rlt);
								favoriteLayout.setClickable(true);
								Toast.makeText(mContext,
										R.string.list_cancle_favorite_fail,
										Toast.LENGTH_SHORT).show();
							}
						}));

	}

	private void addFavorite(final SupplyDemand isFavoriteSupplyDemand, View v) {
		final LinearLayout favoriteLayout = (LinearLayout) v;

		final Button favoriteButton = (Button) favoriteLayout
				.findViewById(R.id.supply_demand_list_listitem_favorite_bt);

		favoriteLayout.setClickable(false);
		RequestParams params = new RequestParams();
		params.put("id", Preferences.getAccountUserId() + "");
		params.put("dest_user_id", isFavoriteSupplyDemand.getUserId() + "");
		params.put("dest_res_id", isFavoriteSupplyDemand.getId() + "");
		params.put("type", (isFavoriteSupplyDemand.getType() + 3) + "");

		RestClient.post(Constant.API_LIST_FAVORITE_ADD, params,
				new AsyncHttpResponseHandler(mContext,
						new JsonHttpResponseHandler() {
							@Override
							public void onSuccess(JSONObject res) {
								try {
									if (res.getInt(Constant.JSON_KEY_CODE) == Constant.CODE_SUCCESS) {
										setFavoriteDrawable(
												favoriteTrueDrawableTrue,
												favoriteButton);
										isFavoriteSupplyDemand
												.setIsFavorite(true);
										Toast.makeText(
												mContext,
												R.string.list_is_favorite_success,
												Toast.LENGTH_SHORT).show();
									} else {
										Toast.makeText(mContext,
												R.string.list_is_favorite_fail,
												Toast.LENGTH_SHORT).show();
									}
								} catch (JSONException e) {
									e.printStackTrace();
									Toast.makeText(mContext,
											R.string.list_is_favorite_fail,
											Toast.LENGTH_SHORT).show();
								}
								favoriteLayout.setClickable(true);
							}

							@Override
							public void onFailure(Throwable e, JSONObject rlt) {
								super.onFailure(e, rlt);
								favoriteLayout.setClickable(true);
								Toast.makeText(mContext,
										R.string.list_is_favorite_fail,
										Toast.LENGTH_SHORT).show();
							}
						}));
	}

	public static void setFavoriteDrawable(Drawable nowDrawable,
			Button favoriteButton) {
		favoriteButton.setCompoundDrawablesWithIntrinsicBounds(nowDrawable,
				null, null, null);
	}

}
