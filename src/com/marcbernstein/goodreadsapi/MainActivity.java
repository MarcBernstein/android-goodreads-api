package com.marcbernstein.goodreadsapi;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;

public class MainActivity extends Activity {

	public static final String TAG = MainActivity.class.getSimpleName();
	private String mOauthCallbackUrl;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		final String oauthConsumerKey = getString(R.string.oauth_consumer_key);
		final String oauthConsumerSecret = getString(R.string.oauth_consumer_secret);
		mOauthCallbackUrl = getString(R.string.oauth_callback_url);

		if (oauthConsumerKey.equals("REPLACE") || oauthConsumerSecret.equals("REPLACE")
				|| mOauthCallbackUrl.equals("REPLACE")) {
			String exception = "oauth_consumer_key and oauth_consumer_secret string resources must be replaced with your actual developer key.";
			Log.e(TAG, exception);
			throw new RuntimeException(exception);
		}

		// FIXME - Just seeing if it works
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitNetwork().build());

		SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);
		String token = sharedPreferences.getString("oauth_token", "");

		if (TextUtils.isEmpty(token)) {
			try {
				OAuthConsumer consumer = new DefaultOAuthConsumer(oauthConsumerKey, oauthConsumerSecret);
				OAuthProvider provider = new DefaultOAuthProvider("http://www.goodreads.com/oauth/request_token",
						"http://www.goodreads.com/oauth/access_token", "http://www.goodreads.com/oauth/authorize");
				String authUrl = provider.retrieveRequestToken(consumer, mOauthCallbackUrl);

				SharedPreferences.Editor editor = sharedPreferences.edit();
				editor.putString("RequestToken", consumer.getToken());
				editor.putString("RequestTokenSecret", consumer.getTokenSecret());
				editor.commit();

				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(authUrl)));
			} catch (OAuthMessageSignerException e) {
				Log.e(TAG, "OAuth Exception", e);
			} catch (OAuthNotAuthorizedException e) {
				Log.e(TAG, "OAuth Exception", e);
			} catch (OAuthExpectationFailedException e) {
				Log.e(TAG, "OAuth Exception", e);
			} catch (OAuthCommunicationException e) {
				Log.e(TAG, "OAuth Exception", e);
			}
		} else {
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		Uri uri = this.getIntent().getData();
		if (uri != null && uri.getHost().equals(mOauthCallbackUrl)) {

		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
