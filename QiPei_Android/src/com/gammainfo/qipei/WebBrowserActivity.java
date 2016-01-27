package com.gammainfo.qipei;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.gammainfo.qipei.utils.UrlAssert;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshWebView;

public class WebBrowserActivity extends BaseActivity implements
		OnRefreshListener<WebView> {
	public static final String ACTION_URL_TEXT = "com.gammainfo.qipei.urltext";
	private PullToRefreshWebView mPullRefreshWebView;
	private WebView mWebView;
	private View mBackView;
	private View mForwardView;
	private String mHomeUrl;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_webbrowser);

		mForwardView = findViewById(R.id.ibtn_webbrowser_goforward);
		mBackView = findViewById(R.id.ibtn_webbrowser_goback);
		mPullRefreshWebView = (PullToRefreshWebView) findViewById(R.id.wv_webbrowser);
		mPullRefreshWebView.setOnRefreshListener(this);

		mWebView = mPullRefreshWebView.getRefreshableView();
		WebSettings ws = mWebView.getSettings();
		ws.setJavaScriptEnabled(true);
		// ws.setUseWideViewPort(true);
		ws.setSupportZoom(true);
		// ws.setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
		mWebView.setWebViewClient(new MyWebViewClient());
		String urlOrText = getIntent().getStringExtra(ACTION_URL_TEXT);
		if (UrlAssert.isUrl(urlOrText)) {
			mWebView.loadUrl(urlOrText);
		} else {
			mWebView.loadDataWithBaseURL(null, urlOrText, "text/html", "utf-8",
					null);
		}
	}

	public void onBackClick(View v) {
		mWebView.goBack();

	}

	public void onForwardClick(View v) {
		mWebView.goForward();
	}

	public void onHomeClick(View v) {
		if (mHomeUrl == null) {
			// TODO test
			mHomeUrl = "http://www.gammainfo.com";
		}
		mWebView.loadUrl(mHomeUrl);
	}

	private void updateBackForwardState() {
		if (mWebView.canGoBack()) {
			mBackView.setEnabled(true);
		} else {
			mBackView.setEnabled(false);
		}
		if (mWebView.canGoForward()) {
			mForwardView.setEnabled(true);
		} else {
			mForwardView.setEnabled(false);
		}
	}

	private class MyWebViewClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			view.loadUrl(url);
			return true;
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			updateBackForwardState();
			mPullRefreshWebView.onRefreshComplete();
			super.onPageFinished(view, url);
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			updateBackForwardState();
			super.onPageStarted(view, url, favicon);
		}

	}

	@Override
	public void onRefresh(final PullToRefreshBase<WebView> refreshView) {
		mWebView.reload();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			if (mWebView.canGoBack()) {
				mWebView.goBack();
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
}
