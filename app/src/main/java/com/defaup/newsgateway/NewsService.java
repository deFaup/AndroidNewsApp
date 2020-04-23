package com.defaup.newsgateway;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;

public class NewsService extends Service
{
    private static final String TAG = "Greg_NewsService";
    ArrayList<Article> articles = new ArrayList<>();
    ServiceReceiver serviceReceiver;
    String receiverIntent, broadcastIntent;

    @Override
    public IBinder onBind(Intent intent) {return null;}

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        serviceReceiver = new ServiceReceiver();
        Log.d(TAG, "onStartCommand: Service 1");
        if(intent.hasExtra("RECEIVER_INTENT"))
        {
            Log.d(TAG, "onStartCommand: Service 2");
            Log.d(TAG, "onStartCommand: ");
            // register as a receiver to INTENT_TO_SERVICE (cf strings.xml) filters
            receiverIntent = intent.getStringExtra("RECEIVER_INTENT");
            IntentFilter filter = new IntentFilter(receiverIntent);
            registerReceiver(serviceReceiver, filter);
        }
        if(intent.hasExtra("BROADCAST_INTENT"))
            broadcastIntent = intent.getStringExtra("BROADCAST_INTENT");

        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy()
    {
        Log.d(TAG, "onDestroy: ");
        unregisterReceiver(serviceReceiver);
        super.onDestroy();
    }

    public void setArticles(ArrayList<Article> articles)
    {
        this.articles.clear();
        this.articles.addAll(articles);

        Intent intent = new Intent(broadcastIntent);
        intent.putExtra(Intent.ACTION_ATTACH_DATA, articles);
        sendBroadcast(intent);
    }


    /* Service Receiver */
    class ServiceReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            if (action == null || !action.equals(receiverIntent)) {return;}

            Source source = null;
            if (intent.hasExtra(Intent.ACTION_ATTACH_DATA))
            {
                source = (Source) intent.getSerializableExtra(Intent.ACTION_ATTACH_DATA);
                Log.d(TAG, "onReceive: " + source.getName());

                new AsyncArticleDownloader(NewsService.this, source.getId(), context).execute();
            }
        }
    }

}
