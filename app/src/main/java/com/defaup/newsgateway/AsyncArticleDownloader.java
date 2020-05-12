package com.defaup.newsgateway;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

class AsyncArticleDownloader extends AsyncTask<Void, Void, List<Article>>
{
    private NewsService newsService;
    private Source source;
    private Context context;

    AsyncArticleDownloader(NewsService newsService, Source source, Context context)
    {
        this.newsService = newsService;
        this.source = source;
        this.context = context;
    }

    @Override
    protected List<Article> doInBackground(Void... voids)
    {
        JSONObject jsonObject = getArticles();
        if (jsonObject == null)
            return null;

        return parseArticles(jsonObject);
    }
    @Override
    protected void onPostExecute(List<Article> articles)
    {
        if(articles != null)
            newsService.setArticles(articles);
    }

    private JSONObject getArticles()
    {
        StringBuilder url_ = new StringBuilder();
        url_.append(context.getString(R.string.ARTICLE_BEGIN_URL));
        if(source.getId()==null)
        {
            url_.append(context.getString(R.string.ARTICLE_QUERY));
            url_.append(source.getName()); // not controlling the user input there
            // in case of a bad input url is wrong then exception is raised & catched
            // evil input works just fine ex: apple&from=2020-05-02
        }
        else
        {
            url_.append(context.getString(R.string.ARTICLE_SOURCE));
            url_.append(source.getId());
        }
        url_.append(context.getString(R.string.ARTICLE_PARAM_LANGUAGE_EN));
        url_.append(context.getString(R.string.ARTICLE_PARAM_PAGE_SIZE)); url_.append(100);
        url_.append(context.getString(R.string.ARTICLE_PARAM_SORT_RECENT));
        url_.append(context.getString(R.string.ARTICLE_PARAM_KEY));
        url_.append(context.getString(R.string.API_KEY));

        JSONObject jsonObject = null;
        String urlToUse = Uri.parse(url_.toString()).toString();

        try
        {
            java.net.URL url = new URL(urlToUse);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK)
                throw (new Exception());

            conn.setRequestMethod("GET");

            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader((new InputStreamReader(is)));
            String line;
            StringBuilder sb = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
            jsonObject = new JSONObject(sb.toString());
            return jsonObject;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return jsonObject;
        }
    }
    private List<Article> parseArticles(JSONObject jsonObject)
    {
        List<Article> articles = new ArrayList<>();

        try {
            if (jsonObject.has("articles"))
            {
                JSONArray articlesArray = jsonObject.getJSONArray("articles");
                for (int i = 0; i < articlesArray.length(); ++i)
                {
                    JSONObject articleObject = articlesArray.getJSONObject(i);
                    // exception safe as values are always given; empty string if no value
                    Article article = new Article(
                            articleObject.getString("author"),
                            articleObject.getString("title"),
                            articleObject.getString("description"),
                            articleObject.getString("url"),
                            articleObject.getString("urlToImage"),
                            articleObject.getString("publishedAt")
                    );
                    articles.add(article);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            return articles;
        }
    }
}
