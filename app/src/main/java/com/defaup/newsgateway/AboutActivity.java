package com.defaup.newsgateway;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        //MainActivity.setCustomActionBar(this);

        TextView aboutNewsApi = (TextView)findViewById(R.id.aboutNewsAPI);
        aboutNewsApi.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
        final String civicApiUrl = getString(R.string.NEWS_API);
        aboutNewsApi.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(civicApiUrl));
                startActivity(intent);
            }
        });

        final String authorUrl = getString(R.string.AUTHOR_URL);
        ((TextView)findViewById(R.id.copyright)).setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);

        findViewById(R.id.copyright).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(authorUrl));
                startActivity(intent);
            }
        });
    }
}
