package com.example.newappstage2;

import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class NewsActivity extends AppCompatActivity implements LoaderCallbacks<List<News>> {

    /**
     * URL for news data from the Guardian dataset
     */
    private static final String apiKey = BuildConfig.API_KEY;

    //Base URI
    private static final String GUARDIAN_REQUEST_URL = "https://content.guardianapis.com/search?api-key=" + apiKey;

    /**
     * Constant value for the news loader ID. Any integer can be chosen.
     * This really only comes into play if you're using multiple loaders.
     */
    private static final int NEWS_LOADER_ID = 1;
    /**
     * TextView that is displayed when the list is empty
     */
    private TextView mEmptyStateTextView;
    private View loadingIndicator;
    /**
     * Adapter for the list of news articles
     */
    private NewsAdapter mNewsAdapter;
    private boolean isNetworkConnected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.news_activity);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        myToolbar.setTitle(R.string.app_title);
        setSupportActionBar(myToolbar);

        loadingIndicator = findViewById(R.id.pb_loading_indicator);

        ListView newsListView = (ListView) findViewById(R.id.news_list);

        mEmptyStateTextView = (TextView) findViewById(R.id.tv_empty_state);
        newsListView.setEmptyView(mEmptyStateTextView);

        mNewsAdapter = new NewsAdapter(this, new ArrayList<News>());

        newsListView.setAdapter(mNewsAdapter);

        //Setting an itemClickListener to list item, which sends an intent to a web browser
        //to open the selected article on the Guardian website
        newsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                //Find the current news article that was clicked on
                News selectedArticle = mNewsAdapter.getItem(position);
                // Convert the String URL into a URI object (to pass into the Intent constructor)
                Uri newsUri = null;
                if (selectedArticle != null) {
                    newsUri = Uri.parse(selectedArticle.getArticleUrl());
                }
                // Create a new intent to view the news URI
                Intent websiteIntent = new Intent(Intent.ACTION_VIEW, newsUri);
                // Send the intent to launch a new activity
                startActivity(websiteIntent);
            }
        });

        //Check state of network connectivity
        isNetworkConnected = checkConnectivity();

        //If there is a network connection, fetch data
        initializeLoader();
    }

    /*
     * Check for network connectivity
     */
    public boolean checkConnectivity() {
        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        return networkInfo != null && networkInfo.isConnected();
    }

    /*
     * Initialize loader
     * */
    public void initializeLoader() {
        if (isNetworkConnected) {
            // Get a reference to the LoaderManager, in order to interact with loaders.
            LoaderManager loaderManager = getLoaderManager();
            // Initialize the loader.
            loaderManager.initLoader(NEWS_LOADER_ID, null, this);
        } else {
            // Otherwise, display error
            // First, hide loading indicator so error message will be visible
            loadingIndicator.setVisibility(View.GONE);

            // Update empty state with no connection error message
            mEmptyStateTextView.setText(R.string.no_internet_connection);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                loadingIndicator.setVisibility(View.VISIBLE);
                //Check state of network connectivity
                isNetworkConnected = checkConnectivity();
                //If available, restart loader
                if (isNetworkConnected) {
                    getLoaderManager().restartLoader(NEWS_LOADER_ID, null, this);
                } else {
                    loadingIndicator.setVisibility(View.GONE);
                    mNewsAdapter.clear();
                    mEmptyStateTextView.setText(R.string.no_internet_connection);
                }
                return true;
            case R.id.menu_settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public Loader<List<News>> onCreateLoader(int i, Bundle bundle) {

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        String pageSize = sharedPrefs.getString(getString(R.string.settings_page_size_key), getString(R.string.settings_page_size_default));
        String orderBy = sharedPrefs.getString(getString(R.string.settings_order_by_key), getString(R.string.settings_order_by_default));
        String section = sharedPrefs.getString(getString(R.string.settings_section_key), getString(R.string.settings_section_default));

        // parse breaks apart the URI string that's passed into its parameter
        Uri baseUri = Uri.parse(GUARDIAN_REQUEST_URL);

        // buildUpon prepares the baseUri that we just parsed so we can add query parameters to it
        Uri.Builder uriBuilder = baseUri.buildUpon();

        // Append query parameter and its value.
        uriBuilder.appendQueryParameter("show-tags", "contributor");
        uriBuilder.appendQueryParameter("show-fields", "thumbnail");
        uriBuilder.appendQueryParameter("page-size", pageSize);
        uriBuilder.appendQueryParameter("orderby", orderBy);
        if (!Objects.equals(section, getString(R.string.settings_section_default))) {
            uriBuilder.appendQueryParameter("section", section);
        }
        return new NewsLoader(this, uriBuilder.toString());
    }

    @Override
    public void onLoadFinished(Loader<List<News>> loader, List<News> news) {

        // Hide loading indicator as the data has been loaded
        loadingIndicator.setVisibility(View.GONE);

        // Set empty state text to display "No news articles found."
        mEmptyStateTextView.setText(R.string.no_news);

        //Clear the adapter of previous news data
        mNewsAdapter.clear();

        // If there is a valid list of {@link News} articles, then add them to the adapter's
        // data set. This will trigger the ListView to update.
        if (news != null && !news.isEmpty()) {
            mNewsAdapter.addAll(news);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<News>> loader) {
        mNewsAdapter.clear();
    }
}
