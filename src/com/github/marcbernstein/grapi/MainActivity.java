package com.github.marcbernstein.grapi;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.marcbernstein.grapi.GoodreadsAPI.ApiEventListener;
import com.github.marcbernstein.grapi.GoodreadsAPI.OAuthLoginCallback;
import com.github.marcbernstein.grapi.utils.StringUtils;
import com.github.marcbernstein.grapi.xml.objects.AuthUser;
import com.github.marcbernstein.grapi.xml.objects.Author;
import com.marcbernstein.goodreadsapi.R;

public class MainActivity extends Activity {

	public static final String TAG = MainActivity.class.getSimpleName();
	private GoodreadsAPI mGoodreadsApi;

	private ProgressBar mProgressBar;
	private TextView mDebugTextView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mProgressBar = (ProgressBar) findViewById(R.id.network_progressbar);
		mDebugTextView = (TextView) findViewById(R.id.debug_textview);

		final String oauthDeveloperKey = getString(R.string.oauth_developer_key);
		final String oauthDeveloperSecret = getString(R.string.oauth_developer_secret);
		final String oauthCallbackUrl = getString(R.string.oauth_callback_url);

		mGoodreadsApi = new GoodreadsAPI(this, new MyApiEventListener());
		mGoodreadsApi.setOAuthInfo(oauthDeveloperKey, oauthDeveloperSecret, oauthCallbackUrl);

		if (mGoodreadsApi.isLoggedIn()) {
			Log.d(TAG, "isLoggedIn");
			start();
		} else {
			Log.d(TAG, "!isLoggedIn - calling login()");
			handleLogin();
		}
	}

	class MyApiEventListener implements ApiEventListener {

		@Override
		public void OnNeedsCredentials() {
			handleLogin();
		}

	}

	private void start() {
		new FetchUserInfoTask().execute();
		new FetchAuthorInfoTask().execute();
		new FetchIdTask().execute();
	}

	public void handleLogin() {
		showProgressBar(true);

		mGoodreadsApi.login(new OAuthLoginCallback() {

			@Override
			public void onSuccess() {
				start();
				showProgressBar(false);
			}

			@Override
			public void onError(Throwable tr) {
				Log.e(TAG, "onError", tr);
				showProgressBar(false);
			}
		});
	}

	private void showProgressBar(boolean show) {
		if (mProgressBar != null) {
			mProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
		}
	}

	private class FetchUserInfoTask extends AsyncTask<Void, Void, AuthUser> {

		@Override
		protected void onPreExecute() {
			showProgressBar(true);
		}

		@Override
		protected AuthUser doInBackground(Void... params) {
			return mGoodreadsApi.getAuthUserInfo();
		}

		@Override
		protected void onPostExecute(AuthUser authUser) {
			showProgressBar(false);

			if (authUser != null) {
				appendDebugInfo("User Name: " + authUser.getName());
			}
		}

	}

	private class FetchAuthorInfoTask extends AsyncTask<Void, Void, Author> {

		@Override
		protected void onPreExecute() {
			showProgressBar(true);
		}

		@Override
		protected Author doInBackground(Void... params) {
			return mGoodreadsApi.getAuthorInfo(18541);
		}

		@Override
		protected void onPostExecute(Author author) {
			showProgressBar(false);

			if (author != null) {
				appendDebugInfo("Author name: " + author.getName());
			}
		}
	}

	private class FetchIdTask extends AsyncTask<Void, Void, String> {

		@Override
		protected void onPreExecute() {
			showProgressBar(true);
		}

		@Override
		protected String doInBackground(Void... params) {
			return mGoodreadsApi.getIsbnToId("0441172717");
		}

		@Override
		protected void onPostExecute(String id) {
			showProgressBar(false);

			if (StringUtils.isNotEmpty(id)) {
				appendDebugInfo("Book ID: " + id);
			}
		}
	}

	public void appendDebugInfo(String str) {
		mDebugTextView.setText(mDebugTextView.getText() + "\n" + str);
	}

}
