package com.gammainfo.qipei.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Image implements Parcelable {
	private int mId;
	private int mProductId;//图片所对应产品的id
	private String mUrl;
	private int mCreateTime;
	private String mDescribed;
	
	
	public int getId() {
		return mId;
	}
	public void setId(int mId) {
		this.mId = mId;
	}
	public int getProductId() {
		return mProductId;
	}
	public void setProductId(int mProductId) {
		this.mProductId = mProductId;
	}
	public String getUrl() {
		return mUrl;
	}
	public void setUrl(String mUrl) {
		this.mUrl = mUrl;
	}
	public int getCreateTime() {
		return mCreateTime;
	}
	public void setCreateTime(int mCreateTime) {
		this.mCreateTime = mCreateTime;
	}
	public String getDescribed() {
		return mDescribed;
	}
	public void setDescribed(String mDescribed) {
		this.mDescribed = mDescribed;
	}
	
	public static Parcelable.Creator<Image> getCreator() {
		return CREATOR;
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
		dest.writeInt(mProductId);
		dest.writeString(mUrl);
		dest.writeInt(mCreateTime);
		dest.writeString(mDescribed);
	}
	
	public static final Parcelable.Creator<Image> CREATOR = new Parcelable.Creator<Image>() {
		public Image createFromParcel(Parcel in) {
			Image img = new Image();
			img.setId(in.readInt());
			img.setProductId(in.readInt());
			img.setUrl(in.readString());
			img.setCreateTime(in.readInt());
			img.setDescribed(in.readString());
			return img;
		}

		public Image[] newArray(int size) {
			return new Image[size];
		}
	};

	  
}
