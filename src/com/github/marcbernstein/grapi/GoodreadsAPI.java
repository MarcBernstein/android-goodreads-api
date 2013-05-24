package com.github.marcbernstein.grapi;

import java.io.File;
import java.io.InputStream;

import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.github.marcbernstein.grapi.auth.OAuthDialogFragment;
import com.github.marcbernstein.grapi.auth.OAuthDialogFragment.AuthorizeListener;
import com.github.marcbernstein.grapi.utils.StringUtils;
import com.github.marcbernstein.grapi.utils.UIUtils;
import com.github.marcbernstein.grapi.xml.objects.AuthUser;
import com.github.marcbernstein.grapi.xml.objects.Author;
import com.github.marcbernstein.grapi.xml.responses.AuthorResponse;

public class GoodreadsAPI {

	private static final String API_URL = "https://www.goodreads.com/";

	private static final String AUTHORIZATION_WEBSITE_URL = "http://www.goodreads.com/oauth/authorize?mobile=1";
	private static final String ACCESS_TOKEN_ENPOINT_URL = "http://www.goodreads.com/oauth/access_token";
	private static final String REQUEST_TOKEN_ENDPOINT_URL = "http://www.goodreads.com/oauth/request_token";

	private static final String SHARED_PREF_FILENAME = GoodreadsAPI.class.getPackage().getName();
	public static final String USER_TOKEN = "USER_TOKEN";
	public static final String USER_SECRET = "USER_SECRET";
	public static final String REQUEST_TOKEN = "REQUEST_TOKEN";
	public static final String REQUEST_SECRET = "REQUEST_SECRET";

	public static final String TAG = GoodreadsAPI.class.getSimpleName();

	private Activity mActivity;
	private CommonsHttpOAuthConsumer mConsumer;
	private CommonsHttpOAuthProvider mProvider;
	private String mToken;
	private String mTokenSecret;
	private String mOAuthDeveloperKey;
	private String mOAuthDeveloperSecret;
	private String mOAuthCallbackUrl;
	private OAuthLoginDialogType mOAuthLoginDialogType;

	private boolean mIsLoggedIn;

	private ApiEventListener mListener;

	public static enum OAuthLoginDialogType {
		FULLSCREEN, DIALOG
	}

	public interface OAuthLoginCallback {
		void onSuccess();

		void onError(Throwable tr);
	}

	public interface ApiEventListener {

		void OnNeedsCredentials();
	}

	public GoodreadsAPI(Activity activity, ApiEventListener listener) {
		mActivity = activity;
		mListener = listener;

		// Set a default, can be changed by user
		mOAuthLoginDialogType = UIUtils.isSmallestWidthGreaterThan600dp(activity) ? OAuthLoginDialogType.DIALOG
				: OAuthLoginDialogType.FULLSCREEN;
	}

	public void setOAuthInfo(String oAuthDeveloperKey, String oAuthDeveloperSecret, String oAuthCallbackUrl) {
		mOAuthDeveloperKey = oAuthDeveloperKey;
		mOAuthDeveloperSecret = oAuthDeveloperSecret;
		mOAuthCallbackUrl = oAuthCallbackUrl;

		if (TextUtils.isEmpty(mOAuthDeveloperSecret) || TextUtils.isEmpty(mOAuthDeveloperSecret)
				|| TextUtils.isEmpty(mOAuthCallbackUrl)) {
			String exception = "None may be empty: oAuthDeveloperKey, oAuthDeveloperSecret, oAuthCallbackUrl.";
			Log.e(TAG, exception);
			throw new RuntimeException(exception);
		}

		mConsumer = new CommonsHttpOAuthConsumer(mOAuthDeveloperKey, mOAuthDeveloperSecret);
		mProvider = new CommonsHttpOAuthProvider(REQUEST_TOKEN_ENDPOINT_URL, ACCESS_TOKEN_ENPOINT_URL,
				AUTHORIZATION_WEBSITE_URL);
		mProvider.setOAuth10a(true);

		// Check if we have a token
		SharedPreferences sp = getSharedPrefs();

		String token = sp.getString(USER_TOKEN, null);
		String tokenSecret = sp.getString(USER_SECRET, null);

		if (StringUtils.isNotEmpty(token) && StringUtils.isNotEmpty(tokenSecret)) {
			mConsumer.setTokenWithSecret(token, tokenSecret);
			mIsLoggedIn = true;
		} else {
			mIsLoggedIn = false;
		}
	}

	public void setOAuthLoginDialogType(OAuthLoginDialogType oAuthLoginDialogType) {
		mOAuthLoginDialogType = oAuthLoginDialogType;
	}

	public void login(OAuthLoginCallback callback) {
		new RetrieveRequestTokenTask(callback).execute();
	}

	private String request(String service) throws Exception {
		return request(service, null);
	}

	private String request(String service, HttpParams params) throws Exception {
		String output = null;

		HttpGet get = new HttpGet(API_URL + service);
		if (params != null) {
			get.setParams(params);
		}

		mConsumer.sign(get);
		HttpClient httpClient = new DefaultHttpClient();
		HttpResponse response = httpClient.execute(get);
		InputStream is = response.getEntity().getContent();
		output = IOUtils.toString(is, "UTF-8");

		final int statusCode = response.getStatusLine().getStatusCode();
		Log.d(TAG, "status code = " + statusCode);
		// Log.d(TAG, "output = " + output);

		if (statusCode == 401) {
			clearAuthInformation();

			if (mListener != null) {
				mListener.OnNeedsCredentials();
			}
		}

		FileUtils.writeStringToFile(new File(Environment.getExternalStorageDirectory().getPath() + "/" + service), output,
				"UTF-8");

		return output;
	}

