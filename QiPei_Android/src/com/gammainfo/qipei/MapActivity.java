package com.gammainfo.qipei;

import java.util.Date;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.MKGeneralListener;
import com.baidu.mapapi.map.ItemizedOverlay;
import com.baidu.mapapi.map.LocationData;
import com.baidu.mapapi.map.MKEvent;
import com.baidu.mapapi.map.MapController;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationOverlay;
import com.baidu.mapapi.map.OverlayItem;
import com.baidu.mapapi.map.PopupOverlay;
import com.baidu.mapapi.map.RouteOverlay;
import com.baidu.mapapi.search.MKAddrInfo;
import com.baidu.mapapi.search.MKBusLineResult;
import com.baidu.mapapi.search.MKDrivingRouteResult;
import com.baidu.mapapi.search.MKPlanNode;
import com.baidu.mapapi.search.MKPoiResult;
import com.baidu.mapapi.search.MKRoute;
import com.baidu.mapapi.search.MKSearch;
import com.baidu.mapapi.search.MKSearchListener;
import com.baidu.mapapi.search.MKShareUrlResult;
import com.baidu.mapapi.search.MKSuggestionResult;
import com.baidu.mapapi.search.MKTransitRouteResult;
import com.baidu.mapapi.search.MKWalkingRouteResult;
import com.baidu.platform.comapi.basestruct.GeoPoint;
import com.gammainfo.qipei.db.Preferences;
import com.gammainfo.qipei.utils.Constant;
import com.gammainfo.qipei.utils.ToastHelper;

public class MapActivity extends BaseActivity implements MKGeneralListener {
	private BMapManager mBMapMan = null;
	private MapView mMapView = null;
	private MapController mMapController = null;
	private PopupOverlay pop = null;
	private View mViewCache = null;
	private TextView mPopupText = null;
	private GeoPoint mPoint;// 目标用户位置
	private GeoPoint mMyPoint;// 我的位置
	private String mAdress;
	private BroadcastReceiver mGpsLocationChangedReceiver;
	private Button mMyLocaiontButton;
	private Button mPathButton;
	private Button mShowPathButton;
	private ToastHelper mToastHelper;
	MyLocationOverlay myLocationOverlay;
	// 浏览路线节点相关

