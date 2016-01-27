package com.gammainfo.qipei;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gammainfo.avatarpick.CropImageActivity;
import com.gammainfo.qipei.convert.ProductJSONConvert;
import com.gammainfo.qipei.db.Preferences;
import com.gammainfo.qipei.http.AsyncHttpResponseHandler;
import com.gammainfo.qipei.http.RequestParams;
import com.gammainfo.qipei.model.Image;
import com.gammainfo.qipei.model.Product;
import com.gammainfo.qipei.utils.Constant;
import com.gammainfo.qipei.utils.PhotoChoose;
import com.gammainfo.qipei.utils.UrlAssert;
import com.gammainfo.qipei.widget.ContextMenuDialog;
import com.gammainfo.qipei.widget.PicturePreviewDialog;
import com.gammainfo.qipei.widget.UploadDialog;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.image.SmartImageView;

public class ProductPublishActivity extends BaseActivity {
	public static final String EXTRA_PRODUCT = "product";
	private static final int FLAG_CHOOSE_IMG = 5;
	private static final int FLAG_CHOOSE_PHONE = 6;
	private static final int FLAG_MODIFY_FINISH = 7;
	private EditText mTitleEt;
	private EditText mContentEt;
	private ViewGroup mPictureContainer;
	private LayoutInflater mLayoutInflater;
	private View mTitleTipsView;
	private View mContentTipsView;
	private PhotoChoose mPhotoChooseDialog;
	private static String localTempImageFileName = "tt_";
	public static final String IMAGE_PATH = "chinaqszx";
	public static final File FILE_SDCARD = Environment
			.getExternalStorageDirectory();
	public static final File FILE_LOCAL = new File(FILE_SDCARD, IMAGE_PATH);
	public static final File FILE_PIC_SCREENSHOT = new File(FILE_LOCAL,
			"images/screenshots");
	private View mCurrentHitPictureView;
	private Product mProduct;
	private ContextMenuDialog mContextMenuDialog;
	private AsyncHttpClient mAsyncHttpClient;
	private AsyncHttpResponseHandler mUploadProductHandler;
	private UploadDialog mUploadDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_product_publish);
		mProduct = getIntent().getParcelableExtra(EXTRA_PRODUCT);
		if (mProduct == null) {
			mProduct = new Product();
		}
		TextView activtiyTitleView = (TextView) findViewById(R.id.tv_product_publish_activity_title);
		if (mProduct.getId() == 0) {
			activtiyTitleView.setText("添加产品");
		} else {
			activtiyTitleView.setText("编辑产品");
		}
		mLayoutInflater = getLayoutInflater();
		mTitleEt = (EditText) findViewById(R.id.et_product_title);
		mContentEt = (EditText) findViewById(R.id.et_product_content);
		mPictureContainer = (ViewGroup) findViewById(R.id.ll_product_picture_container);
		mTitleTipsView = findViewById(R.id.tv_product_title_tips);
		mContentTipsView = findViewById(R.id.tv_product_content_tips);
		mTitleEt.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				if (!TextUtils.isEmpty(s.toString())) {
					mTitleTipsView.setVisibility(View.INVISIBLE);
				} else {
					mTitleTipsView.setVisibility(View.VISIBLE);
				}
				mProduct.setProductName(s.toString());
			}
		});
		mContentEt.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				if (!TextUtils.isEmpty(s.toString())) {
					mContentTipsView.setVisibility(View.INVISIBLE);
				} else {
					mContentTipsView.setVisibility(View.VISIBLE);
				}
				mProduct.setBrief(s.toString());
			}
		});

		load();
	}

	private OnClickListener mOnPictureViewClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			String curPath = v.getTag().toString();
			int curPosition = 0;
			int length = mPictureContainer.getChildCount();
			ArrayList<String> imagePathList = new ArrayList<String>(length);
			for (int i = 0; i < length; i++) {
				String path = mPictureContainer.getChildAt(i).getTag()
						.toString();
				if (path.equals(curPath)) {
					curPosition = i;
				}
				imagePathList.add(path);
			}
			PicturePreviewDialog.build(ProductPublishActivity.this,
					imagePathList, curPosition).show();
		}
	};

	private View.OnLongClickListener mOnPictureViewLongClickListener = new View.OnLongClickListener() {

		@Override
		public boolean onLongClick(View v) {
			mCurrentHitPictureView = v;
			showContextMenu();
			return true;
		}
	};

	/**
	 * 添加或更新图片
	 * 
	 * @param currentHitView
	 *            如果为空添加新图片，否则更新
	 * @param path
	 *            图片地址，http:// 或 file://
	 */
	private void addOrUpdatePicture(View currentHitView, String path) {
		if (currentHitView == null) {
			for (int i = 0, size = mPictureContainer.getChildCount(); i < size; i++) {
				if (path.equals(mPictureContainer.getChildAt(i).getTag())) {
					return;
				}
			}
			View pictureContainer = mLayoutInflater.inflate(
					R.layout.product_publish_listitem_picture, null);
			Resources res = getResources();
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					res.getDimensionPixelSize(R.dimen.product_publish_picture_width),
					res.getDimensionPixelSize(R.dimen.product_publish_picture_height));
			if (mPictureContainer.getChildCount() > 0) {
				params.leftMargin = res
						.getDimensionPixelSize(R.dimen.product_publish_spacing);
			}
			pictureContainer.setLayoutParams(params);
			pictureContainer.setClickable(true);

			pictureContainer.setOnClickListener(mOnPictureViewClickListener);
			pictureContainer
					.setOnLongClickListener(mOnPictureViewLongClickListener);
			mPictureContainer.addView(pictureContainer);
			currentHitView = pictureContainer;
		}
		SmartImageView iv = (SmartImageView) currentHitView
				.findViewById(R.id.iv_product_publish_listitem_picture);
		boolean needAdd = true;
		ArrayList<Image> images = mProduct.getImgs();
		if (images != null) {
			Object prevPath = currentHitView.getTag();
			for (Image image : images) {
				String url = image.getUrl();
				if (url.equals(prevPath) || url.equals(path)) {
					needAdd = false;
					image.setUrl(path);
				}
			}
		}
		if (needAdd) {
			if (images == null) {
				images = new ArrayList<Image>();
				mProduct.setImgs(images);
			}
			Image image = new Image();
			image.setUrl(path);
			images.add(image);
		}
		currentHitView.setTag(path);
		if (UrlAssert.isUrl(path)) {
			iv.setImageUrl(path, R.drawable.ic_user_product_default);
		} else {
			Bitmap bm = BitmapFactory.decodeFile(path);
			iv.setImageBitmap(bm);
		}
	}

	private void removePricture() {
		if (mCurrentHitPictureView != null) {
			Object path = mCurrentHitPictureView.getTag();
			Image delImage = null;
			for (Image image : mProduct.getImgs()) {
				if (path.equals(image.getUrl())) {
					delImage = image;
					break;
				}
			}
			if (delImage != null) {
				mProduct.getImgs().remove(delImage);
			}
			mPictureContainer.removeView(mCurrentHitPictureView);
			if (mPictureContainer.getChildCount() > 0) {
				((LinearLayout.LayoutParams) mPictureContainer.getChildAt(0)
						.getLayoutParams()).leftMargin = 0;
			}
		}
	}

	private void updatePictureViewTag(String oldTag, String newTag) {
		View tmp;
		for (int i = 0, size = mPictureContainer.getChildCount(); i < size; i++) {
			tmp = mPictureContainer.getChildAt(i);
			if (oldTag.equals(tmp.getTag())) {
				tmp.setTag(newTag);
				break;
			}
		}
	}

	public void onBackClick(View v) {
		save();
		finish();
	}

	public void onAddPictureClick(View v) {
		mCurrentHitPictureView = null;
		choosePhoto();
	}

	public void onPublishClick(View v) {
		// 调用服务发布产品
		// step 1，图片上传
		// step 2，发布产品
		String productName = mTitleEt.getText().toString().trim();
		if (productName.equals("")) {
			Toast.makeText(this, R.string.profile_label_input_title_text,
					Toast.LENGTH_SHORT).show();
			return;
		}
		String brief = mContentEt.getText().toString().trim();
		if (brief.equals("")) {
			Toast.makeText(this,
					R.string.profile_label_input_detail_content_text,
					Toast.LENGTH_SHORT).show();
			return;
		}
		mProduct.setProductName(productName);
		mProduct.setBrief(brief);
		ArrayList<Image> imageList = mProduct.getImgs();
		if (imageList == null || imageList.size() == 0) {
			Toast.makeText(this, "请添加图片", Toast.LENGTH_SHORT).show();
			return;
		}

		uploadPhoto(imageList.get(0));
	}

	private void save() {
		// TODO 保存新增产品但未提交的信息
		if (mProduct != null && mProduct.getId() == 0) {
			ArrayList<Image> imageList = mProduct.getImgs();
			if (!TextUtils.isEmpty(mProduct.getProductName())
					|| !TextUtils.isEmpty(mProduct.getBrief())
					|| (imageList != null && imageList.size() > 0)) {
				Toast.makeText(this, "已保存，下次可继续操作", Toast.LENGTH_SHORT).show();
				Preferences.saveUploadingProduct(ProductJSONConvert
						.convertProduct(mProduct).toString());
			} else {
				Preferences.saveUploadingProduct(null);
			}
		}
	}

	private void load() {
		// TODO 加载上次保存的产品
		if (mProduct.getId() == 0) {
			// 新增
			String productJson = Preferences.getUploadingProduct();
			if (productJson != null) {
				try {
					mProduct = ProductJSONConvert
							.convertJsonToItem(new JSONObject(productJson));
					Toast.makeText(this, "已恢复上次操作", Toast.LENGTH_SHORT).show();
					updateViewContent();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		} else {
			// 修改
			// TODO 获取产品详细
			updateViewContent();
			getProductInfo();
		}
	}

	private void updateViewContent() {
		mTitleEt.setText(mProduct.getProductName());
		mContentEt.setText(mProduct.getBrief());

		ArrayList<Image> imageList = (ArrayList<Image>) mProduct.getImgs()
				.clone();
		if (imageList != null) {
			for (Image image : imageList) {
				addOrUpdatePicture(null, image.getUrl());
			}
		}
	}

	private void clear() {
		// TODO 清除保存产品信息
		mTitleEt.getText().clear();
		mContentEt.getText().clear();
		mPictureContainer.removeAllViews();
	}

	private void choosePhoto() {
		if (mPhotoChooseDialog == null) {
			mPhotoChooseDialog = PhotoChoose.build(this,
					new PhotoChoose.OnPhotoChooseListener() {

						@Override
						public void cancel(PhotoChoose target) {
							target.dismiss();
						}

						@Override
						public void camera(PhotoChoose target) {
							target.dismiss();
							String status = Environment
									.getExternalStorageState();
							if (status.equals(Environment.MEDIA_MOUNTED)) {
								try {
									localTempImageFileName = String
											.valueOf((new Date()).getTime())
											+ ".png";
									File filePath = FILE_PIC_SCREENSHOT;
									if (!filePath.exists()) {
										filePath.mkdirs();
									}
									Intent intent = new Intent(
											android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
									File f = new File(filePath,
											localTempImageFileName);
									// localTempImgDir和localTempImageFileName是自己定义的名字
									Uri u = Uri.fromFile(f);
									intent.putExtra(
											MediaStore.Images.Media.ORIENTATION,
											0);
									intent.putExtra(MediaStore.EXTRA_OUTPUT, u);
									startActivityForResult(intent,
											FLAG_CHOOSE_PHONE);
								} catch (ActivityNotFoundException e) {
									//
								}
							}
						}

						@Override
						public void album(PhotoChoose target) {
							target.dismiss();
							Intent intent = new Intent();
							intent.setAction(Intent.ACTION_PICK);
							intent.setType("image/*");
							startActivityForResult(intent, FLAG_CHOOSE_IMG);
						}
					});
		}
		mPhotoChooseDialog.show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			if (requestCode == FLAG_CHOOSE_IMG) {
				if (data != null) {
					Uri uri = data.getData();
					if (!TextUtils.isEmpty(uri.getAuthority())) {
						Cursor cursor = getContentResolver().query(uri,
								new String[] { MediaStore.Images.Media.DATA },
								null, null, null);
						if (null == cursor) {
							Toast.makeText(this, "图片没找到", Toast.LENGTH_SHORT)
									.show();
							return;
						}
						cursor.moveToFirst();
						String path = cursor.getString(cursor
								.getColumnIndex(MediaStore.Images.Media.DATA));
						cursor.close();
						startCropImageActivity(path);
					} else {
						startCropImageActivity(uri.getPath());
					}
				}
			} else if (requestCode == FLAG_CHOOSE_PHONE) {
				File f = new File(FILE_PIC_SCREENSHOT, localTempImageFileName);
				startCropImageActivity(f.getAbsolutePath());
			} else if (requestCode == FLAG_MODIFY_FINISH) {
				if (data != null) {
					String path = data.getStringExtra("path");
					addOrUpdatePicture(mCurrentHitPictureView, path);
				}
			}
		}
	}

	private void startCropImageActivity(String path) {
		Intent intent = new Intent(this, CropImageActivity.class);
		intent.putExtra("path", path);
		intent.putExtra("rectRatioX", 4);
		intent.putExtra("rectRatioY", 3);
		intent.putExtra("imageWidth", 800);
		startActivityForResult(intent, FLAG_MODIFY_FINISH);
	}

	/**
	 * 获取下一个要图片上传的图片
	 * 
	 * @param image
	 * @return 如果没有下一个图片，就返回null，否则返回图片
	 */
	private Image nextUploadPhoto(Image image) {
		ArrayList<Image> imgList = mProduct.getImgs();
		int index = imgList.indexOf(image);
		if (index == imgList.size() - 1) {
			return null;
		}
		return imgList.get(index + 1);
	}

	public void uploadPhoto(Image image) {
		if (mUploadDialog == null) {
			mUploadDialog = UploadDialog.build(this, "正在上传", "请稍等...",
					new UploadDialog.OnUploadDialogListener() {

						@Override
						public void onRetry(UploadDialog targetDialog) {
							targetDialog.dismiss();
							onPublishClick(null);
						}

						@Override
						public void onCancel(UploadDialog targetDialog) {
							Toast.makeText(ProductPublishActivity.this,
									"已取消发布", Toast.LENGTH_SHORT).show();
							mAsyncHttpClient.cancelRequests(
									ProductPublishActivity.this, true);
							targetDialog.dismiss();
						}
					});
		}
		mUploadDialog.show();
		mUploadDialog.hideRetryButton();
		mUploadDialog.setTitle("正在上传图片");

		final Image fImage = image;
		if (image == null) {
			// TODO 发布产品
			publish();
			return;
		}
		String path = image.getUrl();
		mUploadDialog.setDesc(path);
		if (UrlAssert.isUrl(path)) {
			// 不需要上传
			uploadPhoto(nextUploadPhoto(image));
			return;
		}

		Bitmap b = BitmapFactory.decodeFile(path);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		b.compress(Bitmap.CompressFormat.PNG, 100, out);
		try {
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
			mUploadDialog.setTitle("图片上传失败");
			mUploadDialog.setDesc("检查存储空间是否已满，请重试...");
			mUploadDialog.showRetryButton();
			return;
		}
		byte[] buffer = out.toByteArray();
		byte[] encode = Base64.encode(buffer, Base64.DEFAULT);
		String photo = new String(encode);
		int id = Preferences.getAccountUserId();
		RequestParams params = new RequestParams();
		params.put("user_id", String.valueOf(id));
		params.put("image", photo);
		params.put("type", "2");// 供求：1，产品：2
		if (mAsyncHttpClient == null) {
			mAsyncHttpClient = new AsyncHttpClient();
		} else {
			mAsyncHttpClient.cancelRequests(this, true);
		}

		mAsyncHttpClient.post(this, Constant.API_UPLOAD_PHOTO, params,
				new AsyncHttpResponseHandler(this,
						new JsonHttpResponseHandler() {

							@Override
							public void onFailure(Throwable e,
									JSONObject errorResponse) {
								super.onFailure(e, errorResponse);
								mUploadDialog.setTitle("图片上传失败");
								mUploadDialog.setDesc("检查网络是否畅通，请重试...");
								mUploadDialog.showRetryButton();
							}

							@Override
							public void onFailure(int statusCode, Throwable e,
									JSONArray errorResponse) {
								super.onFailure(statusCode, e, errorResponse);
								mUploadDialog.setTitle("图片上传失败");
								mUploadDialog.setDesc("检查网络是否畅通，请重试...");
								mUploadDialog.showRetryButton();
							}

							@Override
							public void onSuccess(JSONObject rlt) {
								try {
									if (rlt.getInt(Constant.JSON_KEY_CODE) == Constant.CODE_SUCCESS) {
										JSONObject info = rlt
												.getJSONObject("info");
										String photoUrl = info.getString("url");
										updatePictureViewTag(fImage.getUrl(),
												photoUrl);
										fImage.setUrl(photoUrl);
										uploadPhoto(nextUploadPhoto(fImage));
									} else {
										mUploadDialog.setTitle("图片上传失败");
										mUploadDialog.setDesc(rlt
												.getString("msg"));
										mUploadDialog.showRetryButton();
									}
								} catch (JSONException e) {
									e.printStackTrace();
								}
							}
						}));
	}

	private void publish() {
		mUploadDialog.setTitle("正在发布产品");
		mUploadDialog.setDesc("产品存在生成中，请稍等...");
		StringBuilder sbImgs = new StringBuilder();
		ArrayList<Image> imgList = mProduct.getImgs();
		if (imgList != null) {
			for (int i = 0, size = imgList.size(); i < size; i++) {
				sbImgs.append(imgList.get(i).getUrl());
				if (i + 1 < size) {
					sbImgs.append("*|*");
				}
			}
		}

		RequestParams params = new RequestParams();
		params.put("user_id", String.valueOf(Preferences.getAccountUserId()));
		params.put("product_name", mProduct.getProductName());
		params.put("imgs", sbImgs.toString());
		params.put("brief", mProduct.getBrief());
		params.put("type", "2");// 供求：1，产品：2
		String api;
		int id = mProduct.getId();
		if (id == 0) {
			// TODO 新增
			api = Constant.API_ADD_PRODUCT;
		} else {
			// TODO 编辑
			api = Constant.API_EDIT_PRODUCT;
			params.put("id", String.valueOf(id));
		}
		if (mAsyncHttpClient == null) {
			mAsyncHttpClient = new AsyncHttpClient();
		} else {
			mAsyncHttpClient.cancelRequests(this, true);
		}
		if (mUploadProductHandler == null) {
			mUploadProductHandler = new AsyncHttpResponseHandler(this,
					new JsonHttpResponseHandler() {

						@Override
						public void onFailure(Throwable e,
								JSONObject errorResponse) {
							super.onFailure(e, errorResponse);
							mUploadDialog.showRetryButton();
						}

						@Override
						public void onFailure(int statusCode, Throwable e,
								JSONArray errorResponse) {
							super.onFailure(statusCode, e, errorResponse);
							mUploadDialog.showRetryButton();
						}

						@Override
						public void onSuccess(JSONObject rlt) {
							try {
								if (rlt.getInt(Constant.JSON_KEY_CODE) == Constant.CODE_SUCCESS) {
									mUploadDialog.dismiss();
									if (mProduct.getId() == 0) {
										JSONObject info = rlt
												.getJSONObject("info");
										mProduct.setId(info.getInt("id"));
									}
									Toast.makeText(ProductPublishActivity.this,
											"发布成功", Toast.LENGTH_LONG).show();
									if (TextUtils.isEmpty(mProduct.getThumb())) {
										ArrayList<Image> imageList = mProduct
												.getImgs();
										if (imageList != null
												&& imageList.size() > 0) {
											mProduct.setThumb(imageList.get(0)
													.getUrl());
										}
									}
									Intent intent = new Intent();
									intent.putExtra(EXTRA_PRODUCT, mProduct);
									setResult(RESULT_OK, intent);
									finish();
									mProduct = null;
									Preferences.saveUploadingProduct(null);
								} else {
									mUploadDialog.setTitle("发布失败");
									mUploadDialog.setDesc(rlt.getString("msg"));
									mUploadDialog.showRetryButton();
								}
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
					});
		}
		mAsyncHttpClient.post(this, api, params, mUploadProductHandler);
	}

	@Override
	protected void onDestroy() {
		if (mPhotoChooseDialog != null) {
			mPhotoChooseDialog.dismiss();
		}
		if (mContextMenuDialog != null) {
			mContextMenuDialog.dismiss();
		}
		if (mUploadDialog != null) {
			mUploadDialog.dismiss();
		}
		super.onDestroy();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_DOWN) {
			save();
		}
		return super.onKeyDown(keyCode, event);
	}

	private void showContextMenu() {
		if (mContextMenuDialog == null) {
			ArrayList<String> datasource = new ArrayList<String>();
			datasource.add("编辑");
			datasource.add("删除");
			mContextMenuDialog = ContextMenuDialog.build(this,
					new ContextMenuDialog.OnContextMenuListener() {

						@Override
						public void onItemClick(ContextMenuDialog target,
								int index) {
							target.dismiss();
							if (index == 0) {
								// 编辑
								choosePhoto();
							} else {
								// 删除
								removePricture();
							}
						}

						@Override
						public void onCancelClick(ContextMenuDialog target) {
							target.dismiss();
						}
					}, datasource);
		}
		mContextMenuDialog.show();
	}

	private void getProductInfo() {
		Toast.makeText(this, "正在加载产品....", Toast.LENGTH_SHORT).show();
		RequestParams params = new RequestParams();
		params.put("id", mProduct.getId() + "");
		if (mAsyncHttpClient == null) {
			mAsyncHttpClient = new AsyncHttpClient();
		}
		mAsyncHttpClient.post(this, Constant.API_USER_PRODUCT_ITEM_GET, params,
				new AsyncHttpResponseHandler(this,
						new JsonHttpResponseHandler() {

							@Override
							public void onFailure(Throwable e,
									JSONArray errorResponse) {
								super.onFailure(e, errorResponse);
								Toast.makeText(ProductPublishActivity.this,
										"加载失败", Toast.LENGTH_SHORT).show();
								finish();
							}

							@Override
							public void onFailure(Throwable e,
									JSONObject errorResponse) {
								super.onFailure(e, errorResponse);
								Toast.makeText(ProductPublishActivity.this,
										"加载失败", Toast.LENGTH_SHORT).show();
								finish();
							}

							@Override
							public void onSuccess(JSONObject res) {
								JSONObject jsonInfo = null;
								try {
									if (res.getInt(Constant.JSON_KEY_CODE) == Constant.CODE_SUCCESS) {
										jsonInfo = res
												.getJSONObject(Constant.JSON_KEY_INFO);
										mProduct = ProductJSONConvert
												.convertJsonToItem(jsonInfo);
										updateViewContent();
									} else {
										Toast.makeText(
												ProductPublishActivity.this,
												res.getString("msg"),
												Toast.LENGTH_SHORT).show();
										finish();
									}
								} catch (JSONException e) {
									e.printStackTrace();
								}
							}
						}));
	}
}
