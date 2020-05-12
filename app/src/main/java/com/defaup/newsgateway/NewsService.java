package com.defaup.newsgateway;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class NewsService extends Service
{
    private static final String TAG = "Greg_NewsService";
    ArrayList<Article> articles = new ArrayList<>();
    ServiceReceiver serviceReceiver;

    @Override
    public IBinder onBind(Intent intent) {return null;}

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        //Log.d(TAG, "onStartCommand: ");

        serviceReceiver = new ServiceReceiver();
        IntentFilter filter = new IntentFilter(getApplicationContext().getString(R.string.INTENT_TO_SERVICE));
        registerReceiver(serviceReceiver, filter);

        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy()
    {
        //Log.d(TAG, "onDestroy: ");
        unregisterReceiver(serviceReceiver);
        super.onDestroy();
    }

    // send articles to MainActivity through a broadcast
    public void setArticles(List<Article> articles)
    {
        if(articles == null)
            return;
        Intent intent = new Intent(getApplicationContext().getString(R.string.INTENT_TO_MAIN));
        intent.putExtra(Intent.ACTION_ATTACH_DATA, (ArrayList) articles);
        sendBroadcast(intent);
    }


    /* Service Receiver */
    class ServiceReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            if (action == null || !action.equals(getString(R.string.INTENT_TO_SERVICE))) {return;}

            if (intent.hasExtra(Intent.ACTION_ATTACH_DATA))
            {
                Source source = (Source) intent.getSerializableExtra(Intent.ACTION_ATTACH_DATA);
                //Log.d(TAG, "onReceive: " + source.getName());

                new AsyncArticleDownloader(NewsService.this, source, context).execute();
            }
        }
    }

}
