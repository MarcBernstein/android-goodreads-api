package com.github.marcbernstein.grapi.auth;

import oauth.signpost.OAuth;
import android.app.Activity;
import android.app.DialogFragment;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.github.marcbernstein.grapi.utils.StringUtils;

/**
 * See this link for more info: http://developer.android.com/reference/android/app/DialogFragment.html#DialogOrEmbed <BR>
 * <BR>
 * An instance of this fragment can be created and shown as a dialog:
 * 
 * <pre>
 * OAuthDialogFragment newFragment = OAuthDialogFragment.newInstance();
 * newFragment.show(getFragmentManager(), &quot;dialog&quot;);
 * </pre>
 * 
 * It can also be added as content in a view hierarchy:
 * 
 * <pre>
 * FragmentTransaction ft = getFragmentManager().beginTransaction();
 * OAuthDialogFragment newFragment = OAuthDialogFragment.newInstance();
 * ft.add(android.R.id.content, newFragment);
 * ft.commit();
 * </pre>
 * 
 * @author Marc Bernstein
 * 
 */
public class OAuthDialogFragment extends DialogFragment {

	public static final String TAG = OAuthDialogFragment.class.getSimpleName();

	private String mAuthorizeUrl;
	private String mOAuthCallbackUrl;
	private AuthorizeListener mAuthorizeListener;

	public interface AuthorizeListener {
		void onAuthorized(String token);

		void onAuthorizeError();
	}

	public static OAuthDialogFragment newInstance(String authorizeUrl, String oAuthCallbackUrl) {
		OAuthDialogFragment ret = new OAuthDialogFragment();

		ret.mAuthorizeUrl = authorizeUrl;
		ret.mOAuthCallbackUrl = oAuthCallbackUrl;

		return ret;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if (activity instanceof AuthorizeListener) {
			mAuthorizeListener = (AuthorizeListener) activity;
		} else {
			throw new IllegalStateException("Activity attaching this fragment must implement AuthorizeListener.");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		WebView webView = new WebView(getActivity());

		webView.setWebViewClient(new WebViewClient() {

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				if (url.contains(mOAuthCallbackUrl)) {

					Uri uri = Uri.parse(url);
					String token = uri.getQueryParameter(OAuth.OAUTH_TOKEN);

					if (mAuthorizeListener != null) {
						if (StringUtils.isNotEmpty(token)) {
							mAuthorizeListener.onAuthorized(token);
						} else {
							mAuthorizeListener.onAuthorizeError();
						}
					}

					dismiss();

					return true;
				}

				return false;
			}
		});

		webView.loadUrl(mAuthorizeUrl);

		return webView;
	}
}
