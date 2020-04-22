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

class AsyncArticleDownloader extends AsyncTask<Void, Void, ArrayList<Article>>
{
    private NewsService newsService;
    private String id;
    private Context context;

    AsyncArticleDownloader(NewsService newsService, String id, Context context)
    {
        this.newsService = newsService;
        this.id = id;
        this.context = context;
    }

    @Override
    protected ArrayList<Article> doInBackground(Void... voids)
    {
        JSONObject jsonObject = getArticles();
        if (jsonObject == null)
            return null;

        return parseArticles(jsonObject);
    }
    @Override
    protected void onPostExecute(ArrayList<Article> articles)
    {
        newsService.setArticles(articles);
    }

    private JSONObject getArticles()
    {
        String url_ = String.format("%s%s%s%s",
                context.getString(R.string.ARTICLE_BEGIN_URL),
                id,
                context.getString(R.string.ARTICLE_END_URL),
                context.getString(R.string.API_KEY));

        JSONObject jsonObject = null;
        String urlToUse = Uri.parse(url_).toString();

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
    private ArrayList<Article> parseArticles(JSONObject jsonObject)
    {
        ArrayList<Article> articles = new ArrayList<>();

        try {
            if (jsonObject.has("articles"))
            {
                JSONArray articlesArray = jsonObject.getJSONArray("articles");
                for (int i = 0; i < articlesArray.length(); ++i)
                {
                    JSONObject articleObject = articlesArray.getJSONObject(i);

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
