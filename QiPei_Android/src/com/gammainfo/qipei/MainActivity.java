package com.gammainfo.qipei;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;

import com.gammainfo.qipei.db.Preferences;
import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;

public class MainActivity extends FragmentActivity implements
		OnCheckedChangeListener {
	private static final int REQ_SWITCH_CITY = 0x01;
	private SearchFragment mSearchFragment;
	private ListFragment mListFragment;
	private NewsFragment mNewsFragment;
	private ProfileFragment mProfileFragment;

	// private ImageView mTabSearchImage;
	// private ImageView mTabListImage;
	// private ImageView mTabNewsImage;
	// private ImageView mTabProfileImage;

	private FragmentManager mFragmentManager;
	private long mExitTime = 0;
	private ArrayList<OnTabChangedListener> mOnTabChangedListenerList;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i("MainActivity", "-----------Hello--------------------");
		String myCity = Application.getCurrentCity();
		Log.i("MainActivity", "-----------myCity："+myCity+"--------------------");
		if (myCity == null) {
			myCity = Preferences.getMyCityFromLocalFile();
			if (myCity == null) {
				startCityChooseActivity();
				return;
			}
			Application.setCurrentCity(myCity);
		}
		setContentView(R.layout.activity_main);
		// 初始化布局元素
		((RadioGroup) findViewById(R.id.tabbar))
				.setOnCheckedChangeListener(this);
		mFragmentManager = getSupportFragmentManager();
		// 第一次启动时选中第0个tab
		((RadioButton) findViewById(R.id.tab_search)).setChecked(true);
		startNewsContentActivityIfNeed(getIntent());
		UmengUpdateAgent.setUpdateOnlyWifi(false);
		UmengUpdateAgent.update(this);
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		setTabSelection(checkedId);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		startNewsContentActivityIfNeed(intent);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQ_SWITCH_CITY) {
			if (resultCode == Activity.RESULT_OK) {
				String newCity = data
						.getStringExtra(AreaBaseActivity.EXTRA_CITY);
				Preferences.setMyCity(newCity);
				Preferences.putCityInHistoryPool(newCity);
				finish();
				startActivity(new Intent(this, MainActivity.class));
			} else {
				Toast.makeText(this, "您需要选择一个城市才能开始体验软件", Toast.LENGTH_SHORT)
						.show();
				startCityChooseActivity();
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void startCityChooseActivity() {
		Intent intent = new Intent(this, ProvinceActivity.class);
		intent.putExtra(AreaBaseActivity.EXTRA_LEVEL,
				AreaBaseActivity.LEVEL_PROVINCE_CITY);
		intent.putExtra(AreaBaseActivity.EXTRA_AREA_SHORT, true);
		startActivityForResult(intent, REQ_SWITCH_CITY);
	}

	/**
	 * 根据传入的resId参数来设置选中的tab页。
	 * 
	 * @param resId
	 *            每个tab页中对应的资源ID(layout id)
	 */
	private void setTabSelection(int resId) {
		// 开启一个Fragment事务
		FragmentTransaction transaction = mFragmentManager.beginTransaction();
		// 先隐藏掉所有的Fragment，以防止有多个Fragment显示在界面上的情况
		hideFragments(transaction);
		switch (resId) {
		case R.id.tab_search:
			// 当点击了消息tab时，改变控件的图片和文字颜色
			if (mSearchFragment == null) {
				// 如果SearchFragment为空，则创建一个并添加到界面上
				mSearchFragment = new SearchFragment();
				transaction.add(R.id.content, mSearchFragment);
			} else {
				// 如果SearchFragment不为空，则直接将它显示出来
				transaction.show(mSearchFragment);
			}

			break;
		case R.id.tab_list:
			// 当点击了联系人tab时，改变控件的图片和文字颜色
			if (mListFragment == null) {
				// 如果ListFragment为空，则创建一个并添加到界面上
				mListFragment = new ListFragment();
				transaction.add(R.id.content, mListFragment);
			} else {
				// 如果ListFragment不为空，则直接将它显示出来
				transaction.show(mListFragment);
			}
			break;
		case R.id.tab_news:
			// 当点击了动态tab时，改变控件的图片和文字颜色
			if (mNewsFragment == null) {
				// 如果NewsFragment为空，则创建一个并添加到界面上
				mNewsFragment = new NewsFragment();
				transaction.add(R.id.content, mNewsFragment);
			} else {
				// 如果NewsFragment不为空，则直接将它显示出来
				transaction.show(mNewsFragment);
			}
			break;
		case R.id.tab_profile:
		default:
			resId = R.id.tab_profile;
			// 当点击了设置tab时，改变控件的图片和文字颜色
			if (mProfileFragment == null) {
				// 如果ProfileFragment为空，则创建一个并添加到界面上
				mProfileFragment = new ProfileFragment();
				transaction.add(R.id.content, mProfileFragment);
			} else {
				// 如果ProfileFragment不为空，则直接将它显示出来
				transaction.show(mProfileFragment);
			}
			break;
		}
		transaction.commit();
		if (mOnTabChangedListenerList != null) {
			for (OnTabChangedListener listener : mOnTabChangedListenerList) {
				listener.onChanged(this, resId);
			}
		}
	}

	/**
	 * 将所有的Fragment都置为隐藏状态。
	 * 
	 * @param transaction
	 *            用于对Fragment执行操作的事务
	 */
	private void hideFragments(FragmentTransaction transaction) {
		if (mSearchFragment != null) {
			transaction.hide(mSearchFragment);
		}
		if (mListFragment != null) {
			transaction.hide(mListFragment);
		}
		if (mNewsFragment != null) {
			transaction.hide(mNewsFragment);
		}
		if (mProfileFragment != null) {
			transaction.hide(mProfileFragment);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_DOWN) {
			if ((System.currentTimeMillis() - mExitTime) > 2000) {
				Toast.makeText(getApplicationContext(),
						R.string.common_toast_exitapp, Toast.LENGTH_SHORT)
						.show();
				mExitTime = System.currentTimeMillis();
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	@Override
	public void onPause() {
		MobclickAgent.onPause(this);
		super.onPause();
	}

	public int getTabbarHeight() {
		return findViewById(R.id.tabbar).getMeasuredHeight();
	}

	public void registerOnTabChangedListener(
			OnTabChangedListener onTabChangedListener) {
		if (onTabChangedListener != null) {
			if (mOnTabChangedListenerList == null) {
				mOnTabChangedListenerList = new ArrayList<MainActivity.OnTabChangedListener>();
			}
			mOnTabChangedListenerList.add(onTabChangedListener);
		}
	}

	public void unregisterOnTabChangedListener(
			OnTabChangedListener onTabChangedListener) {
		if (onTabChangedListener != null) {
			if (mOnTabChangedListenerList != null) {
				mOnTabChangedListenerList.remove(onTabChangedListener);
			}
		}
	}

	public static interface OnTabChangedListener {
		void onChanged(MainActivity mainActivity, int selectedTabId);
	}

	private void startNewsContentActivityIfNeed(Intent intent) {
		if (intent.getBooleanExtra("noti", false)) {
			intent.setClass(this, NewsContentActivity.class);
			startActivity(intent);
		}
	}
}
