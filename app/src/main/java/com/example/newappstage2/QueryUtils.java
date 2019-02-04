package com.example.newappstage2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class QueryUtils {

    /**
     * Tag for the log messages
     */
    private static final String LOG_TAG = QueryUtils.class.getSimpleName();

    private static final int RESPONSE_CODE = 200;
    private static final int READ_TIME_OUT = 10000;
    private static final int CONNECT_TIME_OUT = 15000;
    private static final String REQUEST_METHOD = "GET";

    private QueryUtils() {
    }

    /**
     * Query the Guardian dataset and return a list of {@link News} objects.
     */
    public static List<News> fetchNewsData(Context context, String requestUrl) {

        // Create URL object
        URL url = createUrl(context, requestUrl);

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(context, url);
        } catch (IOException e) {
            Log.e(LOG_TAG, context.getResources().getString(R.string.error_http_request), e);
        }

        // Extract relevant fields from the JSON response and create a list of {@link News}
        List<News> myNews = extractArticlesFromJson(context, jsonResponse);

        // Return the list of {@link News}
        return myNews;
    }

    /**
     * Returns new URL object from the given string URL.
     */
    private static URL createUrl(Context context, String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, context.getResources().getString(R.string.error_making_url), e);
        }
        return url;
    }

    /**
     * Make an HTTP request to the given URL and return a String as the response.
     */
    private static String makeHttpRequest(Context context, URL url) throws IOException {
        String jsonResponse = "";

        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(READ_TIME_OUT /* milliseconds */);
            urlConnection.setConnectTimeout(CONNECT_TIME_OUT /* milliseconds */);
            urlConnection.setRequestMethod(REQUEST_METHOD);
            urlConnection.connect();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == RESPONSE_CODE) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, String.format(context.getResources().getString(R.string.error_code), urlConnection.getResponseCode()));
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, context.getResources().getString(R.string.error_json_results), e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    /**
     * Convert the {@link InputStream} into a String which contains the
     * whole JSON response from the server.
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    /**
     * Return a list of {@link News} objects that has been built up from
     * parsing a JSON response.
     */
    private static List<News> extractArticlesFromJson(Context context, String newsJSON) {

        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(newsJSON)) {
            return null;
        }

        // Create an empty ArrayList that we can start adding news to
        List<News> myNews = new ArrayList<>();

        //Parse the JSON response string
        try {
            // Create a JSONObject from the JSON response string
            JSONObject baseJsonResponseObj = new JSONObject(newsJSON);

            // Extract the JSONobject associated with the key called "response",
            // which represents a collection of key value pairs
            JSONObject responseObj = baseJsonResponseObj.getJSONObject(context.getResources().getString(R.string.json_object_response));

            // Extract the JSONArray associated with the key called "results",
            // which represents a list of news articles
            JSONArray newsResultsArray = responseObj.getJSONArray(context.getResources().getString(R.string.json_array_results));

            // For each news article in the resultsArray, create an {@link News} object
            for (int i = 0; i < newsResultsArray.length(); i++) {

                //Declare and set variables to default
                String title = null;
                String section = null;
                String date = null;
                String url = null;
                Bitmap thumbnailBmap = null;
                ArrayList<String> authors = new ArrayList<>();

                // Get a single news article at position i within the list of news articles
                JSONObject currentNewsArticleObj = newsResultsArray.getJSONObject(i);

                //Get title of the article
                if (currentNewsArticleObj.has(context.getResources().getString(R.string.json_prim_title))) {
                    title = currentNewsArticleObj.getString(context.getResources().getString(R.string.json_prim_title));
                }

                //Get section of the article
                if (currentNewsArticleObj.has(context.getResources().getString(R.string.json_prim_section))) {
                    section = currentNewsArticleObj.getString(context.getResources().getString(R.string.json_prim_section));
                }

                //Get publication date and time
                if (currentNewsArticleObj.has(context.getResources().getString(R.string.json_prim_date))) {
                    date = currentNewsArticleObj.getString(context.getResources().getString(R.string.json_prim_date));
                }

                //Get web url
                if (currentNewsArticleObj.has(context.getResources().getString(R.string.json_prim_webUrl))) {
                    url = currentNewsArticleObj.getString(context.getResources().getString(R.string.json_prim_webUrl));
                }

                //Get image thumbnail
                if (currentNewsArticleObj.has(context.getResources().getString(R.string.json_object_fields))) {
                    JSONObject currentFieldsObj = currentNewsArticleObj.getJSONObject(context.getResources().getString(R.string.json_object_fields));
                    if (currentFieldsObj.has(context.getResources().getString(R.string.json_prim_thumbnail))) {
                        String thumbnail = currentFieldsObj.getString(context.getResources().getString(R.string.json_prim_thumbnail));
                        URL thumbnailUrl = new URL(thumbnail);
                        thumbnailBmap = BitmapFactory.decodeStream(thumbnailUrl.openConnection().getInputStream());
                    } else {
                        thumbnailBmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.image_not_available);
                    }
                } else {
                    thumbnailBmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.image_not_available);
                }

                //Get author/authors of the article
                if (currentNewsArticleObj.has(context.getResources().getString(R.string.json_array_tags))) {
                    JSONArray tagsArray = currentNewsArticleObj.getJSONArray(context.getResources().getString(R.string.json_array_tags));
                    if (tagsArray != null && tagsArray.length() != 0) {
                        for (int j = 0; j < tagsArray.length(); j++) {
                            JSONObject authorObj = tagsArray.getJSONObject(j);
                            authors.add(authorObj.getString(context.getResources().getString(R.string.json_prim_contributors)));
                        }
                    } else {
                        authors.add(context.getResources().getString(R.string.no_author));
                    }
                }
                myNews.add(new News(title, section, date, thumbnailBmap, url, authors));
            }
        } catch (JSONException je) {
            Log.e(LOG_TAG, context.getResources().getString(R.string.error_json_parse), je);
        } catch (IOException ioe) {
            Log.e(LOG_TAG, context.getResources().getString(R.string.error_json_thumbnail), ioe);
        }
        return myNews;
    }
}
