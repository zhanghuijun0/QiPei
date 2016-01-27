package com.gammainfo.qipei.adapter;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gammainfo.qipei.NewsContentActivity;
import com.gammainfo.qipei.R;
import com.gammainfo.qipei.model.News;
import com.gammainfo.qipei.utils.Constant;
import com.loopj.android.image.SmartImageView;

public class NewsCompanyBannerAdapter extends PagerAdapter implements
		View.OnClickListener {

	private Activity mContext;
	private LayoutInflater mLayoutInflater;
	private ArrayList<News> mNewsBannerList;

	public NewsCompanyBannerAdapter(Activity context, ArrayList<News> bannerList) {
		mContext = context;
		mNewsBannerList = bannerList;
		mLayoutInflater = LayoutInflater.from(context);
	}

	public void removeDataSource() {
		mNewsBannerList = new ArrayList<News>();
		notifyDataSetChanged();
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == arg1;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		((ViewPager) container).removeView((View) object);
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		View view = mLayoutInflater.inflate(
				R.layout.news_listitem_company_banner, null);
		TextView BannerTitle = (TextView) view
				.findViewById(R.id.tv_news_company_banner_title);
		SmartImageView BannerImage = (SmartImageView) view
				.findViewById(R.id.siv_news_company_banner_image);
		News mNews = mNewsBannerList.get(position);
		BannerTitle.setText(TextUtils.isEmpty(mNews.getTitle()) ? "" : mNews
				.getTitle());
		// 为ImageView加上默认图片
		BannerImage.setImageUrl(mNews.getImageUrl(),
				R.drawable.ic_user_product_default);
		view.setClickable(true);
		view.setOnClickListener(this);
		view.setId(position);
		container.addView(view);
		return view;
	}

	@Override
	public int getCount() {
		return mNewsBannerList.size();
	}

	@Override
	public void onClick(View v) {
		int position = v.getId();
		News newsBanner = mNewsBannerList.get(position);
		Bundle bundle = new Bundle();
		bundle.putInt(NewsContentActivity.EXTRA_TYPE,
				NewsContentActivity.TYPE_COMPANY);
		bundle.putString(NewsContentActivity.EXTRA_URL, newsBanner.getPageUrl());
		Intent companyActivityIntent = new Intent(mContext,
				NewsContentActivity.class);
		companyActivityIntent.putExtras(bundle);
		mContext.startActivity(companyActivityIntent);
	}

	public void setDataSource(ArrayList<News> bannerList) {
		mNewsBannerList = bannerList;
		notifyDataSetChanged();
	}
}