	private int nodeIndex = -2;// 节点索引,供浏览节点时使用
	private MKSearch mSearch = null; // 搜索模块，也可去掉地图模块独立使用
	private RouteOverlay routeOverlay = null;
	private MKRoute route = null;// 保存驾车/步行路线数据的变量，供浏览节点时使用
	private boolean mCanCreateToastHelper = true;
	private boolean mIsRoutePlan = false;// 记录是否在路径规划中

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		initMap();
		addOverLay();
		myLocation();
	}

	private void initMap() {
		// TODO Auto-generated method stub
		mBMapMan = new BMapManager(getApplication());

		mBMapMan.init(this);
		setContentView(R.layout.activity_map);
		Intent mapIntent = getIntent();
		mMapView = (MapView) findViewById(R.id.bmapsView);
		mShowPathButton = (Button) findViewById(R.id.showPath);
		mMapController = mMapView.getController();
		mMapView.setBuiltInZoomControls(true);
		mSearch = new MKSearch();
		mMyLocaiontButton = (Button) findViewById(R.id.myLocation);
		mPathButton = (Button) findViewById(R.id.showPath);
		double cLat = 39.945;
		double cLon = 116.404;
		// 设置启用内置的缩放控件
		MapController mMapController = mMapView.getController();
		// 得到mMapView的控制权,可以用它控制和驱动平移和缩放
		if (mapIntent.hasExtra("latitude") && mapIntent.hasExtra("longitude")) {
			Bundle bundle = mapIntent.getExtras();
			mPoint = new GeoPoint((int) (bundle.getDouble("latitude") * 1E6),
					(int) (bundle.getDouble("longitude") * 1E6));
			mAdress = bundle.getString("adress");
			mMapController.setCenter(mPoint);// 设置地图中心点
		} else {
			// 设置中心点为天安门
			mPoint = new GeoPoint((int) (cLat * 1E6), (int) (cLon * 1E6));
		}
		// 用给定的经纬度构造一个GeoPoint，单位是微度 (度 * 1E6)

		mMapController.setZoom(12);// 设置地图zoom级别

		mSearch.init(mBMapMan, new MKSearchListener() {
			public void onGetDrivingRouteResult(MKDrivingRouteResult res,
					int error) {
			}

			@Override
			public void onGetAddrResult(MKAddrInfo arg0, int arg1) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onGetBusDetailResult(MKBusLineResult arg0, int arg1) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onGetPoiDetailSearchResult(int arg0, int arg1) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onGetPoiResult(MKPoiResult arg0, int arg1, int arg2) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onGetShareUrlResult(MKShareUrlResult arg0, int arg1,
					int arg2) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onGetSuggestionResult(MKSuggestionResult arg0, int arg1) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onGetTransitRouteResult(MKTransitRouteResult arg0,
					int arg1) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onGetWalkingRouteResult(MKWalkingRouteResult res,
					int error) {
				// TODO Auto-generated method stub
				System.out.println("2" + error);
				if (error != 0 || res == null) {
					Toast.makeText(MapActivity.this, "抱歉，未找到结果",
							Toast.LENGTH_SHORT).show();
					mShowPathButton.setClickable(true);
					return;
				}

				routeOverlay = new RouteOverlay(MapActivity.this, mMapView);
				// 此处仅展示一个方案作为示例
				routeOverlay.setData(res.getPlan(0).getRoute(0));
				// 清除其他图层
				mMapView.getOverlays().clear();
				// 添加路线图层
				mMapView.getOverlays().add(routeOverlay);
				mShowPathButton.setText(getResources().getString(
						R.string.list_map_button_hide));
				mShowPathButton.setClickable(true);
				// 执行刷新使生效
				mMapView.refresh();
				// 使用zoomToSpan()绽放地图，使路线能完全显示在地图上
				mMapView.getController().zoomToSpan(
						routeOverlay.getLatSpanE6(),
						routeOverlay.getLonSpanE6());
				// 移动地图到起点
				mMapView.getController().animateTo(res.getStart().pt);
				// 将路线数据保存给全局变量
				route = res.getPlan(0).getRoute(0);
				// 重置路线节点索引，节点浏览时使用
				nodeIndex = -1;
			}

		});

	}

	/*
	 * 设置地图中我的位置
	 */
	private void myLocation() {
		if (Preferences.getMyLatitude() != 0
				&& Preferences.getMyLongitude() != 0
				&& Constant.REQUEST_GPS_LOCATION_INTERVAL
						+ Preferences.getMyLocationLatestTimestamp() > new Date()
							.getTime()) {
			mMyLocaiontButton.setVisibility(View.VISIBLE);
			mPathButton.setVisibility(View.VISIBLE);
			myLocationOverlay = new MyLocationOverlay(mMapView);
			myLocationOverlay.setMarker(getResources().getDrawable(
					R.drawable.icon_map_me));
			LocationData locData = new LocationData();
			// 手动将位置源置为天安门，在实际应用中，请使用百度定位SDK获取位置信息，要在SDK中显示一个位置，需要使用百度经纬度坐标（bd09ll）
			locData.latitude = Preferences.getMyLatitude();
			locData.longitude = Preferences.getMyLongitude();
			mMyPoint = new GeoPoint((int) (locData.latitude * 1E6),
					(int) (locData.longitude * 1E6));
			locData.direction = 2.0f;
			myLocationOverlay.setData(locData);
			mMapView.getOverlays().add(myLocationOverlay);
			mMapView.refresh();
		} else {
			mMyLocaiontButton.setVisibility(View.INVISIBLE);
			mPathButton.setVisibility(View.INVISIBLE);
		}
	}

	/*
	 * 添加目标位置覆盖物
	 */
	private void addOverLay() {
		Drawable mark = getResources().getDrawable(R.drawable.icon_gcoding);
		// 用OverlayItem准备Overlay数据
		OverlayItem item = new OverlayItem(mPoint, mAdress, "");
		// 创建IteminizedOverlay
		OverlayTest itemOverlay = new OverlayTest(mark, mMapView);
		// 将IteminizedOverlay添加到MapView中
		// mMapView.getOverlays().clear();
		mMapView.getOverlays().add(itemOverlay);
		// 现在所有准备工作已准备好，使用以下方法管理overlay.
		itemOverlay.addItem(item);
		mMapView.refresh();
		pop = new PopupOverlay(mMapView, null);
		/*
		 * 向地图添加自定义View.
		 */
		mViewCache = getLayoutInflater().inflate(
				R.layout.map_show_adress_lable, null);
		mPopupText = (TextView) mViewCache.findViewById(R.id.textcache);
	}

	// 常用事件监听，用来处理通常的网络错误，授权验证错误等
	@Override
	public void onGetNetworkState(int iError) {
		if (iError == MKEvent.ERROR_NETWORK_CONNECT) {
			Toast.makeText(this, "您的网络出错啦！", Toast.LENGTH_LONG).show();
		} else if (iError == MKEvent.ERROR_NETWORK_DATA) {
			Toast.makeText(this, "输入正确的检索条件！", Toast.LENGTH_LONG).show();
		}
	}

	public void onGetPermissionState(int iError) {
		// 非零值表示key验证未通过
		/*
		 * if (iError != 0) { // 授权Key错误： Log.e("fail", "请输入正确的授权Key"); //
		 * Application.getInstance().m_bKeyRight = false; } else { //
		 * Application.getInstance().m_bKeyRight = true; Log.e("success",
		 * "key认证成功"); }
		 */
	}

	/*
	 * 路线规划
	 */
	public void showPath(View v) {
		Button button = (Button) v;

		if (button
				.getText()
				.toString()
				.equals(getResources().getString(R.string.list_map_button_step))) {
			searchProcess(v);
			button.setClickable(false);
			mIsRoutePlan = true;
			/*
			 * button.setText(getResources().getString(
			 * R.string.list_map_button_hide));
			 */
		} else {
			mMapView.getOverlays().clear();
			addOverLay();
			myLocation();
			button.setText(getResources().getString(
					R.string.list_map_button_step));
			mIsRoutePlan = false;
		}
	}

	/*
	 * 移动到我的位置
	 */
	public void toMyLocation(View v) {
		// 移动地图到定位点
		mMapController.animateTo(new GeoPoint((int) (Preferences
				.getMyLatitude() * 1e6),
				(int) (Preferences.getMyLongitude() * 1e6)));
	}

	/*
	 * 移动到目标位置
	 */
	public void toAimLocation(View v) {
		mMapController.animateTo(mPoint);
	}

	public void back(View v) {
		// TODO Auto-generated method stub
		finish();
	}

	/**
	 * 发起路线规划搜索示例
	 * 
	 * @param v
	 */
	private void searchProcess(View v) {
		/* 对起始节点以及终点进行赋值 */
		MKPlanNode stNode = new MKPlanNode();
		stNode.pt = mMyPoint;
		System.out.println(mMyPoint.toString());
		MKPlanNode enNode = new MKPlanNode();
		enNode.pt = mPoint;
		// 实际使用中请对起点终点城市进行正确的设定
		mSearch.walkingSearch(null, stNode, null, enNode);
	}

	/*
	 * 要处理overlay点击事件时需要继承ItemizedOverlay 不处理点击事件时可直接生成ItemizedOverlay.
	 */
	class OverlayTest extends ItemizedOverlay<OverlayItem> {
		// 用MapView构造ItemizedOverlay

		public OverlayTest(Drawable mark, MapView mapView) {
			super(mark, mapView);
		}

		protected boolean onTap(int index) {
			// 在此处理item点击事件

			mPopupText.setText(mAdress);
			// 弹出自定义View
			pop.showPopup(mViewCache, mPoint, 75);
			return true;
		}

		public boolean onTap(GeoPoint pt, MapView mapView) {
			// 在此处理MapView的点击事件，当返回 true时
			if (pop != null) {
				pop.hidePop();
				mMapView.removeView(mViewCache);
			}
			return false;
		}
	}

	@Override
	public void onDestroy() {
		if (mToastHelper != null) {
			mToastHelper.dismiss();
		}
		super.onDestroy();
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if (mGpsLocationChangedReceiver == null) {
			mGpsLocationChangedReceiver = new GpsSerivceBroadcastReceiver();
		}
		this.registerReceiver(mGpsLocationChangedReceiver, new IntentFilter(
				Constant.ACTION_GPS_LOCATION_CHANGED_RECEIVER));
		if (((GpsSerivceBroadcastReceiver) mGpsLocationChangedReceiver).mIsReceived
				&& Constant.REQUEST_GPS_LOCATION_INTERVAL
						+ Preferences.getMyLocationLatestTimestamp() <= new Date()
							.getTime()) {
			this.sendBroadcast(new Intent(Constant.ACTION_GPS_SERVICE_RECEIVER));
			if (mToastHelper == null) {
				mToastHelper = ToastHelper.make(this,
						R.string.common_toast_location_loading);
			} else {
				mToastHelper
						.setText(getString(R.string.common_toast_location_loading));
			}

			((GpsSerivceBroadcastReceiver) mGpsLocationChangedReceiver).mIsReceived = false;
			new Handler().postDelayed(new Runnable() {

				@Override
				public void run() {
					do {
						if (mCanCreateToastHelper) {
							mToastHelper.show(mMapView);
							mCanCreateToastHelper = false;
							break;
						}
						try {
							Thread.sleep(50);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					} while (true);
				}
			}, 100);

		}
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub

		if (mGpsLocationChangedReceiver != null) {
			this.unregisterReceiver(mGpsLocationChangedReceiver);
			mGpsLocationChangedReceiver = null;
		}
		if (mToastHelper != null) {
			mToastHelper.dismiss();
		}
		super.onPause();
	}

	private class GpsSerivceBroadcastReceiver extends BroadcastReceiver {
		public boolean mIsReceived = true;

		public void onReceive(Context context, Intent intent) {
			if (mToastHelper != null) {
				mToastHelper.dismiss(
						getString(R.string.common_toast_location_complete),
						1000);
			}
			mIsReceived = true;
			String targetCity = intent.getStringExtra(GpsService.EXTRA_CITY);
			if (targetCity != null && mIsRoutePlan != true) {
				addOverLay();
				myLocation();
			} else {
				Toast.makeText(context, R.string.common_toast_location_failed,
						Toast.LENGTH_SHORT).show();
			}
		}
	}
}
