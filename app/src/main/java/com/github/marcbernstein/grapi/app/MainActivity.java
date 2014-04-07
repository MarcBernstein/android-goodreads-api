package com.github.marcbernstein.grapi.app;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.marcbernstein.grapi.GoodreadsAPI;
import com.github.marcbernstein.grapi.GoodreadsAPI.ApiEventListener;
import com.github.marcbernstein.grapi.GoodreadsAPI.OAuthLoginCallback;
import com.github.marcbernstein.grapi.auth.OAuthDialogFragment.AuthorizeListener;
import com.github.marcbernstein.grapi.auth.OAuthLoginDialogType;
import com.github.marcbernstein.grapi.auth.SigningClient;
import com.github.marcbernstein.grapi.utils.StringUtils;
import com.github.marcbernstein.grapi.xml.objects.AuthUser;
import com.github.marcbernstein.grapi.xml.objects.Author;
import com.github.marcbernstein.grapi.xml.responses.AuthorResponse;

import retrofit.RestAdapter;
import retrofit.android.AndroidApacheClient;
import retrofit.android.AndroidLog;
import retrofit.converter.SimpleXMLConverter;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;

public class MainActivity extends Activity implements AuthorizeListener {

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
        mGoodreadsApi.setOAuthLoginDialogType(OAuthLoginDialogType.FULLSCREEN);

        if (mGoodreadsApi.isLoggedIn()) {
            Log.d(TAG, "isLoggedIn");
            start();
        } else {
            Log.d(TAG, "!isLoggedIn - calling login()");
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

    private void showProgressBar(final boolean show) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mProgressBar != null) {
                    mProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            }
        });
    }

    public void appendDebugInfo(String str) {
        mDebugTextView.setText(mDebugTextView.getText() + "\n" + str);
    }

    @Override
    public void onAuthorized(String token) {
        mGoodreadsApi.onAuthorized(token);
    }

    @Override
    public void onAuthorizeError() {
        mGoodreadsApi.onAuthorizeError();
    }

    public interface GoodreadsService {
        @GET("/author/show/{authorId}.xml")
        AuthorResponse getAuthorInfo(@Path("authorId") String authorId, @Query("key") String key);
    }

    class MyApiEventListener implements ApiEventListener {

        @Override
        public void OnNeedsCredentials() {
            handleLogin();
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
            // https://www.goodreads.com/author/show/18541.xml?key=84uSfOCs4L6R8VdgnDrOLQ
            RestAdapter restAdapter = new RestAdapter.Builder()
                    .setEndpoint("https://www.goodreads.com")
                    .setConverter(new SimpleXMLConverter())
                    .setClient(new SigningClient(new AndroidApacheClient(), mGoodreadsApi))
                    .setLogLevel(RestAdapter.LogLevel.BASIC)
                    .setLog(new AndroidLog("FetchAuthorInfoTask"))
                    .build();
            GoodreadsService service = restAdapter.create(GoodreadsService.class);
            AuthorResponse author = service.getAuthorInfo("18541", "84uSfOCs4L6R8VdgnDrOLQ");
            if (author != null) {
                String name = author.getAuthor().getName();
                String id = author.getAuthor().getUserId();
                Log.d("FetchAuthorInfoTask", "FetchAuthorInfoTask name: " + name + ", id: " + id);
            }

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

}
