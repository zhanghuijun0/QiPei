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

import com.gammainfo.qipei.LoginActivity;
import com.gammainfo.qipei.R;
import com.gammainfo.qipei.db.Preferences;
import com.gammainfo.qipei.http.AsyncHttpResponseHandler;
import com.gammainfo.qipei.http.RequestParams;
import com.gammainfo.qipei.http.RestClient;
import com.gammainfo.qipei.model.User;
import com.gammainfo.qipei.utils.Constant;
import com.gammainfo.qipei.widget.AlertDialog;
import com.gammainfo.qipei.widget.AlertDialog.OnAlertDialogListener;
import com.gammainfo.qipei.widget.SearchView;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.image.SmartImageView;

public class UserAdapter extends BaseAdapter {
	private ArrayList<User> mUserList;
	private int mPageNumber = 1;
	private int mPageSize = Constant.PAGE_SIZE;

	private Drawable favoriteTrueDrawableTrue;
	private Drawable favoriteTrueDrawableFalse;

	private Context mContext;
	private LayoutInflater mLayoutInflater;
	private boolean mEnableDistance;
	private AlertDialog mLoginAlertDialog;

	public UserAdapter(ArrayList<User> userList, Context context) {
		mUserList = userList;
		mContext = context;
		mLayoutInflater = LayoutInflater.from(mContext);
		favoriteTrueDrawableTrue = mContext.getResources().getDrawable(
				R.drawable.list_item_bt_left_favorite_true);
		favoriteTrueDrawableFalse = mContext.getResources().getDrawable(
				R.drawable.list_item_bt_left_favorite_false);
		mEnableDistance = false;
	}

	public void setDataSource(ArrayList<User> userList) {
		mUserList = userList;
		notifyDataSetChanged();
	}

	public void removeDataSource() {
		mPageNumber = 1;
		mUserList = new ArrayList<User>();
		notifyDataSetChanged();
	}

	public void appendDataSource(ArrayList<User> userList) {
		mUserList.addAll(userList);
		notifyDataSetChanged();
	}

	public ArrayList<User> getDataSource() {
		return mUserList;
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
		return mUserList.size();
	}

