package com.gammainfo.qipei.widget;

import java.lang.reflect.Field;

import android.content.Context;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.Scroller;

import com.gammainfo.qipei.utils.Constant;
// ms
// Tell our parent to stop intercepting our events!

public class UninterceptableViewPager extends ViewPager {
	private static final int FLIP_DELAY = 5000;// ms
	private Handler mHandler;
	private FixedSpeedScroller mScroller;

	public UninterceptableViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
		enableFlip();
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		int action = ev.getAction();
		if (action == MotionEvent.ACTION_DOWN) {
			disableFlip();
		} else if (action == MotionEvent.ACTION_UP
				|| action == MotionEvent.ACTION_CANCEL
				|| action == MotionEvent.ACTION_OUTSIDE) {
			enableFlip();
		}
		return super.onTouchEvent(ev);
	}

	public boolean onInterceptTouchEvent(MotionEvent ev) {
		// Tell our parent to stop intercepting our events!
		if (ev.getAction() == MotionEvent.ACTION_DOWN) {
			disableFlip();
		}
		boolean ret = super.onInterceptTouchEvent(ev);
		if (ret) {
			getParent().requestDisallowInterceptTouchEvent(true);
		}
		return ret;
	}

	public void enableFlip() {
		if (mHandler == null) {
			mHandler = new Handler();
		}
		this.mHandler.removeCallbacks(mFlipRunnable);
		mHandler.postDelayed(mFlipRunnable, FLIP_DELAY);
	}

	public void disableFlip() {
		if (mHandler != null) {
			mHandler.removeCallbacks(mFlipRunnable);
		}
	}

	private Runnable mFlipRunnable = new Runnable() {
		@Override
		public void run() {
			if (mScroller == null) {
				try {
					Field mField = ViewPager.class
							.getDeclaredField("mScroller");
					mField.setAccessible(true);
					mScroller = new FixedSpeedScroller(
							UninterceptableViewPager.this.getContext(),
							new DecelerateInterpolator());
					mField.set(UninterceptableViewPager.this, mScroller);
				} catch (Exception e) {
					if (Constant.DEBUG) {
						e.printStackTrace();
					}
				}
			}
			int currentItem = UninterceptableViewPager.this.getCurrentItem();
			if (currentItem == UninterceptableViewPager.this.getAdapter()
					.getCount() - 1) {
				UninterceptableViewPager.this.setCurrentItem(0, true);
			} else {
				UninterceptableViewPager.this.setCurrentItem(currentItem + 1,
						true);
			}
			mHandler.postDelayed(this, FLIP_DELAY);
		}
	};

	public class FixedSpeedScroller extends Scroller {
		private int mDuration = 800;

		public FixedSpeedScroller(Context context) {
			super(context);
		}

		public FixedSpeedScroller(Context context, Interpolator interpolator) {
			super(context, interpolator);
		}

		@Override
		public void startScroll(int startX, int startY, int dx, int dy,
				int duration) {
			// Ignore received duration, use fixed one instead
			super.startScroll(startX, startY, dx, dy, mDuration);
		}

		@Override
		public void startScroll(int startX, int startY, int dx, int dy) {
			// Ignore received duration, use fixed one instead
			super.startScroll(startX, startY, dx, dy, mDuration);
		}

		public void setDuration(int time) {
			mDuration = time;
		}
	}
}
