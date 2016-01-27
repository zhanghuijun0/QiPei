package com.gammainfo.qipei.model;

import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;

public class SupplyDemand implements Parcelable {
	// 与数据库值对应，不要修改
	public static final int TYPE_SUPLLY = 1;
	public static final int TYPE_DEMAMD = 2;
	private int mId;
	private int mUserId;
	private String mTitle;
	private String mContent;
	private int mType;
	private String mArea;
	private boolean mIsFavorite;
	private long mUpdateTime;
	private String mCompanyName;
	private String mPhone;
	private ArrayList<Image> mImgs;
	private String mThumb;

	public int getId() {
		return mId;
	}

	public void setId(int mId) {
		this.mId = mId;
	}

	public int getUserId() {
		return mUserId;
	}

	public void setUserId(int mUserId) {
		this.mUserId = mUserId;
	}

	public String getTitle() {
		return mTitle;
	}

	public void setTitle(String mTitle) {
		this.mTitle = mTitle;
	}

	public String getContent() {
		return mContent;
	}

	public void setContent(String mContent) {
		this.mContent = mContent;
	}

	public int getType() {
		return mType;
	}

	public void setType(int mType) {
		this.mType = mType;
	}

	public String getArea() {
		return mArea;
	}

	public void setArea(String mArea) {
		this.mArea = mArea;
	}

	public long getUpdateTime() {
		return mUpdateTime;
	}

	public boolean getIsFavorite() {
		return mIsFavorite;
	}

	public void setIsFavorite(boolean mIsFavorite) {
		this.mIsFavorite = mIsFavorite;
	}

	public void setUpdateTime(long mUpdateTime) {
		this.mUpdateTime = mUpdateTime;
	}

	public String getCompanyName() {
		return mCompanyName;
	}

	public void setCompanyName(String mCompanyName) {
		this.mCompanyName = mCompanyName;
	}

	public String getPhone() {
		return mPhone;
	}

	public void setPhone(String mPhone) {
		this.mPhone = mPhone;
	}

	public ArrayList<Image> getImgs() {
		return mImgs;
	}

	public void setImgs(ArrayList<Image> imgs) {
		this.mImgs = imgs;
	}

	public String getThumb() {
		return mThumb;
	}

	public void setThumb(String thumb) {
		this.mThumb = thumb;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(mId);
		dest.writeInt(mUserId);
		dest.writeString(mTitle);
		dest.writeString(mContent);
		dest.writeInt(mType);
		dest.writeString(mArea);
		dest.writeString(mPhone);
		dest.writeString(mCompanyName);
		dest.writeLong(mUpdateTime);
		dest.writeInt(mIsFavorite ? 1 : 0);
		dest.writeString(mThumb);
		dest.writeList(mImgs);
	}

	public static final Parcelable.Creator<SupplyDemand> CREATOR = new Parcelable.Creator<SupplyDemand>() {
		public SupplyDemand createFromParcel(Parcel in) {
			SupplyDemand supplyDemand = new SupplyDemand();
			supplyDemand.setId(in.readInt());
			supplyDemand.setUserId(in.readInt());
			supplyDemand.setTitle(in.readString());
			supplyDemand.setContent(in.readString());
			supplyDemand.setType(in.readInt());
			supplyDemand.setArea(in.readString());
			supplyDemand.setPhone(in.readString());
			supplyDemand.setCompanyName(in.readString());
			supplyDemand.setUpdateTime(in.readLong());
			supplyDemand.setIsFavorite(in.readInt() == 0 ? false : true);
			supplyDemand.setThumb(in.readString());
			ArrayList<Image> imgs = new ArrayList<Image>();
			in.readList(imgs, Image.class.getClassLoader());
			supplyDemand.setImgs(imgs);
			return supplyDemand;
		}

		public SupplyDemand[] newArray(int size) {
			return new SupplyDemand[size];
		}
	};

	public boolean equals(Object other) {
		if (this == other)
			return true;
		if (other == null)
			return false;
		SupplyDemand supplyDemand = (SupplyDemand) other;
		if (this.getIsFavorite() != supplyDemand.getIsFavorite())
			return false;
		if (this.getId() != supplyDemand.getId())
			return false;
		if (this.getType() != supplyDemand.getType())
			return false;

		if (!this.getArea().equals(supplyDemand.getArea()))
			return false;
		if (!this.getCompanyName().equals(supplyDemand.getCompanyName()))
			return false;
		if (!this.getContent().equals(supplyDemand.getContent()))
			return false;
		if (!this.getPhone().equals(supplyDemand.getPhone()))
			return false;
		if (!this.getTitle().equals(supplyDemand.getTitle()))
			return false;
		return true;
	}

}
