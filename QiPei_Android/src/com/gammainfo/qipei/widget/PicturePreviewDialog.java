package com.gammainfo.qipei.widget;

import java.util.ArrayList;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.FloatMath;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import cn.amose.vpi.CirclePageIndicator;
import cn.amose.vpi.PageIndicator;

import com.gammainfo.qipei.R;
import com.gammainfo.qipei.utils.UrlAssert;
import com.loopj.android.image.SmartImageTask;
import com.loopj.android.image.SmartImageView;

public class PicturePreviewDialog extends Dialog {
	private ViewPager mViewPager;
	private ArrayList<String> mImagePathList;
	private ViewAdapter mViewPagerAdapter;
	private int mInitPosition;
	private LayoutInflater mLayoutInflater;

	public PicturePreviewDialog(Context context) {
		super(context);
	}

	public PicturePreviewDialog(Context context, int theme) {
		super(context, theme);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_picture_preview);
		DisplayMetrics dm = new DisplayMetrics();
		getWindow().getWindowManager().getDefaultDisplay().getMetrics(dm);
		getWindow().getAttributes().width = dm.widthPixels;
		mLayoutInflater = getLayoutInflater();

		setCanceledOnTouchOutside(true);// 设置点击Dialog外部任意区域关闭Dialog
		if (mImagePathList == null) {
			mImagePathList = new ArrayList<String>();
		}
		mViewPager = (ViewPager) findViewById(R.id.vp_picture_preview_pager);
		mViewPagerAdapter = new ViewAdapter();
		mViewPager.setAdapter(mViewPagerAdapter);
		PageIndicator indicator = (CirclePageIndicator) findViewById(R.id.cpi_picture_preview_pager_indcator);
		indicator.setViewPager(mViewPager);
		mViewPager.setCurrentItem(mInitPosition);
		findViewById(R.id.iv_picture_preview_back_btn).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						dismiss();
					}
				});
	}

	public void setDataSource(ArrayList<String> imagePathList, int curPosition) {
		mImagePathList = imagePathList;
		mInitPosition = curPosition;
		if (mViewPager != null) {
			mViewPagerAdapter.notifyDataSetChanged();
			mViewPager.setCurrentItem(mInitPosition);
		}
	}

	public static PicturePreviewDialog build(Context context,
			ArrayList<String> imagePathList, int currentPosition) {
		PicturePreviewDialog pc = new PicturePreviewDialog(context,
				R.style.FullScreenDialog);
		pc.setDataSource(imagePathList, currentPosition);
		return pc;
	}

	private class ViewAdapter extends PagerAdapter {

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
			View viewContainer = mLayoutInflater.inflate(
					R.layout.picture_preview_listitem, null);

			final SmartImageView smartImageView = (SmartImageView) viewContainer
					.findViewById(R.id.iv_picutre_preview_listitem_photo);
			String path = mImagePathList.get(position);
			if (UrlAssert.isUrl(path)) {
				smartImageView.setImageUrl(path,
						R.drawable.ic_user_product_default,
						new SmartImageTask.OnCompleteListener() {

							@Override
							public void onComplete() {

								smartImageView
										.setOnTouchListener(new MulitPointTouchListener(
												(ImageView) smartImageView));
							}
						});
			} else {
				Bitmap bm = BitmapFactory.decodeFile(path);
				smartImageView.setImageBitmap(bm);
				smartImageView.setOnTouchListener(new MulitPointTouchListener(
						(ImageView) smartImageView));
			}
			container.addView(viewContainer);
			return viewContainer;
		}

		@Override
		public int getCount() {
			return mImagePathList.size();
		}

	}

	public class MulitPointTouchListener implements OnTouchListener {
		private static final String TAG = "Touch";
		// These matrices will be used to move and zoom image
		Matrix matrix = new Matrix();
		Matrix savedMatrix = new Matrix();

		// We can be in one of these 3 states
		static final int NONE = 0;
		static final int DRAG = 1;
		static final int ZOOM = 2;
		int mode = NONE;
		ImageView view;
		int imageWidth;
		int imageHeight;
		// Remember some things for zooming
		PointF start = new PointF();
		PointF mid = new PointF();
		float oldDist = 1f;

		public MulitPointTouchListener(ImageView iv) {
			view = iv;
			center();
		}

		@Override
		public boolean onTouch(View v, MotionEvent event) {

			// ImageView view = (ImageView) v;
			// Log.e("view_width",
			// view.getImageMatrix()..toString()+"*"+v.getWidth());
			// Dump touch event to log
			// dumpEvent(event);

			// Handle touch events here...
			switch (event.getAction() & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_DOWN:

				matrix.set(view.getImageMatrix());
				savedMatrix.set(matrix);
				start.set(event.getX(), event.getY());
				// Log.d(TAG, "mode=DRAG");
				mode = DRAG;

				// Log.d(TAG, "mode=NONE");
				break;
			case MotionEvent.ACTION_POINTER_DOWN:
				oldDist = spacing(event);
				// Log.d(TAG, "oldDist=" + oldDist);
				if (oldDist > 10f) {
					savedMatrix.set(matrix);
					midPoint(mid, event);
					mode = ZOOM;
					// Log.d(TAG, "mode=ZOOM");
				}
				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_POINTER_UP:
				mode = NONE;
				// Log.e("view.getWidth", view.getWidth() + "");
				// Log.e("view.getHeight", view.getHeight() + "");

				break;
			case MotionEvent.ACTION_MOVE:
				if (mode == DRAG) {
					// ...
					matrix.set(savedMatrix);
					matrix.postTranslate(event.getX() - start.x, event.getY()
							- start.y);
				} else if (mode == ZOOM) {
					float newDist = spacing(event);
					// Log.d(TAG, "newDist=" + newDist);
					if (newDist > 10f) {
						matrix.set(savedMatrix);
						float scale = newDist / oldDist;
						matrix.postScale(scale, scale, mid.x, mid.y);
					}
				}
				break;
			}

			view.setImageMatrix(matrix);
			return true; // indicate event was handled
		}

		/**
		 * 横向、纵向居中
		 */
		protected void center() {

			ViewTreeObserver vto = view.getViewTreeObserver();
			vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
				public boolean onPreDraw() {
					imageHeight = view.getMeasuredHeight();
					imageWidth = view.getMeasuredWidth();

					return true;
				}
			});

			BitmapDrawable bitmapDrawable = (BitmapDrawable) view.getDrawable();
			if (bitmapDrawable == null) {
				return;
			}
			Bitmap bitmap = bitmapDrawable.getBitmap();

			int imageWidth = bitmap.getWidth();
			int imageHeight = bitmap.getHeight();

			DisplayMetrics dm = getContext().getResources().getDisplayMetrics();

			int screenWidth = dm.widthPixels;
			int screenHeight = dm.heightPixels;

			int widthFactor = (screenWidth - imageWidth) / 2;
			int heightFactor = (screenHeight - imageHeight) / 2;
			Matrix m = view.getImageMatrix();

			// RectF viewRect = new RectF(imageWidth - widthFactor, imageHeight
			// - heightFactor, imageWidth + widthFactor, imageHeight
			// + widthFactor);
			// m.setRectToRect(drawableRect, viewRect,
			// Matrix.ScaleToFit.CENTER);
			m.postTranslate(widthFactor, heightFactor);

			/*
			 * Matrix m = new Matrix(); m.set(matrix); RectF rect = new RectF(0,
			 * 0, bitmap.getWidth(), bitmap.getHeight()); m.mapRect(rect);
			 * 
			 * float height = rect.height(); float width = rect.width();
			 * 
			 * float deltaX = 0, deltaY = 0;
			 * 
			 * if (vertical) { // 图片小于屏幕大小，则居中显示。大于屏幕，上方留空则往上移，下方留空则往下移 int
			 * screenHeight = dm.heightPixels; if (height < screenHeight) {
			 * deltaY = (screenHeight - height) / 2 - rect.top; } else if
			 * (rect.top > 0) { deltaY = -rect.top; } else if (rect.bottom <
			 * screenHeight) { deltaY = view.getHeight() - rect.bottom; } }
			 * 
			 * if (horizontal) { int screenWidth = dm.widthPixels; if (width <
			 * screenWidth) { deltaX = (screenWidth - width) / 2 - rect.left; }
			 * else if (rect.left > 0) { deltaX = -rect.left; } else if
			 * (rect.right < screenWidth) { deltaX = screenWidth - rect.right; }
			 * } matrix.postTranslate(deltaX, deltaY);
			 */
			view.setImageMatrix(m);
		}

		private void dumpEvent(MotionEvent event) {
			String names[] = { "DOWN", "UP", "MOVE", "CANCEL", "OUTSIDE",
					"POINTER_DOWN", "POINTER_UP", "7?", "8?", "9?" };
			StringBuilder sb = new StringBuilder();
			int action = event.getAction();
			int actionCode = action & MotionEvent.ACTION_MASK;
			sb.append("event ACTION_").append(names[actionCode]);
			if (actionCode == MotionEvent.ACTION_POINTER_DOWN
					|| actionCode == MotionEvent.ACTION_POINTER_UP) {
				sb.append("(pid ").append(
						action >> MotionEvent.ACTION_POINTER_ID_SHIFT);
				sb.append(")");
			}
			sb.append("[");
			for (int i = 0; i < event.getPointerCount(); i++) {
				sb.append("#").append(i);
				sb.append("(pid ").append(event.getPointerId(i));
				sb.append(")=").append((int) event.getX(i));
				sb.append(",").append((int) event.getY(i));
				if (i + 1 < event.getPointerCount())
					sb.append(";");
			}
			sb.append("]");
			// Log.d(TAG, sb.toString());
		}

		private float spacing(MotionEvent event) {
			float x = event.getX(0) - event.getX(1);
			float y = event.getY(0) - event.getY(1);
			return FloatMath.sqrt(x * x + y * y);
		}

		private void midPoint(PointF point, MotionEvent event) {
			float x = event.getX(0) + event.getX(1);
			float y = event.getY(0) + event.getY(1);
			point.set(x / 2, y / 2);
		}
	}
}