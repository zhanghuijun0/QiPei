package com.gammainfo.qipei.adapter;

import java.util.ArrayList;

import android.app.Activity;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.gammainfo.qipei.R;
import com.gammainfo.qipei.model.News;
import com.gammainfo.qipei.utils.Constant;
import com.loopj.android.image.SmartImageView;

/**
 * 活动列表适配器
 * 
 * @author ZhangHuijun
 * 
 */
public class NewsActivityAdapter extends BaseAdapter {
	private ArrayList<News> mNewsList;
	private int mPageNumber = 1;
	private int mPageSize = Constant.PAGE_SIZE;
	private Activity mContext;

	public NewsActivityAdapter(Activity context, ArrayList<News> newsList) {
		mNewsList = newsList;
		mContext = context;
	}

	public ArrayList<News> getDataSource() {
		return mNewsList;
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
	
	public void setDataSource(ArrayList<News> newsList) {
		mNewsList = newsList;
		notifyDataSetChanged();
	}

	public void removeDataSource() {
		mNewsList = new ArrayList<News>();
		notifyDataSetChanged();
	}

	public void appendDataSource(ArrayList<News> newsList) {
		mNewsList.addAll(newsList);
		notifyDataSetChanged();
	}

	public int getCount() {
		return mNewsList.size();
	}

	public Object getItem(int position) {
		return mNewsList.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = mContext.getLayoutInflater().inflate(
					R.layout.news_listitem_activity, null);
			viewHolder.mTitleTv = (TextView) convertView
					.findViewById(R.id.tv_news_activity_listitem_title);
			viewHolder.mImageSiv = (SmartImageView) convertView
					.findViewById(R.id.siv_news_activity_listitem_image);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		News news = mNewsList.get(position);
		// 为ImageView加上默认图片
		viewHolder.mImageSiv.setImageUrl(news.getImageUrl(),
				R.drawable.ic_user_product_default);
		viewHolder.mTitleTv.setText(TextUtils.isEmpty(news.getTitle()) ? " "
				: news.getTitle());
		return convertView;
	}

	final private class ViewHolder {
		TextView mTitleTv;
		SmartImageView mImageSiv;
	}

}