	@Override
	public Object getItem(int position) {
		return mUserList.get(position);
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
			convertView = mLayoutInflater.inflate(R.layout.list_listitem_user,
					null);

			viewHolder.mTitleTv = (TextView) convertView
					.findViewById(R.id.tv_list_listitem_title);
			viewHolder.mBriefTv = (TextView) convertView
					.findViewById(R.id.tv_list_listitem_brief);
			viewHolder.mStatusTv = (TextView) convertView
					.findViewById(R.id.tv_list_listitem_status);
			viewHolder.mImageSiv = (SmartImageView) convertView
					.findViewById(R.id.siv_list_listitem_image);
			viewHolder.callPhonelayout = (LinearLayout) convertView
					.findViewById(R.id.siv_list_listitem_button_phone);
			viewHolder.favoriteLayout = (LinearLayout) convertView
					.findViewById(R.id.siv_list_listitem_button_favorite);
			viewHolder.favoriteButton = (Button) convertView
					.findViewById(R.id.list_use_favorite_bt);
			viewHolder.mDistanceTv = (TextView) convertView
					.findViewById(R.id.tv_list_listitem_distance);
			viewHolder.mUserTypeTv = (TextView) convertView
					.findViewById(R.id.tv_list_listitem_user_type);
			viewHolder.mSplitView = convertView
					.findViewById(R.id.tv_list_listitem_split);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		User user = mUserList.get(position);
		viewHolder.mImageSiv.setImageUrl(user.getPhotoUrl(),
				R.drawable.ic_user_product_default);
		viewHolder.mTitleTv
				.setText(TextUtils.isEmpty(user.getCompanyName()) ? "----"
						: user.getCompanyName());

		switch (user.getRoleId()) {
		case SearchView.SEARCH_TYPE_AGENCY:
			viewHolder.mUserTypeTv.setText(R.string.search_label_agency);
			break;
		case SearchView.SEARCH_TYPE_MANUFACTURER:
			viewHolder.mUserTypeTv.setText(R.string.search_label_manufacturer);
			break;
		case SearchView.SEARCH_TYPE_ENDUSER_4SDIAN:
			viewHolder.mUserTypeTv
					.setText(R.string.search_label_enduser_4sdian);
			break;
		case SearchView.SEARCH_TYPE_ENDUSER_KUAIXINDIAN:
			viewHolder.mUserTypeTv
					.setText(R.string.search_label_enduser_kuaixiudian);
			break;
		case SearchView.SEARCH_TYPE_ENDUSER_MEIRONGDIAN:
			viewHolder.mUserTypeTv
					.setText(R.string.search_label_enduser_meirongdian);
			break;
		case SearchView.SEARCH_TYPE_ENDUSER_QIXIUCHANG:
			viewHolder.mUserTypeTv
					.setText(R.string.search_label_enduser_qixiuchang);
			break;
		default:
			viewHolder.mUserTypeTv.setText(R.string.search_label_enduser);
			break;
		}

		viewHolder.mBriefTv.setText(user.getIntro());

		if (user.isCertificated()) {
			viewHolder.mStatusTv.setText(mContext
					.getText(R.string.common_hint_user_certificated));
			viewHolder.mSplitView.setVisibility(View.VISIBLE);
		} else {
			viewHolder.mStatusTv.setText("");
			viewHolder.mSplitView.setVisibility(View.INVISIBLE);
		}

		if (mEnableDistance) {
			viewHolder.mDistanceTv.setVisibility(View.VISIBLE);
			int distance = (int) user.getDistance();
			if (distance == 0) {
				viewHolder.mDistanceTv.setText("----");
			} else if (distance < 1000) {
				viewHolder.mDistanceTv.setText(distance + "m");
			} else {
				viewHolder.mDistanceTv.setText(distance / 1000 + "km");
			}
		} else {
			viewHolder.mDistanceTv.setVisibility(View.GONE);
		}

		/* 设置是否被收藏 */

		Drawable nowDrawable = user.isIsFavorite() ? favoriteTrueDrawableTrue
				: favoriteTrueDrawableFalse;
		setFavoriteDrawable(nowDrawable, viewHolder.favoriteButton);
		/* 给拨电话以及收藏按钮添加监听 */
		viewHolder.callPhonelayout.setOnClickListener(mOnClickListener);
		viewHolder.favoriteLayout.setOnClickListener(mOnClickListener);
		viewHolder.callPhonelayout.setTag(String.valueOf(position));
		viewHolder.favoriteLayout.setTag(String.valueOf(position));
		return convertView;
	}

	final private class ViewHolder {
		TextView mTitleTv;
		TextView mBriefTv;
		TextView mStatusTv;
		SmartImageView mImageSiv;
		Button favoriteButton;
		LinearLayout callPhonelayout;
		LinearLayout favoriteLayout;
		TextView mDistanceTv;
		View mSplitView;
		TextView mUserTypeTv;
	}

