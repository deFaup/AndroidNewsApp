package com.defaup.newsgateway;

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
import java.util.Map;
import java.util.TreeMap;

class AsyncSourceDownload extends AsyncTask<Void, Void, Map<String, ArrayList<Source>>>
{
    private MainActivity mainActivity;

    AsyncSourceDownload(MainActivity mainActivity){this.mainActivity = mainActivity;}

    @Override
    protected Map<String, ArrayList<Source>> doInBackground(Void... voids)
    {
        JSONObject jsonObject = getSources();
        if (jsonObject == null)
            return null;

        return parseSources(jsonObject);
    }
    @Override
    protected void onPostExecute(Map<String, ArrayList<Source>> sourcesMap)
    {
        mainActivity.onPostSourceDownload(sourcesMap);
    }


    private JSONObject getSources()
    {
        String url_ = String.format("%s%s",
                mainActivity.getApplicationContext().getString(R.string.SOURCE_URL),
                mainActivity.getApplicationContext().getString(R.string.API_KEY));

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
    private Map<String, ArrayList<Source>> parseSources(JSONObject jsonObject)
    {
        Map<String, ArrayList<Source>> sourcesMap = new TreeMap<>();
        ArrayList<Source> allSources = new ArrayList<>();
        try {
            if (jsonObject.has("sources"))
            {
                JSONArray sourcesArray = jsonObject.getJSONArray("sources");
                for (int i = 0; i < sourcesArray.length(); ++i)
                {
                    JSONObject sourceObject = sourcesArray.getJSONObject(i);

                    String id = sourceObject.getString("id");
                    if("null".equals(id)) continue;
                    String name = sourceObject.getString("name");
                    if("null".equals(name)) continue;
                    String category = sourceObject.getString("category");
                    if("null".equals(category)) continue;

                    Source source = new Source(id, name, category);

                    // if the category is not yet in the map we instantiate a new array List and
                    // put a new category + arrayList associated to it
                    // else we get the arrayList reference and add a new source Object in it.
                    ArrayList<Source> sourceArrayList = sourcesMap.get(source.getCategory());
                    if (sourceArrayList == null)
                    {
                        sourceArrayList = new ArrayList<>();
                        sourceArrayList.add(source);
                        sourcesMap.put(source.getCategory(), sourceArrayList);
                    }
                    else sourceArrayList.add(source);
                    allSources.add(source);
                }
                sourcesMap.put("all", allSources);
            }
        }
        catch (JSONException e) {e.printStackTrace();}
        finally {return sourcesMap;}
    }

}