	private class RetrieveRequestTokenTask extends AsyncTask<Void, Void, String> {

		private OAuthLoginCallback mCallback;

		public RetrieveRequestTokenTask(OAuthLoginCallback callback) {
			mCallback = callback;
		}

		@Override
		protected String doInBackground(Void... params) {
			String authorizeUrl = null;
			try {
				authorizeUrl = mProvider.retrieveRequestToken(mConsumer, mOAuthCallbackUrl);
			} catch (Exception e) {
				Log.e(TAG, "Exception while retrieving request token.", e);
				if (mCallback != null) {
					mCallback.onError(e);
				}
			}
			return authorizeUrl;
		}

		@Override
		protected void onPostExecute(String authorizeUrl) {
			if (authorizeUrl != null) {

				OAuthDialogFragment f = OAuthDialogFragment.newInstance(authorizeUrl, mOAuthCallbackUrl,
						new AuthorizeListener() {

							@Override
							public void onAuthorized(String verifier) {
								new RetrieveAccessTokenTask(mCallback).execute(verifier);
							}
						});

				if (mOAuthLoginDialogType == OAuthLoginDialogType.FULLSCREEN) {
					FragmentTransaction ft = mActivity.getFragmentManager().beginTransaction();
					ft.add(android.R.id.content, f);
					ft.commit();
				} else {
					f.show(mActivity.getFragmentManager(), OAuthDialogFragment.TAG);
				}

				saveRequestInformation(mConsumer.getToken(), mConsumer.getTokenSecret());
			}
		}
	}

	private class RetrieveAccessTokenTask extends AsyncTask<String, Void, Boolean> {

		private OAuthLoginCallback mCallback;

		public RetrieveAccessTokenTask(OAuthLoginCallback callback) {
			mCallback = callback;
		}

		@Override
		protected Boolean doInBackground(String... params) {
			boolean success = false;
			try {
				mProvider.retrieveAccessToken(mConsumer, params[0]);
				success = true;
			} catch (Exception e) {
				Log.e(TAG, "Exception while retrieving access token.", e);
				if (mCallback != null) {
					mCallback.onError(e);
				}
				success = false;
			}

			return success;
		}

		@Override
		protected void onPostExecute(Boolean success) {
			mIsLoggedIn = success;

			if (success) {
				mToken = mConsumer.getToken();
				mTokenSecret = mConsumer.getTokenSecret();
				mConsumer.setTokenWithSecret(mToken, mTokenSecret);

				saveAuthInformation(mToken, mTokenSecret);
				clearRequestInformation();

				if (mCallback != null) {
					mCallback.onSuccess();
				}
			}
		}
	}

	public boolean isLoggedIn() {
		return mIsLoggedIn;
	}

	private void saveAuthInformation(String token, String secret) {
		Editor editor = getSharedPrefs().edit();

		editor.putString(USER_TOKEN, token);
		editor.putString(USER_SECRET, secret);

		editor.commit();
	}

	private void clearAuthInformation() {
		Editor editor = getSharedPrefs().edit();

		editor.remove(USER_TOKEN);
		editor.remove(USER_SECRET);

		editor.commit();
	}

	private void saveRequestInformation(String token, String secret) {
		Editor editor = getSharedPrefs().edit();

		editor.putString(REQUEST_TOKEN, token);
		editor.putString(REQUEST_SECRET, secret);

		editor.commit();
	}

	private void clearRequestInformation() {
		Editor editor = getSharedPrefs().edit();

		editor.remove(REQUEST_TOKEN);
		editor.remove(REQUEST_SECRET);

		editor.commit();
	}

	private SharedPreferences getSharedPrefs() {
		return mActivity.getSharedPreferences(SHARED_PREF_FILENAME, Activity.MODE_PRIVATE);
	}

	/**
	 * Get id of user who authorized OAuth.
	 * 
	 * @return
	 */
	public AuthUser getAuthUserInfo() {
		AuthUser ret = null;

		try {
			String output = request("api/auth_user");

			Serializer serializer = new Persister();

			ret = serializer.read(AuthUser.class, output);
		} catch (Exception e) {
			Log.e(TAG, "Exception", e);
		}

		return ret;
	}

	/**
	 * Get a paginated list of an authors books.
	 * 
	 * @return
	 */
	public AuthorResponse getAuthorBooks(int authorId) {
		return getAuthorBooks(authorId, 0);
	}

	/**
	 * Get a paginated list of an authors books.
	 * 
	 * @return
	 */
	public AuthorResponse getAuthorBooks(int authorId, int page) {
		AuthorResponse ret = null;

		try {
			HttpParams params = new BasicHttpParams();
			params.setIntParameter("page", page);

			String output = request("author/list/" + authorId + ".xml", params);

			Serializer serializer = new Persister();

			ret = serializer.read(AuthorResponse.class, output);
		} catch (Exception e) {
			Log.e(TAG, "Exception", e);
		}

		return ret;
	}

	public Author getAuthorInfo(int authorId) {
		Author ret = null;

		try {
			String output = request("author/show/" + authorId + ".xml");

			Serializer serializer = new Persister();

			AuthorResponse response = serializer.read(AuthorResponse.class, output);
			if (response != null) {
				ret = response.getAuthor();
			}
		} catch (Exception e) {
			Log.e(TAG, "Exception", e);
		}

		return ret;
	}
}
