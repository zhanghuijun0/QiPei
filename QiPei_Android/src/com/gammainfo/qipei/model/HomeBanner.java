package com.gammainfo.qipei.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.gammainfo.qipei.utils.UrlAssert;

/**
 * Banner
 * 
 * @author ligl
 * 
 */
public class HomeBanner implements Parcelable {
	public static final int TYPE_USER = 1;// 商家
	public static final int TYPE_PRODUCT = 2;// 产品
	public static final int TYPE_URL = 3;// 普通页面

	private int mId;
	private String mTitle;
	private String mImgUrl;
	private String mClickUrl;
	private int mTypeId;
	private int mType;
	// 到期时间（如果是0，则永不过期）
	private long mExpires;

	public HomeBanner(int id, String title, int type, int typeId,
			String imgUrl, String clickUrl, long expires) {
		mId = id;
		mType = type;
		mTypeId = typeId;
		mTitle = title;
		mClickUrl = clickUrl;
		mImgUrl = imgUrl;
		mExpires = expires;
	}

	public int getId() {
		return mId;
	}

	public String getTitle() {
		return mTitle;
	}

	public String getImgUrl() {
		return mImgUrl;
	}

	public String getClickUrl() {
		return mClickUrl;
	}

	public int getType() {
		return mType;
	}

	public int getTypeId() {
		return mTypeId;
	}

	public long getExpires() {
		return mExpires;
	}

	/**
	 * 测试 text属性是否是URL
	 * 
	 * @return true 是，false,others
	 */
	public boolean textIsUrl() {
		return UrlAssert.isUrl(mClickUrl);
	}

	

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(mId);
		dest.writeString(mTitle);
		dest.writeInt(mType);
		dest.writeString(mImgUrl);
		dest.writeString(mClickUrl);
		dest.writeInt(mTypeId);
		dest.writeLong(mExpires);
	}

	public HomeBanner(Parcel in) {
		mId = in.readInt();
		mTitle = in.readString();
		mType = in.readInt();
		mImgUrl = in.readString();
		mClickUrl = in.readString();
		mTypeId = in.readInt();
		mExpires = in.readLong();
	}

	public static final Parcelable.Creator<HomeBanner> CREATOR = new Parcelable.Creator<HomeBanner>() {
		public HomeBanner createFromParcel(Parcel in) {
			return new HomeBanner(in);
		}

		public HomeBanner[] newArray(int size) {
			return new HomeBanner[size];
		}
	};
}
