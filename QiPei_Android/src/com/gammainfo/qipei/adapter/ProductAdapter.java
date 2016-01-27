package com.gammainfo.qipei.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.gammainfo.qipei.R;
import com.gammainfo.qipei.model.Product;
import com.gammainfo.qipei.utils.Constant;
import com.loopj.android.image.SmartImageView;

public class ProductAdapter extends BaseAdapter {
	private ArrayList<Product> mProductList;
	private int mPageNumber = 1;
	private final int mPageSize = Constant.PAGE_SIZE;
	private Context mContext;
	private LayoutInflater mLayoutInflater;

	public ProductAdapter(ArrayList<Product> productList, Context context) {
		mProductList = productList;
		mContext = context;
		mLayoutInflater = LayoutInflater.from(context);
	}

	public void setDataSource(ArrayList<Product> productList) {
		mProductList = productList;
		notifyDataSetChanged();
	}

	public void removeDataSource() {
		mPageNumber = 1;
		mProductList = new ArrayList<Product>();
		notifyDataSetChanged();
	}

	public void appendDataSource(ArrayList<Product> productList) {
		mProductList.addAll(productList);
		notifyDataSetChanged();
	}

	public ArrayList<Product> getDataSource() {
		return mProductList;
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
		return mProductList.size();
	}

	@Override
	public Object getItem(int position) {
		return mProductList.get(position);
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
					R.layout.company_listitem_product, null);
			viewHolder.mTitle = (TextView) convertView
					.findViewById(R.id.companyinfo_product_listitem_name);
			viewHolder.mBrand = (TextView) convertView
					.findViewById(R.id.companyinfo_product_listitem_brand);
			viewHolder.mIntro = (TextView) convertView
					.findViewById(R.id.companyinfo_product_listitem_intro);
			viewHolder.mImageSiv = (SmartImageView) convertView
					.findViewById(R.id.companyinfo_product_listitem_image);

			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		Product Product = mProductList.get(position);

		viewHolder.mImageSiv.setImageUrl(Product.getThumb(),
				R.drawable.ic_user_product_default);

		viewHolder.mTitle
				.setText(TextUtils.isEmpty(Product.getProductName()) ? "----"
						: Product.getProductName());
		viewHolder.mBrand.setText(Product.getBrand());
		viewHolder.mIntro.setText(Product.getBrief());

		return convertView;
	}

	final private class ViewHolder {
		SmartImageView mImageSiv;
		TextView mTitle;
		TextView mBrand;
		TextView mIntro;

	}

}
