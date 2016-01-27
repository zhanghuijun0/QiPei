package com.gammainfo.qipei.adapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

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
public class NewsMediaAdapter extends BaseAdapter {
	private ArrayList<News> mNewsList;
	private int mPageNumber = 1;
	private int mPageSize = Constant.PAGE_SIZE;
	private Activity mContext;

	public NewsMediaAdapter(Activity context, ArrayList<News> newsList) {
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

	@Override
	public int getCount() {
		return mNewsList.size();
	}

	@Override
	public Object getItem(int position) {
		return mNewsList.get(position);
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
			convertView = mContext.getLayoutInflater().inflate(
					R.layout.news_listitem_media, null);
			viewHolder.mTitleTv = (TextView) convertView
					.findViewById(R.id.tv_news_media_listitem_title);
			viewHolder.mBriefTv = (TextView) convertView
					.findViewById(R.id.tv_news_media_listitem_brief);
			viewHolder.mDateTv = (TextView) convertView
					.findViewById(R.id.tv_news_media_listitem_date);
			viewHolder.mTimeTv = (TextView) convertView
					.findViewById(R.id.tv_news_media_listitem_mediatime);
			viewHolder.mImageSiv = (SmartImageView) convertView
					.findViewById(R.id.siv_news_media_listitem_image);
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
		viewHolder.mBriefTv.setText(TextUtils.isEmpty(news.getInfro()) ? " "
				: news.getInfro());
		// TODO 新闻时间
		String date = new SimpleDateFormat("yyyy.MM.dd").format(new Date(news
				.getCreateTime() * 1000L));
		viewHolder.mDateTv.setText(date);
		// TODO 得到视频时间长度
		viewHolder.mTimeTv.setText(changeTime(news.getMediaDuration()));
		return convertView;
	}

	// 把秒转化为时间字符串
	private CharSequence changeTime(int mediaDuration) {
		int hour = 0;
		int minute = 0;
		int seconds = 0;
		hour = mediaDuration / 3600;
		minute = ((mediaDuration % 3600) / 60);
		seconds = ((mediaDuration % 3600) % 60);
		String time = " " + hour + ":" + changeType(minute) + ":"
				+ changeType(seconds);
		return time;
	}

	private String changeType(int asd) {
		String masd;
		if (asd < 10) {
			masd = "0" + asd;
		} else {
			masd = "" + asd;
		}
		return masd;
	}

	final private class ViewHolder {
		SmartImageView mImageSiv;
		TextView mTitleTv;
		TextView mBriefTv;
		TextView mDateTv;
		TextView mTimeTv;
	}

}