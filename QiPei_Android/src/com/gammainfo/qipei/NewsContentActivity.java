package com.gammainfo.qipei;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshWebView;

public class NewsContentActivity extends BaseActivity implements
		OnRefreshListener<WebView> {
	public static final String EXTRA_TYPE = "type";
	public static final String EXTRA_URL = "pageUrl";
	public static final int TYPE_COMPANY = 1;
	public static final int TYPE_TRADE = 2;
	public static final int TYPE_ACTIVITY = 3;
	public static final int TYPE_MEDIA = 4;
	private PullToRefreshWebView mPullRefreshWebView;
	private FrameLayout customViewContainer;
	private WebChromeClient.CustomViewCallback customViewCallback;
	private WebView mWebView;
	private TextView mNewsTypeTextView;
	private int type;
	private String pageUrl;
	private MyWebChromeClient mWebChromeClient;
	private View mCustomView;
	private ProgressBar mLoadingProgressBar;

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_news_content);
		super.onCreate(savedInstanceState);
		mLoadingProgressBar = (ProgressBar) findViewById(R.id.news_web_view_progress);
		customViewContainer = (FrameLayout) findViewById(R.id.news_customViewContainer);
		mPullRefreshWebView = (PullToRefreshWebView) findViewById(R.id.news_web_view);
		mPullRefreshWebView.setOnRefreshListener(this);
		mWebView = mPullRefreshWebView.getRefreshableView();
		WebSettings ws = mWebView.getSettings();
		ws.setJavaScriptEnabled(true);
		ws.setUseWideViewPort(true);
		ws.setSupportZoom(true);
		// 开启插件（对flash的支持）
		ws.setPluginState(PluginState.ON);
		ws.setJavaScriptCanOpenWindowsAutomatically(true);

		// ws.setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
		mWebChromeClient = new MyWebChromeClient();
		mWebView.setWebViewClient(new MyWebViewClient());
		mWebView.setWebChromeClient(mWebChromeClient);
		mNewsTypeTextView = (TextView) findViewById(R.id.news_type);
		Intent getIntent = getIntent();
		type = getIntent.getIntExtra(EXTRA_TYPE, 0);
		switch (type) {
		case TYPE_ACTIVITY:
			mNewsTypeTextView.setText("活动");
			break;
		case TYPE_COMPANY:
			mNewsTypeTextView.setText("公司");
			break;
		case TYPE_MEDIA:
			mNewsTypeTextView.setText("媒体");
			break;
		case TYPE_TRADE:
			mNewsTypeTextView.setText("行业");
			break;
		default:
			mNewsTypeTextView.setText("");
			break;
		}
		pageUrl = getIntent.getStringExtra(EXTRA_URL);
		mWebView.loadUrl(pageUrl);
	}

	// 后退图标的监听
	public void back(View view) {
		finish();
	}

	class MyWebChromeClient extends WebChromeClient {
		private View mVideoProgressView;

		@Override
		public void onProgressChanged(WebView view, int newProgress) {
			super.onProgressChanged(view, newProgress);
			mLoadingProgressBar.setProgress(newProgress);
			if (newProgress == 100) {
				mLoadingProgressBar.setProgress(0);
			}
		}

//		@Override
//		public void onShowCustomView(View view, int requestedOrientation,
//				CustomViewCallback callback) {
//			onShowCustomView(view, callback); // To change body of overridden
//												// methods use File | Settings |
//												// File Templates.
//		}

//		@Override
//		public void onShowCustomView(View view, CustomViewCallback callback) {

			// if a view already exists then immediately terminate the new one
//			if (mCustomView != null) {
//				callback.onCustomViewHidden();
//				return;
//			}
//			mCustomView = view;
//			mWebView.setVisibility(View.GONE);
//			customViewContainer.setVisibility(View.VISIBLE);
//			customViewContainer.addView(view);
//			customViewCallback = callback;
//		}

//		@Override
//		public View getVideoLoadingProgressView() {
//
//			if (mVideoProgressView == null) {
//				LayoutInflater inflater = LayoutInflater
//						.from(NewsContentActivity.this);
//				mVideoProgressView = inflater.inflate(R.layout.video_progress,
//						null);
//			}
//			return mVideoProgressView;
//		}

		// @Override
		// public void onHideCustomView() {
		// super.onHideCustomView(); // To change body of overridden methods
		// // use File | Settings | File Templates.
		// if (mCustomView == null) {
		// return;
		// }
		//
		// mWebView.setVisibility(View.VISIBLE);
		// customViewContainer.setVisibility(View.GONE);
		//
		// // Hide the custom view.
		// mCustomView.setVisibility(View.GONE);
		//
		// // Remove the custom view from its container.
		// customViewContainer.removeView(mCustomView);
		// customViewCallback.onCustomViewHidden();
		//
		// mCustomView = null;
		// }
	}

	private class MyWebViewClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			view.loadUrl(url);
			return true;
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			mPullRefreshWebView.onRefreshComplete();
			super.onPageFinished(view, url);
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			super.onPageStarted(view, url, favicon);
		}

	}

	public void onPause() {
		super.onPause();
		mWebView.onPause();
	}

	public void onResume() {
		super.onResume();
		mWebView.onResume();
	}

	@Override
	public void onRefresh(final PullToRefreshBase<WebView> refreshView) {
		mWebView.reload();
	}

	@Override
	public void onBackPressed() {
		if (mCustomView != null) {
			mWebChromeClient.onHideCustomView();
		} else {
			super.onBackPressed();
		}
	}

}
