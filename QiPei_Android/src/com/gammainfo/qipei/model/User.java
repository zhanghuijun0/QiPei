package com.gammainfo.qipei.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 厂商、经销商、终端客户的抽象类
 * 
 * @author amose
 * 
 */
public class User implements Parcelable {
	private String mAccount;
	private int mId;
	private String mCompanyName;
	private String mPhone;
	private int mRoleId;
	private String mRoleName;
	private int mMemberGrade;
	private String mProvince;
	private String mCity;
	private String mCounty;
	private String mAddress;
	private String mIntro;
	private String mPhotoUrl;
	private boolean mIsCertificated;
	private boolean mIsMemberCertificated;
	private double mLongitude;
	private double mLatitude;
	private int mProductNum;
	private int mPV;
	private int mFavoriteNum;
	private int mHotNum;
	private boolean mIsFavorite;// 是否被收藏
	private float mDistance;// 该用户与当前手机用户的距离，单位km

	public String getAccount() {
		return mAccount;
	}

	public void setAccount(String account) {
		this.mAccount = account;
	}

	public int getId() {
		return mId;
	}

	public void setId(int id) {
		this.mId = id;
	}

	public String getCompanyName() {
		return mCompanyName;
	}

	public void setCompanyName(String companyName) {
		this.mCompanyName = companyName;
	}

	public int getProductNum() {
		return mProductNum;
	}

	public void setProductNum(int productNum) {
		this.mProductNum = productNum;
	}

	public String getPhone() {
		return mPhone;
	}

	public void setPhone(String phone) {
		this.mPhone = phone;
	}

	public int getRoleId() {
		return mRoleId;
	}

	public String getRoleName() {
		return mRoleName;
	}

	public void setRoleId(int roleId) {
		this.mRoleId = roleId;
	}

	public void setRoleName(String roleName) {
		this.mRoleName = roleName;
	}

	public int getMemberGrade() {
		return mMemberGrade;
	}

	public void setMemberGrade(int memberGrade) {
		this.mMemberGrade = memberGrade;
	}

	public String getProvince() {
		return mProvince;
	}

	public void setProvince(String province) {
		this.mProvince = province;
	}

	public String getCity() {
		return mCity;
	}

	public void setCity(String city) {
		this.mCity = city;
	}

	public String getCounty() {
		return mCounty;
	}

	public void setCounty(String county) {
		this.mCounty = county;
	}

	public String getAddress() {
		return mAddress;
	}

	public void setAddress(String address) {
		this.mAddress = address;
	}

	public String getIntro() {
		return mIntro;
	}

	public void setIntro(String intro) {
		this.mIntro = intro;
	}

	public String getPhotoUrl() {
		return mPhotoUrl;
	}

	public void setPhotoUrl(String photoUrl) {
		this.mPhotoUrl = photoUrl;
	}

	public boolean isCertificated() {
		return mIsCertificated;
	}

	public void setIsCertificated(boolean isCertificated) {
		this.mIsCertificated = isCertificated;
	}

	public boolean isMemberCertificated() {
		return mIsMemberCertificated;
	}

	public void setIsMemberCertificated(boolean isMemberCertificated) {
		this.mIsMemberCertificated = isMemberCertificated;
	}

	public double getLongitude() {
		return mLongitude;
	}

	public void setLongitude(double longitude) {
		this.mLongitude = longitude;
	}

	public double getLatitude() {
		return mLatitude;
	}

	public void setLatitude(double latitude) {
		this.mLatitude = latitude;
	}

	public int getPV() {
		return mPV;
	}

	public void setPV(int pv) {
		this.mPV = pv;
	}

	public int getFavoriteNum() {
		return mFavoriteNum;
	}

	public void setFavoriteNum(int favoriteNum) {
		this.mFavoriteNum = favoriteNum;
	}

	public int getHotNum() {
		return mHotNum;
	}

	public boolean isIsFavorite() {
		return mIsFavorite;
	}

	public void setIsFavorite(boolean mIsFavorite) {
		this.mIsFavorite = mIsFavorite;
	}

	public void setHotNum(int hotNum) {
		this.mHotNum = hotNum;
	}

	/**
	 * 设置该用户与当前手机用户的距离，单位km
	 * 
	 * @param distance
	 */
	public void setDistance(float distance) {
		mDistance = distance;
	}

	/**
	 * 获取该用户与当前手机用户的距离，单位km
	 * 
	 * @return
	 */
	public float getDistance() {
		return mDistance;
	}

	public static Parcelable.Creator<User> getCreator() {
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
		dest.writeString(mAccount);
		dest.writeString(mCompanyName);

		dest.writeString(mPhone);
		dest.writeInt(mRoleId);
		dest.writeInt(mMemberGrade);

		dest.writeString(mRoleName);
		dest.writeString(mProvince);
		dest.writeString(mCity);

		dest.writeString(mCounty);
		dest.writeString(mAddress);
		dest.writeString(mIntro);

		dest.writeString(mPhotoUrl);
		dest.writeInt(mIsCertificated ? 1 : 0);
		dest.writeInt(mIsMemberCertificated ? 1 : 0);
		dest.writeInt(mIsFavorite ? 1 : 0);

		dest.writeDouble(mLongitude);
		dest.writeDouble(mLatitude);
		dest.writeInt(mProductNum);

		dest.writeInt(mPV);
		dest.writeInt(mFavoriteNum);
		dest.writeInt(mHotNum);

		dest.writeFloat(mDistance);
	}

	public User() {

	}

	public User(Parcel in) {
		mId = in.readInt();
		mAccount = in.readString();
		mCompanyName = in.readString();

		mPhone = in.readString();
		mRoleId = in.readInt();
		mMemberGrade = in.readInt();

		mRoleName = in.readString();
		mProvince = in.readString();
		mCity = in.readString();

		mCounty = in.readString();
		mAddress = in.readString();
		mIntro = in.readString();

		mPhotoUrl = in.readString();
		mIsCertificated = in.readInt() == 1 ? true : false;
		mIsMemberCertificated = in.readInt() == 1 ? true : false;
		mIsFavorite = in.readInt() == 1 ? true : false;

		mLongitude = in.readDouble();
		mLatitude = in.readDouble();
		mProductNum = in.readInt();

		mPV = in.readInt();
		mFavoriteNum = in.readInt();
		mHotNum = in.readInt();

		mDistance = in.readFloat();

	}

	public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
		public User createFromParcel(Parcel in) {
			return new User(in);
		}

		public User[] newArray(int size) {
			return new User[size];
		}
	};

	public boolean equals(Object other) {
		if (this == other) {// 先检查是否其自反性，后比较other是否为空。这样效率高
			return true;
		}

		if (other == null) {
			return false;
		}
		final User user = (User)other;
		 if( !this.getAccount().equals(user.getAccount())) return false; 
		 if( !this.getAddress().equals(user.getAddress())) return false;
		 if( !this.getCity().equals(user.getCity())) return false;
		 if( !this.getCompanyName().equals(user.getCompanyName())) return false;
		 if( !this.getCounty().equals(user.getCounty())) return false;
		 if( !this.getIntro().equals(user.getIntro())) return false;
		 if( !this.getPhone().equals(user.getPhone())) return false;
		 if( !this.getPhotoUrl().equals(user.getPhotoUrl())) return false;
		 if( !this.getRoleName().equals(user.getRoleName())) return false;
		 if( this.getFavoriteNum()!=user.getFavoriteNum()) return false;
		
		return true;
	};

}