	OnClickListener mOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {

			int position = Integer.valueOf(v.getTag().toString());
			User user = mUserList.get(position);
			if (!Preferences.isLogin()) {
				if (mLoginAlertDialog == null) {
					mLoginAlertDialog = AlertDialog.build(mContext,
							R.string.common_label_unlogin,
							R.string.common_label_gotologin,
							R.string.common_label_cancel,
							new OnAlertDialogListener() {

								@Override
								public void onOk(AlertDialog alertDialogView) {
									mContext.startActivity(new Intent(mContext,
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
			switch (v.getId()) {
			case R.id.siv_list_listitem_button_phone:
				callToCompany(user.getPhone());
				break;
			case R.id.siv_list_listitem_button_favorite:
				if (user.isIsFavorite()) {
					cancelFavorite(user, v);
				} else {
					addFavorite(user, v);
				}
				break;
			}

		}

	};

	/* 拨打所选公司的电话 */
	public void callToCompany(String phoneNum) {
		if (!TextUtils.isEmpty(phoneNum)) {
			Intent callIntent = new Intent();
			callIntent.setAction(Intent.ACTION_DIAL);
			callIntent.setData(Uri.parse(String.format("tel:%s", phoneNum)));
			mContext.startActivity(callIntent);
		} else {
			Toast.makeText(mContext, R.string.list_call_phone_fail,
					Toast.LENGTH_SHORT).show();
		}

	}

	private void cancelFavorite(final User isFavoriteUser, View v) {
		final LinearLayout favoriteLayout = (LinearLayout) v;
		final Button favoriteButton = (Button) favoriteLayout
				.findViewById(R.id.list_use_favorite_bt);
		favoriteLayout.setClickable(false);
		RequestParams params = new RequestParams();
		params.put("id", Preferences.getAccountUserId() + "");
		params.put("dest_user_id", isFavoriteUser.getId() + "");
		params.put("dest_res_id", 0 + "");
		RestClient.post(Constant.API_LIST_FAVORITE_CANCEL, params,
				new AsyncHttpResponseHandler(mContext,
						new JsonHttpResponseHandler() {
							@Override
							public void onSuccess(JSONObject res) {
								// TODO Auto-generated method stub
								try {
									if (res.getInt(Constant.JSON_KEY_CODE) == Constant.CODE_SUCCESS) {
										setFavoriteDrawable(
												favoriteTrueDrawableFalse,
												favoriteButton);
										isFavoriteUser.setIsFavorite(false);
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
									// TODO Auto-generated catch block
									e.printStackTrace();
									Toast.makeText(mContext,
											R.string.list_cancle_favorite_fail,
											Toast.LENGTH_SHORT).show();
								}
								favoriteLayout.setClickable(true);
							}

							@Override
							public void onFailure(Throwable e, JSONObject rlt) {
								// TODO Auto-generated method stub
								super.onFailure(e, rlt);
								favoriteLayout.setClickable(true);
								Toast.makeText(mContext,
										R.string.list_cancle_favorite_fail,
										Toast.LENGTH_SHORT).show();
							}
						}));

	}

	private void addFavorite(final User isFavoriteUser, View v) {
		final LinearLayout favoriteLayout = (LinearLayout) v;
		final Button favoriteButton = (Button) favoriteLayout
				.findViewById(R.id.list_use_favorite_bt);
		favoriteLayout.setClickable(false);
		RequestParams params = new RequestParams();
		params.put("id", Preferences.getAccountUserId() + "");
		params.put("dest_user_id", isFavoriteUser.getId() + "");
		params.put("dest_res_id", 0 + "");
		params.put("type", isFavoriteUser.getRoleId() + "");
		RestClient.post(Constant.API_LIST_FAVORITE_ADD, params,
				new AsyncHttpResponseHandler(mContext,
						new JsonHttpResponseHandler() {
							@Override
							public void onSuccess(JSONObject res) {
								// TODO Auto-generated method stub
								try {
									if (res.getInt(Constant.JSON_KEY_CODE) == Constant.CODE_SUCCESS) {
										setFavoriteDrawable(
												favoriteTrueDrawableTrue,
												favoriteButton);
										isFavoriteUser.setIsFavorite(true);
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
									// TODO Auto-generated catch block
									e.printStackTrace();
									Toast.makeText(mContext,
											R.string.list_is_favorite_fail,
											Toast.LENGTH_SHORT).show();
								}
								favoriteLayout.setClickable(true);
							}

							@Override
							public void onFailure(Throwable e, JSONObject rlt) {
								// TODO Auto-generated method stub
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

	public void setDistanceStatus(boolean enable) {
		mEnableDistance = enable;
	}
}
