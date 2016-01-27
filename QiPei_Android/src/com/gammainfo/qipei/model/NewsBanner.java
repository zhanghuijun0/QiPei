package com.gammainfo.qipei.model;


public class NewsBanner{

	
	private int mId;
	private String mTitle;
	private String mIntro;
	private String mContent;
	private int mCreatTime;
	private String mImageUrl;
	private int mType;
	private String MediaUrl;
	private int MediaDuration;
	private int mExpires;
	
//	public NewsBanner(int id, String title, String imageUrl, int type, int expires){
//		mId = id;
//		mTitle = title;
//		mImageUrl = imageUrl;
//		mType = type;
//		mExpires = expires;
//	}
	public int getId() {
		return mId;
	}

	public void setId(int id) {
		mId = id;
	}

	public String getTitle() {
		return mTitle;
	}

	public void setTitle(String title) {
		mTitle = title;
	}

	public String getImageUrl() {
		return mImageUrl;
	}

	public void setImageUrl(String imageUrl) {
		mImageUrl = imageUrl;
	}

	public int getType() {
		return mType;
	}

	public void setType(int type) {
		mType = type;
	}

	public int getExpires() {
		return mExpires;
	}

	public void setExpires(int expires) {
		mExpires = expires;
	}
}
