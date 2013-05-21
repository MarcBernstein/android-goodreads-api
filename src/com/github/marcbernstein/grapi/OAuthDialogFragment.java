package com.github.marcbernstein.grapi;

import oauth.signpost.OAuth;
import android.app.DialogFragment;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

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
		void onAuthorized(String verifier);
	}

	public static OAuthDialogFragment newInstance(String authorizeUrl, String oAuthCallbackUrl,
			AuthorizeListener authorizeListener) {
		OAuthDialogFragment ret = new OAuthDialogFragment();

		ret.mAuthorizeUrl = authorizeUrl;
		ret.mOAuthCallbackUrl = oAuthCallbackUrl;
		ret.mAuthorizeListener = authorizeListener;

		return ret;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		WebView webView = new WebView(getActivity());

		webView.setWebViewClient(new WebViewClient() {

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				if (url.contains(mOAuthCallbackUrl)) {

					Uri uri = Uri.parse(url);
					String verifier = uri.getQueryParameter(OAuth.OAUTH_VERIFIER);

					if (mAuthorizeListener != null) {
						mAuthorizeListener.onAuthorized(verifier);
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
