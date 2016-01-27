package com.gammainfo.qipei.model;

import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;

public class Product implements Parcelable {
	private int mId;
	private String mCompanyName;
	private String mProductName;
	private String mProductInfo;
	private String mBrief;
	private String mBrand;// 品牌
	private int mPv;
	private int mFavoriteNum;
	private int mHotNum;
	private String mProperty;
	private String mThumb;
	private ArrayList<Image> mImgs;

	public int getId() {
		return mId;
	}

	public void setId(int mId) {
		this.mId = mId;
	}

	public void setCompanyName(String mCompanyName) {
		this.mCompanyName = mCompanyName;
	}

	public String getCompanyName() {
		return mCompanyName;
	}

	public String getProductName() {
		return mProductName;
	}

	public void setProductName(String mProductName) {
		this.mProductName = mProductName;
	}

	public String getProductInfo() {
		return mProductInfo;
	}

	public void setProductInfo(String mProductInfo) {
		this.mProductInfo = mProductInfo;
	}

	public String getBrand() {
		return mBrand;
	}

	public void setBrand(String mBrand) {
		this.mBrand = mBrand;
	}

	public int getPv() {
		return mPv;
	}

	public void setPv(int mPv) {
		this.mPv = mPv;
	}

	public int getFavoriteNum() {
		return mFavoriteNum;
	}

	public void setFavoriteNum(int mFavoriteNum) {
		this.mFavoriteNum = mFavoriteNum;
	}

	public int getHotNum() {
		return mHotNum;
	}

	public void setHotNum(int mHotNum) {
		this.mHotNum = mHotNum;
	}

	public String getProperty() {
		return mProperty;
	}

	public void setProperty(String mProperty) {
		this.mProperty = mProperty;
	}

	public ArrayList<Image> getImgs() {
		return mImgs;
	}

	public void setImgs(ArrayList<Image> mImgs) {
		this.mImgs = mImgs;
	}

	public String getBrief() {
		return mBrief;
	}

	public void setBrief(String mBrief) {
		this.mBrief = mBrief;
	}

	public static Parcelable.Creator<Product> getCreator() {
		return CREATOR;
	}

	public String getThumb() {
		return mThumb;
	}

	public void setThumb(String mThumb) {
		this.mThumb = mThumb;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeInt(mId);
		dest.writeString(mCompanyName);
		dest.writeString(mProductName);
		dest.writeString(mProductInfo);
		dest.writeString(mBrand);
		dest.writeInt(mPv);
		dest.writeInt(mFavoriteNum);
		dest.writeInt(mHotNum);
		dest.writeString(mProperty);
		dest.writeString(mBrief);
		dest.writeString(mThumb);
		dest.writeInt(mImgs.size());
		for (Image image : mImgs) {
			dest.writeParcelable(image, flags);
		}
	}

	public static final Parcelable.Creator<Product> CREATOR = new Parcelable.Creator<Product>() {
		public Product createFromParcel(Parcel in) {
			Product product = new Product();
			product.setId(in.readInt());
			product.setCompanyName(in.readString());
			product.setProductName(in.readString());
			product.setProductInfo(in.readString());
			product.setBrand(in.readString());
			product.setPv(in.readInt());
			product.setFavoriteNum(in.readInt());
			product.setHotNum(in.readInt());
			product.setProperty(in.readString());
			product.setBrief(in.readString());
			product.setThumb(in.readString());
			int imgSize = in.readInt();
			ArrayList<Image> imgs = new ArrayList<Image>();
			for (int i = 0; i < imgSize; i++) {
				imgs.add((Image) in.readParcelable(Image.class.getClassLoader()));
			}
			product.setImgs(imgs);
			return product;
		}

		public Product[] newArray(int size) {
			return new Product[size];
		}
	};
}
